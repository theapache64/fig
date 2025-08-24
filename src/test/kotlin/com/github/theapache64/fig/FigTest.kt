package com.github.theapache64.fig

import com.github.theapache64.expekt.should
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class FigTest {

    val fig = Fig().apply {
        runBlocking {
            init("https://docs.google.com/spreadsheets/d/1LD1Su7HVzAxPlbRp9MO7lni2E5SOqfAsLMCd1FC9A8s/edit?usp=sharing")
        }
    }


    @Test
    fun `simple getValue call`() {
        fig.getValue("fruit", null).should.equal("apple")
    }

    @Test
    fun test2() {
        println("QuickTag: FigTest:test2: ")
    }
}