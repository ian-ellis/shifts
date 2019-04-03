package com.github.ianellis.shifts.presentation

import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.ianellis.shifts.domain.Shift

abstract class ShiftViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    abstract fun bind(shift: Shift)
}