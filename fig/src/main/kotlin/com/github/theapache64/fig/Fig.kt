package com.github.theapache64.fig

import com.github.theapache64.retrosheet.RetrosheetInterceptor
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.jvm.Throws

class FigException (message: String) : Exception(message)

class Fig  {
    companion object{
        private const val KEY_MISSING_ERROR = "Required value 'key' missing at \$[1]"
    }

    private var inMemCache: Map<String, String?>? = null

    @Throws(FigException::class, JsonDataException::class)
    suspend fun init(
        sheetUrl: String
    ) = withContext(Dispatchers.IO) {
        val retrosheetInterceptor = RetrosheetInterceptor.Builder()
            .setLogging(false)
            .addSheet("Sheet1", "key", "value")
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(retrosheetInterceptor) // and attach the interceptor
            .build()

        val moshi = Moshi.Builder()
            .build()

        val url = if(sheetUrl.endsWith("edit?usp=sharing")){
            sheetUrl.replace("edit?usp=sharing", "")
        }else{
            sheetUrl
        }

        // Building retrofit client
        val figApi = Retrofit.Builder()
            // with baseUrl as sheet's public URL
            .baseUrl(url) // Sheet's public URL
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FigApi::class.java)

        try {
            inMemCache = figApi.getKeyValues().associate { it.key to it.value }
        }catch (e: JsonDataException){
            if(e.message == KEY_MISSING_ERROR){
                throw FigException("You can't use both string and int. Use `=TO_TEXT()` to convert to string")
            }else{
                throw FigException(e.message ?: "Unknown error")
            }
        }
    }

    fun getValue(key: String, defaultValue: String?): String? {
        return inMemCache.let { keyValues ->
            if(keyValues == null) {
                println("WARNING: Fig.init not called, failed or not completed yet")
            }
            keyValues?.get(key) ?: defaultValue
        }
    }
}