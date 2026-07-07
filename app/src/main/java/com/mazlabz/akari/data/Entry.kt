package com.mazlabz.akari.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single diary event. One flexible row type keeps the schema simple and
 * export-friendly; unused fields stay null.
 *
 * type: checkin | activity | rest | symptom | food | vitals | pem
 */
@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ts: Long = System.currentTimeMillis(),
    val type: String,
    val name: String? = null,     // activity or symptom name
    val cost: Int? = null,        // activity: % of battery
    val kind: String? = null,     // activity: physical | cognitive | emotional | mixed
    val sev: Int? = null,         // symptom: 1 mild, 2 moderate, 3 severe
    val text: String? = null,     // food / meds / notes
    val hr: Int? = null,          // vitals
    val bp: String? = null,       // vitals
    val spo2: Int? = null,        // vitals
    val battery: Int? = null,     // checkin: morning battery %
    val sleepQ: Int? = null       // checkin: 1 poor, 2 okay, 3 good
)
