package com.example.smarthome.model

data class Device(
    val id:String ="",
    val name:String="",
    var type:DeviceType = DeviceType.LIGHT,
    val status:DeviceStatus=DeviceStatus.ON,
) {
}

enum class DeviceType {
    LIGHT, FAN, SENSOR, SWITCH, OTHER
}
enum class DeviceStatus {
    ON, OFF, UNKNOWN
}
