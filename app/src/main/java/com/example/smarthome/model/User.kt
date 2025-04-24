package com.example.smarthome.model


class User(
    var id:String="",
    var email:String="",
    var name:String = "",
    var homeList:MutableList<Cupbo> = mutableListOf(),
    var homeCur:String = "ttt"

) {
    public fun getNumHome():Int{
        return homeList.size
    }
}