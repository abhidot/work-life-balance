package com.worklife.boundary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TimeStepper(label: String, minutes: Int, onChange: (Int) -> Unit) {
    val hour = minutes / 60
    val minute = minutes % 60
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onChange(((hour + 23) % 24) * 60 + minute) }) { Text("−") }
            Text(String.format("%02d:%02d", hour, minute), style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = { onChange(((hour + 1) % 24) * 60 + minute) }) { Text("+") }
        }
    }
}
