package com.github.ianellis.shifts.domain.time

import java.util.*

interface Clock {
    fun now() = Date()
    fun timezone() = Calendar.getInstance().timeZone
}