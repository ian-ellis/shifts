package com.github.ianellis.shifts.presentation

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.view.View
import com.github.ianellis.shifts.domain.Shift
import com.github.ianellis.shifts.domain.ShiftState
import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.events.Ignore
import com.github.ianellis.shifts.domain.location.PermissionRequiredException
import com.github.ianellis.shifts.presentation.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ShiftsViewModel constructor(
    listenForShifts: ((Event<List<Shift>>) -> Unit) -> Ignore,
    private val loadShifts: () -> Unit,
    private val startShift: () -> Deferred<Unit>,
    private val endShift: () -> Deferred<Unit>,
    override val coroutineContext: CoroutineContext
) : ViewModel(), CoroutineScope  {

    val shifts: MutableLiveData<List<Shift>> = MutableLiveData()
    val shiftState: MutableLiveData<CurrentShiftState> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    val requestPermission: SingleLiveEvent<String> = SingleLiveEvent()
    val showGenericError: SingleLiveEvent<Throwable> = SingleLiveEvent()

    var onPermissionGranted: (() -> Unit)? = null

    private var shiftUpdateJob: Job? = null

    init {
        loadShifts()
    }

    private val onShiftsChanged: (Event<List<Shift>>) -> Unit = { event ->
        launch {
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

    private val ignoreShifts: Ignore

    init {
        shifts.value = emptyList()
        shiftState.value = CurrentShiftState.OFF
        ignoreShifts = listenForShifts(onShiftsChanged)
        loading.value = true
    }

    fun startShift(v: View? = null) {
        if (shiftUpdateJob?.isActive != true && shiftState.value == CurrentShiftState.OFF) {
            shiftUpdateJob = async {
                val previousState = shiftState.value
                try {
                    shiftState.value = CurrentShiftState.STARTING
                    startShift.invoke().await()
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

    fun endShift(v: View? = null) {
        if (shiftUpdateJob?.isActive != true && shiftState.value == CurrentShiftState.STARTED) {
            shiftUpdateJob = async {
                val previousState = shiftState.value
                try {
                    shiftState.value = CurrentShiftState.ENDING
                    endShift.invoke().await()
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
        ignoreShifts()
        shiftUpdateJob?.cancel()
        super.onCleared()
    }
}