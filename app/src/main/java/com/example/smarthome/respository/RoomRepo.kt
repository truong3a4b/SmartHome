package com.example.smarthome.respository

import com.example.smarthome.model.Home
import com.example.smarthome.model.Room
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RoomRepo {
    private val dbRef = FirebaseDatabase.getInstance().getReference("Room");

    fun getKey():String{
        val id = dbRef.push().key!!;
        return id;
    }
    fun addRoom(room: Room, onComplete:(Boolean) -> Unit){
        dbRef.child(room.id).setValue(room)
            .addOnCompleteListener{task ->
                onComplete(task.isSuccessful)
            }
    }
    fun getRoomList(roomListId: List<String>,onResult: (List<Room>) -> Unit, onError: (String) -> Unit){
        val resultList = mutableListOf<Room>()

        if (roomListId.isEmpty()) {
            onResult(emptyList())
            return
        }
        var loadedCount = 0;
        for (id in roomListId) {
            dbRef.child(id).get()
                .addOnSuccessListener { snapshot ->
                    val room = snapshot.getValue(Room::class.java)
                    if (room != null) {
                        resultList.add(room)
                    }

                    loadedCount++
                    if (loadedCount == roomListId.size) {
                        onResult(resultList)
                    }
                }
                .addOnFailureListener { e ->
                    onError(e.message ?: "Lỗi không xác định")
                }
        }
    }

    fun getRoomById(roomId:String, onResult:(Room?) -> Unit, onError:(String) -> Unit){
        dbRef.child(roomId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue(Room::class.java)
                onResult(room)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
    }

    fun deleteRoom(roomId: String,homeId:String, onComplete: (Boolean) -> Unit){
        val homeRepo = HomeRepo();
        val deviceRepo = DeviceRepo();

        homeRepo.getHomeById(homeId,
            onResult = { home ->
                if(home != null){
                    home.roomList.removeIf { it.equals(roomId) }
                    homeRepo.addHome(home){suc ->
                        if(suc){
                            getRoomById(roomId,
                                onResult = {room ->
                                    if(room != null){
                                        val dbRefDev = FirebaseDatabase.getInstance().getReference("Device");
                                        for(item in room.deviceList){
                                            dbRefDev.child(item).removeValue()
                                                .addOnCompleteListener{tak ->
                                                    dbRef.child(roomId).removeValue()
                                                        .addOnCompleteListener{task ->
                                                            onComplete(task.isSuccessful);
                                                        }
                                                }

                                        }


                                    }
                                },
                                onError = {

                                }
                            )

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
}