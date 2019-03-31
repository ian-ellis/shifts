package com.github.ianellis.shifts.domain.events

import java.lang.ref.WeakReference

class LatestEventEmitter<T> : EventEmitter<T> {

    private var lastEvent: Event<T>? = null
    private val listeners: MutableList<WeakReference<(Event<T>) -> Unit>> = mutableListOf()

    override fun addListener(listener: (Event<T>) -> Unit) {
        listeners.add(WeakReference(listener))
        lastEvent?.let(listener)
    }

    override fun removeListener(listener: (Event<T>) -> Unit) {
        listeners.removeAll {
            it.get() == null || it.get() == listener
        }
    }

    override fun sendEvent(event: Event<T>) {
        listeners.forEach {
            it.get()?.invoke(event)
        }
        lastEvent = event
    }
}