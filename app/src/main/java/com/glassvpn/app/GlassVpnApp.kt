package com.glassvpn.app

import android.app.Application
import de.blinkt.openvpn.core.VpnStatus

class GlassVpnApp : Application() {
    override fun onCreate() {
        super.onCreate()
        VpnStatus.initLogCache(applicationContext.cacheDir)
    }
}
