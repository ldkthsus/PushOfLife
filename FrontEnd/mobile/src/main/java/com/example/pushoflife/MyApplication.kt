package com.example.pushoflife

import android.app.Application
import com.example.pushoflife.di.repositoryModule
import com.example.pushoflife.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Koin 초기화
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)  // Context 주입
            androidFileProperties()
            modules(listOf(repositoryModule, viewModelModule))
        }
    }
}
