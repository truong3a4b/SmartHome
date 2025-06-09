package com.example.smarthome.respository

import com.example.smarthome.model.Device
import com.example.smarthome.model.Room
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
    fun getDeviceList(deviceListId: List<String>,onResult: (List<Device>) -> Unit, onError: (String) -> Unit){
        val resultList = mutableListOf<Device>()

        if (deviceListId.isEmpty()) {
            onResult(emptyList())
            return
        }
        var loadedCount = 0;
        for (id in deviceListId) {
            dbRef.child(id).get()
                .addOnSuccessListener { snapshot ->
                    val device = snapshot.getValue(Device::class.java)
                    if (device != null) {
                        resultList.add(device)
                    }

                    loadedCount++
                    if (loadedCount == deviceListId.size) {
                        onResult(resultList)
                    }
                }
                .addOnFailureListener { e ->
                    onError(e.message ?: "Lỗi không xác định")
                }
        }
    }

    fun getDeviceById(deviceId:String, onResult:(Device?) -> Unit, onError:(String) -> Unit){
        dbRef.child(deviceId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dev = snapshot.getValue(Device::class.java)
                onResult(dev)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
    }
    fun deleteDevice(deviceId: String, onComplete: (Boolean) -> Unit){
        val roomRepo = RoomRepo();

        getDeviceById(deviceId,
            onResult = {device ->
                if(device != null){
                    val roomId = device.room
                    roomRepo.getRoomById(roomId,
                        onResult = { room ->
                            if(room != null){
                                room.deviceList.removeIf { it.equals(deviceId) }
                                roomRepo.addRoom(room){suc ->
                                    if(suc){
                                        dbRef.child(deviceId).removeValue()
                                            .addOnCompleteListener{task ->
                                                onComplete(task.isSuccessful);
                                            }
                                    }else{
                                        onComplete(false)
                                    }
                                }
                            } else{
                                onComplete(false)
                            }

                        },
                        onError = {
                            onComplete(false);
                        }
                    )
                }

            },
            onError = {}
        )


    }
}