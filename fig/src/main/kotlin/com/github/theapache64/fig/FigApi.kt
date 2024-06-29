package com.github.theapache64.fig

import com.github.theapache64.retrosheet.annotations.Read
import retrofit2.http.GET

internal interface FigApi {
    @Read("SELECT *")
    @GET("Sheet1")
    suspend fun getKeyValues(): List<KeyValue>
}