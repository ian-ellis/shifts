package com.github.ianellis.shifts.domain.location

import java.lang.RuntimeException

class PermissionRequiredException(val permission:String) : RuntimeException("Location permission not granted")