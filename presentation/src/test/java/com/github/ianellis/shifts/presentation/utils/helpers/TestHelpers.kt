package com.github.ianellis.shifts.presentation.helpers

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk


inline fun <reified T : Any> mockFunction(): (T) -> Unit {
    val function: (T) -> Unit = mockk()
    every { function(any()) } just Runs
    return function
}