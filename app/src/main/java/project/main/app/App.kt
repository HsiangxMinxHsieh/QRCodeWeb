package project.main.app

import android.app.Application
import com.microsoft.appcenter.crashes.Crashes

import com.microsoft.appcenter.analytics.Analytics

import com.microsoft.appcenter.AppCenter
import project.main.const.msKey

class App:Application() {
    override fun onCreate() {
        super.onCreate()
        AppCenter.start(
            this, msKey,
            Analytics::class.java, Crashes::class.java
        )
    }
}