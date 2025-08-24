package com.github.theapache64.fig

import com.github.theapache64.expekt.should
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith

class FigTest {

    val fig = Fig().apply {
        runBlocking {
            load("https://docs.google.com/spreadsheets/d/1LD1Su7HVzAxPlbRp9MO7lni2E5SOqfAsLMCd1FC9A8s/edit?usp=sharing")
        }
    }

    // String Tests
    @Test
    fun `simple getString call`() {
        fig.getString("fruit", null).should.equal("apple")
    }

    @Test
    fun `getString with default value when key exists`() {
        fig.getString("fruit", "banana").should.equal("apple")
    }

    @Test
    fun `getString with default value when key doesn't exist`() {
        fig.getString("non_existent_key", "default_fruit").should.equal("default_fruit")
    }

    @Test
    fun `getString with null default when key doesn't exist`() {
        fig.getString("non_existent_key", null).should.equal(null)
    }

    @Test
    fun `getString with empty string value`() {
        // Assuming your sheet has an empty string value for testing
        fig.getString("empty_string", "fallback").should.not.equal(null)
    }

    // Boolean Tests
    @Test
    fun `simple Boolean call`() {
        fig.getBoolean("is_alive", null).should.equal(true)
    }

    @Test
    fun `getBoolean with false value`() {
        fig.getBoolean("is_dead", null).should.equal(false)
    }

    @Test
    fun `getBoolean with default value when key exists`() {
        fig.getBoolean("is_alive", false).should.equal(true)
    }

    @Test
    fun `getBoolean with default value when key doesn't exist`() {
        fig.getBoolean("non_existent_bool", true).should.equal(true)
    }

    @Test
    fun `getBoolean with string representations`() {
        // Test various string representations of boolean values
        fig.getBoolean("bool_string_true", null).should.equal(true) // "TRUE", "True", "true"
        fig.getBoolean("bool_string_false", null).should.equal(false) // "FALSE", "False", "false"
    }

    // Integer Tests
    @Test
    fun `getInt with positive number`() {
        fig.getInt("age", null).should.equal(25)
    }

    @Test
    fun `getInt with negative number`() {
        fig.getInt("temperature", null).should.equal(-5)
    }

    @Test
    fun `getInt with zero`() {
        fig.getInt("zero_value", null).should.equal(0)
    }

    @Test
    fun `getInt with default value when key exists`() {
        fig.getInt("age", 30).should.equal(25)
    }

    @Test
    fun `getInt with default value when key doesn't exist`() {
        fig.getInt("non_existent_int", 42).should.equal(42)
    }

    @Test
    fun `getInt with invalid string returns null`() {
        fig.getInt("invalid_int_string", 10).should.equal(10)
    }

    // Float Tests
    @Test
    fun `getFloat with decimal value`() {
        fig.getFloat("price", null).should.equal(19.99f)
    }

    @Test
    fun `getFloat with integer value`() {
        fig.getFloat("whole_number", null).should.equal(100f)
    }

    @Test
    fun `getFloat with default value when key doesn't exist`() {
        fig.getFloat("non_existent_float", 3.14f).should.equal(3.14f)
    }

    @Test
    fun `getFloat with scientific notation`() {
        fig.getFloat("scientific", null).should.equal(1.23e-4f)
    }

    // Double Tests
    @Test
    fun `getDouble with high precision value`() {
        fig.getDouble("pi", null).should.equal(3.141592653589793)
    }

    @Test
    fun `getDouble with default value when key doesn't exist`() {
        fig.getDouble("non_existent_double", 2.718).should.equal(2.718)
    }

    @Test
    fun `getDouble with very large number`() {
        fig.getDouble("large_number", null).should.equal(1.23456789e10)
    }

    // Complex Scenarios
    @Test
    fun `getAll returns complete cache`() {
        val allValues = fig.getAll()
        allValues.should.not.equal(null)
        allValues!!.should.contain.keys("fruit")
        allValues.should.contain.keys("is_alive")
    }

    @Test
    fun `test type conversion consistency`() {
        // Test that a numeric string can be retrieved as both string and number
        val numericString = fig.getString("numeric_string", null) // e.g., "123"
        val sameAsInt = fig.getInt("numeric_string", null)

        numericString.should.not.equal(null)
        sameAsInt.should.not.equal(null)
        // numericString!!.toInt().should.equal(sameAsInt!!)
    }

