package com.github.ianellis.shifts.entities

data class EndShiftRequest(
    val time: ISO8601,
    val latitude: String,
    val longitude: String
)