package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.domain.location.LatLng
import com.github.ianellis.shifts.domain.location.LocationRespoitory
import com.github.ianellis.shifts.domain.location.LocationUnavailableException
import com.github.ianellis.shifts.domain.time.Clock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*

class StartShiftCommandSpec {

    private lateinit var locationRepository: LocationRespoitory
    private lateinit var shiftRepository: ShiftRepository
    private lateinit var clock: Clock
    private lateinit var command: StartShiftCommand

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

        command = StartShiftCommand(locationRepository, shiftRepository, clock)
    }


    @Test
    fun `invoke() - retrieves location and makes call to shiftRepository`() {
        //given we will get the location successfully
        val lat: Double = -33.881399
        val lng: Double = 151.199564
        coEvery { locationRepository.getLocation() } returns LatLng(lat, lng)

        //and starting a shift will succeed
        coEvery { shiftRepository.startShift("2019-03-31T19:25:59+11:00", "-33.881399", "151.199564") } returns Unit

        //when we invoke
        runBlocking {
            command.invoke()
        }

        coVerify { shiftRepository.startShift("2019-03-31T19:25:59+11:00", "-33.881399", "151.199564") }

    }

    @Test
    fun `invoke() uses default location if Location unavailable`() {
        //given we will get the location successfully

        coEvery { locationRepository.getLocation() } throws LocationUnavailableException()

        //and starting a shift will succeed
        coEvery { shiftRepository.startShift("2019-03-31T19:25:59+11:00", "0.0", "0.0") } returns Unit

        //when we invoke
        runBlocking {
            command.invoke()
        }

        coVerify { shiftRepository.startShift("2019-03-31T19:25:59+11:00", "0.0", "0.0") }
    }
}