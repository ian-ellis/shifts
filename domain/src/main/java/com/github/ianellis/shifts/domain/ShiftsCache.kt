package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.entities.ShiftEntity

interface ShiftsCache {
    suspend fun setShifts(shifts: List<ShiftEntity>)
    suspend fun getShifts(): List<ShiftEntity>
}