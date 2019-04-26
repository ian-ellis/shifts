package com.github.ianellis.shifts.presentation

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.Observer
import android.arch.lifecycle.callClear
import com.github.ianellis.shifts.domain.Action
import com.github.ianellis.shifts.domain.Shift
import com.github.ianellis.shifts.domain.ShiftState
import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.location.PermissionRequiredException
import com.github.ianellis.shifts.presentation.helpers.mockFunction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineContext
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

class ShiftsViewModelSpec {

    @Rule
    fun rule() = InstantTaskExecutorRule()

    private lateinit var lifecycle: LifecycleRegistry
    private lateinit var owner: LifecycleOwner


    private lateinit var testContext: TestCoroutineContext
    private lateinit var listenForShiftsBroadcast: ConflatedBroadcastChannel<Event<List<Shift>>>
    private lateinit var listenForShiftsReceive: ReceiveChannel<Event<List<Shift>>>
    private lateinit var loadShifts: Action
    private lateinit var startShift: Action
    private lateinit var endShift: Action
    private lateinit var viewModel: ShiftsViewModel

    @Before
    fun setup() {
        owner = LifecycleOwner { lifecycle }
        lifecycle = LifecycleRegistry(owner)
        lifecycle.markState(Lifecycle.State.RESUMED)
        loadShifts = mockk()
        listenForShiftsBroadcast = ConflatedBroadcastChannel()
        listenForShiftsReceive = listenForShiftsBroadcast.openSubscription()
        startShift = mockk()
        endShift = mockk()
        testContext = TestCoroutineContext()
        viewModel = ShiftsViewModel(listenForShiftsReceive, loadShifts, startShift, endShift, testContext)
    }

    @After
    fun teardown(){
        testContext.cancelAllActions()
    }

    @Test
    fun `ShiftsViewModel loads shifts on init`() {
        testContext.triggerActions()
        coVerify { loadShifts() }
    }

    @Test
    fun `ShiftsViewModel listens for shifts, displays results`() {

        //when we receive results
        val shifts: List<Shift> = listOf(shift(), shift())
        runBlocking {
            listenForShiftsBroadcast.send(Event(shifts))
            testContext.triggerActions()
        }

        //then we display the results
        viewModel.shifts.value shouldEqual shifts

        //and stop loading
        viewModel.loading.value shouldBe false
    }

    @Test
    fun `ShiftsViewModel listens for shifts, displays error`() {


        //given we are listening for errors
        val errorListener: (Throwable) -> Unit = mockFunction<Throwable>()
        viewModel.showGenericError.observe(owner, Observer {
            it?.let(errorListener)
        })

        //then we should be loading
        viewModel.loading.value shouldBe true

        //when we receive results
        val error = RuntimeException("OOPS")
        runBlocking {
            listenForShiftsBroadcast.send(Event(error))
            testContext.triggerActions()
        }

        //then we receive the error event
        verify { errorListener(error) }

        //and stop loading
        viewModel.loading.value shouldBe false
    }

    @Test
    fun `ShiftsViewModel sets state to started if last value in the list is started`() {

        //when we receive results
        val shifts: List<Shift> = listOf(shift(), shift(1, ShiftState.Started(Date())))
        runBlocking {
            listenForShiftsBroadcast.send(Event(shifts))
            testContext.triggerActions()
        }

        //then we display the results
        viewModel.shiftState.value shouldEqual CurrentShiftState.STARTED
    }

    //
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
        coEvery { startShift() } returns Unit

