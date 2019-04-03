package com.github.ianellis.shifts.presentation

import com.github.ianellis.shifts.domain.Shift
import com.github.ianellis.shifts.domain.ShiftState
import com.github.ianellis.shifts.presentation.databinding.RecyclerItemShiftCompleteBinding
import java.lang.IllegalStateException

class ShiftCompleteViewHolder(private val binding: RecyclerItemShiftCompleteBinding) : ShiftViewHolder(binding.root) {

    override fun bind(shift: Shift) {
        val state = shift.state
        when (state) {
            is ShiftState.Started -> {
                throw IllegalStateException("passed started shift to ShiftCompleteViewHolder")
            }
            is ShiftState.Complete -> {
                binding.start = state.start
                binding.end = state.end
                binding.imageUrl = shift.imageUrl.toString()
            }
        }
    }
}