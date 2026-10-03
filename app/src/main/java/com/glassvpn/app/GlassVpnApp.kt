package com.glassvpn.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import com.glassvpn.app.util.CrashReporter
import de.blinkt.openvpn.core.GlobalPreferences
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.VpnStatus

class GlassVpnApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
        CrashReporter.breadcrumb(this, "app started")
        // Required by ics-openvpn: OpenVPNService crashes without this ("Global preferences instance is not set")
        GlobalPreferences.setInstance(false, false, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels()
        }
        VpnStatus.initLogCache(applicationContext.cacheDir)
    }

    /** Mirrors ICSOpenVPNApplication.createNotificationChannels() — without these,
     *  startForeground() throws "Bad notification for startForeground". */
    private fun createNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var channel = NotificationChannel(
            OpenVPNService.NOTIFICATION_CHANNEL_BG_ID,
            getString(R.string.channel_name_background),
            NotificationManager.IMPORTANCE_MIN
        )
        channel.description = getString(R.string.channel_description_background)
        channel.enableLights(false)
        channel.lightColor = Color.DKGRAY
        nm.createNotificationChannel(channel)

        channel = NotificationChannel(
            OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID,
            getString(R.string.channel_name_status),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = getString(R.string.channel_description_status)
        channel.enableLights(true)
        channel.lightColor = Color.BLUE
        nm.createNotificationChannel(channel)

        channel = NotificationChannel(
            OpenVPNService.NOTIFICATION_CHANNEL_USERREQ_ID,
            getString(R.string.channel_name_userreq),
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = getString(R.string.channel_description_userreq)
        channel.enableVibration(true)
        channel.lightColor = Color.CYAN
        nm.createNotificationChannel(channel)
    }
}
