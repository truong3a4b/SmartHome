package com.example.smarthome.model

class Device(
    val id:String,
    val name:String,
    val image:Int,
    val categoryId:String,
    val categoryName:String,
    val status:String,
    val shareUsers:List<String>
) {
}