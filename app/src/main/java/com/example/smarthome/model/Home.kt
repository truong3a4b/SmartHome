package com.example.smarthome.model

data class Home(
    var id:String = "",
    var name:String= "",
    var ownerId:String="",
    var roomList:MutableList<String> = mutableListOf(),
    var sharedUsers:MutableList<Cupbo> = mutableListOf()
) {
    public fun getNumUser():Int{
        return sharedUsers.size;
    }
    public fun getNumRoom():Int{
        return roomList.size
    }
}