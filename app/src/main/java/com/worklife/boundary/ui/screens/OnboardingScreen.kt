package com.worklife.boundary.ui.screens

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.worklife.boundary.data.BoundaryMappers
import com.worklife.boundary.data.local.DayOfWeekBitmask
import com.worklife.boundary.ui.BoundaryViewModel
import com.worklife.boundary.ui.components.BoundaryCard
import com.worklife.boundary.ui.components.BoundaryLogo
import com.worklife.boundary.ui.components.DayOfWeekSelector
import com.worklife.boundary.ui.components.SectionHeader

@Composable
fun OnboardingScreen(
    viewModel: BoundaryViewModel,
    onComplete: () -> Unit,
) {
    val context = LocalContext.current
    var step by rememberSaveable { mutableIntStateOf(0) }
    var selectedDays by rememberSaveable { mutableIntStateOf(DayOfWeekBitmask.encode(DayOfWeekBitmask.weekdays)) }
    var startMinutes by rememberSaveable { mutableIntStateOf(9 * 60) }
    var endMinutes by rememberSaveable { mutableIntStateOf(18 * 60) }

    val days = DayOfWeekBitmask.decode(selectedDays)
    val start = BoundaryMappers.minutesToLocalTime(startMinutes)
    val end = BoundaryMappers.minutesToLocalTime(endMinutes)

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.refreshScreeningRole()
        step = 2
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { step = 1 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BoundaryLogo()
        Text("Boundary", style = MaterialTheme.typography.headlineLarge)
        Text("Protect your off-hours", color = MaterialTheme.colorScheme.onSurfaceVariant)

        when (step) {
            0 -> {
                BoundaryCard {
                    SectionHeader("Welcome", "Blocklist callers cannot reach you outside allowed hours — the call never connects.")
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.READ_PHONE_STATE,
                                    android.Manifest.permission.READ_CALL_LOG,
                                    android.Manifest.permission.READ_CONTACTS,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Continue") }
                }
            }
            1 -> {
                BoundaryCard {
                    SectionHeader("Call screening", "Grant Boundary permission to screen incoming calls.")
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val roleManager = context.getSystemService(RoleManager::class.java)
                                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                                    roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                                } else {
                                    context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
                                    step = 2
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Set as call screener") }
                    OutlinedButton(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Skip for now") }
                }
            }
            2 -> {
                BoundaryCard {
                    SectionHeader("Default work hours", "Blocklist contacts inherit this schedule unless customized.")
                    Spacer(Modifier.height(8.dp))
                    DayOfWeekSelector(
                        selected = days,
                        onToggle = { day ->
                            selectedDays = if (day in days) {
                                val next = days - day
                                if (next.isEmpty()) selectedDays else DayOfWeekBitmask.encode(next)
                            } else {
                                DayOfWeekBitmask.encode(days + day)
                            }
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                    TimeStepper("Start", startMinutes) { value -> startMinutes = value }
                    TimeStepper("End", endMinutes) { value -> endMinutes = value }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.completeOnboarding(days, start, end)
                            onComplete()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Start protecting") }
                }
            }
        }
    }
}
