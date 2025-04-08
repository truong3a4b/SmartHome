package com.example.smarthome.model

class DeviceCategory(
     val id:String,
     val name:String,
     val deviceList: MutableList<String>
) {
   public fun getNumDevice():Int{
       return deviceList.size;
   }
}