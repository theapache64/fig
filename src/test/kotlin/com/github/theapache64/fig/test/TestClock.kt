package com.github.theapache64.fig.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler

class TestClock(private val scheduler: TestCoroutineScheduler) : com.github.theapache64.fig.Clock {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun now() = scheduler.currentTime
}