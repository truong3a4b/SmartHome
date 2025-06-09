package com.example.smarthome.model

import android.net.MacAddress

data class Device(
    var id:String ="",
    var name:String="",
    var macAddress:String = "",
    var ipAddress: String = "",
    var room :String = "",
    var type:DeviceType = DeviceType.OTHER,
    var status:DeviceStatus=DeviceStatus.DISCONNECTED,

) {

    fun setTypeOnName(){
        val part = name.split("]")[0];
        if(part.equals("[LIGHT")) type = DeviceType.LIGHT;
        else if(part.equals("[FAN")) type = DeviceType.FAN;
        else if(part.equals("[SENSOR")) type = DeviceType.SENSOR
        else if(part.equals("[TV")) type = DeviceType.TV;
        else type = DeviceType.OTHER
    }
}

enum class DeviceType {
    LIGHT, FAN, SENSOR, SWITCH, OTHER,TV
}
enum class DeviceStatus {
    CONNECTED, DISCONNECTED,UNKNOWN
}
