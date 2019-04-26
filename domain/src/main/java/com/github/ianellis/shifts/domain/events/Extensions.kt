package com.github.ianellis.shifts.domain.events

fun <T, R> Event<T>.map(mapper: (T) -> R): Event<R> {
    return this.value?.let {
        Event(mapper(it), this.error)
    } ?: Event(this.error!!)
}