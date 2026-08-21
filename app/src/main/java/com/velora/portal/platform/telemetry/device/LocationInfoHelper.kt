package com.velora.portal.platform.telemetry.device

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.velora.portal.application.MainApplication
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object LocationInfoHelper {

    private val fusedCacheTimeout = 500.milliseconds
    private val freshLocationTimeout = 2.seconds
    private const val ADDRESS_TIMEOUT_MS = 1_000L
    private val totalTimeout = 3.seconds
    private val geocoderExecutor = Executors.newCachedThreadPool()

    private fun hasCoarsePermission() =
        ContextCompat.checkSelfPermission(
            MainApplication.appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun getLocationInfo(): Pair<Location?, Address?> {
        if (!hasCoarsePermission()) return null to null

        val startedAt = SystemClock.elapsedRealtime()
        val locationManager =
            MainApplication.appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = withTimeoutOrNull(totalTimeout) {
            getCachedLocation(locationManager) ?: getFreshLocation(locationManager)
        } ?: return null to null

        val elapsed = SystemClock.elapsedRealtime() - startedAt
        val remainingAddressTime = (totalTimeout.inWholeMilliseconds - elapsed)
            .coerceAtMost(ADDRESS_TIMEOUT_MS)
        val address = if (remainingAddressTime > 0L) {
            withTimeoutOrNull(remainingAddressTime.milliseconds) {
                getAddress(location.latitude, location.longitude)
            }
        } else {
            null
        }
        return location to address
    }

    private suspend fun getCachedLocation(locationManager: LocationManager): Location? =
        getManagerCache(locationManager) ?: getFusedCache()

    @SuppressLint("MissingPermission")
    private suspend fun getFusedCache(): Location? =
        withTimeoutOrNull(fusedCacheTimeout) {
            val client = LocationServices.getFusedLocationProviderClient(MainApplication.appContext)
            suspendCancellableCoroutine { continuation ->
                client.lastLocation
                    .addOnSuccessListener { location ->
                        if (continuation.isActive) continuation.resume(location)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
            }
        }

    @SuppressLint("MissingPermission")
    private suspend fun getFusedCurrentLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            val client = LocationServices.getFusedLocationProviderClient(MainApplication.appContext)
            client.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token,
            )
                .addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
                .addOnCanceledListener {
                    if (continuation.isActive) continuation.resume(null)
                }
            continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
        }

    @SuppressLint("MissingPermission")
    private fun getManagerCache(locationManager: LocationManager): Location? =
        listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull(Location::getTime)

    private fun coarseProviders(locationManager: LocationManager): List<String> {
        val enabled = runCatching { locationManager.getProviders(true) }.getOrDefault(emptyList())
        return listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .filter(enabled::contains)
    }

    @SuppressLint("MissingPermission")
    private suspend fun getFreshLocation(locationManager: LocationManager): Location? =
        withTimeoutOrNull(freshLocationTimeout) {
            coroutineScope {
                val requests = buildList<Deferred<Location?>> {
                    add(async { getFusedCurrentLocation() })
                    coarseProviders(locationManager).forEach { provider ->
                        add(async { requestProviderLocation(locationManager, provider) })
                    }
                }
                awaitFirstValidLocation(requests)
            }
        }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private suspend fun requestProviderLocation(
        locationManager: LocationManager,
        provider: String,
    ): Location? = suspendCancellableCoroutine { continuation ->
        val handler = Handler(Looper.getMainLooper())
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (continuation.isActive) continuation.resume(location)
                runCatching { locationManager.removeUpdates(this) }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) {
                if (continuation.isActive) continuation.resume(null)
                runCatching { locationManager.removeUpdates(this) }
            }
        }

        handler.post {
            if (!continuation.isActive) return@post
            runCatching {
                locationManager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper(),
                )
            }.onFailure {
                if (continuation.isActive) continuation.resume(null)
            }
        }
        continuation.invokeOnCancellation {
            runCatching { locationManager.removeUpdates(listener) }
        }
    }

    private suspend fun awaitFirstValidLocation(
        requests: List<Deferred<Location?>>,
    ): Location? {
        val pending = requests.toMutableList()
        return try {
            while (pending.isNotEmpty()) {
                val (completed, location) = select {
                    pending.forEach { request ->
                        request.onAwait { request to it }
                    }
                }
                pending.remove(completed)
                if (location != null) return location
            }
            null
        } finally {
            requests.forEach { it.cancel() }
        }
    }

    private suspend fun getAddress(latitude: Double, longitude: Double): Address? =
        suspendCancellableCoroutine { continuation ->
            val handler = Handler(Looper.getMainLooper())
            val timeout = Runnable {
                if (continuation.isActive) continuation.resume(null)
            }
            val complete: (Address?) -> Unit = { address ->
                handler.removeCallbacks(timeout)
                if (continuation.isActive) continuation.resume(address)
            }

            handler.postDelayed(timeout, ADDRESS_TIMEOUT_MS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                runCatching {
                    Geocoder(MainApplication.appContext, Locale.ENGLISH)
                        .getFromLocation(latitude, longitude, 1) {
                            complete(it.firstOrNull())
                        }
                }.onFailure { complete(null) }
            } else {
                geocoderExecutor.execute {
                    val address = runCatching {
                        @Suppress("DEPRECATION")
                        Geocoder(MainApplication.appContext, Locale.ENGLISH)
                            .getFromLocation(latitude, longitude, 1)
                            ?.firstOrNull()
                    }.getOrNull()
                    complete(address)
                }
            }
            continuation.invokeOnCancellation { handler.removeCallbacks(timeout) }
        }
}
