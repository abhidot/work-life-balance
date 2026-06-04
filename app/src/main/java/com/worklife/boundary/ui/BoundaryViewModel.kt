package com.worklife.boundary.ui

import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.worklife.boundary.BoundaryApplication
import com.worklife.boundary.data.BoundaryMappers
import com.worklife.boundary.data.local.DayOfWeekBitmask
import com.worklife.boundary.data.local.entity.AppSettingsEntity
import com.worklife.boundary.data.local.entity.BlocklistEntryEntity
import com.worklife.boundary.domain.schedule.EntryScheduleOverrides
import com.worklife.boundary.domain.schedule.OverrideMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

data class BoundaryUiState(
    val settings: AppSettingsEntity? = null,
    val blocklist: List<BlocklistEntryEntity> = emptyList(),
    val blocklistCount: Int = 0,
    val screeningRoleHeld: Boolean = false,
)

class BoundaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as BoundaryApplication).repository

    private val screeningRoleFlow = kotlinx.coroutines.flow.MutableStateFlow(false)

    val uiState: StateFlow<BoundaryUiState> = combine(
        repository.appSettings,
        repository.blocklistEntries,
        repository.blocklistCount,
        screeningRoleFlow,
    ) { settings, entries, count, role ->
        BoundaryUiState(
            settings = settings,
            blocklist = entries,
            blocklistCount = count,
            screeningRoleHeld = role,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BoundaryUiState())

    init {
        viewModelScope.launch { repository.ensureDefaults() }
        refreshScreeningRole()
    }

    fun refreshScreeningRole() {
        screeningRoleFlow.value = isCallScreeningRoleHeld(getApplication())
    }

    fun setMasterBlocking(enabled: Boolean) {
        viewModelScope.launch { repository.setMasterBlocking(enabled) }
    }

    fun setSendBlockedCallsToVoicemail(enabled: Boolean) {
        viewModelScope.launch { repository.setSendBlockedCallsToVoicemail(enabled) }
    }

    fun completeOnboarding(days: Set<DayOfWeek>, start: LocalTime, end: LocalTime) {
        viewModelScope.launch { repository.completeOnboarding(days, start, end) }
    }

    fun updateGlobalSchedule(days: Set<DayOfWeek>, start: LocalTime, end: LocalTime) {
        viewModelScope.launch { repository.updateGlobalSchedule(days, start, end) }
    }

    fun addManualEntry(name: String, number: String, onResult: (Result<Long>) -> Unit) {
        viewModelScope.launch { onResult(repository.addManualEntry(name, number)) }
    }

    fun addContactEntry(name: String, contactId: String, numbers: List<String>, onResult: (Result<Long>) -> Unit) {
        viewModelScope.launch { onResult(repository.addContactEntry(name, contactId, numbers)) }
    }

    fun setEntryEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch { repository.setEntryEnabled(id, enabled) }
    }

    fun updateEntrySchedule(id: Long, overrides: EntryScheduleOverrides) {
        viewModelScope.launch { repository.updateEntrySchedule(id, overrides) }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { repository.deleteEntry(id) }
    }

    fun globalScheduleFrom(settings: AppSettingsEntity) = BoundaryMappers.toGlobalSchedule(settings)

    fun defaultWeekdays(): Set<DayOfWeek> = DayOfWeekBitmask.weekdays

    companion object {
        fun isCallScreeningRoleHeld(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
            val roleManager = context.getSystemService(RoleManager::class.java) ?: return false
            return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        }
    }
}
