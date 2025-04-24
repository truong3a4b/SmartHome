package com.example.smarthome.model

class Room(
     var id:String="",
     var name: String="",
     var deviceList:MutableList<String> = mutableListOf()
) {
    public fun getNumDevice():Int{
        return deviceList.size;
    }
}