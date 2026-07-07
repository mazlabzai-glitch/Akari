package com.mazlabz.akari.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
            .padding(horizontal = 16.dp, vertical = 7.dp),
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
