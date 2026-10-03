package com.glassvpn.app

import android.app.Application
import com.glassvpn.app.util.CrashReporter
import de.blinkt.openvpn.core.VpnStatus

class GlassVpnApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
        CrashReporter.breadcrumb(this, "app started")
        VpnStatus.initLogCache(applicationContext.cacheDir)
    }
}
