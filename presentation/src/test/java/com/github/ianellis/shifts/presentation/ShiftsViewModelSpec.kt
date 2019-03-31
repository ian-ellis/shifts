package com.github.ianellis.shifts.presentation

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.Observer
import android.arch.lifecycle.callClear
import com.github.ianellis.shifts.domain.Shift
import com.github.ianellis.shifts.domain.ShiftState
import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.events.Ignore
import com.github.ianellis.shifts.domain.location.PermissionRequiredException
import com.github.ianellis.shifts.presentation.helpers.mockFunction
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.URL
import java.util.*

class ShiftsViewModelSpec {

    @Rule
    fun rule() = InstantTaskExecutorRule()

    private lateinit var lifecycle: LifecycleRegistry
    private lateinit var owner: LifecycleOwner


    private lateinit var shiftsListenerSlot: CapturingSlot<(Event<List<Shift>>) -> Unit>
    private lateinit var ignore: Ignore
    private lateinit var listenForShifts: ((Event<List<Shift>>) -> Unit) -> Ignore
    private lateinit var loadShifts: () -> Unit
    private lateinit var startShift: () -> Deferred<Unit>
    private lateinit var endShift: () -> Deferred<Unit>
    private lateinit var viewModel: ShiftsViewModel

    @Before
    fun setup() {
        owner = LifecycleOwner { lifecycle }
        lifecycle = LifecycleRegistry(owner)
        lifecycle.markState(Lifecycle.State.RESUMED)
        loadShifts = mockFunction()
        shiftsListenerSlot = slot()
        ignore = mockFunction()
        listenForShifts = mockk()
        every { listenForShifts(capture(shiftsListenerSlot)) } returns ignore
        startShift = mockk()
        endShift = mockk()

        viewModel = ShiftsViewModel(listenForShifts, loadShifts, startShift, endShift, Dispatchers.Unconfined)
    }

    @Test
    fun `ShiftsViewModel loads shifts on init`() {
        verify { loadShifts() }
    }

    @Test
    fun `ShiftsViewModel listens for shifts, displays results`() {
        //given we are listening for shifts
        verify { listenForShifts(any()) }

        //then we should initially have an empty list
        viewModel.shifts.value shouldEqual emptyList()

        //and we should be loading
        viewModel.loading.value shouldBe true

        //when we receive results
        val shifts: List<Shift> = listOf(shift(), shift())
        shiftsListenerSlot.captured(Event(shifts))

        //then we display the results
        viewModel.shifts.value shouldEqual shifts

        //and stop loading
        viewModel.loading.value shouldBe false
    }

    @Test
    fun `ShiftsViewModel listens for shifts, displays error`() {
        //given we are listening for shifts
        verify { listenForShifts(any()) }

        //and ar listening for errors
        val errorListener: (Throwable) -> Unit = mockFunction<Throwable>()
        viewModel.showGenericError.observe(owner, Observer {
            it?.let(errorListener)
        })

        //then we should be loading
        viewModel.loading.value shouldBe true

        //when we receive results
        val error = RuntimeException("OOPS")
        shiftsListenerSlot.captured(Event(error))

        //then we receive the error event
        verify { errorListener(error) }

        //and stop loading
        viewModel.loading.value shouldBe false
    }

    @Test
    fun `ShiftsViewModel sets state to started if last value in the list is started`() {
        //given we are listening for shifts
        verify { listenForShifts(any()) }

        //when we receive results
        val shifts: List<Shift> = listOf(shift(), shift(1, ShiftState.Started(Date())))
        shiftsListenerSlot.captured(Event(shifts))

        //then we display the results
        viewModel.shiftState.value shouldEqual CurrentShiftState.STARTED
    }

    private fun shift(
        id: Int = 1,
        state: ShiftState = ShiftState.Complete(Date(), Date()),
        url: String = "http://google.com/someimage.jpg"
    ): Shift {
        return Shift(id, state, URL(url))
    }

    @Test
    fun `startShift() - starts shift, updates state`() {
        //give starting a shift is successful
        val startResult: CompletableDeferred<Unit> = CompletableDeferred()
        every { startShift() } returns startResult

        //when we start a shift
        viewModel.startShift(null)

        //the the state moves to starting
        viewModel.shiftState.value shouldEqual CurrentShiftState.STARTING

        //when the request completes
        startResult.complete(Unit)

        //the the state moves to started
        viewModel.shiftState.value shouldEqual CurrentShiftState.STARTED
    }

    @Test
    fun `startShift() - when start fails with Permission exception we request permission from user`() {
        //give starting a shift will fail
        val startResult: CompletableDeferred<Unit> = CompletableDeferred()
        every { startShift() } returns startResult

        //and we are listening for permission errors
        val permissionListener: (String) -> Unit = mockFunction<String>()
        viewModel.requestPermission.observe(owner, Observer { it?.let(permissionListener) })

        //when we start a shift
        viewModel.startShift(null)

        //the the state moves to starting
        viewModel.shiftState.value shouldEqual CurrentShiftState.STARTING

        //when the request errors
        val permissionName = "location"
        startResult.completeExceptionally(PermissionRequiredException(permissionName))

        //the the state moves to the previous state
        viewModel.shiftState.value shouldEqual CurrentShiftState.OFF

        //and we receive the event to request permission
        verify { permissionListener(permissionName) }
    }

