package com.github.theapache64.fig

import com.github.theapache64.retrosheet.RetrosheetInterceptor
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import kotlin.math.roundToInt

/**
 * Custom exception thrown when Fig encounters configuration or data errors.
 *
 * @param message The error message describing what went wrong
 */
class FigException(message: String) : Exception(message)

/**
 * Fig is a configuration management class that loads key-value pairs from Google Sheets.
 * It provides type-safe accessors for different data types and caches the data in memory
 * for efficient access. Also supports local fallback for improved reliability.
 *
 * @param sheetUrl The public URL of the Google Sheet containing configuration data.
 *                 The sheet should have two columns: 'key' and 'value'.
 *                 URLs ending with "edit?usp=sharing" will be automatically converted.
 * @param localFallbackPath Optional path to a local JSON file to use as fallback when
 *                          Google Sheets is unavailable. If null, no fallback is used.
 */
class Fig(
    private val sheetUrl: String? = null,
    private val localFallbackPath: String? = null,
) {
    companion object {
        private const val KEY_MISSING_ERROR = "Required value 'key' missing at \$[1]"
    }

    private var inMemCache: Map<String, Any?>? = null

    /**
     * Initializes the Fig instance by loading configuration data from a Google Sheet.
     * This method must be called before using any of the getter methods.
     *
     * @throws FigException If there are data type inconsistencies in the sheet or other configuration errors
     * @throws JsonDataException If there are JSON parsing errors during data retrieval
     *
     * @sample
     * ```kotlin
     * val fig = Fig("https://docs.google.com/spreadsheets/d/your-sheet-id/edit?usp=sharing")
     * fig.load()
     * ```
     */
    @Throws(FigException::class, JsonDataException::class)
    suspend fun load() = withContext(Dispatchers.IO) {
        val urlToUse = sheetUrl ?: throw FigException("Sheet URL not provided in constructor. Use Fig(sheetUrl) or load(sheetUrl)")
        loadFromUrl(urlToUse)
    }

    /**
     * Initializes the Fig instance by loading configuration data from a Google Sheet.
     * This overload allows specifying the sheet URL at load time.
     *
     * @param sheetUrl The public URL of the Google Sheet containing configuration data
     * @throws FigException If there are data type inconsistencies in the sheet or other configuration errors
     * @throws JsonDataException If there are JSON parsing errors during data retrieval
     *
     * @sample
     * ```kotlin
     * val fig = Fig()
     * fig.load("https://docs.google.com/spreadsheets/d/your-sheet-id/edit?usp=sharing")
     * ```
     */
    @Throws(FigException::class, JsonDataException::class)
    suspend fun load(sheetUrl: String) = withContext(Dispatchers.IO) {
        loadFromUrl(sheetUrl)
    }

    private suspend fun loadFromUrl(url: String) {
        try {
            loadFromGoogleSheets(url)
            println("Fig: Successfully loaded from Google Sheets")
        } catch (e: Exception) {
            println("Fig: Failed to load from Google Sheets: ${e.message}")
            if (localFallbackPath != null) {
                try {
                    loadFromLocalFile(localFallbackPath)
                    println("Fig: Successfully loaded from local fallback: $localFallbackPath")
                } catch (fallbackError: Exception) {
                    throw FigException("Failed to load from both Google Sheets and local fallback. Google Sheets error: ${e.message}, Local fallback error: ${fallbackError.message}")
                }
            } else {
                // Re-throw original exception if no fallback is available
                when (e) {
                    is JsonDataException -> {
                        if (e.message == KEY_MISSING_ERROR) {
                            throw FigException("You can't use multiple data types. Use `=TO_TEXT()` to convert non-string values in your sheet")
                        } else {
                            throw FigException(e.message ?: "Unknown error")
                        }
                    }
                    else -> throw FigException(e.message ?: "Failed to load configuration from Google Sheets")
                }
            }
        }
    }

    private suspend fun loadFromGoogleSheets(url: String) {
        val retrosheetInterceptor =
            RetrosheetInterceptor.Builder().setLogging(true).addSheet("Sheet1", "key", "value").build()

        val okHttpClient = OkHttpClient.Builder().addInterceptor(retrosheetInterceptor) // and attach the interceptor
            .build()

        val moshi = Moshi.Builder()
            .build()

        val processedUrl = if (url.endsWith("edit?usp=sharing")) {
            url.replace("edit?usp=sharing", "")
        } else {
            url
        }

        // Building retrofit client
        val figApi = Retrofit.Builder()
            // with baseUrl as sheet's public URL
            .baseUrl(processedUrl) // Sheet's public URL
            .client(okHttpClient).addConverterFactory(MoshiConverterFactory.create(moshi)).build()
            .create(FigApi::class.java)

        inMemCache = figApi.getKeyValues().associate { it.key to it.value }
        println("Fig: Loaded ${inMemCache?.size} configuration values from Google Sheets")
    }

    /**
     * Loads configuration from a local JSON file.
     * The JSON file should contain a map of key-value pairs.
     *
     * @param filePath Path to the local JSON file
     * @throws FigException If the file cannot be read or parsed
     */
    private fun loadFromLocalFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            throw FigException("Local fallback file not found: $filePath")
        }

        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        val adapter = moshi.adapter<Map<String, Any?>>(type)

        try {
            val jsonContent = file.readText()
            inMemCache = adapter.fromJson(jsonContent)
            println("Fig: Loaded ${inMemCache?.size} configuration values from local file: $filePath")
        } catch (e: Exception) {
            throw FigException("Failed to parse local fallback file $filePath: ${e.message}")
        }
    }

    /**
     * Exports the current configuration to a local JSON file.
     * This is useful for creating local fallback files from successfully loaded Google Sheets data.
     *
     * @param filePath Path where to save the JSON file
     * @throws FigException If the configuration is not loaded or the file cannot be written
     *
     * @sample
     * ```kotlin
     * val fig = Fig("https://docs.google.com/spreadsheets/d/your-sheet-id/edit?usp=sharing")
     * fig.load()
     * fig.exportToLocalFile("config-backup.json")
     * ```
     */
    fun exportToLocalFile(filePath: String) {
        val config = inMemCache ?: throw FigException("Configuration not loaded. Call load() first.")

        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        val adapter = moshi.adapter<Map<String, Any?>>(type)

        try {
            val file = File(filePath)
            file.parentFile?.mkdirs() // Create parent directories if needed
            val jsonContent = adapter.toJson(config)
            file.writeText(jsonContent)
            println("Fig: Exported ${config.size} configuration values to $filePath")
        } catch (e: Exception) {
            throw FigException("Failed to export configuration to $filePath: ${e.message}")
        }
    }

    /**
     * Gets a configuration value as a string.
     *
     * @deprecated Use one of the type-specific methods like getString(), getInt(), getFloat(),
     *             getDouble(), or getBoolean() for better type safety.
     * @param key The configuration key to retrieve
     * @param defaultValue The default value to return if the key is not found
     * @return The configuration value as a String, or the default value if not found
     */
    @Deprecated(
        message = "Use one of getString(), getInt(), getFloat(), getDouble() or getBoolean()",
        replaceWith = ReplaceWith("getString(key, defaultValue)")
    )
    fun getValue(key: String, defaultValue: String?): String? {
        return inMemCache.let { keyValues ->
            if (keyValues == null) {
                println("WARNING: Fig.init not called, failed or not completed yet")
            }
            (keyValues?.get(key) ?: defaultValue) as? String
        }
    }

    /**
     * Returns all loaded configuration data as a map.
     *
     * @return A map containing all key-value pairs loaded from the sheet,
     *         or null if init() hasn't been called successfully
     *
     * @sample
     * ```kotlin
     * val allConfig = fig.getAll()
     * allConfig?.forEach { (key, value) ->
     *     println("$key = $value")
     * }
     * ```
     */
    fun getAll(): Map<String, Any?>? {
        if (!isLoaded()) {
            println("WARNING: Fig.init not called, failed or not completed yet")
        }
        return inMemCache
    }

    /**
     * Checks if the Fig instance has been successfully initialized and data has been loaded.
     *
     * @return true if data has been loaded, false otherwise
     */
    private fun isLoaded(): Boolean {
        return inMemCache != null
    }

    /**
     * Gets a configuration value as a String.
     *
     * @param key The configuration key to retrieve
     * @param defaultValue The default value to return if the key is not found (default: null)
     * @return The configuration value as a String, or the default value if not found
     *
     * @sample
     * ```kotlin
     * val appName = fig.getString("app_name", "Default App")
     * val apiUrl = fig.getString("api_url") // returns null if not found
     * ```
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return getAll()?.let { keyValues ->
            val value = keyValues.getOrDefault(key, defaultValue)
            value?.toString()
        } ?: defaultValue
    }

    /**
     * Gets a configuration value as an Integer.
     * The value will be parsed from a string and rounded to the nearest integer if it's a decimal.
     *
     * @param key The configuration key to retrieve
     * @param defaultValue The default value to return if the key is not found or cannot be parsed (default: null)
     * @return The configuration value as an Int, or the default value if not found or invalid
     *
     * @sample
     * ```kotlin
     * val maxRetries = fig.getInt("max_retries", 3)
     * val timeout = fig.getInt("timeout_seconds") // returns null if not found
     * ```
     */
    fun getInt(key: String, defaultValue: Int? = null): Int? {
        return getAll()?.let { keyValues ->
            getAll()?.getOrDefault(key, defaultValue)?.toString()?.toDoubleOrNull()?.roundToInt()
        } ?: defaultValue
    }

    /**
     * Gets a configuration value as a Boolean.
     * Accepts "true", "false" (case-insensitive). Other values will return the default.
     *
     * @param key The configuration key to retrieve
     * @param defaultValue The default value to return if the key is not found or cannot be parsed (default: null)
     * @return The configuration value as a Boolean, or the default value if not found or invalid
     *
     * @sample
     * ```kotlin
     * val debugMode = fig.getBoolean("debug_enabled", false)
     * val featureFlag = fig.getBoolean("new_feature_enabled") // returns null if not found
     * ```
     */
    fun getBoolean(key: String, defaultValue: Boolean? = null): Boolean? {
        return getAll()?.let { keyValues ->
            keyValues.getOrDefault(key, defaultValue)?.toString()?.toBooleanStrictOrNull()
        } ?: defaultValue
    }

    /**
     * Gets a configuration value as a Float.
     *
     * @param key The configuration key to retrieve
     * @param defaultValue The default value to return if the key is not found or cannot be parsed (default: null)
     * @return The configuration value as a Float, or the default value if not found or invalid
     *
     * @sample
     * ```kotlin
     * val animationSpeed = fig.getFloat("animation_speed", 1.0f)
     * val threshold = fig.getFloat("accuracy_threshold") // returns null if not found
     * ```
     */
    fun getFloat(key: String, defaultValue: Float? = null): Float? {
        return getAll()?.let { keyValues ->
            keyValues.getOrDefault(key, defaultValue)?.toString()?.toFloatOrNull()
        } ?: defaultValue
    }

    /**
     * Gets a configuration value as a Double.
     *
     * @param key The configuration key to retrieve
     * @param defaultValue The default value to return if the key is not found or cannot be parsed (default: null)
     * @return The configuration value as a Double, or the default value if not found or invalid
     *
     * @sample
     * ```kotlin
     * val precision = fig.getDouble("calculation_precision", 0.001)
     * val latitude = fig.getDouble("default_latitude") // returns null if not found
     * ```
     */
    fun getDouble(key: String, defaultValue: Double? = null): Double? {
        return getAll()?.let { keyValues ->
            keyValues.getOrDefault(key, defaultValue)?.toString()?.toDoubleOrNull()
        } ?: defaultValue
    }
}