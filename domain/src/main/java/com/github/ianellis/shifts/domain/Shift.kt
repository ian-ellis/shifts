package com.github.ianellis.shifts.domain

import java.net.URL

data class Shift(
    val state: ShiftState,
    val imageUrl: URL
)