package com.github.theapache64.fig.examples

import com.github.theapache64.fig.Fig
import kotlinx.coroutines.runBlocking

/**
 * Comprehensive example demonstrating Fig's local fallback capabilities
 */
object LocalFallbackExample {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("🚀 Fig Local Fallback Example")
        println("=============================")

        // Example 1: Basic usage with fallback
        basicFallbackExample()

        // Example 2: Export and reuse configuration
        exportExample()

        // Example 3: Production-ready setup
        productionExample()
    }

    private suspend fun basicFallbackExample() {
        println("\n📝 Example 1: Basic Local Fallback")
        
        try {
            val fig = Fig(
                sheetUrl = "https://docs.google.com/spreadsheets/d/your-sheet-id/edit?usp=sharing",
                localFallbackPath = "config-backup.json" // Will be used if Google Sheets fails
            )
            
            fig.load()
            
            // These calls work regardless of whether data came from Google Sheets or local file
            val appName = fig.getString("app_name", "Default App")
            val version = fig.getInt("version_code", 1)
            val isDebug = fig.getBoolean("debug_mode", false)
            
            println("✅ App: $appName v$version (debug: $isDebug)")
            
        } catch (e: Exception) {
            println("❌ Failed to load config: ${e.message}")
        }
    }

    private suspend fun exportExample() {
        println("\n💾 Example 2: Export Configuration")
        
        try {
            // Load from Google Sheets (when available)
            val fig = Fig("https://docs.google.com/spreadsheets/d/your-sheet-id/edit?usp=sharing")
            fig.load()
            
            // Export to create a local backup
            fig.exportToLocalFile("production-config.json")
            println("✅ Configuration exported to production-config.json")
            
            // Now you can use this exported file as fallback in your production deployment
            val prodFig = Fig(
                sheetUrl = "https://docs.google.com/spreadsheets/d/your-sheet-id/edit?usp=sharing",
                localFallbackPath = "production-config.json"
            )
            prodFig.load() // Will use local file if Google Sheets is down
            
        } catch (e: Exception) {
            println("❌ Export example failed: ${e.message}")
        }
    }

    private suspend fun productionExample() {
        println("\n🏭 Example 3: Production-Ready Setup")
        
        // Create a sample local config for demonstration
        createSampleConfig()
        
        try {
            val fig = Fig(
                sheetUrl = "https://docs.google.com/spreadsheets/d/invalid-for-demo/",
                localFallbackPath = "sample-config.json"
            )
            
            // This will fail for Google Sheets but succeed with local fallback
            fig.load()
            
            // Your application code remains the same regardless of the source
            val config = ApplicationConfig(
                appName = fig.getString("app_name", "Unknown"),
                version = fig.getInt("version", 1),
                apiTimeout = fig.getDouble("api_timeout", 30.0),
                isDebugEnabled = fig.getBoolean("debug_enabled", false),
                maxRetries = fig.getInt("max_retries", 3)
            )
            
            println("✅ Loaded configuration:")
            println("   📱 App: ${config.appName} v${config.version}")
            println("   ⏱️  API Timeout: ${config.apiTimeout}s")
            println("   🐛 Debug: ${config.isDebugEnabled}")
            println("   🔄 Max Retries: ${config.maxRetries}")
            
        } catch (e: Exception) {
            println("❌ Production example failed: ${e.message}")
        }
    }

    private fun createSampleConfig() {
        val configFile = java.io.File("sample-config.json")
        configFile.writeText("""
            {
              "app_name": "Production App",
              "version": 42,
              "api_timeout": 45.0,
              "debug_enabled": false,
              "max_retries": 5,
              "feature_flags": {
                "new_ui": true,
                "beta_features": false
              }
            }
        """.trimIndent())
        println("📄 Created sample-config.json for demonstration")
    }

    data class ApplicationConfig(
        val appName: String,
        val version: Int,
        val apiTimeout: Double,
        val isDebugEnabled: Boolean,
        val maxRetries: Int
    )
}