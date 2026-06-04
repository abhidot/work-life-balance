package com.worklife.boundary

import android.app.Application
import com.worklife.boundary.data.BoundaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BoundaryApplication : Application() {
    lateinit var repository: BoundaryRepository
        private set

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        repository = BoundaryRepository.from(this)
        applicationScope.launch {
            repository.ensureDefaults()
        }
    }
}
