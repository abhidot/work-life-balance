package com.worklife.boundary.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.worklife.boundary.ui.BoundaryUiState
import com.worklife.boundary.ui.BoundaryViewModel
import com.worklife.boundary.ui.components.BoundaryCard
import com.worklife.boundary.ui.components.BoundaryLogo
import com.worklife.boundary.ui.components.SectionHeader

@Composable
fun HomeScreen(
    uiState: BoundaryUiState,
    viewModel: BoundaryViewModel,
    onWorkHours: () -> Unit,
    onBlocklist: () -> Unit,
    onSettings: () -> Unit,
) {
    val settings = uiState.settings
    val masterOn = settings?.masterBlockingEnabled == true
    val screeningOk = uiState.screeningRoleHeld
    val onboardingDone = settings?.onboardingComplete == true

    val statusTitle = when {
        !onboardingDone -> "Finish setup"
        !screeningOk -> "Setup required"
        masterOn -> "Blocking active"
        else -> "Blocking paused"
    }
    val statusSubtitle = when {
        !screeningOk -> "Set Boundary as your call screening app."
        masterOn -> "${uiState.blocklistCount} on blocklist · off-hours callers cannot reach you"
        else -> "All calls will connect until you turn blocking on."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BoundaryLogo(Modifier.padding(bottom = 4.dp))
        Text("Boundary", style = MaterialTheme.typography.headlineLarge)

        BoundaryCard {
            SectionHeader(statusTitle, statusSubtitle)
            if (onboardingDone) {
                Spacer(Modifier.height(12.dp))
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text("Master blocking")
                    Switch(
                        checked = masterOn,
                        onCheckedChange = viewModel::setMasterBlocking,
                    )
                }
            }
        }

        Button(onClick = onWorkHours, modifier = Modifier.fillMaxWidth()) {
            Text("Work hours")
        }
        Button(onClick = onBlocklist, modifier = Modifier.fillMaxWidth()) {
            Text("Blocklist (${uiState.blocklistCount})")
        }
        Button(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Settings")
        }
    }
}
