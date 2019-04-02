package com.github.ianellis.shifts.domain

import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class LoadShiftsCommandSpec {
    
    private lateinit var repository: ShiftRepository
    private lateinit var command: LoadShiftsCommand

    @Before
    fun setup(){
        repository = mockk(relaxUnitFun = true)
        command = LoadShiftsCommand(repository)
    }

    @Test
    fun `invoke()- calls delegates load call to repository`(){
        command()
        verify { repository.loadShifts() }
    }
}