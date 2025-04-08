package com.example.smarthome.model

class Room(
     val id:String,
     val name: String,
     val image: Int,
     val deviceList:MutableList<String>
) {
    public fun getNumDevice():Int{
        return deviceList.size;
    }
}