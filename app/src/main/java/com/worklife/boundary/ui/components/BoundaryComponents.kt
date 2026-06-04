package com.worklife.boundary.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.worklife.boundary.R

@Composable
fun BoundaryLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.ic_logo_boundary),
        contentDescription = "Boundary logo",
        modifier = modifier.then(Modifier.size(72.dp)),
    )
}

@Composable
fun BoundaryCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp), content = { content() })
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        if (subtitle != null) {
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DayOfWeekSelector(
    selected: Set<java.time.DayOfWeek>,
    onToggle: (java.time.DayOfWeek) -> Unit,
) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    val days = java.time.DayOfWeek.entries
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        days.forEachIndexed { index, day ->
            val isSelected = day in selected
            androidx.compose.material3.FilterChip(
                selected = isSelected,
                onClick = { onToggle(day) },
                label = { Text(labels[index]) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
