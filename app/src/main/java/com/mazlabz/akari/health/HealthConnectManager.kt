package com.mazlabz.akari.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class HealthSnapshot(
    val latestHr: Int? = null,
    val restingHr: Int? = null,
    val steps: Long? = null,
    val sleepMinutes: Long? = null,
    val error: String? = null
)

class HealthConnectManager(private val context: Context) {

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    fun availability(): Int = try {
        HealthConnectClient.getSdkStatus(context)
    } catch (t: Throwable) {
        HealthConnectClient.SDK_UNAVAILABLE
    }

    val isAvailable: Boolean get() = availability() == HealthConnectClient.SDK_AVAILABLE

    private val client: HealthConnectClient? by lazy {
        if (isAvailable) HealthConnectClient.getOrCreate(context) else null
    }

    suspend fun hasAllPermissions(): Boolean {
        val c = client ?: return false
        return try {
            c.permissionController.getGrantedPermissions().containsAll(permissions)
        } catch (t: Throwable) {
            false
        }
    }

    /** Best-effort read of today's wearable data. Never throws. */
    suspend fun todaySnapshot(): HealthSnapshot {
        val c = client ?: return HealthSnapshot(error = "Health Connect not available")
        return try {
            val zone = ZoneId.systemDefault()
            val startOfDay = LocalDate.now().atStartOfDay(zone).toInstant()
            val now = Instant.now()
            val dayFilter = TimeRangeFilter.between(startOfDay, now)

            val hrRecords = c.readRecords(
                ReadRecordsRequest(HeartRateRecord::class, timeRangeFilter = dayFilter)
            ).records
            val latestHr = hrRecords
                .flatMap { it.samples }
                .maxByOrNull { it.time }
                ?.beatsPerMinute?.toInt()

            val restingHr = c.readRecords(
                ReadRecordsRequest(RestingHeartRateRecord::class, timeRangeFilter = dayFilter)
            ).records.maxByOrNull { it.time }?.beatsPerMinute?.toInt()

            val steps = c.readRecords(
                ReadRecordsRequest(StepsRecord::class, timeRangeFilter = dayFilter)
            ).records.sumOf { it.count }

            // Last night: sessions overlapping the previous 24h
            val sleepFilter = TimeRangeFilter.between(now.minus(Duration.ofHours(24)), now)
            val sleepMinutes = c.readRecords(
                ReadRecordsRequest(SleepSessionRecord::class, timeRangeFilter = sleepFilter)
            ).records.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }

            HealthSnapshot(
                latestHr = latestHr,
                restingHr = restingHr,
                steps = if (steps > 0) steps else null,
                sleepMinutes = if (sleepMinutes > 0) sleepMinutes else null
            )
        } catch (t: Throwable) {
            HealthSnapshot(error = t.message ?: "Couldn't read Health Connect")
        }
    }
}
