package com.example.smarthome.model


class User(
    val id:String,
    val userName:String,
    val password:String,
    val email:String,
    val realName:String,
    val homeList:MutableList<String>
) {
    public fun getNumHome():Int{
        return homeList.size
    }
}