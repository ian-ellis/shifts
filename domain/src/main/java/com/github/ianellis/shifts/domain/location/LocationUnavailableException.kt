package com.github.ianellis.shifts.domain.location

import java.lang.RuntimeException

class LocationUnavailableException : RuntimeException("Sorry We were unable to find your location")