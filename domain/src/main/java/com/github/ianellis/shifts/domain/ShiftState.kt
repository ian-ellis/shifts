package com.github.ianellis.shifts.domain

import java.util.*

sealed class ShiftState {
    data class Started(val date: Date) : ShiftState()
    data class Complete(val start: Date, val end: Date) : ShiftState()
}