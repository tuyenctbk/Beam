package com.example.beam.server

import android.content.Context
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.util.Collections

object NetworkUtils {
    fun getLocalIpAddress(context: Context): String {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            if (ipAddress != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    ipAddress and 0xff,
                    ipAddress shr 8 and 0xff,
                    ipAddress shr 16 and 0xff,
                    ipAddress shr 24 and 0xff
                )
            }
        } catch (_: Exception) {
        }

        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress
                        if (host != null && !host.startsWith("127.")) {
                            return host
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }

        return "192.168.1.100"
    }

    fun findAvailablePort(startPort: Int = 8080, maxPort: Int = 8120): Int {
        for (port in startPort..maxPort) {
            try {
                ServerSocket(port).use {
                    return port
                }
            } catch (_: Exception) {
                // Port occupied or in use, try next
            }
        }
        // If range is exhausted, let OS assign an available dynamic port
        return try {
            ServerSocket(0).use {
                it.localPort
            }
        } catch (_: Exception) {
            startPort
        }
    }
}

