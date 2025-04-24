package com.example.smarthome.respository

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthome.adapter.UserListAdapter
import com.example.smarthome.model.Cupbo
import com.example.smarthome.model.Home
import com.example.smarthome.model.Room
import com.example.smarthome.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.tan

class HomeRepo {
    private val dbRef = FirebaseDatabase.getInstance().getReference("Home")

    fun getHomeById(homeId:String, onResult: (Home?) -> Unit, onError:(String) -> Unit){
        dbRef.child(homeId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val home = snapshot.getValue(Home::class.java)
                    onResult(home)
                } else{
                    onResult(null)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
    }

    fun getHomeList(homeListId: List<String>,onResult: (List<Home>) -> Unit, onError: (String) -> Unit){
        val resultList = mutableListOf<Home>()

        if (homeListId.isEmpty()) {
            onResult(emptyList())
            return
        }
        var loadedCount = 0;
        for (id in homeListId) {
            dbRef.child(id).get()
                .addOnSuccessListener { snapshot ->
                    val home = snapshot.getValue(Home::class.java)
                    if (home != null) {
                        resultList.add(home)
                    }

                    loadedCount++
                    if (loadedCount == homeListId.size) {
                        onResult(resultList)
                    }
                }
                .addOnFailureListener { e ->
                    onError(e.message ?: "Lỗi không xác định")
                }
        }
    }
    fun addHome(home: Home,onComplete:(Boolean) -> Unit){
        dbRef.child(home.id).setValue(home)
            .addOnCompleteListener{task ->
                onComplete(task.isSuccessful)
            }
    }
    fun getKey():String{
        val id = dbRef.push().key!!;
        return id;
    }

    fun getUserListId(homeId: String, onResult: (List<Cupbo>) -> Unit,onError: (String) -> Unit){
        getHomeById(homeId,
            onResult = {home ->
                if(home != null){
                    onResult(home.sharedUsers)
                }else{
                    onResult(mutableListOf())
                }
            },
            onError = {err ->
                onError(err)
            }
        )
    }
    fun deleteHome(homeId: String, onComplete: (Boolean) -> Unit){
        getUserListId(homeId,
            onResult = { userList ->
                val userListId = mutableListOf<String>();
                for(item in userList){
                    userListId.add(item.first);
                }
                val userRepo = UserRepo();
                userRepo.removeHomeFromUser(userListId,homeId){success ->
                    dbRef.child(homeId).removeValue()
                        .addOnCompleteListener{task ->
                            onComplete(task.isSuccessful);
                        }
                }
            },
            onError = {
                onComplete(false);
            }
        )

    }
    fun getOwnerOfHome(homeId:String, onResult: (User) -> Unit, onError: (String) -> Unit){
        getHomeById(homeId,
            onResult ={home ->
                if(home != null){
                    val userRepo = UserRepo();
                    userRepo.getUserById(home.ownerId,
                        onResult={user ->
                            if(user != null) {
                                onResult(user)
                            }
                        },
                        onError={err ->
                            onError(err)
                        }
                    )
                }
            },
            onError={err ->
                onError(err)
            }
        )
    }
    fun getRoomListId(homeId: String, onResult: (List<String>) -> Unit, onError: (String) -> Unit){
        getHomeById(homeId,
            onResult = {home ->
                if(home != null){
                    onResult(home.roomList)
                }else{
                    onResult(mutableListOf())
                }
            },
            onError = {err ->
                onError(err)
            }
        )
    }
}