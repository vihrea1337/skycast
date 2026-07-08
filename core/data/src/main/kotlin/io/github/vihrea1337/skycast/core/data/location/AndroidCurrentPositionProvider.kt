package io.github.vihrea1337.skycast.core.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

internal class AndroidCurrentPositionProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : CurrentPositionProvider {

    @SuppressLint("MissingPermission") // разрешение проверяет UI до вызова
    override suspend fun currentPosition(): CurrentPositionProvider.Position? {
        val fused = LocationServices.getFusedLocationProviderClient(context)
        // getCurrentLocation, а не lastLocation: последняя может быть null или протухшей
        val location = fused.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token,
        ).await()
        return location?.let { CurrentPositionProvider.Position(it.latitude, it.longitude) }
    }

    override suspend fun localityName(position: CurrentPositionProvider.Position): String? {
        @Suppress("DEPRECATION") // синхронный Geocoder допустим на IO-диспетчере; async API только с SDK 33
        return Geocoder(context, Locale.getDefault())
            .getFromLocation(position.latitude, position.longitude, 1)
            ?.firstOrNull()
            ?.locality
    }
}
