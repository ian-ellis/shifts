package com.github.ianellis.shifts.domain.location

import kotlinx.coroutines.Deferred

interface LocationRespoitory {

    fun getLocationAsync(): Deferred<LatLng>
}