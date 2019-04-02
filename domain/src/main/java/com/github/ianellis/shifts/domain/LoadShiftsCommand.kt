package com.github.ianellis.shifts.domain

class LoadShiftsCommand constructor(
    private val shiftsRepository: ShiftRepository
) : () -> Unit {
    override fun invoke() {
        shiftsRepository.loadShifts()
    }
}