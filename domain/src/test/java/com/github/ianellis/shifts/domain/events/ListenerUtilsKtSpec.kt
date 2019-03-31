package com.github.ianellis.shifts.domain.events

import com.github.ianellis.shifts.domain.helpers.mockFunction
import io.mockk.verify
import org.amshove.kluent.shouldEqual
import org.junit.Assert.*
import org.junit.Test
import java.lang.RuntimeException

class ListenerUtilsKtSpec {

    @Test
    fun `map converts listener with provided method`() {
        val rootListener: (String) -> Unit = mockFunction()
        val mappedListener: (Int) -> Unit = rootListener.map {
            "$it"
        }
        //when
        mappedListener(1)

        verify { rootListener.invoke("1") }
    }

    @Test
    fun `Event#map converts value of event and pairs with error`() {
        val originalError = RuntimeException("OOPS")
        val originalEvent = Event("foo", originalError)
        val mapped = originalEvent.map { 1 }
        mapped shouldEqual Event(1, originalError)
    }
}