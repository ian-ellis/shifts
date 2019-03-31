package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.domain.location.LatLng
import com.github.ianellis.shifts.domain.location.LocationRespoitory
import com.github.ianellis.shifts.domain.time.Clock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class EndShiftCommandSpec {
    private lateinit var locationRepository: LocationRespoitory
    private lateinit var shiftRepository: ShiftRepository
    private lateinit var clock: Clock
    private lateinit var command: EndShiftCommand

    companion object {
        const val TEST_TIME = 1554020759599L
    }

    @Before
    fun setup() {
        locationRepository = mockk()
        shiftRepository = mockk()
        clock = object : Clock {
            override fun now() = Date(TEST_TIME)
            override fun timezone() = TimeZone.getTimeZone("Australia/Sydney")
        }

        command = EndShiftCommand(locationRepository, shiftRepository, clock, Dispatchers.Default)
    }


    @Test
    fun `invoke() - retrieves location and makes call to shiftRepository`() {
        //given we will get the location successfully
        val lat: Double = -33.881399
        val lng: Double = 151.199564
        coEvery { locationRepository.getLocationAsync() } returns GlobalScope.async { LatLng(lat, lng) }

        //and starting a shift will succeed
        coEvery { shiftRepository.endShiftAsync("2019-03-31T19:25:59+11:00", "-33.881399", "151.199564") } returns GlobalScope.async { Unit }

        //when we invoke
        runBlocking {
            command.invoke().await()
        }

        coVerify { shiftRepository.endShiftAsync("2019-03-31T19:25:59+11:00", "-33.881399", "151.199564") }

    }
}