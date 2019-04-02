package com.github.ianellis.shifts.domain.events

interface EventEmitter<T> {

    fun addListener(listener: (Event<T>) -> Unit)
    fun removeListener(listener: (Event<T>) -> Unit)
    fun sendEvent(event: Event<T>)
}
