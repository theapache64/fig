package com.github.theapache64.fig

interface Clock {
    fun now(): Long
}

class SystemClock : Clock {
    override fun now() = System.currentTimeMillis()
}