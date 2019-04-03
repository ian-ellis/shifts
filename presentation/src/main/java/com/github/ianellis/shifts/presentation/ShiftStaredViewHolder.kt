package com.github.ianellis.shifts.presentation

import com.github.ianellis.shifts.domain.Shift
import com.github.ianellis.shifts.domain.ShiftState
import com.github.ianellis.shifts.presentation.databinding.RecyclerItemShiftStartedBinding

class ShiftStaredViewHolder(private val binding: RecyclerItemShiftStartedBinding) : ShiftViewHolder(binding.root) {

    override fun bind(shift: Shift) {
        val state = shift.state
        when (state) {
            is ShiftState.Started -> {
                binding.start = state.date
            }
            is ShiftState.Complete -> {
                throw IllegalStateException("passed complete  shift to ShiftStaredViewHolder")
            }
        }
    }

}