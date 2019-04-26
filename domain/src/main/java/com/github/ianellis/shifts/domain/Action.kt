package com.github.ianellis.shifts.domain

interface Action {
    suspend operator fun invoke()
}