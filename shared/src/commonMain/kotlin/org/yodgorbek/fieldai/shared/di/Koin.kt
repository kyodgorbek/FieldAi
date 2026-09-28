package org.yodgorbek.fieldai.shared.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            platformModule,
            sharedModule,
            databaseModule,
            org.yodgorbek.fieldai.shared.network.networkModule
        )
    }
}

val sharedModule = module {
    single<org.yodgorbek.fieldai.shared.domain.repository.ConversationRepository> {
        org.yodgorbek.fieldai.shared.data.repository.LocalConversationRepository(get())
    }
    single<org.yodgorbek.fieldai.shared.domain.repository.MessageRepository> {
        org.yodgorbek.fieldai.shared.data.repository.LocalMessageRepository(get())
    }
    single<org.yodgorbek.fieldai.shared.domain.repository.PendingOperationRepository> {
        org.yodgorbek.fieldai.shared.data.repository.LocalPendingOperationRepository(get())
    }
    single<org.yodgorbek.fieldai.shared.domain.repository.SyncMetadataRepository> {
        org.yodgorbek.fieldai.shared.data.repository.LocalSyncMetadataRepository(get())
    }
    single<org.yodgorbek.fieldai.shared.domain.repository.DocumentRepository> {
        org.yodgorbek.fieldai.shared.data.repository.LocalDocumentRepository(get())
    }
    
    single {
        org.yodgorbek.fieldai.shared.domain.synchronization.SyncEngine(get(), get(), get())
    }
    
    factory {
        org.yodgorbek.fieldai.shared.presentation.AssistantViewModel(get(), get(), get())
    }
}
