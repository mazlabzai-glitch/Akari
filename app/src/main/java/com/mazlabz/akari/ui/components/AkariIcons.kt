package com.mazlabz.akari.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.unit.dp

/**
 * A small, hand-drawn line-icon set in the washi aesthetic — one coherent visual
 * language in place of mixed emoji. Every glyph is a 24dp stroked outline with no
 * fill, so Icon(tint = …) recolours it to Ink / InkFaded / an accent. Unlike
 * emoji it stays crisp at any size and looks identical on every device.
 */
private fun line(name: String, block: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(
            pathData = PathData(block),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

object AkariIcons {

    val Today: ImageVector by lazy {
        line("today") {
            moveTo(12f, 4f)
            curveTo(8f, 8f, 8f, 12f, 8f, 15f)
            curveTo(8f, 18f, 9.8f, 20f, 12f, 20f)
            curveTo(14.2f, 20f, 16f, 18f, 16f, 15f)
            curveTo(16f, 12f, 14f, 9f, 12f, 4f)
            close()
        }
    }

    val Trends: ImageVector by lazy {
        line("trends") {
            moveTo(4f, 19f); horizontalLineTo(20f)
            moveTo(4f, 14f); lineTo(9f, 9f); lineTo(13f, 12f); lineTo(20f, 5f)
        }
    }

    val Settings: ImageVector by lazy {
        line("settings") {
            moveTo(4f, 8f); horizontalLineTo(20f)
            moveTo(4f, 12f); horizontalLineTo(20f)
            moveTo(4f, 16f); horizontalLineTo(20f)
            moveTo(9f, 6.3f); verticalLineTo(9.7f)
            moveTo(15f, 10.3f); verticalLineTo(13.7f)
            moveTo(8f, 14.3f); verticalLineTo(17.7f)
        }
    }

    val Checkin: ImageVector by lazy {
        line("checkin") {
            moveTo(3f, 18f); horizontalLineTo(21f)
            moveTo(7.5f, 18f); curveTo(7.5f, 12.8f, 16.5f, 12.8f, 16.5f, 18f)
            moveTo(12f, 8.2f); verticalLineTo(6f)
            moveTo(6.6f, 12f); lineTo(5.2f, 10.8f)
            moveTo(17.4f, 12f); lineTo(18.8f, 10.8f)
        }
    }

    val Activity: ImageVector by lazy {
        line("activity") {
            moveTo(13f, 3f); verticalLineTo(10f); horizontalLineTo(19f)
            lineTo(11f, 21f); verticalLineTo(14f); horizontalLineTo(5f)
            close()
        }
    }

    val Rest: ImageVector by lazy {
        line("rest") {
            moveTo(5f, 19f); curveTo(5f, 11f, 11f, 5f, 19f, 5f)
            curveTo(19f, 13f, 13f, 19f, 5f, 19f)
            close()
            moveTo(8f, 16f); lineTo(16f, 8f)
        }
    }

    val Symptom: ImageVector by lazy {
        line("symptom") {
            moveTo(10f, 13.5f); verticalLineTo(6f)
            curveTo(10f, 4.5f, 14f, 4.5f, 14f, 6f); verticalLineTo(13.5f)
            moveTo(9f, 16f)
            arcToRelative(3f, 3f, 0f, true, false, 6f, 0f)
            arcToRelative(3f, 3f, 0f, true, false, -6f, 0f)
        }
    }

    val Food: ImageVector by lazy {
        line("food") {
            moveTo(7f, 8f); horizontalLineTo(17f); lineTo(15.6f, 18f); horizontalLineTo(8.4f); close()
            moveTo(17f, 10f); curveTo(20f, 10.5f, 20f, 14f, 17f, 14.5f)
            moveTo(10f, 5f); verticalLineTo(3.2f)
            moveTo(13.5f, 5f); verticalLineTo(3.2f)
        }
    }

    val Vitals: ImageVector by lazy {
        line("vitals") {
            moveTo(3f, 12f); horizontalLineTo(8f); lineTo(10f, 8f); lineTo(13f, 16f); lineTo(15f, 12f); horizontalLineTo(21f)
        }
    }

    val Pem: ImageVector by lazy {
        line("pem") {
            moveTo(15f, 4f)
            arcToRelative(8f, 8f, 0f, true, false, 5f, 12f)
            arcToRelative(6.5f, 6.5f, 0f, false, true, -5f, -12f)
            close()
        }
    }

    val Caution: ImageVector by lazy {
        line("caution") {
            moveTo(4f, 9f); horizontalLineTo(20f)
            moveTo(4f, 13f); horizontalLineTo(18f)
            moveTo(7f, 17f); horizontalLineTo(20f)
        }
    }
}
