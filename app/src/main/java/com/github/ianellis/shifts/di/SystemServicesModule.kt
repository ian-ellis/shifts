package com.github.ianellis.shifts.di

import com.github.ianellis.shifts.domain.time.Clock
import dagger.Module
import dagger.Provides

@Module
class SystemServicesModule {

    @Provides
    fun clock(): Clock = object : Clock {}

}