    @Test
    fun `startShift() - when start fails with unknown exception we show a generic error to the user`() {
        //give starting a shift will fail
        val startResult: CompletableDeferred<Unit> = CompletableDeferred()
        every { startShift() } returns startResult

        //and we are listening for permission errors
        val errorListener: (Throwable) -> Unit = mockFunction<Throwable>()
        viewModel.showGenericError.observe(owner, Observer { it?.let(errorListener) })

        //when we start a shift
        viewModel.startShift(null)

        //the the state moves to starting
        viewModel.shiftState.value shouldEqual CurrentShiftState.STARTING

        //when the request errors
        val error = RuntimeException("oops")
        startResult.completeExceptionally(error)

        //the the state moves to the previous state
        viewModel.shiftState.value shouldEqual CurrentShiftState.OFF

        //and we receive the event to request permission
        verify { errorListener(error) }
    }

    @Test
    fun `startShift() - ignores call if state not OFF`() {
        viewModel.shiftState.value = CurrentShiftState.STARTED
        viewModel.startShift()
        verify(exactly = 0) { startShift() }
    }

    @Test
    fun `startShift() - ignores call if already starting shift`() {
        every { startShift() } returns CompletableDeferred()

        //when we call startShift
        viewModel.startShift()

        //we make the request
        verify(exactly = 1) { startShift() }

        //when we call again (before the first call finished)
        viewModel.startShift()

        //then we do not make another request
        verify(exactly = 1) { startShift() }
    }

    @Test
    fun `startShift() - ignores call if ending starting shift`() {
        every { startShift() } returns CompletableDeferred()
        every { endShift() } returns CompletableDeferred()
        //given we have started
        viewModel.shiftState.value = CurrentShiftState.STARTED

        //when we call endShift
        viewModel.endShift()

        //we make an end request
        verify(exactly = 1) { endShift() }

        //when we start before end finishes
        viewModel.startShift()

        //then we do not make the request
        verify(exactly = 0) { startShift() }
    }

    @Test
    fun `endShift() - ends shift, updates state`() {
        //given we have ended a shift
        viewModel.shiftState.value = CurrentShiftState.STARTED

        //give end a shift is successful
        val endResult: CompletableDeferred<Unit> = CompletableDeferred()
        every { endShift() } returns endResult

        //when we start a shift
        viewModel.endShift(null)

        //the the state moves to starting
        viewModel.shiftState.value shouldEqual CurrentShiftState.ENDING

        //when the request completes
        endResult.complete(Unit)

        //the the state moves to off
        viewModel.shiftState.value shouldEqual CurrentShiftState.OFF
    }

    @Test
    fun `endShift() - when end fails with Permission exception we request permission from user`() {
        //give we have started a shift
        viewModel.shiftState.value = CurrentShiftState.STARTED

        //andending a shift will fail
        val endResult: CompletableDeferred<Unit> = CompletableDeferred()
        every { endShift() } returns endResult

        //and we are listening for permission errors
        val permissionListener: (String) -> Unit = mockFunction<String>()
        viewModel.requestPermission.observe(owner, Observer { it?.let(permissionListener) })

        //when we end a shift
        viewModel.endShift(null)

        //the the state moves to starting
        viewModel.shiftState.value shouldEqual CurrentShiftState.ENDING

        //when the request errors
        val permissionName = "location"
        endResult.completeExceptionally(PermissionRequiredException(permissionName))

        //the the state moves to the previous state
        viewModel.shiftState.value shouldEqual CurrentShiftState.STARTED

        //and we receive the event to request permission
        verify { permissionListener(permissionName) }
    }

    @Test
    fun `endShift() - when end fails with unknown exception we show a generic error to the user`() {
        //give we have started a shift
        viewModel.shiftState.value = CurrentShiftState.STARTED

        //and ending will fail
        val endResult: CompletableDeferred<Unit> = CompletableDeferred()
        every { endShift() } returns endResult

        //and we are listening for permission errors
        val errorListener: (Throwable) -> Unit = mockFunction<Throwable>()
        viewModel.showGenericError.observe(owner, Observer { it?.let(errorListener) })

        //when we end a shift
        viewModel.endShift(null)

        //the the state moves to ending
        viewModel.shiftState.value shouldEqual CurrentShiftState.ENDING

        //when the request errors
        val error = RuntimeException("oops")
        endResult.completeExceptionally(error)

        //the the state moves to the previous state
        viewModel.shiftState.value shouldEqual CurrentShiftState.STARTED

        //and we receive the event to request permission
        verify { errorListener(error) }
    }

    @Test
    fun `endShift() - ignores call if state not STARTED`() {
        viewModel.shiftState.value = CurrentShiftState.STARTING
        viewModel.endShift()
        verify(exactly = 0) { endShift() }
    }

    @Test
    fun `endShift() - ignores call if already ending shift`() {
        every { endShift() } returns CompletableDeferred()

        //give we have started a shift
        viewModel.shiftState.value = CurrentShiftState.STARTED


        //when we call endShift
        viewModel.endShift()

        //we make the request
        verify(exactly = 1) { endShift() }

        //when we call again (before the first call finished)
        viewModel.endShift()

        //then we do not make another request
        verify(exactly = 1) { endShift() }
    }

    @Test
    fun `endShift() - ignores call if starting shift`() {
        every { startShift() } returns CompletableDeferred()
        every { endShift() } returns CompletableDeferred()

        //when we call startShift
        viewModel.startShift()

        //we make a start request
        verify(exactly = 1) { startShift() }

        //when we end before start finishes
        viewModel.endShift()

        //then we do not make the request
        verify(exactly = 0) { endShift() }
    }

    @Test
    fun `onClear() - ignores updated shifts`() {
        //when the view model is cleared
        viewModel.callClear()
        //then we stop listening for shifts
        verify { ignore() }
    }
}