package com.github.theapache64.fig

import com.github.theapache64.expekt.should
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith
import java.io.File

class LocalFallbackTest {

    @Test
    fun `test local fallback when Google Sheets fails`() {
        runBlocking {
            val tempFile = File.createTempFile("fig_test", ".json")
            tempFile.writeText("""
                {
                  "app_name": "Fallback App",
                  "version_code": 123,
                  "is_enabled": true,
                  "timeout": 45.5
                }
            """.trimIndent())

            val fig = Fig(
                sheetUrl = "https://invalid-url-that-should-fail.com/invalid",
                localFallbackPath = tempFile.absolutePath
            )

            // This should fail for Google Sheets but succeed with local fallback
            fig.load()

            // Test that values are loaded from local fallback
            fig.getString("app_name").should.equal("Fallback App")
            fig.getInt("version_code").should.equal(123)
            fig.getBoolean("is_enabled").should.equal(true)
            fig.getDouble("timeout").should.equal(45.5)

            // Clean up
            tempFile.delete()
        }
    }

    @Test
    fun `test loading directly from local file with load(url) method`() {
        runBlocking {
            val tempFile = File.createTempFile("fig_test", ".json")
            tempFile.writeText("""
                {
                  "direct_load": "success",
                  "number": 456
                }
            """.trimIndent())

            val fig = Fig(localFallbackPath = tempFile.absolutePath)

            // This should try Google Sheets first, then fallback
            fig.load("https://invalid-url.com/")

            // Test that values are loaded
            fig.getString("direct_load").should.equal("success")
            fig.getInt("number").should.equal(456)

            // Clean up
            tempFile.delete()
        }
    }

    @Test
    fun `test export to local file functionality`() {
        runBlocking {
            val sourceFile = File.createTempFile("fig_source", ".json")
            val exportFile = File.createTempFile("fig_export", ".json")
            
            sourceFile.writeText("""
                {
                  "export_test": "original",
                  "count": 789
                }
            """.trimIndent())

            val fig = Fig(localFallbackPath = sourceFile.absolutePath)
            fig.load("https://invalid-url.com/")

            // Export to new file
            fig.exportToLocalFile(exportFile.absolutePath)

            // Verify export file exists and has correct content
            exportFile.exists().should.equal(true)
            val exportedContent = exportFile.readText()
            exportedContent.should.contain("export_test")
            exportedContent.should.contain("original")
            exportedContent.should.contain("789")

            // Test loading from exported file
            val fig2 = Fig(localFallbackPath = exportFile.absolutePath)
            fig2.load("https://another-invalid-url.com/")
            fig2.getString("export_test").should.equal("original")
            fig2.getInt("count").should.equal(789)

            // Clean up
            sourceFile.delete()
            exportFile.delete()
        }
    }

    @Test
    fun `test error when both Google Sheets and local fallback fail`() {
        assertFailsWith<FigException> {
            runBlocking {
                val fig = Fig(
                    sheetUrl = "https://invalid-url.com/",
                    localFallbackPath = "/non/existent/path/config.json"
                )
                fig.load()
            }
        }
    }

    @Test
    fun `test error when no fallback is provided and Google Sheets fails`() {
        assertFailsWith<FigException> {
            runBlocking {
                val fig = Fig(sheetUrl = "https://invalid-url.com/")
                fig.load()
            }
        }
    }

    @Test
    fun `test export fails when config not loaded`() {
        assertFailsWith<FigException> {
            val fig = Fig()
            fig.exportToLocalFile("/tmp/should_fail.json")
        }
    }
}