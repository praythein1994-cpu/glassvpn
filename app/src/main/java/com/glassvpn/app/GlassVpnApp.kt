package com.glassvpn.app

import android.app.Application
import com.glassvpn.app.util.CrashReporter
import de.blinkt.openvpn.core.GlobalPreferences
import de.blinkt.openvpn.core.VpnStatus

class GlassVpnApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
        CrashReporter.breadcrumb(this, "app started")
        // Required by ics-openvpn: OpenVPNService crashes without this ("Global preferences instance is not set")
        GlobalPreferences.setInstance(false, false, false)
        VpnStatus.initLogCache(applicationContext.cacheDir)
    }
}
