package com.example.beam.data.model

data class BatteryState(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false,
    val isSimulatedLowBattery: Boolean = false
) {
    val isLowBattery: Boolean
        get() = isSimulatedLowBattery || isPowerSaveMode || (batteryPercent <= 20 && !isCharging)

    val displayPercent: Int
        get() = if (isSimulatedLowBattery) 15 else batteryPercent
}
