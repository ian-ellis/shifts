package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.helpers.mockFunction
import com.github.ianellis.shifts.domain.time.Clock
import com.github.ianellis.shifts.entities.ShiftEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.*

class ListenForShiftUpdatesCommandSpec {

    private lateinit var repository: ShiftRepository
    private lateinit var broadcastChannel: ConflatedBroadcastChannel<Event<List<ShiftEntity>>>
    private lateinit var clock: Clock

    private lateinit var command: ListenForShiftUpdatesCommand

    companion object {
        const val TEST_TIME = 1554020759599L
    }

    @Before
    fun setup() {
        repository = mockk(relaxUnitFun = true)

        broadcastChannel = ConflatedBroadcastChannel()
        every { repository.broadcastChannel } returns broadcastChannel

        clock = object : Clock {
            override fun now() = Date(TEST_TIME)
            override fun timezone() = TimeZone.getTimeZone("Australia/Sydney")
        }

        command = ListenForShiftUpdatesCommand(repository, clock)
    }

    @Test
    fun `invoke() registers listener for events and maps repository data to Shift Entity`() {
        // given the following entities
        val imageUrl1 = "http://google.com/some1.jpg"
        val imageUrl2 = "http://google.com/some2.jpg"

        val shiftEntities = listOf(
            ShiftEntity(2, "2019-04-02T08:15:43+11:00", "", "0.00", "0.00", "", "", imageUrl2),
            ShiftEntity(1, "2019-03-31T21:15:18+11:00", "2019-03-31T22:15:43+11:00", "0.00", "0.00", "0.00", "0.00", imageUrl1)
        )

        //when the repository broadcasts an event
        runBlocking {
            broadcastChannel.send(Event(shiftEntities))
        }

        //then we receive mapped Shift data objects
        val expectedShifts = listOf(
            Shift(2, ShiftState.Started(Date(1554153343000)), URL(imageUrl2)),
            Shift(1, ShiftState.Complete(Date(1554027318000), Date(1554030943000)), URL(imageUrl1))
        )

        runBlocking {
            command().receive() shouldEqual Event(expectedShifts)
        }

    }
}