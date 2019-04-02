package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.entities.EndShiftRequest
import com.github.ianellis.shifts.entities.ShiftEntity
import com.github.ianellis.shifts.entities.ShiftsResponseEntity
import com.github.ianellis.shifts.entities.StartShiftRequest
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ShiftsService {

    @POST("/dmc/shift/start")
    fun startShiftAsync(@Body request: StartShiftRequest): Deferred<Unit>

    @POST("/dmc/shift/end")
    fun endShiftAsync(@Body request: EndShiftRequest): Deferred<Unit>

    @GET("/dmc/shifts")
    fun shiftAsync(): Deferred<ShiftsResponseEntity>
}