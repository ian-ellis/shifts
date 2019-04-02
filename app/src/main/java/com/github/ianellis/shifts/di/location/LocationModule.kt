package com.github.ianellis.shifts.di.location

import android.app.Application
import com.github.ianellis.shifts.domain.location.LocationRespoitory
import com.github.ianellis.shifts.location.AndroidLocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides

@Module
class LocationModule {

    @Provides
    fun providesLocationRepository(context: Application): LocationRespoitory {
        return AndroidLocationRepository(context, FusedLocationProviderClient(context))
    }
}