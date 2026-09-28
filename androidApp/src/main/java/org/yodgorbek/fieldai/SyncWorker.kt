package org.yodgorbek.fieldai

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.yodgorbek.fieldai.shared.domain.synchronization.SyncEngine

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val syncEngine: SyncEngine by inject()

    override suspend fun doWork(): Result {
        return try {
            syncEngine.startSync()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
