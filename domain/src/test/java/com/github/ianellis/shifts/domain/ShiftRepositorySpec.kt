package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.events.LatestEventEmitter
import com.github.ianellis.shifts.domain.events.SimpleEventEmitter
import com.github.ianellis.shifts.domain.helpers.mockFunction
import com.github.ianellis.shifts.entities.EndShiftRequest
import com.github.ianellis.shifts.entities.ShiftEntity
import com.github.ianellis.shifts.entities.StartShiftRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test

class ShiftRepositorySpec {

    private lateinit var service: ShiftsService
    private lateinit var cache: ShiftsCache
    private lateinit var repository: ShiftRepository

    @Before
    fun setup() {
        service = mockk()
        cache = mockk(relaxUnitFun = true)
        repository = ShiftRepository(service, cache, Dispatchers.Default, LatestEventEmitter())
    }

    @Test
    fun `getShiftsAsync() - returns cached shifts`() {
        //given we have saved shifts
        val cachedShifts = listOf<ShiftEntity>(mockk(), mockk())
        coEvery { cache.getShifts() } returns cachedShifts

        //when we request shifts
        val shifts = runBlocking {
            repository.getShiftsAsync().await()
        }

        //then we should be returned the cached shifts
        shifts shouldEqual cachedShifts
    }

    @Test
    fun `startShiftAsync() - calls service to start shift, reloads shifts on success`() {
        //give the following data
        val time = "somedate"
        val lat = "someLat"
        val long = "someLat"

        //and the service will respond successfully
        coEvery { service.startShiftAsync(StartShiftRequest(time, lat, long)) } returns GlobalScope.async { Unit }

        //and reloading will be successful
        val loadedShifts: List<ShiftEntity> = listOf(mockk(), mockk())
        coEvery { service.shiftAsync() } returns GlobalScope.async { loadedShifts }

        //and we are listening for events
        val listener: (Event<List<ShiftEntity>>) -> Unit = mockFunction()
        repository.addListener(listener)

        //when we start a shift
        runBlocking {
            repository.startShiftAsync(time, lat, long).await()
        }

        //then we pass the data to the service as StartShiftRequest
        coVerify(exactly = 1) { service.startShiftAsync(StartShiftRequest(time, lat, long)) }

        //then we reload shifts
        coVerify(exactly = 1) { service.shiftAsync() }

        //and cache the returned value
        coVerify { cache.setShifts(loadedShifts) }

        //then we receive an update of new shifts
        verify { listener(Event(loadedShifts)) }
    }

    @Test
    fun `endShiftAsync() - calls service to end shift, reloads shifts on success, then emits events`() {
        //give the following data
        val time = "somedate"
        val lat = "someLat"
        val long = "someLat"

        //and the service will respond successfully
        every { service.endShiftAsync(EndShiftRequest(time, lat, long)) } returns GlobalScope.async { Unit }

        //and reloading will be successful
        val loadedShifts: List<ShiftEntity> = listOf(mockk(), mockk())
        coEvery { service.shiftAsync() } returns GlobalScope.async { loadedShifts }

        //and we are listening for events
        val listener: (Event<List<ShiftEntity>>) -> Unit = mockFunction()
        repository.addListener(listener)

        //when we start a shift
        runBlocking {
            repository.endShiftAsync(time, lat, long).await()
        }

        //then we pass the data to the service as EndShiftRequest
        coVerify(exactly = 1) { service.endShiftAsync(EndShiftRequest(time, lat, long)) }

        //then we reload shifts
        coVerify(exactly = 1) { service.shiftAsync() }

        //and cache the returned value
        coVerify { cache.setShifts(loadedShifts) }

        //then we receive an update of new shifts
        verify { listener(Event(loadedShifts)) }
    }

    @Test
    fun `loadShifts() - loads shifts and emits as new event`() {
        //given loading shifts will succeed
        val loadedShifts: List<ShiftEntity> = listOf(mockk(), mockk())
        coEvery { service.shiftAsync() } returns GlobalScope.async { loadedShifts }

        //and we are listening for events
        val listener: (Event<List<ShiftEntity>>) -> Unit = mockFunction()
        repository.addListener(listener)

        //when we loadShifts
        repository.loadShifts()

        //then we store the response
        coVerify { cache.setShifts(loadedShifts) }

        //then we get updated with the new events
        verify { listener(Event(loadedShifts)) }
    }

    @Test
    fun `loadShifts() - returns previous shifts and error when error loading`() {
        //given we have previously cached shifts
        val cachedShifts: List<ShiftEntity> = listOf(mockk(), mockk())
        coEvery { cache.getShifts() } returns cachedShifts

        //and loading shifts will fail
        val networkError = RuntimeException("OOPS")
        coEvery { service.shiftAsync() } throws networkError

        //and we are listening for events
        val listener: (Event<List<ShiftEntity>>) -> Unit = mockFunction()
        repository.addListener(listener)

        //when we loadShifts
        repository.loadShifts()

        //then we receive the previously cached values with the network exception
        verify { listener(Event(cachedShifts, networkError )) }
    }

    @Test
    fun `removeListener unregisters listener, no further events received`(){
        //given loading shifts will succeed
        val loadedShifts: List<ShiftEntity> = listOf(mockk(), mockk())
        coEvery { service.shiftAsync() } returns GlobalScope.async { loadedShifts }

        //and we are listening for events
        val listener: (Event<List<ShiftEntity>>) -> Unit = mockFunction()
        repository.addListener(listener)

        //when we unregister
        repository.removeListener(listener)

        //and we load
        repository.loadShifts()

        //then we do not receive any events
        verify(exactly = 0) { listener(any()) }
    }
}