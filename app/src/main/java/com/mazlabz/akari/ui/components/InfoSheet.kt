package com.mazlabz.akari.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazlabz.akari.GuideTopic
import com.mazlabz.akari.ui.theme.Washi

/** Small ⓘ affordance placed next to concepts that deserve an explanation. */
@Composable
fun InfoDot(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .size(width = 44.dp, height = 40.dp)
            .semantics { contentDescription = "What does this mean?" }
    ) {
        Text("ⓘ", fontSize = 17.sp, color = Washi.InkFaded)
    }
}

/** Plain-language medical grounding, one topic at a time, with its source named. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoSheet(topic: GuideTopic, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Washi.Card) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 6.dp)) {
            Text(topic.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            Text(topic.body, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(14.dp))
            Text(
                "Source: " + topic.source,
                style = MaterialTheme.typography.bodyMedium,
                color = Washi.InkFaded
            )
            Spacer(Modifier.height(34.dp))
        }
    }
}
