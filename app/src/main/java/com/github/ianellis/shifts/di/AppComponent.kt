package com.github.ianellis.shifts.di

import android.app.Application
import com.github.ianellis.shifts.ShiftsApplication
import com.github.ianellis.shifts.di.location.LocationModule
import com.github.ianellis.shifts.di.networking.ServicesModule
import com.github.ianellis.shifts.presentation.di.shifts.ShiftsActivityBuilder
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AndroidSupportInjectionModule::class,
    LocationModule::class,
    ShiftsActivityBuilder::class,
    SystemServicesModule::class,
    ServicesModule::class
])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: ShiftsApplication)
}