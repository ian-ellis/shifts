package com.github.ianellis.shifts.domain

import java.net.URL

data class Shift(
    val id: Int,
    val state: ShiftState,
    val imageUrl: URL
)