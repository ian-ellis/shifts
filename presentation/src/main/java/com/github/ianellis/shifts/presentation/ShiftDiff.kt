package com.github.ianellis.shifts.presentation

import android.support.v7.util.DiffUtil
import com.github.ianellis.shifts.domain.Shift

class ShiftDiff(private val old:List<Shift>, private val new:List<Shift>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition].id == new[newItemPosition].id
    }

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int  = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }
}