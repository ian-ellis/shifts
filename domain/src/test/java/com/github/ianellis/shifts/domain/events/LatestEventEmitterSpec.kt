package com.github.ianellis.shifts.domain.events

import com.github.ianellis.shifts.domain.helpers.mockFunction
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class LatestEventEmitterSpec {

    private lateinit var emitter: LatestEventEmitter<String>

    @Before
    fun setup() {
        emitter = LatestEventEmitter()
    }

    @Test
    fun `addListener() - adds listener and receives subsequent events`() {
        //given we have added a listener
        val listener: (Event<String>) -> Unit = mockFunction()
        emitter.addListener(listener)

        //when we emmit an event
        val event = Event("Foo")
        emitter.sendEvent(event)

        //then we receive the event
        verify(exactly = 1) { listener(event) }
    }

    @Test
    fun `addListener() - adds listener and receives previous event`() {
        //given we have previously sent an event
        val event = Event("Foo")
        emitter.sendEvent(event)

        //when we have added a listener
        val listener: (Event<String>) -> Unit = mockFunction()
        emitter.addListener(listener)

        //then we do not receive the event
        verify(exactly = 1) { listener(event) }
    }

    @Test
    fun `removeListener() - removes listener and does not receive further events`() {
        //given we have added a listener
        val listener: (Event<String>) -> Unit = mockFunction()
        emitter.addListener(listener)

        //and removed it
        emitter.removeListener(listener)

        //when we emmit an event
        val event = Event("Foo")
        emitter.sendEvent(event)

        //then we do not receive the event
        verify(exactly = 0) { listener(event) }
    }
}