package com.example.util

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress

data class DiscoveredDevice(
    val name: String,
    val hostIp: String,
    val port: Int,
    val serviceName: String
)

class NsdHelper(private val context: Context) {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private val SERVICE_TYPE = "_beam-transfer._tcp."

    fun registerService(port: Int) {
        if (nsdManager == null) return
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "Beam TV (${Build.MODEL})"
            serviceType = SERVICE_TYPE
            setPort(port)
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
                Log.d("NsdHelper", "Service registered: ${NsdServiceInfo.serviceName}")
            }

            override fun onRegistrationFailed(arg0: NsdServiceInfo, arg1: Int) {
                Log.e("NsdHelper", "Service registration failed: $arg1")
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                Log.d("NsdHelper", "Service unregistered")
            }

            override fun onUnregistrationFailed(arg0: NsdServiceInfo, arg1: Int) {
                Log.e("NsdHelper", "Unregistration failed: $arg1")
            }
        }

        try {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e("NsdHelper", "Failed to register NSD service: ${e.message}")
        }
    }

    fun startDiscovery() {
        if (nsdManager == null || discoveryListener != null) return

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d("NsdHelper", "Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d("NsdHelper", "Service found: ${service.serviceName}")
                if (service.serviceType.contains("_beam-transfer")) {
                    resolveService(service)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d("NsdHelper", "Service lost: ${service.serviceName}")
                val current = _discoveredDevices.value.filter { it.serviceName != service.serviceName }
                _discoveredDevices.value = current
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d("NsdHelper", "Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("NsdHelper", "Discovery start failed: $errorCode")
                stopDiscovery()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("NsdHelper", "Discovery stop failed: $errorCode")
            }
        }

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e("NsdHelper", "Error starting discovery: ${e.message}")
        }
    }

    private fun resolveService(service: NsdServiceInfo) {
        nsdManager?.resolveService(service, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e("NsdHelper", "Resolve failed: $errorCode")
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val host: InetAddress? = serviceInfo.host
                val hostIp = host?.hostAddress ?: return
                val deviceName = serviceInfo.serviceName.replace("Beam TV (", "").replace(")", "").ifEmpty { serviceInfo.serviceName }

                val device = DiscoveredDevice(
                    name = deviceName,
                    hostIp = hostIp,
                    port = serviceInfo.port,
                    serviceName = serviceInfo.serviceName
                )

                val current = _discoveredDevices.value.filter { it.serviceName != serviceInfo.serviceName }.toMutableList()
                current.add(device)
                _discoveredDevices.value = current
            }
        })
    }

    fun stopDiscovery() {
        if (discoveryListener != null && nsdManager != null) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.e("NsdHelper", "Error stopping discovery: ${e.message}")
            }
            discoveryListener = null
        }
    }

    fun unregisterService() {
        if (registrationListener != null && nsdManager != null) {
            try {
                nsdManager.unregisterService(registrationListener)
            } catch (e: Exception) {
                Log.e("NsdHelper", "Error unregistering service: ${e.message}")
            }
            registrationListener = null
        }
    }

    fun tearDown() {
        stopDiscovery()
        unregisterService()
    }
}
