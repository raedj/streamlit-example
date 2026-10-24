package com.polyglot.academy.core.device

data class DeviceInfo(
    val totalRamMb: Long,
    val availableStorageMb: Long,
    val osApiLevel: Int,
    val cpuCores: Int,
    val is64Bit: Boolean,
    val isLowRamDevice: Boolean,
    val batteryPercent: Int?,
    val isPowerSaveMode: Boolean,
    val thermalThrottled: Boolean,
    val hasMicrophone: Boolean,
    val platformName: String,
)

enum class DeviceTier { LOW, MID, HIGH }

object DeviceCapability {
    fun tier(d: DeviceInfo): DeviceTier = when {
        d.isLowRamDevice || d.totalRamMb < 3_000 -> DeviceTier.LOW
        d.totalRamMb >= 8_000 && d.cpuCores >= 8 && d.is64Bit -> DeviceTier.HIGH
        else -> DeviceTier.MID
    }

    /** Animation budget: low-tier or power-save devices get reduced decorative motion. */
    fun prefersReducedEffects(d: DeviceInfo) = tier(d) == DeviceTier.LOW || d.isPowerSaveMode || d.thermalThrottled
}
