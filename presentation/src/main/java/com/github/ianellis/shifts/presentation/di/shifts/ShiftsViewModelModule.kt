package com.github.ianellis.shifts.presentation.di.shifts

import android.arch.lifecycle.ViewModel
import com.github.ianellis.shifts.domain.Shift
import com.github.ianellis.shifts.domain.di.shifts.EndShift
import com.github.ianellis.shifts.domain.di.shifts.LoadShifts
import com.github.ianellis.shifts.domain.di.shifts.StartShift
import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.events.Ignore
import com.github.ianellis.shifts.presentation.ShiftsViewModel
import com.github.ianellis.shifts.presentation.di.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers


@Module
object ShiftsViewModelModule {

    @JvmStatic
    @Provides
    @IntoMap
    @ViewModelKey(ShiftsViewModel::class)
    internal fun postShiftsViewModel(
        listenForShifts: Function1<
            @JvmSuppressWildcards Function1<@JvmSuppressWildcards Event<List<Shift>>, Unit>,
            @JvmSuppressWildcards Function0<Unit>
            >,
        @LoadShifts loadShifts: () -> Unit,
        @StartShift startShift: () -> @JvmSuppressWildcards Deferred<Unit>,
        @EndShift endShift: () -> @JvmSuppressWildcards Deferred<Unit>
    ): ViewModel {
        return ShiftsViewModel(listenForShifts, loadShifts, startShift, endShift, Dispatchers.Main)
    }

}