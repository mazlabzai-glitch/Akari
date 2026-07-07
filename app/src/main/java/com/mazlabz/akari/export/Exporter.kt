package com.mazlabz.akari.export

import com.mazlabz.akari.data.Entry
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Exporter {

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    private fun sevName(sev: Int?) = when (sev) {
        1 -> "mild"; 2 -> "moderate"; 3 -> "severe"; else -> ""
    }
    private fun sleepName(q: Int?) = when (q) {
        1 -> "poor"; 2 -> "okay"; 3 -> "good"; else -> ""
    }

    fun toCsv(entries: List<Entry>): String {
        val sb = StringBuilder("date,time,type,detail,value\n")
        entries.sortedBy { it.ts }.forEach { e ->
            val zdt = Instant.ofEpochMilli(e.ts).atZone(ZoneId.systemDefault())
            val (detail, value) = when (e.type) {
                "checkin" -> "morning battery" to
                    "${e.battery}%" + (e.sleepQ?.let { " sleep:${sleepName(it)}" } ?: "")
                "activity" -> "${e.name} (${e.kind})" to "-${e.cost}%"
                "symptom" -> (e.name ?: "") to sevName(e.sev)
                "food" -> (e.text ?: "") to ""
                "vitals" -> "vitals" to listOfNotNull(
                    e.hr?.let { "HR $it" }, e.bp?.let { "BP $it" }, e.spo2?.let { "SpO2 $it" }
                ).joinToString(" ")
                "rest" -> "rest break" to ""
                "pem" -> "PEM / crash flagged" to ""
                else -> e.type to ""
            }
            fun q(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
            sb.append(
                listOf(
                    zdt.format(dateFmt), zdt.format(timeFmt), e.type, q(detail), q(value)
                ).joinToString(",")
            ).append("\n")
        }
        return sb.toString()
    }

    fun toJson(entries: List<Entry>): String {
        val arr = JSONArray()
        entries.forEach { e ->
            val o = JSONObject()
            o.put("ts", e.ts)
            o.put("type", e.type)
            e.name?.let { o.put("name", it) }
            e.cost?.let { o.put("cost", it) }
            e.kind?.let { o.put("kind", it) }
            e.sev?.let { o.put("sev", it) }
            e.text?.let { o.put("text", it) }
            e.hr?.let { o.put("hr", it) }
            e.bp?.let { o.put("bp", it) }
            e.spo2?.let { o.put("spo2", it) }
            e.battery?.let { o.put("battery", it) }
            e.sleepQ?.let { o.put("sleepQ", it) }
            arr.put(o)
        }
        return arr.toString(1)
    }

    fun fromJson(json: String): List<Entry> {
        val arr = JSONArray(json)
        val out = mutableListOf<Entry>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Entry(
                    ts = o.optLong("ts", System.currentTimeMillis()),
                    type = o.getString("type"),
                    name = o.optString("name").ifEmpty { null },
                    cost = if (o.has("cost")) o.getInt("cost") else null,
                    kind = o.optString("kind").ifEmpty { null },
                    sev = if (o.has("sev")) o.getInt("sev") else null,
                    text = o.optString("text").ifEmpty { null },
                    hr = if (o.has("hr")) o.getInt("hr") else null,
                    bp = o.optString("bp").ifEmpty { null },
                    spo2 = if (o.has("spo2")) o.getInt("spo2") else null,
                    battery = if (o.has("battery")) o.getInt("battery") else null,
                    sleepQ = if (o.has("sleepQ")) o.getInt("sleepQ") else null
                )
            )
        }
        return out
    }
}
