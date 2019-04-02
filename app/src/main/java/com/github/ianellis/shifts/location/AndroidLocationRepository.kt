package com.github.ianellis.shifts.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import com.github.ianellis.shifts.domain.location.LatLng
import com.github.ianellis.shifts.domain.location.LocationRespoitory
import com.github.ianellis.shifts.domain.location.LocationUnavailableException
import com.github.ianellis.shifts.domain.location.PermissionRequiredException
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AndroidLocationRepository(
    private val context: Context,
    private val locationProvider: FusedLocationProviderClient
) : LocationRespoitory {

    companion object {
        private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
    }

    override fun getLocationAsync(): Deferred<LatLng> {

        return GlobalScope.async {
            if (permissionGranted()) {
                withContext(Dispatchers.Main) {
                    getLocationFromManagerAsync()
                }
            } else {
                throw PermissionRequiredException(LOCATION_PERMISSION)
            }
        }
    }

    private fun permissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun getLocationFromManagerAsync(): LatLng {
        return suspendCoroutine { cont ->
            locationProvider.lastLocation.addOnCompleteListener { taskResult ->
                if (taskResult.isSuccessful && taskResult.result != null) {
                    val location = taskResult.result
                    cont.resume(LatLng(location.latitude, location.longitude))
                } else {
                    cont.resumeWithException(LocationUnavailableException())
                }

            }
        }
    }
}
