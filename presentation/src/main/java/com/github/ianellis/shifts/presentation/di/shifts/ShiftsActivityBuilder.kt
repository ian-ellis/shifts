package com.github.ianellis.shifts.presentation.di.shifts

import com.github.ianellis.shifts.domain.di.ActivityScope
import com.github.ianellis.shifts.domain.di.shifts.ShiftsDomainModule
import com.github.ianellis.shifts.presentation.ShiftsActivity
import com.github.ianellis.shifts.presentation.di.ViewModelFactoryModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ShiftsActivityBuilder {

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        ViewModelFactoryModule::class,
        ShiftsViewModelModule::class,
        ShiftsDomainModule::class
    ])

    abstract fun bindShiftsActivity(): ShiftsActivity

}