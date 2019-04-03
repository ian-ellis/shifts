package com.github.ianellis.shifts.presentation

import android.databinding.BindingAdapter
import android.widget.TextView
import com.github.ianellis.shifts.domain.Shift
import java.text.SimpleDateFormat
import java.util.*

object ShiftBindings {

    @JvmStatic
    @BindingAdapter("shifts")
    fun bindShifts(recycler: ShiftsRecycler, shifts: List<Shift>?) {
        recycler.setShifts(shifts ?: emptyList())
    }

    @JvmStatic
    @BindingAdapter("shiftTime")
    fun bindShifts(text: TextView, date: Date?) {
        val defaultValue = ""
        text.text = date?.let {
            val format = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            format.timeZone = Calendar.getInstance().timeZone
            format.format(it)
        } ?: defaultValue
    }
}