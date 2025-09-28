package com.github.theapache64.fig

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler

class TestClock(private val scheduler: TestCoroutineScheduler) : Clock {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun now() = scheduler.currentTime
}