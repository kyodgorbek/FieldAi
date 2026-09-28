package org.yodgorbek.fieldai.shared.di

import org.koin.dsl.module
import org.yodgorbek.fieldai.shared.database.DatabaseDriverFactory
import org.yodgorbek.fieldai.shared.database.FieldAiDatabase

val databaseModule = module {
    single { 
        val driverFactory = get<DatabaseDriverFactory>()
        FieldAiDatabase(driverFactory.createDriver())
    }
}
