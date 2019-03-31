package com.github.ianellis.shifts.domain.events

import java.lang.ref.WeakReference

class SimpleEventEmitter<T> : EventEmitter<T> {

    private val listeners: MutableList<WeakReference<(Event<T>) -> Unit>> = mutableListOf()

    override fun addListener(listener: (Event<T>) -> Unit) {
        listeners.add(WeakReference(listener))
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
    }
}