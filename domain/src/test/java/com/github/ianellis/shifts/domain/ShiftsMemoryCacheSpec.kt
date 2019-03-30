package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.entities.ShiftEntity
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test

class ShiftsMemoryCacheSpec {

    private lateinit var cache: ShiftsMemoryCache

    @Before
    fun setup() {
        cache = ShiftsMemoryCache()
    }

    @Test
    fun `getShifts() - returns empty list if no value set`() {
        runBlocking {
            cache.getShifts() shouldEqual emptyList()
        }
    }

    @Test
    fun `setShifts() - stores shifts to be returned by getShifts()`() {
        val shifts:List<ShiftEntity> = listOf(mockk(), mockk())
        runBlocking {
            cache.setShifts(shifts)
            cache.getShifts() shouldEqual shifts
        }
    }
}