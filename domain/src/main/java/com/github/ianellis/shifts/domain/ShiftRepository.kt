package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.events.EventEmitter
import com.github.ianellis.shifts.entities.EndShiftRequest
import com.github.ianellis.shifts.entities.ISO8601
import com.github.ianellis.shifts.entities.ShiftEntity
import com.github.ianellis.shifts.entities.StartShiftRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class ShiftRepository(
    private val service: ShiftsService,
    private val cache: ShiftsCache,
    private val backgroundDispatcher: CoroutineDispatcher,
    private val eventEmitter: EventEmitter<List<ShiftEntity>>
) {

    fun addListener(listener: (Event<List<ShiftEntity>>) -> Unit) {
        eventEmitter.addListener(listener)
    }

    fun removeListener(listener: (Event<List<ShiftEntity>>) -> Unit) {
        eventEmitter.removeListener(listener)
    }

    fun loadShifts() {
        doLoadAsync()
    }

    fun getShiftsAsync(): Deferred<List<ShiftEntity>> {
        return GlobalScope.async(backgroundDispatcher) {
            cache.getShifts()
        }
    }

    fun startShiftAsync(time: ISO8601, lat: String, lon: String): Deferred<Unit> {
        return GlobalScope.async(backgroundDispatcher) {
            service.startShiftAsync(StartShiftRequest(time, lat, lon)).await()
            doLoadAsync().await()
        }
    }

    fun endShiftAsync(time: ISO8601, lat: String, lon: String): Deferred<Unit> {
        return GlobalScope.async(backgroundDispatcher) {
            service.endShiftAsync(EndShiftRequest(time, lat, lon)).await()
            doLoadAsync().await()
        }
    }

    private fun doLoadAsync(): Deferred<Unit> {
        return GlobalScope.async(backgroundDispatcher) {
            try {
                val shifts = service.shiftAsync().await()
                cache.setShifts(shifts)
                eventEmitter.sendEvent(Event(shifts))
            } catch (e: Exception) {
                val previousShifts = cache.getShifts()
                eventEmitter.sendEvent(Event(previousShifts, e))
            }
        }
    }


}