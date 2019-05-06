package com.github.ianellis.shifts.domain.location


interface LocationRespoitory {

    suspend fun getLocation(): LatLng
}