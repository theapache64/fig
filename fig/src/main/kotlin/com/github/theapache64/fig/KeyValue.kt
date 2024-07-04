package com.github.theapache64.fig

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KeyValue(
    @Json(name = "key")
    val key: String,
    @Json(name = "value")
    val value: String? = null
)
