package com.example.smarthome.model

class WifiState(
    var enable:Boolean = true,
    var wifiConnected:Boolean = true,
    var is5G:Boolean = false,
    var ssid:String = "",
    var ssidBytes:ByteArray = byteArrayOf(),
    var bssid:String = "",
) {
}