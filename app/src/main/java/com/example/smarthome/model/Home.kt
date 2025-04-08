package com.example.smarthome.model

class Home(
    val id:String,
    val name:String,
    val ownerId:String,
    val roomList:MutableList<String>,
    val sharedUsers:MutableList<String>
) {
    public fun getNumUser():Int{
        return sharedUsers.size;
    }
}