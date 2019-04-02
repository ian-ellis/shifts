package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.domain.events.Event
import com.github.ianellis.shifts.domain.events.Ignore
import com.github.ianellis.shifts.domain.events.map
import com.github.ianellis.shifts.domain.time.Clock
import com.github.ianellis.shifts.entities.ShiftEntity
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ListenForShiftUpdatesCommand constructor(
    private val shiftRepository: ShiftRepository,
    private val clock: Clock
) : ((Event<List<Shift>>) -> Unit) -> Ignore {

    companion object {
        private const val ISO8601Format = "yyyy-MM-dd'T'HH:mm:ssZ"
    }

    override fun invoke(listener: (Event<List<Shift>>) -> Unit): Ignore {
        val repoListener: (Event<List<ShiftEntity>>) -> Unit = { event ->
            val shifts = event.map { entityList ->
                entityList.map { entity -> entity.toShift() }
            }
            listener(shifts)
        }
        shiftRepository.addListener(repoListener)
        return { shiftRepository.removeListener(repoListener) }
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

    private fun String.makeAndroidDateTimeCompliant():String{
        return this.replaceFirst(Regex("(\\d\\d):(\\d\\d)$"), "$1$2")
    }
}