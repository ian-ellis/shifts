package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.entities.EndShiftRequest
import com.github.ianellis.shifts.entities.ISO8601
import com.github.ianellis.shifts.entities.ShiftEntity
import com.github.ianellis.shifts.entities.StartShiftRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

@ExperimentalCoroutinesApi
class ShiftRepository(
    private val service: ShiftsService,
    private val cache: ShiftsCache
) {

    val broadcastChannel = ConflatedBroadcastChannel<Event<List<ShiftEntity>>>()

    suspend fun loadShifts() {
        doLoad()
    }

    suspend fun getShifts(): List<ShiftEntity> = cache.getShifts()

    suspend fun startShift(time: ISO8601, lat: String, lon: String) {
        service.startShiftAsync(StartShiftRequest(time, lat, lon)).await()
        doLoad()
    }

    suspend fun endShift(time: ISO8601, lat: String, lon: String) {
        service.endShiftAsync(EndShiftRequest(time, lat, lon)).await()
        doLoad()
    }

    private suspend fun doLoad() {
        try {
            val shifts = service.shiftAsync().await()
            cache.setShifts(shifts)
            broadcastChannel.send((Event(shifts)))
        } catch (e: Exception) {
            val previousShifts = cache.getShifts()
            broadcastChannel.send(Event(previousShifts, e))
        }

    }


}