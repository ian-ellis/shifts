package com.github.ianellis.shifts.domain

class LoadShiftsCommand constructor(
    private val shiftsRepository: ShiftRepository
) : Action {
    override suspend fun invoke() {
        shiftsRepository.loadShifts()
    }
}