package com.github.ianellis.shifts.domain.di.shifts

import com.github.ianellis.shifts.domain.Action
import com.github.ianellis.shifts.domain.EndShiftCommand
import com.github.ianellis.shifts.domain.ListenForShiftUpdatesCommand
import com.github.ianellis.shifts.domain.LoadShiftsCommand
import com.github.ianellis.shifts.domain.Shift
import com.github.ianellis.shifts.domain.ShiftRepository
import com.github.ianellis.shifts.domain.ShiftsMemoryCache
import com.github.ianellis.shifts.domain.ShiftsService
import com.github.ianellis.shifts.domain.StartShiftCommand
import com.github.ianellis.shifts.domain.di.ActivityScope
import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.events.LatestEventEmitter
import com.github.ianellis.shifts.domain.location.LocationRespoitory
import com.github.ianellis.shifts.domain.time.Clock
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
class ShiftsDomainModule {

    @Provides
    @StartShift
    fun providesStartShift(
        locationRepository: LocationRespoitory,
        shiftRepository: ShiftRepository,
        clock: Clock
    ): Action {
        return StartShiftCommand(locationRepository, shiftRepository, clock)
    }

    @Provides
    @EndShift
    fun providesEndShift(
        locationRepository: LocationRespoitory,
        shiftRepository: ShiftRepository,
        clock: Clock
    ): Action {
        return EndShiftCommand(locationRepository, shiftRepository, clock)
    }

    @Provides
    @LoadShifts
    fun providesLoadShifts(
        repository: ShiftRepository
    ): Action {
        return LoadShiftsCommand(repository)
    }

    @Provides
    fun providesListenForShifts(
        shiftRepository: ShiftRepository,
        clock: Clock
    ): Function1<@JvmSuppressWildcards Function1<@JvmSuppressWildcards Event<List<Shift>>, kotlin.Unit>, @JvmSuppressWildcards Function0<kotlin.Unit>> {
        return ListenForShiftUpdatesCommand(shiftRepository, clock)
    }

    @Provides
    @ActivityScope
    fun providesShiftRepository(
        service: ShiftsService
    ): ShiftRepository {
        return ShiftRepository(service, ShiftsMemoryCache())
    }


}