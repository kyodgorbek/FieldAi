package org.yodgorbek.fieldai.shared.domain.synchronization

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.yodgorbek.fieldai.shared.domain.model.PendingOperation
import org.yodgorbek.fieldai.shared.domain.repository.PendingOperationRepository
import org.yodgorbek.fieldai.shared.domain.repository.SyncMetadataRepository
import org.yodgorbek.fieldai.shared.network.FieldAiApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakePendingOperationRepository : PendingOperationRepository {
    var pendingOps = mutableListOf<PendingOperation>()
    var deletedOps = mutableListOf<String>()
    var incrementedOps = mutableListOf<String>()

    override fun getPendingOperations(): Flow<List<PendingOperation>> = flowOf(pendingOps)

    override suspend fun addPendingOperation(type: String, payload: String) {}

    override suspend fun deletePendingOperation(id: String) {
        deletedOps.add(id)
        pendingOps.removeAll { it.id == id }
    }

    override suspend fun incrementRetryCount(id: String) {
        incrementedOps.add(id)
        val op = pendingOps.find { it.id == id }
        if (op != null) {
            val idx = pendingOps.indexOf(op)
            pendingOps[idx] = op.copy(retryCount = op.retryCount + 1)
        }
    }
}

class FakeSyncMetadataRepository : SyncMetadataRepository {
    private val metadata = mutableMapOf<String, Long>()

    override suspend fun updateSyncMetadata(key: String, lastSynced: Long) {
        metadata[key] = lastSynced
    }

    override suspend fun getSyncMetadata(key: String): Long? {
        return metadata[key]
    }
}

class FakeFieldAiApi : FieldAiApi {
    var shouldFail = false
    val executedOps = mutableListOf<Pair<String, String>>()

    override suspend fun executeOperation(type: String, payload: String) {
        if (shouldFail) throw Exception("Network error")
        executedOps.add(type to payload)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SyncEngineTest {

    @Test
    fun `sync processes pending operations successfully`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val fakePendingRepo = FakePendingOperationRepository()
        val fakeSyncMetadataRepo = FakeSyncMetadataRepository()
        val fakeApi = FakeFieldAiApi()

        fakePendingRepo.pendingOps.add(PendingOperation("op_1", "MSG_SEND", "hello", 0, 0))
        fakePendingRepo.pendingOps.add(PendingOperation("op_2", "MSG_SEND", "world", 0, 0))

        val syncEngine = SyncEngine(fakePendingRepo, fakeSyncMetadataRepo, fakeApi, dispatcher)
        
        val job = syncEngine.startSync()
        dispatcher.scheduler.advanceUntilIdle()
        job.join()

        // Verify API was called
        assertEquals(2, fakeApi.executedOps.size)
        assertEquals("hello", fakeApi.executedOps[0].second)
        
        // Verify ops were deleted
        assertEquals(2, fakePendingRepo.deletedOps.size)
        assertTrue(fakePendingRepo.deletedOps.contains("op_1"))
        
        // Verify metadata updated
        val lastSynced = fakeSyncMetadataRepo.getSyncMetadata(SyncEngine.SYNC_KEY_CONVERSATIONS)
        assertTrue(lastSynced != null && lastSynced > 0)
    }

    @Test
    fun `sync handles api failures by incrementing retry count`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val fakePendingRepo = FakePendingOperationRepository()
        val fakeSyncMetadataRepo = FakeSyncMetadataRepository()
        val fakeApi = FakeFieldAiApi().apply { shouldFail = true }

        fakePendingRepo.pendingOps.add(PendingOperation("op_1", "MSG_SEND", "hello", 0, 0))

        val syncEngine = SyncEngine(fakePendingRepo, fakeSyncMetadataRepo, fakeApi, dispatcher)
        
        val job = syncEngine.startSync()
        dispatcher.scheduler.advanceUntilIdle()
        job.join()

        // Verify API failed, so it was not fully executed in the fake (throws)
        assertTrue(fakeApi.executedOps.isEmpty())
        
        // Verify op was NOT deleted, but incremented
        assertTrue(fakePendingRepo.deletedOps.isEmpty())
        assertEquals(1, fakePendingRepo.incrementedOps.size)
        assertTrue(fakePendingRepo.incrementedOps.contains("op_1"))
    }
    
    @Test
    fun `sync deletes operations exceeding max retries`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val fakePendingRepo = FakePendingOperationRepository()
        val fakeSyncMetadataRepo = FakeSyncMetadataRepository()
        val fakeApi = FakeFieldAiApi()

        // op_1 is at exactly max retries
        fakePendingRepo.pendingOps.add(PendingOperation("op_1", "MSG_SEND", "hello", SyncEngine.MAX_RETRIES, 0))

        val syncEngine = SyncEngine(fakePendingRepo, fakeSyncMetadataRepo, fakeApi, dispatcher)
        
        val job = syncEngine.startSync()
        dispatcher.scheduler.advanceUntilIdle()
        job.join()

        // Verify API was NOT called for this op
        assertTrue(fakeApi.executedOps.isEmpty())
        
        // Verify op WAS deleted due to max retries
        assertEquals(1, fakePendingRepo.deletedOps.size)
        assertTrue(fakePendingRepo.deletedOps.contains("op_1"))
    }
}
