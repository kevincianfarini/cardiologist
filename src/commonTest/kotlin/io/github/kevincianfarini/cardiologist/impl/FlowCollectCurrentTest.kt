package io.github.kevincianfarini.cardiologist.impl

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FlowCollectCurrentTest {

    @Test
    fun skips_elements_when_backpressure_occurs() = runTest {
        val flow = flow {
            emit(1)
            delay(50)
            emit(2)
        }
        val output = buildList {
            flow.collectCurrent { value ->
                add(value)
                delay(100)
            }
        }
        assertEquals(expected = listOf(1), actual = output)
    }
}