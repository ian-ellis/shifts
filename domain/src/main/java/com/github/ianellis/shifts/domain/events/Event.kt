package com.github.ianellis.shifts.domain.events

class Event<T> {

    val value: T?
    val error: Throwable?

    constructor(value: T) {
        this.value = value
        this.error = null
    }

    constructor(error: Throwable) {
        this.value = null
        this.error = error
    }

    constructor(value: T, error: Throwable?) {
        this.value = value
        this.error = error
    }

    override fun toString(): String {
        return "Event{$value / $error}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event<*>

        if (value != other.value) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }

}
