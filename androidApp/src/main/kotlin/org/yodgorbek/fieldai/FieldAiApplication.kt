package org.yodgorbek.fieldai

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.yodgorbek.fieldai.shared.di.initKoin

class FieldAiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        initKoin {
            androidLogger()
            androidContext(this@FieldAiApplication)
        }
    }
}
