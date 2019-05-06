package com.github.ianellis.shifts.domain.events

import org.amshove.kluent.shouldEqual
import org.junit.Test
import java.lang.RuntimeException

class ExtensionsSpec {
    @Test
    fun `Event#map converts value of event and pairs with error`() {
        val originalError = RuntimeException("OOPS")
        val originalEvent = Event("foo", originalError)
        val mapped = originalEvent.map { 1 }
        mapped shouldEqual Event(1, originalError)
    }
}