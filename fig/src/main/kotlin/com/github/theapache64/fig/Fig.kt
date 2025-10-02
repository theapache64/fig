package com.github.theapache64.fig

import com.github.theapache64.retrosheet.RetrosheetInterceptor
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.math.roundToInt
import kotlin.time.Duration

/**
 * Custom exception thrown when Fig encounters configuration or data errors.
 *
 * @param message The error message describing what went wrong
 */
class FigException(message: String) : Exception(message)

/**
 * Fig is a configuration management class that loads key-value pairs from Google Sheets.
 * It provides type-safe accessors for different data types and caches the data in memory
 * for efficient access.
 *
 * @param sheetUrl The public URL of the Google Sheet containing configuration data.
 *                 The sheet should have two columns: 'key' and 'value'.
 *                 URLs ending with "edit?usp=sharing" will be automatically converted.
 */
class Fig(
    private val sheetUrl: String,
    private val clock: Clock = SystemClock()
) {
    companion object {
        private const val KEY_MISSING_ERROR = "Required value 'key' missing at \$[1]"
    }

    private fun String.toCallSiteKeyOrThrow(tll: Duration): String {
        return "${this}:${tll.inWholeMilliseconds}"
    }

    private val figApi by lazy {
        val retrosheetInterceptor =
            RetrosheetInterceptor.Builder().addSheet("Sheet1", "key", "value").build()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(retrosheetInterceptor) // and attach the interceptor
            .build()

        val moshi = Moshi.Builder()
            .build()

        val url = if (sheetUrl.endsWith("edit?usp=sharing")) {
            sheetUrl.replace("edit?usp=sharing", "")
        } else {
            sheetUrl
        }

        // Building retrofit client
        Retrofit.Builder()
            // with baseUrl as sheet's public URL
            .baseUrl(url) // Sheet's public URL
            .client(okHttpClient).addConverterFactory(MoshiConverterFactory.create(moshi)).build()
            .create(FigApi::class.java)
    }

    private var cacheExpiryMap: MutableMap<String, Long> = mutableMapOf()
    private var inMemCache: Map<String, Any?>? = null
    var inMemCacheUpdatedAt: Long? = null

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
        try {
            inMemCache = figApi.getKeyValues().associate { it.key to it.value }
            inMemCacheUpdatedAt = clock.now()
            println("QuickTag: Fig:init: $inMemCache")
        } catch (e: JsonDataException) {
            if (e.message == KEY_MISSING_ERROR) {
                throw FigException("You can't use multiple data types. Use `=TO_TEXT()` to convert non-string values in your sheet")
            } else {
                throw FigException(e.message ?: "Unknown error")
            }
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

    fun getLong(key: String, defaultValue: Long? = null): Long? {
        return getAll()?.let { keyValues ->
            getAll()?.getOrDefault(key, defaultValue)?.toString()?.toLongOrNull()
        } ?: defaultValue
    }

    suspend fun getBoolean(key: String, defaultValue: Boolean? = null, timeToLive: Duration? = null): Boolean? {
        return withCacheExpiry(
            key = key,
            timeToLive = timeToLive,
        ) { getBoolean(key, defaultValue) }
    }

    suspend fun getString(key: String, defaultValue: String? = null, timeToLive: Duration? = null): String? {
        return withCacheExpiry(
            key = key,
            timeToLive = timeToLive,
        ) { getString(key, defaultValue) }
    }

    suspend fun getInt(key: String, defaultValue: Int? = null, timeToLive: Duration? = null): Int? {
        return withCacheExpiry(
            key = key,
            timeToLive = timeToLive,
        ) { getInt(key, defaultValue) }
    }

    suspend fun getDouble(key: String, defaultValue: Double? = null, timeToLive: Duration? = null): Double? {
        return withCacheExpiry(
            key = key,
            timeToLive = timeToLive,
        ) { getDouble(key, defaultValue) }
    }

    suspend fun getLong(key: String, defaultValue: Long? = null, timeToLive: Duration? = null): Long? {
        return withCacheExpiry(
            key = key,
            timeToLive = timeToLive,
        ) { getLong(key, defaultValue) }
    }

    suspend fun getFloat(key: String, defaultValue: Float? = null, timeToLive: Duration? = null): Float? {
        return withCacheExpiry(
            key = key,
            timeToLive = timeToLive,
        ) { getFloat(key, defaultValue) }
    }

    private suspend fun <T> withCacheExpiry(key: String, timeToLive: Duration?, block: () -> T): T {
        if (timeToLive != null) {
            // more like refreshInMemCacheIfNeeded(key)
            val callSiteKey = key.toCallSiteKeyOrThrow(timeToLive)
            val currentTime = clock.now()
            val expiryTime = cacheExpiryMap[callSiteKey]
            if (expiryTime != null && currentTime >= expiryTime) {
                load()
            }
        }
        return block().also {
            // update expiry time
            if (timeToLive != null) {
                // more like updateInMemCacheExpiry(key, timeToLive)
                val callSiteKey = key.toCallSiteKeyOrThrow(timeToLive)
                val currentTime = clock.now()
                val newExpiryTime = currentTime + timeToLive.inWholeMilliseconds
                cacheExpiryMap[callSiteKey] = newExpiryTime
            }
        }
    }

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