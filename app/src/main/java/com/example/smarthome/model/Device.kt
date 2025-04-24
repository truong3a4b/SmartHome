package com.example.smarthome.model

import android.net.MacAddress

data class Device(
    var id:String ="",
    var name:String="",
    var macAddress:String = "",
    var ipAddress: String = "",
    var type:DeviceType = DeviceType.OTHER,
    var status:DeviceStatus=DeviceStatus.UNKNOWN,
) {
}

enum class DeviceType {
    LIGHT, FAN, SENSOR, SWITCH, OTHER
}
enum class DeviceStatus {
    ON, OFF, UNKNOWN
}
