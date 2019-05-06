package com.github.ianellis.shifts.presentation

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.view.View
import com.github.ianellis.shifts.domain.Action
import com.github.ianellis.shifts.domain.Shift
import com.github.ianellis.shifts.domain.ShiftState
import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.location.PermissionRequiredException
import com.github.ianellis.shifts.presentation.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ShiftsViewModel constructor(
    shiftsUpdated: ReceiveChannel<Event<List<Shift>>>,
    private val loadShifts: Action,
    private val startShift: Action,
    private val endShift: Action,
    override val coroutineContext: CoroutineContext
) : ViewModel(), CoroutineScope {

    val shifts: MutableLiveData<List<Shift>> = MutableLiveData()
    val shiftState: MutableLiveData<CurrentShiftState> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    val requestPermission: SingleLiveEvent<String> = SingleLiveEvent()
    val showGenericError: SingleLiveEvent<Throwable> = SingleLiveEvent()

    var onPermissionGranted: (() -> Unit)? = null

    private var shiftUpdateJob: Job? = null

    init {
        shifts.value = emptyList()
        shiftState.value = CurrentShiftState.OFF

        launch {
            loadShifts()
        }

        launch {
            shiftsUpdated.consumeEach { event ->

                event.value?.let {
                    shifts.value = it
                    if (shifts.value?.lastOrNull()?.state is ShiftState.Started) {
                        shiftState.value = CurrentShiftState.STARTED
                    }
                }

                event.error?.let {
                    showGenericError.value = event.error
                }

                loading.value = false
            }
        }

        loading.value = true
    }

    fun startShift() {
        if (shiftUpdateJob?.isActive != true && shiftState.value == CurrentShiftState.OFF) {
            val previousState = shiftState.value
            shiftState.value = CurrentShiftState.STARTING

            shiftUpdateJob = async {
                try {
                    startShift.invoke()
                    shiftState.value = CurrentShiftState.STARTED
                } catch (e: Exception) {
                    when (e) {
                        is PermissionRequiredException -> {
                            onPermissionGranted = { startShift() }
                            requestPermission.value = e.permission
                        }
                        else -> showGenericError.value = e
                    }
                    shiftState.value = previousState
                }
            }
        }
    }

    fun endShift() {
        if (shiftUpdateJob?.isActive != true && shiftState.value == CurrentShiftState.STARTED) {
            val previousState = shiftState.value
            shiftState.value = CurrentShiftState.ENDING
            shiftUpdateJob = async {
                try {
                    endShift.invoke()
                    shiftState.value = CurrentShiftState.OFF
                } catch (e: Exception) {
                    when (e) {
                        is PermissionRequiredException -> {
                            onPermissionGranted = { endShift() }
                            requestPermission.value = e.permission
                        }
                        else -> showGenericError.value = e
                    }
                    shiftState.value = previousState
                }
            }
        }
    }

    override fun onCleared() {
        shiftUpdateJob?.cancel()
        cancel()
        super.onCleared()
    }
}