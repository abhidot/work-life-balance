package com.worklife.boundary.ui.screens

import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.worklife.boundary.data.BoundaryMappers
import com.worklife.boundary.data.local.DayOfWeekBitmask
import com.worklife.boundary.data.local.entity.BlocklistEntryEntity
import com.worklife.boundary.domain.schedule.EntryScheduleOverrides
import com.worklife.boundary.domain.schedule.OverrideMode
import com.worklife.boundary.ui.BoundaryViewModel
import com.worklife.boundary.ui.components.BoundaryCard
import com.worklife.boundary.ui.components.DayOfWeekSelector
import com.worklife.boundary.ui.components.SectionHeader
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlocklistScreen(
    entries: List<BlocklistEntryEntity>,
    viewModel: BoundaryViewModel,
    onBack: () -> Unit,
    onEntry: (Long) -> Unit,
) {
    val context = LocalContext.current
    var showManual by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val pickContact = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val resolver = context.contentResolver
        val cursor = resolver.query(uri, arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME), null, null, null)
        cursor?.use {
            if (!it.moveToFirst()) return@use
            val id = it.getString(0)
            val name = it.getString(1) ?: "Contact"
            val phones = mutableListOf<String>()
            val phoneCursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=?",
                arrayOf(id),
                null,
            )
            phoneCursor?.use { pc ->
                while (pc.moveToNext()) phones.add(pc.getString(0))
            }
            viewModel.addContactEntry(name, id, phones) { result ->
                result.onFailure { e -> error = e.message }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blocklist") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { pickContact.launch(null) }, modifier = Modifier.weight(1f)) {
                    Text("From contacts")
                }
                Button(onClick = { showManual = true }, modifier = Modifier.weight(1f)) {
                    Text("Add number")
                }
            }
            error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries, key = { it.id }) { entry ->
                    BoundaryCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEntry(entry.id) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.displayName, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                                Text(if (entry.enabled) "Blocking when enabled" else "Disabled", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = entry.enabled,
                                onCheckedChange = { viewModel.setEntryEnabled(entry.id, it) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showManual) {
        ManualEntryDialog(
            onDismiss = { showManual = false },
            onAdd = { name, number ->
                viewModel.addManualEntry(name, number) { result ->
                    result.onSuccess { showManual = false }
                    result.onFailure { e -> error = e.message }
                }
            },
        )
    }
}

@Composable
private fun ManualEntryDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to blocklist") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Phone number") })
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(name.ifBlank { "Unknown" }, number) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    entry: BlocklistEntryEntity,
    viewModel: BoundaryViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    var daysMode by remember(entry) { mutableStateOf(entry.daysMode == "CUSTOM") }
    var timesMode by remember(entry) { mutableStateOf(entry.timesMode == "CUSTOM") }
    var daysMask by remember(entry) { mutableIntStateOf(entry.customDaysMask ?: DayOfWeekBitmask.encode(DayOfWeekBitmask.weekdays)) }
    var startMinutes by remember(entry) { mutableIntStateOf(entry.customStartMinutes ?: 9 * 60) }
    var endMinutes by remember(entry) { mutableIntStateOf(entry.customEndMinutes ?: 18 * 60) }

    val days = DayOfWeekBitmask.decode(daysMask)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry.displayName) },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BoundaryCard {
                SectionHeader("Days")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Use global days", modifier = Modifier.weight(1f))
                    Switch(checked = !daysMode, onCheckedChange = { daysMode = !it })
                }
                if (daysMode) {
                    DayOfWeekSelector(selected = days, onToggle = { day ->
                        daysMask = if (day in days) {
                            val next = days - day
                            if (next.isEmpty()) daysMask else DayOfWeekBitmask.encode(next)
                        } else {
                            DayOfWeekBitmask.encode(days + day)
                        }
                    })
                }
            }
            BoundaryCard {
                SectionHeader("Times")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Use global times", modifier = Modifier.weight(1f))
                    Switch(checked = !timesMode, onCheckedChange = { timesMode = !it })
                }
                if (timesMode) {
                    TimeStepper("Start", startMinutes) { startMinutes = it }
                    TimeStepper("End", endMinutes) { endMinutes = it }
                }
            }
            Button(
                onClick = {
                    viewModel.updateEntrySchedule(
                        entry.id,
                        EntryScheduleOverrides(
                            daysMode = if (daysMode) OverrideMode.CUSTOM else OverrideMode.INHERIT_GLOBAL,
                            customDays = days,
                            timesMode = if (timesMode) OverrideMode.CUSTOM else OverrideMode.INHERIT_GLOBAL,
                            customStartTime = BoundaryMappers.minutesToLocalTime(startMinutes),
                            customEndTime = BoundaryMappers.minutesToLocalTime(endMinutes),
                        ),
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save overrides") }
            TextButton(
                onClick = {
                    viewModel.deleteEntry(entry.id)
                    onDeleted()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Remove from blocklist") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).padding(24.dp)) {
            BoundaryCard {
                SectionHeader("Block method", "Voice calls are rejected via Call Screening when rules match.")
                Spacer(Modifier.height(8.dp))
                Text("SMS blocking is not available in v1. Messages may still arrive in your inbox.")
            }
        }
    }
}