        runBlocking {

            //when we start a shift
            viewModel.startShift(null)

            //the the state moves to starting
            viewModel.shiftState.value shouldEqual CurrentShiftState.STARTING

            //when the request completes
            testContext.triggerActions()

            //the the state moves to started
            viewModel.shiftState.value shouldEqual CurrentShiftState.STARTED
        }
    }


    @Test
    fun `startShift() - when start fails with Permission exception we request permission from user`() {
        //give starting a shift will fail
        val permissionName = "location"

        coEvery { startShift() } throws PermissionRequiredException(permissionName)

        //and we are listening for permission errors
        val permissionListener: (String) -> Unit = mockFunction<String>()
        viewModel.requestPermission.observe(owner, Observer { it?.let(permissionListener) })

        //when we start a shift
        viewModel.startShift(null)

        //the the state moves to starting
        viewModel.shiftState.value shouldEqual CurrentShiftState.STARTING

        //when the request completes
        testContext.triggerActions()


        //the the state moves to the previous state
        viewModel.shiftState.value shouldEqual CurrentShiftState.OFF

        //and we receive the event to request permission
        verify { permissionListener(permissionName) }
    }

    @Test
    fun `startShift() - when start fails with unknown exception we show a generic error to the user`() {
        //give starting a shift will fail
        val error = RuntimeException("oops")
        coEvery { startShift() } throws error

        //and we are listening for permission errors
        val errorListener: (Throwable) -> Unit = mockFunction<Throwable>()
        viewModel.showGenericError.observe(owner, Observer { it?.let(errorListener) })

        //when we start a shift
        viewModel.startShift(null)

        //the the state moves to starting
        viewModel.shiftState.value shouldEqual CurrentShiftState.STARTING

        //when the request errors
        testContext.triggerActions()

        //the the state moves to the previous state
        viewModel.shiftState.value shouldEqual CurrentShiftState.OFF

        //and we receive the event to request permission
        verify { errorListener(error) }
    }

    @Test
    fun `startShift() - ignores call if state not OFF`() {
        viewModel.shiftState.value = CurrentShiftState.STARTED
        viewModel.startShift()
        coVerify(exactly = 0) { startShift() }
    }

    @Test
    fun `startShift() - ignores call if already starting shift`() {
        coEvery { startShift() } returns Unit

        //when we call startShift
        viewModel.startShift()
        testContext.triggerActions()

        //we make the request
        coVerify(exactly = 1) { startShift() }

        //when we call again (before the first call finished)
        viewModel.startShift()
        testContext.advanceTimeBy(1, TimeUnit.MILLISECONDS)

        //then we do not make another request
        coVerify(exactly = 1) { startShift() }
    }

    @Test
    fun `startShift() - ignores call if ending starting shift`() {
        //given ending a shift will take a while
        coEvery { startShift() } returns Unit
        coEvery { endShift() } coAnswers { CompletableDeferred<Nothing>().await() }
        //given we have started
        viewModel.shiftState.value = CurrentShiftState.STARTED

        //when we call endShift
        viewModel.endShift()
        testContext.triggerActions()

        //we make an end request
        coVerify(exactly = 1) { endShift() }

        //when we start before end finishes
        viewModel.startShift()
        testContext.triggerActions()

        //then we do not make the request
        coVerify(exactly = 0) { startShift() }
    }

    @Test
    fun `endShift() - ends shift, updates state`() {
        //given we have ended a shift
        viewModel.shiftState.value = CurrentShiftState.STARTED

        //give end a shift is successful
        coEvery { endShift() } returns Unit

        //when we start a shift
        viewModel.endShift(null)

        //the the state moves to starting
        viewModel.shiftState.value shouldEqual CurrentShiftState.ENDING

        //when the request completes
        testContext.triggerActions()

        //the the state moves to off
        viewModel.shiftState.value shouldEqual CurrentShiftState.OFF
    }

    @Test
    fun `endShift() - when end fails with Permission exception we request permission from user`() {
        //give we have started a shift
        viewModel.shiftState.value = CurrentShiftState.STARTED

        //and ending a shift will fail
        val permissionName = "location"
        coEvery { endShift() } throws PermissionRequiredException(permissionName)

        //and we are listening for permission errors
        val permissionListener: (String) -> Unit = mockFunction<String>()
        viewModel.requestPermission.observe(owner, Observer { it?.let(permissionListener) })

        //when we end a shift
        viewModel.endShift(null)

        //the the state moves to starting
        viewModel.shiftState.value shouldEqual CurrentShiftState.ENDING

        //when the request errors
        testContext.triggerActions()

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
        val error = RuntimeException("oops")
        coEvery { endShift() } throws error

        //and we are listening for permission errors
        val errorListener: (Throwable) -> Unit = mockFunction<Throwable>()
        viewModel.showGenericError.observe(owner, Observer { it?.let(errorListener) })

        //when we end a shift
        viewModel.endShift(null)

        //the the state moves to ending
        viewModel.shiftState.value shouldEqual CurrentShiftState.ENDING

        //when the request errors
        testContext.triggerActions()


        //the the state moves to the previous state
        viewModel.shiftState.value shouldEqual CurrentShiftState.STARTED

        //and we receive the event to request permission
        verify { errorListener(error) }
    }

    @Test
    fun `endShift() - ignores call if state not STARTED`() {
        viewModel.shiftState.value = CurrentShiftState.STARTING
        viewModel.endShift()
        testContext.triggerActions()
        coVerify(exactly = 0) { endShift() }
    }

    @Test
    fun `endShift() - ignores call if already ending shift`() {
        coEvery { endShift() } returns Unit

        //give we have started a shift
        viewModel.shiftState.value = CurrentShiftState.STARTED

        //when we call endShift
        viewModel.endShift()
        testContext.triggerActions()

        //we make the request
        coVerify(exactly = 1) { endShift() }

        //when we call again (before the first call finished)
        viewModel.endShift()
        testContext.triggerActions()

        //then we do not make another request
        coVerify(exactly = 1) { endShift() }
    }

    @Test
    fun `endShift() - ignores call if starting shift`() {
        //given starting a shift will take a while
        coEvery { startShift() } coAnswers { CompletableDeferred<Nothing>().await() }
        coEvery { endShift() } returns Unit

        //when we call startShift
        viewModel.startShift()
        testContext.advanceTimeBy(1, TimeUnit.MILLISECONDS)

        //we make a start request
        coVerify(exactly = 1) { startShift() }

        //when we end before start finishes
        viewModel.endShift()
        testContext.triggerActions()

        //then we do not make the request
        coVerify(exactly = 0) { endShift() }
    }

    @Test
    fun `onClear() - ignores updated shifts`() {
        val job = Job()
        viewModel = ShiftsViewModel(listenForShiftsReceive, loadShifts, startShift, endShift, testContext + job)
        //when the view model is cleared
        viewModel.callClear()
        //then we stop listening for shifts
        job.isCancelled shouldBe true
    }
}