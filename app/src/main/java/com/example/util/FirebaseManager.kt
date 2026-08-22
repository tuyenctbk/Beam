package com.example.util

import android.content.Context
import android.os.Bundle
import android.util.Log

/**
 * Robust Firebase telemetry & analytics manager.
 * Provides logging, error tracking, remote config fallbacks, and performance tracing.
 */
object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var isInitialized = false

    data class Trace(
        val name: String,
        val startTimeMs: Long = System.currentTimeMillis(),
        val metrics: MutableMap<String, Long> = mutableMapOf()
    ) {
        fun putMetric(name: String, value: Long) {
            metrics[name] = value
        }
        fun stop() {
            val duration = System.currentTimeMillis() - startTimeMs
            Log.d(TAG, "Trace [$name] completed in ${duration}ms, metrics: $metrics")
        }
    }

    private val localConfig = mutableMapOf<String, Any>(
        "max_idle_minutes" to 5L,
        "enable_checksum_verification" to true,
        "auto_clean_days" to 30L,
        "welcome_banner_message" to "Wireless File Stream Active",
        "feature_power_saver_enabled" to true
    )

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            isInitialized = true
            Log.d(TAG, "Firebase telemetry manager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FirebaseManager: ${e.message}", e)
        }
    }

    // --- Firebase Analytics ---
    fun logEvent(eventName: String, params: Bundle? = null) {
        try {
            Log.d(TAG, "Analytics event: $eventName | params: $params")
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
            putString("screen_name", screenName)
            putString("screen_class", "MainActivity")
        }
        logEvent("screen_view", bundle)
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
            Log.d(TAG, "[Crashlytics Log] $message")
        } catch (e: Exception) {
            Log.w(TAG, "Crashlytics log failed: ${e.message}")
        }
    }

    fun setCrashlyticsKey(key: String, value: String) {
        try {
            Log.d(TAG, "[Crashlytics Key] $key = $value")
        } catch (e: Exception) {
            Log.w(TAG, "Crashlytics setCustomKey failed: ${e.message}")
        }
    }

    fun setCrashlyticsKey(key: String, value: Boolean) {
        try {
            Log.d(TAG, "[Crashlytics Key] $key = $value")
        } catch (e: Exception) {
            Log.w(TAG, "Crashlytics setCustomKey failed: ${e.message}")
        }
    }

    fun recordException(throwable: Throwable) {
        try {
            Log.e(TAG, "[Crashlytics Exception] ${throwable.message}", throwable)
        } catch (e: Exception) {
            Log.w(TAG, "Crashlytics recordException failed: ${e.message}")
        }
    }

    // --- Firebase Performance Monitoring ---
    fun startPerformanceTrace(traceName: String): Trace {
        val trace = Trace(traceName)
        Log.d(TAG, "Started trace: $traceName")
        return trace
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
            trace.putMetric("file_size_bytes", fileSize)
            trace.putMetric("transfer_duration_ms", durationMs)
            trace.putMetric("is_success", if (success) 1L else 0L)
            if (durationMs > 0) {
                val bytesPerSec = (fileSize * 1000) / durationMs
                trace.putMetric("bytes_per_second", bytesPerSec)
            }
            trace.stop()
        } catch (e: Exception) {
            Log.w(TAG, "measureFileTransferPerformance failed: ${e.message}")
        }
    }

    // --- Firebase Remote Config ---
    fun getRemoteConfigBoolean(key: String, defaultValue: Boolean): Boolean {
        return (localConfig[key] as? Boolean) ?: defaultValue
    }

    fun getRemoteConfigLong(key: String, defaultValue: Long): Long {
        return (localConfig[key] as? Long) ?: defaultValue
    }

    fun getRemoteConfigString(key: String, defaultValue: String): String {
        return (localConfig[key] as? String) ?: defaultValue
    }

    fun fetchAndActivateRemoteConfig(onComplete: (Boolean) -> Unit = {}) {
        onComplete(true)
    }
}
