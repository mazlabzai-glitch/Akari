package com.mazlabz.akari

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Akari's small haptic vocabulary. Three gestures, used sparingly:
 *
 *  - restPulse: a soft, slow, settling pulse — logging rest should feel grounding
 *  - caution:  a distinct double-pulse — crossing into the Rest zone, or breaching
 *              the heart-rate pacing ceiling
 *  - tick:     a feather-light tap — breathing phase changes in crash mode
 *
 * All patterns are gentle by design; this is a calm app for a sensitive
 * nervous system, not a notification machine.
 */
object Haptics {

    private fun vibrator(context: Context): Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                ?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (t: Throwable) {
        null
    }

    private fun play(context: Context, timings: LongArray, amplitudes: IntArray) {
        val v = vibrator(context) ?: return
        try {
            if (v.hasAmplitudeControl()) {
                v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                v.vibrate(VibrationEffect.createWaveform(timings, -1))
            }
        } catch (t: Throwable) {
            // never let a haptic crash the diary
        }
    }

    /** Soft, slow, settling — like an exhale. */
    fun restPulse(context: Context) = play(
        context,
        longArrayOf(0, 90, 160, 140, 220, 180),
        intArrayOf(0, 70, 0, 45, 0, 25)
    )

    /** Two firm-but-gentle taps: attention, kindly meant. */
    fun caution(context: Context) = play(
        context,
        longArrayOf(0, 70, 110, 70),
        intArrayOf(0, 160, 0, 160)
    )

    /** Feather tap for breathing phase changes. */
    fun tick(context: Context) = play(
        context,
        longArrayOf(0, 35),
        intArrayOf(0, 60)
    )
}
