package app.car.raceboxparser

import android.location.Location
import android.os.Build
import android.os.SystemClock
import app.car.raceboxparser.Packet.FixStatus.Fix2D
import app.car.raceboxparser.Packet.FixStatus.Fix3D
import app.car.raceboxparser.Packet.FixStatus.NoFix
import java.util.Calendar
import java.util.Date

fun Packet.location(tag: String): Location {
    return Location(tag).also {
        it.latitude = latitude
        it.longitude = longitude
        it.time = System.currentTimeMillis()
        it.altitude = wgsAltitude
        it.speed = speed
        it.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            it.speedAccuracyMetersPerSecond = speedAccuracy
            it.verticalAccuracyMeters = vAccuracy
        }
    }
}

data class Packet(
    val iTOW: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val validityFlags: Int,
    val timeAccuracy: Int,
    val nanoseconds: Int,
    val fixStatusValue: Int,
    val fixStatusFlags: Int,
    val dateTimeFlags: Int,
    val numberOfSvs: Int,
    val longitude: Double,
    val latitude: Double,
    val wgsAltitude: Double,
    val mslAltitude: Double,
    val hAccuracy: Float,
    val vAccuracy: Float,
    val speed: Float,
    val heading: Double,
    val speedAccuracy: Float,
    val headingAccuracy: Double,
    val pdop: Double,
    val latLonFlags: Int,
    val batteryStatus: Int,
    val gForceX: Float,
    val gForceY: Float,
    val gForceZ: Float,
    val rotationRateX: Float,
    val rotationRateY: Float,
    val rotationRateZ: Float
) {

    // datetime

    val date: Date
        get() = Calendar.getInstance().apply { set(year, month, day, hour, minute, second) }.time

    // validity flags

    val dateValid: Boolean
        get() = validityFlags.bitOf(0) == 1

    val tameValid: Boolean
        get() = validityFlags.bitOf(1) == 1

    val fullyResolved: Boolean
        get() = validityFlags.bitOf(2) == 1

    val magneticDeclinationValid: Boolean
        get() = validityFlags.bitOf(3) == 1

    // fix status

    val fixStatus: FixStatus
        get() = when (fixStatusValue) {
            0 -> NoFix
            2 -> Fix2D
            3 -> Fix3D
            else -> NoFix
        }

    val fixValid: Boolean
        get() = fixStatusFlags.bitOf(0) == 1

    val differentialCorrectionsApplied: Boolean
        get() = fixStatusFlags.bitOf(1) == 1

    val headingValid: Boolean
        get() = fixStatusFlags.bitOf(5) == 1

    // dateTime flags

    val dateTimeConfirmationValid: Boolean
        get() = dateTimeFlags.bitOf(5) == 1

    val utcDateValid: Boolean
        get() = dateTimeFlags.bitOf(6) == 1

    val utcTimeValid: Boolean
        get() = dateTimeFlags.bitOf(7) == 1

    // coordinates

    val coordinatesValid: Boolean
        get() = latLonFlags.bitOf(0) == 0

    // battery

    val isCharging: Boolean
        get() = batteryStatus.and(0x80) == 1

    private fun Int.bitOf(position: Int): Int = (this shr position) and 1

    enum class FixStatus {
        NoFix,
        Fix2D,
        Fix3D
    }
}