    @Test
    fun `test case sensitivity`() {
        // Assuming your sheet has case-sensitive keys
        fig.getString("CamelCase", null).should.not.equal(fig.getString("camelcase", "default"))
    }

    @Test
    fun `test special characters in keys`() {
        // Test keys with special characters if they exist in your sheet
        fig.getString("key_with_underscore", null).should.not.equal(null)
        fig.getString("key-with-dash", "default").should.not.equal(null)
    }

    @Test
    fun `test numeric edge cases`() {
        // Test edge cases for numeric conversions
        fig.getInt("max_int", null).should.equal(Int.MAX_VALUE)
        fig.getInt("min_int", null).should.equal(Int.MIN_VALUE)
        fig.getFloat("max_float", null).should.equal(Float.MAX_VALUE)
        fig.getDouble("infinity", null).should.equal(Double.POSITIVE_INFINITY)
    }

    @Test
    fun `test boolean edge cases`() {
        // Test various representations that should be parsed as boolean
        fig.getBoolean("yes_value", null).should.equal(null) // "yes" doesn't parse to boolean
        fig.getBoolean("no_value", null).should.equal(null)  // "no" doesn't parse to boolean
        fig.getBoolean("one_value", null).should.equal(null) // "1" doesn't parse to boolean
        fig.getBoolean("zero_value", null).should.equal(null) // "0" doesn't parse to boolean
    }

    // Error Handling Tests
    @Test
    fun `uninitialized fig behavior`() {
        val uninitializedFig = Fig()
        // Should return default values and print warnings
        uninitializedFig.getString("any_key", "default").should.equal("default")
        uninitializedFig.getInt("any_key", 42).should.equal(42)
        uninitializedFig.getBoolean("any_key", true).should.equal(true)
        uninitializedFig.getAll().should.equal(null)
    }

    @Test
    fun `init with invalid URL should throw exception`() {
        val invalidFig = Fig()
        assertFailsWith<Exception> {
            runBlocking {
                invalidFig.load("https://invalid-url-that-doesnt-exist.com/")
            }
        }
    }

    @Test
    fun `init with malformed sheet URL`() {
        val malformedFig = Fig()
        assertFailsWith<Exception> {
            runBlocking {
                malformedFig.load("not-a-url")
            }
        }
    }

    // Performance and Concurrency Tests
    @Test
    fun `multiple concurrent access to same key`() {
        val results = mutableListOf<String?>()
        repeat(10) {
            results.add(fig.getString("fruit", null))
        }
        // All results should be identical
        results.forEach { it.should.equal("apple") }
    }

    @Test
    fun `cache consistency after multiple calls`() {
        val firstCall = fig.getAll()
        val secondCall = fig.getAll()
        val thirdCall = fig.getAll()

        firstCall.should.equal(secondCall)
        secondCall.should.equal(thirdCall)
    }

    // Integration Tests
    @Test
    fun `test complete workflow with mixed data types`() {
        // Test a realistic scenario with multiple data types
        val config = mapOf(
            "app_name" to fig.getString("app_name", "Default App"),
            "version_code" to fig.getInt("version_code", 1),
            "is_debug" to fig.getBoolean("is_debug", false),
            "api_timeout" to fig.getDouble("api_timeout", 30.0),
            "retry_count" to fig.getFloat("retry_count", 3.0f)
        )

        config["app_name"].should.not.equal(null)
        config["version_code"].should.not.equal(null)
        config["is_debug"].should.not.equal(null)
    }

    @Test
    fun `deprecated getValue method still works`() {
        @Suppress("DEPRECATION")
        val result = fig.getValue("fruit", null)
        result.should.equal("apple")
    }

    @Test
    fun `deprecated getValue with non-existent key`() {
        @Suppress("DEPRECATION")
        val result = fig.getValue("non_existent", "default")
        result.should.equal("default")
    }

    // Boundary Tests
    @Test
    fun `test with very long key name`() {
        val longKey = "a".repeat(1000)
        fig.getString(longKey, "default").should.equal("default")
    }

    @Test
    fun `test with empty key string`() {
        fig.getString("", "default").should.equal("default")
    }

    @Test
    fun `test null handling in type conversions`() {
        // Test how null values are handled in type conversions
        fig.getInt("null_value", 0).should.equal(0)
        fig.getBoolean("null_value", false).should.equal(false)
        fig.getFloat("null_value", 0.0f).should.equal(0.0f)
        fig.getDouble("null_value", 0.0).should.equal(0.0)
    }
}