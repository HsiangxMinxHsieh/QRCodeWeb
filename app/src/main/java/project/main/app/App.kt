package project.main.app

import android.app.Application
import com.microsoft.appcenter.crashes.Crashes

import com.microsoft.appcenter.analytics.Analytics

import com.microsoft.appcenter.AppCenter

class App:Application() {
    override fun onCreate() {
        super.onCreate()
        AppCenter.start(
            this, "53a7cb26-3cf9-4842-be9f-53d5fba9a9b3",
            Analytics::class.java, Crashes::class.java
        )
    }


}