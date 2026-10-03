package com.glassvpn.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * In-app updater: checks the GitHub Releases feed for a newer version,
 * downloads the APK and fires the install intent. No Play Store needed.
 */
object UpdateChecker {
    private const val TAG = "UpdateChecker"
    private const val OWNER = "praythein1994-cpu"
    private const val REPO = "glassvpn"
    private const val API_URL = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"

    data class UpdateInfo(
        val latestVersion: String,
        val downloadUrl: String,
        val releaseNotes: String
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Returns UpdateInfo if a newer version exists, null if up to date or check failed. */
    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url(API_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .build()
            val resp = client.newCall(req).execute()
            val body = resp.body?.string() ?: return@withContext null
            if (!resp.isSuccessful) return@withContext null

            val json = JSONObject(body)
            val tag = json.optString("tag_name", "").trimStart('v', 'V')
            if (tag.isBlank()) return@withContext null

            val current = currentVersionName(context)
            if (!isNewer(current, tag)) return@withContext null

            val assets = json.optJSONArray("assets") ?: return@withContext null
            var apkUrl: String? = null
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                val name = a.optString("name", "")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = a.optString("browser_download_url", "")
                    break
                }
            }
            if (apkUrl.isNullOrBlank()) return@withContext null

            UpdateInfo(
                latestVersion = tag,
                downloadUrl = apkUrl,
                releaseNotes = json.optString("body", "")
            )
        } catch (e: Exception) {
            Log.e(TAG, "checkForUpdate failed", e)
            null
        }
    }

    /** Downloads the APK to cache dir. Returns the file or null. onProgress: 0..100 */
    suspend fun downloadApk(
        context: Context,
        url: String,
        onProgress: (Int) -> Unit = {}
    ): File? = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url(url).build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) return@withContext null
            val body = resp.body ?: return@withContext null
            val total = body.contentLength()
            val file = File(context.cacheDir, "glassvpn-update.apk")
            body.byteStream().use { input ->
                file.outputStream().use { output ->
                    val buf = ByteArray(8192)
                    var read: Int
                    var done = 0L
                    while (input.read(buf).also { read = it } != -1) {
                        output.write(buf, 0, read)
                        done += read
                        if (total > 0) onProgress(((done * 100) / total).toInt().coerceIn(0, 100))
                    }
                }
            }
            onProgress(100)
            file
        } catch (e: Exception) {
            Log.e(TAG, "downloadApk failed", e)
            null
        }
    }

    /** Fires the system install intent for a downloaded APK. */
    fun installApk(context: Context, apkFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "installApk failed", e)
        }
    }

    fun currentVersionName(context: Context): String {
        return try {
            val pi = if (Build.VERSION.SDK_INT >= 33) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pi.versionName ?: "1.0"
        } catch (_: Exception) { "1.0" }
    }

    /** Simple semver-ish compare: "1.10" > "1.9". */
    fun isNewer(current: String, latest: String): Boolean {
        val c = current.split(".", "-").mapNotNull { it.toIntOrNull() }
        val l = latest.split(".", "-").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(c.size, l.size)) {
            val cv = c.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (lv > cv) return true
            if (lv < cv) return false
        }
        return false
    }
}
