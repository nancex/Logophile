package me.nancex.logophile.data.remote

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

sealed class VersionCheckResult {
    data class UpdateAvailable(val latestVersion: String, val downloadUrl: String) : VersionCheckResult()
    data object UpToDate : VersionCheckResult()
    data class Error(val message: String) : VersionCheckResult()
}

object VersionChecker {

    private const val TAG = "VersionChecker"
    private const val GITHUB_API = "https://api.github.com/repos/nancex/Logophile/releases/latest"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun check(context: Context): VersionCheckResult = withContext(Dispatchers.IO) {
        try {
            val currentVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
            } catch (e: PackageManager.NameNotFoundException) {
                "0.0.0"
            }

            val request = Request.Builder().url(GITHUB_API).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "GitHub API returned ${response.code}")
                return@withContext VersionCheckResult.Error("HTTP ${response.code}")
            }

            val body = response.body?.string() ?: return@withContext VersionCheckResult.Error("Empty response")
            val json = JSONObject(body)
            val tagName = json.getString("tag_name").removePrefix("v")
            val htmlUrl = json.getString("html_url")

            Log.d(TAG, "current=$currentVersion, latest=$tagName")

            if (compareVersions(tagName, currentVersion) > 0) {
                VersionCheckResult.UpdateAvailable(tagName, htmlUrl)
            } else {
                VersionCheckResult.UpToDate
            }
        } catch (e: Exception) {
            Log.e(TAG, "check failed: ${e.message}", e)
            VersionCheckResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun compareVersions(a: String, b: String): Int {
        val aParts = a.split(".").map { it.toIntOrNull() ?: 0 }
        val bParts = b.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(aParts.size, bParts.size)
        for (i in 0 until maxLen) {
            val aVal = aParts.getOrElse(i) { 0 }
            val bVal = bParts.getOrElse(i) { 0 }
            if (aVal != bVal) return aVal - bVal
        }
        return 0
    }
}