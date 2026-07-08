package com.mazlabz.akari.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mazlabz.akari.ui.theme.Washi

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Washi.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = Washi.InkFaded,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

/** The single primary header for a card — one consistent level across every screen. */
@Composable
fun CardTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge)
}

/** The current pacing zone as a readable pill: accent dot, tint fill, deep-accent text. */
@Composable
fun ZoneChip(zone: String) {
    val fg: Color
    val bg: Color
    val dot: Color
    when (zone) {
        "Steady" -> { fg = Washi.MossText; bg = Washi.MossTint; dot = Washi.Moss }
        "Getting low" -> { fg = Washi.AmberText; bg = Washi.AmberTint; dot = Washi.Amber }
        else -> { fg = Washi.PersimmonText; bg = Washi.PersimmonTint; dot = Washi.Persimmon }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Box(modifier = Modifier.size(7.dp).background(dot, CircleShape))
        Spacer(Modifier.width(7.dp))
        Text(zone, style = MaterialTheme.typography.titleMedium, color = fg)
    }
}

/** A soft, large-tap-target button used across the app. */
@Composable
fun GentleButton(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = Washi.Ink,
    filled: Boolean = false,
    subText: String? = null,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 54.dp),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.4.dp, if (filled) accent else Washi.Line),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (filled) accent else Washi.Card,
            contentColor = if (filled) Washi.Paper else accent
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge)
            if (subText != null) {
                Text(
                    subText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (filled) Washi.Paper.copy(alpha = 0.75f) else Washi.InkFaded
                )
            }
        }
    }
}

@Composable
fun KeyValueRow(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(key, style = MaterialTheme.typography.bodyMedium, color = Washi.InkFaded)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
