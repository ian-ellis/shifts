package com.github.ianellis.shifts

import android.app.Activity
import android.app.Application
import com.github.ianellis.shifts.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class ShiftsApplication : Application(), HasActivityInjector {

    @field:[Inject]
    internal lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingActivityInjector
}