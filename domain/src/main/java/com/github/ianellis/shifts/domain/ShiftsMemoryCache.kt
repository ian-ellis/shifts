package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.entities.ShiftEntity

class ShiftsMemoryCache : ShiftsCache {
    private var data: List<ShiftEntity> = emptyList()

    override suspend fun setShifts(shifts: List<ShiftEntity>) {
        data = shifts
    }

    override suspend fun getShifts(): List<ShiftEntity> = data

}