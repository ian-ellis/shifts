package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.events.map
import com.github.ianellis.shifts.domain.time.Clock
import com.github.ianellis.shifts.entities.ShiftEntity
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ListenForShiftUpdatesCommand constructor(
    private val shiftRepository: ShiftRepository,
    private val clock: Clock
) : () -> ReceiveChannel<Event<List<Shift>>> {

    companion object {
        private const val ISO8601Format = "yyyy-MM-dd'T'HH:mm:ssZ"
    }

    override fun invoke(): ReceiveChannel<Event<List<Shift>>> {
        return shiftRepository.broadcastChannel.openSubscription().map { event ->
            event.map { entityList ->
                entityList.map { entity -> entity.toShift() }
            }
        }

    }

    private fun ShiftEntity.toShift(): Shift {
        return Shift(
            id = this.id,
            state = this.state(),
            imageUrl = URL(this.image)
        )
    }

    private fun ShiftEntity.state(): ShiftState {
        return if (this.end.isEmpty()) {
            ShiftState.Started(this.start.parse())
        } else {
            ShiftState.Complete(this.start.parse(), this.end.parse())
        }
    }

    private fun String.parse(): Date {
        val format = SimpleDateFormat(ISO8601Format, Locale.getDefault())
        format.timeZone = clock.timezone()
        return format.parse(this.makeAndroidDateTimeCompliant())
    }

    private fun String.makeAndroidDateTimeCompliant(): String {
        return this.replaceFirst(Regex("(\\d\\d):(\\d\\d)$"), "$1$2")
    }
}