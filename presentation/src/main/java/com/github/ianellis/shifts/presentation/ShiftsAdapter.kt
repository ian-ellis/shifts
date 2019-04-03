package com.github.ianellis.shifts.presentation

import android.databinding.DataBindingUtil
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.ianellis.shifts.domain.Shift
import com.github.ianellis.shifts.domain.ShiftState
import com.github.ianellis.shifts.presentation.databinding.RecyclerItemShiftCompleteBinding
import com.github.ianellis.shifts.presentation.databinding.RecyclerItemShiftStartedBinding
import java.lang.IllegalStateException

class ShiftsAdapter(private val inflater: LayoutInflater) : RecyclerView.Adapter<ShiftViewHolder>() {

    companion object {
        private val STARTED = 0
        private val COMPLETE = 1
    }

    var shifts: List<Shift> = emptyList()
        set(value) {
            val newValue = value.reversed()
            val diff = DiffUtil.calculateDiff(ShiftDiff(field, newValue))
            field = newValue
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftViewHolder {
        return when (viewType) {
            STARTED -> {
                Log.d("IAN","CREATE: STARTED")
                val binding: RecyclerItemShiftStartedBinding = DataBindingUtil.inflate(inflater, R.layout.recycler_item_shift_started, parent, false)
                ShiftStaredViewHolder(binding)
            }
            COMPLETE -> {
                val binding: RecyclerItemShiftCompleteBinding = DataBindingUtil.inflate(inflater, R.layout.recycler_item_shift_complete, parent, false)
                ShiftCompleteViewHolder(binding)
            }
            else -> throw IllegalStateException("Shift type for viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (shifts[position].state) {
            is ShiftState.Started -> STARTED
            is ShiftState.Complete -> COMPLETE
        }
    }

    override fun getItemCount() = shifts.size

    override fun onBindViewHolder(holder: ShiftViewHolder, position: Int) {
        holder.bind(shifts[position])
    }


}