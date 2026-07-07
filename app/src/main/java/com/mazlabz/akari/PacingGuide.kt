package com.mazlabz.akari

/**
 * In-app medical grounding. Plain-language summaries of published guidance,
 * each with its source named. Kept short: brain fog is the reader.
 */
data class GuideTopic(val title: String, val body: String, val source: String)

object PacingGuide {

    // Zone boundaries as fraction of the day's energy
    const val REST_ZONE = 0.15f
    const val LOW_ZONE = 0.40f

    fun zoneLabel(level: Float): String = when {
        level > LOW_ZONE -> "Steady"
        level > REST_ZONE -> "Getting low"
        else -> "Rest zone"
    }

    fun guidance(level: Float): String = when {
        level > LOW_ZONE -> "Room for gentle activity — with rest between."
        level > REST_ZONE -> "One small thing at most, then a proper rest."
        else -> "The kindest move now is rest. Stop. Rest. Pace."
    }

    val ENVELOPE = GuideTopic(
        title = "The lantern is your energy envelope",
        body = "Pacing means staying inside your energy envelope — the energy your body " +
            "actually has today, not what the calendar demands. Studies of \"envelope " +
            "theory\" found that people who keep activity within their envelope have " +
            "fewer, milder crashes over time. Pacing is symptom-contingent: you set the " +
            "lantern by how you feel this morning, and on a bad day the right amount is " +
            "less. There are no targets to hit — the 2021 NICE guideline removed " +
            "exercise quotas (graded exercise therapy) from ME/CFS care.",
        source = "Envelope theory (Jason et al.); NICE guideline NG206 (2021)"
    )

    val PEM = GuideTopic(
        title = "Why one tap when you crash matters",
        body = "Post-exertional malaise (PEM) is the hallmark of ME/CFS: symptoms worsen " +
            "after even small exertion, out of proportion to the effort. Crucially it is " +
            "delayed — typically 12–48 hours after the trigger, sometimes longer — and " +
            "can last days or weeks. Because of that delay, cause and effect can't be " +
            "felt in the moment. When you tap \"I'm crashing\", Akari looks back " +
            "48 hours and, over time, shows which activities most often came before " +
            "your crashes.",
        source = "NICE guideline NG206 (2021); ME Association"
    )

    val HEART_RATE = GuideTopic(
        title = "The heart-rate ceiling",
        body = "The Workwell Foundation's exercise-testing research found most people " +
            "with ME/CFS cross their anaerobic threshold at roughly 15 beats above " +
            "their true resting heart rate. Above that threshold the body switches to " +
            "anaerobic energy and PEM risk climbs steeply. To set it well: measure your " +
            "heart rate on waking, before getting out of bed, for 7 mornings; average " +
            "them; add 15. If your heart rate passes the ceiling: stop, sit or lie " +
            "down, and rest until it settles back near resting.",
        source = "Workwell Foundation heart-rate pacing guidance"
    )

    val EFFORT_TYPES = GuideTopic(
        title = "Body, brain, and heart all spend energy",
        body = "The envelope is drained by three kinds of effort: physical (showering, " +
            "walking), cognitive (screens, reading, decisions), and emotional " +
            "(difficult conversations, worry, even joyful visits). All three draw on " +
            "the same reserve — a hard phone call can cost as much as a walk. That's " +
            "why Akari asks which kind an activity was: your triggers may live in one " +
            "column more than the others.",
        source = "Bateman Horne Center ME/CFS management guidance"
    )

    val ZONES = GuideTopic(
        title = "What the zones mean",
        body = "Steady (green ring): you're inside your envelope — gentle activity with " +
            "rest between is reasonable. Getting low (amber): the envelope is nearly " +
            "spent — one small thing at most, then rest before you feel you need it. " +
            "Rest zone (warm red): stop and rest now; pushing here is what turns a hard " +
            "day into a crash. Pre-emptive rest — resting before you're forced to — is " +
            "one of the best-evidenced pacing habits.",
        source = "Emerge Australia (\"Stop. Rest. Pace.\"); CDC ME/CFS management"
    )


    val ROLLING_LOAD = GuideTopic(
        title = "Why a caution can appear on a good morning",
        body = "PEM often follows cumulative over-exertion, not a single event — and it " +
            "arrives 12–48 hours late. Akari adds up the energy spent across the last " +
            "72 hours (body, brain and heart together) and compares it with your usual " +
            "level. When the 3-day load runs well above your norm, a caution appears " +
            "even if this morning feels fine: the bill for a busy stretch may simply " +
            "not have arrived yet. Treating a flagged day as gentler than it feels is " +
            "pre-emptive rest — resting before symptoms force it, one of the " +
            "best-evidenced pacing habits.",
        source = "NICE NG206 (2021) on delayed PEM; Bateman Horne Center; Emerge Australia"
    )

    val BREATHING = GuideTopic(
        title = "Box breathing in crash mode",
        body = "Slow, even breathing — about 4 seconds in, 4 hold, 4 out, 4 hold — is a " +
            "simple way to support the parasympathetic (rest-and-digest) side of the " +
            "nervous system, which is often dysregulated in ME/CFS. In crash mode the " +
            "rhythm is carried by gentle vibration ticks, so eyes can stay closed and " +
            "the screen dark. It is a comfort measure, not a treatment: never force " +
            "the breath, and stop if it brings dizziness.",
        source = "Paced-breathing / autonomic regulation research; offered as comfort, not therapy"
    )

    val ALL = listOf(ENVELOPE, PEM, HEART_RATE, EFFORT_TYPES, ZONES, ROLLING_LOAD, BREATHING)
}
