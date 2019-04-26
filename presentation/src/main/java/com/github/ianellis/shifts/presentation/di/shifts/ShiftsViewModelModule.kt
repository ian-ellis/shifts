package com.github.ianellis.shifts.presentation.di.shifts

import android.arch.lifecycle.ViewModel
import com.github.ianellis.shifts.domain.Action
import com.github.ianellis.shifts.domain.Shift
import com.github.ianellis.shifts.domain.di.shifts.EndShift
import com.github.ianellis.shifts.domain.di.shifts.LoadShifts
import com.github.ianellis.shifts.domain.di.shifts.StartShift
import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.presentation.ShiftsViewModel
import com.github.ianellis.shifts.presentation.di.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel


@Module
object ShiftsViewModelModule {

    @JvmStatic
    @Provides
    @IntoMap
    @ViewModelKey(ShiftsViewModel::class)
    internal fun postShiftsViewModel(
        listenForShifts: ReceiveChannel<Event<List<Shift>>>,
        @LoadShifts loadShifts: Action,
        @StartShift startShift: Action,
        @EndShift endShift: Action
    ): ViewModel {
        return ShiftsViewModel(listenForShifts, loadShifts, startShift, endShift, Dispatchers.Main)
    }

}