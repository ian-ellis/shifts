package com.github.ianellis.shifts.domain

import com.github.ianellis.shifts.domain.location.LatLng
import com.github.ianellis.shifts.domain.location.LocationRespoitory
import com.github.ianellis.shifts.domain.location.LocationUnavailableException
import com.github.ianellis.shifts.domain.time.Clock
import com.github.ianellis.shifts.entities.ISO8601
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.*

class EndShiftCommand(
    private val locationRepository: LocationRespoitory,
    private val shiftRepository: ShiftRepository,
    private val clock: Clock,
    private val backgroundDispatcher: CoroutineDispatcher
) : () -> Deferred<Unit> {

    companion object {
        private const val ISO8601Format = "yyyy-MM-dd'T'HH:mm:ssZ"
        private val FALLBACK_LOCATION = LatLng(0.0, 0.0)
    }

    override fun invoke(): Deferred<Unit> {
        return GlobalScope.async(backgroundDispatcher) {
            val location = try {
                locationRepository.getLocationAsync().await()
            } catch (e: LocationUnavailableException) {
                FALLBACK_LOCATION
            }
            val date = clock.now().toTime()
            shiftRepository.endShiftAsync(date, location.first.toString(), location.second.toString()).await()
        }
    }

    private fun Date.toTime(): ISO8601 {
        val format = SimpleDateFormat(ISO8601Format, Locale.getDefault())
        format.timeZone = clock.timezone()
        return format.format(this).makeIso8601Compliant()
    }

    private fun String.makeIso8601Compliant(): String {
        val start = this.substring(0, this.length - 2)
        val end = this.substring(this.length - 2)
        return "$start:$end"
    }
}