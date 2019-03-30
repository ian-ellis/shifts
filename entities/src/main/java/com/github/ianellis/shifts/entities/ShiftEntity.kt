package com.github.ianellis.shifts.entities

data class ShiftEntity(
    val id: Int,
    val start: ISO8601,
    val end: ISO8601,
    val startLatitude: String,
    val startLongitude: String,
    val endLatitude: String,
    val endLongitude: String,
    val image: URLString
)