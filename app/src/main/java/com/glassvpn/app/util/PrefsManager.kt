package com.glassvpn.app.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "glassvpn_prefs")

object PrefsManager {
    private val LANG_KEY = stringPreferencesKey("language")
    private val SERVER_KEY = stringPreferencesKey("last_server_ip")

    suspend fun getLanguage(context: Context): String {
        return context.dataStore.data.map { it[LANG_KEY] ?: "en" }.first()
    }

    suspend fun setLanguage(context: Context, lang: String) {
        context.dataStore.edit { it[LANG_KEY] = lang }
    }

    suspend fun getLastServerIp(context: Context): String? {
        return context.dataStore.data.map { it[SERVER_KEY] }.first()
    }

    suspend fun setLastServerIp(context: Context, ip: String) {
        context.dataStore.edit { it[SERVER_KEY] = ip }
    }
}
