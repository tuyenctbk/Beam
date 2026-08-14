package com.example.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var isInitialized = false

    private var analytics: FirebaseAnalytics? = null
    private var crashlytics: FirebaseCrashlytics? = null
    private var performance: FirebasePerformance? = null
    private var remoteConfig: FirebaseRemoteConfig? = null

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            analytics = FirebaseAnalytics.getInstance(context)
            crashlytics = FirebaseCrashlytics.getInstance()
            performance = FirebasePerformance.getInstance()
            remoteConfig = FirebaseRemoteConfig.getInstance()

            // Setup Remote Config Defaults
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build()
            remoteConfig?.setConfigSettingsAsync(configSettings)

            val defaults = mapOf(
                "max_idle_minutes" to 5L,
                "enable_checksum_verification" to true,
                "auto_clean_days" to 30L,
                "welcome_banner_message" to "Wireless File Stream Active",
                "feature_power_saver_enabled" to true
            )
            remoteConfig?.setDefaultsAsync(defaults)

            // Fetch and activate remote config asynchronously
            remoteConfig?.fetchAndActivate()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Remote Config fetched and activated successfully")
                } else {
                    Log.w(TAG, "Remote Config fetch failed")
                }
            }

            isInitialized = true
            Log.d(TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase services: ${e.message}", e)
        }
    }

    // --- Firebase Analytics ---
    fun logEvent(eventName: String, params: Bundle? = null) {
        try {
            analytics?.logEvent(eventName, params)
        } catch (e: Exception) {
            Log.w(TAG, "Analytics logEvent failed: ${e.message}")
        }
    }

    fun logFileTransferCompleted(
        fileName: String,
        fileSize: Long,
        durationMs: Long,
        category: String,
        checksumStatus: String,
        success: Boolean
    ) {
        val bundle = Bundle().apply {
            putString("file_name", fileName)
            putLong("file_size_bytes", fileSize)
            putLong("transfer_duration_ms", durationMs)
            putString("category", category)
            putString("checksum_status", checksumStatus)
            putBoolean("success", success)
            if (durationMs > 0) {
                putLong("kb_per_second", (fileSize / 1024) * 1000 / durationMs)
            }
        }
        logEvent("file_transfer_completed", bundle)
        logCrashlytics("File transfer finished: $fileName ($fileSize bytes) -> Success: $success")
    }

    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        }
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logUserAction(actionName: String, details: String = "") {
        val bundle = Bundle().apply {
            putString("action_name", actionName)
            putString("details", details)
        }
        logEvent("user_action", bundle)
    }

    // --- Firebase Crashlytics ---
    fun logCrashlytics(message: String) {
        try {
            crashlytics?.log(message)
        } catch (e: Exception) {
            Log.w(TAG, "Crashlytics log failed: ${e.message}")
        }
    }

    fun setCrashlyticsKey(key: String, value: String) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.w(TAG, "Crashlytics setCustomKey failed: ${e.message}")
        }
    }

    fun setCrashlyticsKey(key: String, value: Boolean) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.w(TAG, "Crashlytics setCustomKey failed: ${e.message}")
        }
    }

    fun recordException(throwable: Throwable) {
        try {
            crashlytics?.recordException(throwable)
        } catch (e: Exception) {
            Log.w(TAG, "Crashlytics recordException failed: ${e.message}")
        }
    }

    // --- Firebase Performance Monitoring ---
    fun startPerformanceTrace(traceName: String): Trace? {
        return try {
            performance?.newTrace(traceName)?.apply { start() }
        } catch (e: Exception) {
            Log.w(TAG, "Performance startTrace failed: ${e.message}")
            null
        }
    }

    fun stopPerformanceTrace(trace: Trace?, metrics: Map<String, Long> = emptyMap()) {
        try {
            trace?.let {
                metrics.forEach { (key, valMs) -> it.putMetric(key, valMs) }
                it.stop()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Performance stopTrace failed: ${e.message}")
        }
    }

    fun measureFileTransferPerformance(fileSize: Long, durationMs: Long, success: Boolean) {
        try {
            val trace = startPerformanceTrace("file_transfer_latency")
            trace?.let {
                it.putMetric("file_size_bytes", fileSize)
                it.putMetric("transfer_duration_ms", durationMs)
                it.putMetric("is_success", if (success) 1L else 0L)
                if (durationMs > 0) {
                    val bytesPerSec = (fileSize * 1000) / durationMs
                    it.putMetric("bytes_per_second", bytesPerSec)
                }
                it.stop()
            }
        } catch (e: Exception) {
            Log.w(TAG, "measureFileTransferPerformance failed: ${e.message}")
        }
    }

    // --- Firebase Remote Config ---
    fun getRemoteConfigBoolean(key: String, defaultValue: Boolean): Boolean {
        return try {
            remoteConfig?.getBoolean(key) ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }

    fun getRemoteConfigLong(key: String, defaultValue: Long): Long {
        return try {
            remoteConfig?.getLong(key) ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }

    fun getRemoteConfigString(key: String, defaultValue: String): String {
        return try {
            remoteConfig?.getString(key)?.takeIf { it.isNotEmpty() } ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }

    fun fetchAndActivateRemoteConfig(onComplete: (Boolean) -> Unit = {}) {
        try {
            remoteConfig?.fetchAndActivate()?.addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            } ?: onComplete(false)
        } catch (e: Exception) {
            onComplete(false)
        }
    }
}
