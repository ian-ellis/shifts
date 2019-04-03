package com.github.ianellis.shifts.presentation

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import com.github.ianellis.shifts.domain.Shift

class ShiftsRecycler : RecyclerView {

    private val shiftsAdapter = ShiftsAdapter(LayoutInflater.from(context))

    constructor (context: Context) : super(context)
    constructor (context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor (context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    init {
        adapter = shiftsAdapter
        layoutManager = LinearLayoutManager(context)
    }

    fun setShifts(shifts: List<Shift>) {
        shiftsAdapter.shifts = shifts
    }

}