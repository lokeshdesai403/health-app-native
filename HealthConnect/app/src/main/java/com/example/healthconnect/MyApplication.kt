package com.example.healthconnect

import android.app.Application
import com.example.healthconnect.lib.HealthConnectManager

class MyApplication : Application() {
    val healthConnectManager by lazy {
        HealthConnectManager(this)
    }
}
