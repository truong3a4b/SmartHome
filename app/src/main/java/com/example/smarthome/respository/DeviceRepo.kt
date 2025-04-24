package com.example.smarthome.respository

import com.example.smarthome.model.Device
import com.example.smarthome.model.Room
import com.google.firebase.database.FirebaseDatabase

class DeviceRepo {
    private val dbRef = FirebaseDatabase.getInstance().getReference("Device");

    fun getKey():String{
        val id = dbRef.push().key!!;
        return id;
    }
    fun addDevice(device: Device, onComplete:(Boolean) -> Unit){
        dbRef.child(device.id).setValue(device)
            .addOnCompleteListener{task ->
                onComplete(task.isSuccessful)
            }
    }
}