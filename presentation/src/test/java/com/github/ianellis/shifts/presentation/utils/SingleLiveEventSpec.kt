package com.github.ianellis.shifts.presentation.utils

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.Observer
import com.github.ianellis.shifts.presentation.helpers.mockFunction
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class SingleLiveEventSpec {

    @Rule
    fun rule() = InstantTaskExecutorRule()

    private lateinit var lifecycle: LifecycleRegistry
    private lateinit var owner: LifecycleOwner

    @Before
    fun setup() {
        owner = LifecycleOwner { lifecycle }
        lifecycle = LifecycleRegistry(owner)
        lifecycle.markState(Lifecycle.State.RESUMED)
    }

    @Test
    fun `sends event to only subscribers`() {
        //given a property, value and observer
        val value = "foo"

        val property = SingleLiveEvent<String>()

        val observer = mockFunction<String>()

        //when we observe to the empty property
        property.observe(owner, Observer { emittedValue ->
            emittedValue?.let(observer)
        })
        //and then set the value
        property.value = value

        //then our observer is invoked
        verify(exactly = 1) { observer.invoke(value) }
    }

    @Test
    fun `sends event to first subscribers`() {
        //given a property, value and 2 observers
        val value = "foo"

        val property = SingleLiveEvent<String>()

        val observer = mockFunction<String>()
        val observer2 = mockFunction<String>()

        //when we observer the empty property with both observers
        property.observe(owner, Observer { emittedValue ->
            emittedValue?.let(observer)
        })

        property.observe(owner, Observer { emittedValue ->
            emittedValue?.let(observer2)
        })
        //and then set the value
        property.value = value

        //only the first observer is called
        verify(exactly = 1) { observer.invoke(value) }
        verify(exactly = 0) { observer2.invoke(value) }
    }

    @Test
    fun `sends pending event on subscription to only subscriber`() {
        //given a property, value and an observer
        val value = "foo"

        val property = SingleLiveEvent<String>()

        val observer = mockFunction<String>()

        //when we initially set the value
        property.value = value

        //and then observe
        property.observe(owner, Observer { emittedValue ->
            emittedValue?.let(observer)
        })

        // our observer is notified
        verify(exactly = 1) { observer.invoke(value) }
    }

    @Test
    fun `sends pending event to first subscriber only`() {
        //given a property, value and 2 observers
        val value = "foo"

        val property = SingleLiveEvent<String>()

        val observer = mockFunction<String>()
        val observer2 = mockFunction<String>()

        //when we initially set the value
        property.value = value

        //and then observe twice
        property.observe(owner, Observer { emittedValue ->
            emittedValue?.let(observer)
        })

        property.observe(owner, Observer { emittedValue ->
            emittedValue?.let(observer2)
        })

        //only the first observer is notified
        verify(exactly = 1) { observer.invoke(value) }
        verify(exactly = 0) { observer2.invoke(value) }
    }

}