package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.helpers.mockFunction
import com.github.ianellis.shifts.domain.time.Clock
import com.github.ianellis.shifts.entities.ShiftEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.*

class ListenForShiftUpdatesCommandSpec {

    private lateinit var repository: ShiftRepository
    private lateinit var clock: Clock

    private lateinit var command: ListenForShiftUpdatesCommand

    companion object {
        const val TEST_TIME = 1554020759599L
    }

    @Before
    fun setup() {
        repository = mockk(relaxUnitFun = true)
        clock = object : Clock {
            override fun now() = Date(TEST_TIME)
            override fun timezone() = TimeZone.getTimeZone("Australia/Sydney")
        }

        command = ListenForShiftUpdatesCommand(repository, clock)
    }

    @Test
    fun `invoke() registers listener for events and maps repository data to Shift Entity`() {
        //given
        val repositoryListenerSlot = slot<(Event<List<ShiftEntity>>) -> Unit>()
        every { repository.addListener(capture(repositoryListenerSlot)) } returns Unit

        //when we invoke the command
        val listener = mockFunction<Event<List<Shift>>>()
        command(listener)

        //then we register a listener in the repository
        repositoryListenerSlot.isCaptured shouldBe true

        // when the repository listener receives an event
        val imageUrl1 = "http://google.com/some1.jpg"
        val imageUrl2 = "http://google.com/some2.jpg"

        val shiftEntities = listOf(
            ShiftEntity(2, "2019-04-02T08:15:43+11:00", "", "0.00", "0.00", "", "", imageUrl2),
            ShiftEntity(1, "2019-03-31T21:15:18+11:00", "2019-03-31T22:15:43+11:00", "0.00", "0.00", "0.00", "0.00", imageUrl1)
        )
        repositoryListenerSlot.captured(Event(shiftEntities))

        //then we receive mapped Shift data objects
        val expectedShifts = listOf(
            Shift(ShiftState.Started(Date(1554153343000)), URL(imageUrl2)),
            Shift(ShiftState.Complete(Date(1554027318000), Date(1554030943000)), URL(imageUrl1))
        )

        verify { listener(Event(expectedShifts)) }
    }

    @Test
    fun `invoke() returns function to ignore events`() {
        //given
        val repositoryListenerSlot = slot<(Event<List<ShiftEntity>>) -> Unit>()
        every { repository.addListener(capture(repositoryListenerSlot)) } returns Unit

        //when we invoke the command
        val ignore = command(mockFunction())

        //then we register a listener in the repository
        repositoryListenerSlot.isCaptured shouldBe true

        // when we invoke the returned ignore function
        ignore()

        //then we unregister the listener from the repository
        repository.removeListener(repositoryListenerSlot.captured)
    }
}