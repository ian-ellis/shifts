package com.github.ianellis.shifts.presentation

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.ianellis.shifts.presentation.databinding.ActivityShiftsBinding
import dagger.android.AndroidInjection
import javax.inject.Inject

class ShiftsActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var vm: ShiftsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        vm = ViewModelProviders.of(this, viewModelFactory)[ShiftsViewModel::class.java]
        val binding = DataBindingUtil.setContentView<ActivityShiftsBinding>(this, R.layout.activity_shifts)
        binding.lifecycleOwner = this
        binding.viewModel = vm

    }
    
}
