package com.example.smarthome.respository

import android.util.Log
import com.example.smarthome.model.Cupbo
import com.example.smarthome.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepo {
    private val auth = FirebaseAuth.getInstance();
    private val dbRef = FirebaseDatabase.getInstance().getReference("User")

    fun getUserIdCur():String{
        val idUser = auth.currentUser?.uid?:"";
        return idUser;
    }
    fun createUser(user: User,onComplete:(Boolean) -> Unit){
        dbRef.child(user.id).setValue(user)
            .addOnCompleteListener{task ->
                onComplete(task.isSuccessful)
            }
    }
    fun getUserCur( onResult: (User?) -> Unit, onError:(String) -> Unit){
        val idUser = getUserIdCur();
        dbRef.child(idUser).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                onResult(user);
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message);
            }

        })
    }
    fun updateUser(user: User, onComplete: (Boolean) -> Unit){
        dbRef.child(user.id).setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
    }
    fun addHometoUser(homeId:String,state:Boolean,onComplete: (Boolean) -> Unit){
        getUserCur(
            onResult = {user ->
                if(user != null){
                    user.homeList.add(Cupbo(homeId,state))
                    updateUser(user){success ->
                       onComplete(success)
                    }
                }else{
                    onComplete(false)
                }
            },
            onError = {err ->
                onComplete(false)
            }
        )
    }

    fun getHomeListId(onResult: (List<Cupbo>) -> Unit,onError: (String) -> Unit){
        getUserCur(
            onResult = {user ->
                if(user != null){
                    onResult(user.homeList)
                }else{
                    onResult(emptyList())
                }
            },
            onError = {err ->
                onError(err)
            }
        )

    }
    fun getUserList(userListId:List<String>,onResult: (List<User>) -> Unit, onError: (String) -> Unit){
        val resultList = mutableListOf<User>()

        if (userListId.isEmpty()) {
            onResult(emptyList())
            return
        }
        var loadedCount = 0;
        for (id in userListId) {
            dbRef.child(id).get()
                .addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        resultList.add(user)
                    }

                    loadedCount++
                    if (loadedCount == userListId.size) {
                        onResult(resultList)
                    }
                }
                .addOnFailureListener { e ->
                    onError(e.message ?: "Lỗi không xác định")
                }
        }
    }
    fun removeHomeFromUser(userListId: List<String>, homeId: String, onComplete: (Boolean) -> Unit){
        getUserList(userListId,
            onResult={userList ->
                for(user in userList){
                    user.homeList.removeIf{it.first.equals(homeId)};
                    Log.d("User","homeList: ${user.homeList.size}");
                    updateUser(user){success ->
                        onComplete(success)
                    }
                }
            },
            onError={
                Log.e("User","xoa home fail");
                onComplete(false)
            }
        )
    }
    fun getUserById(userId:String, onResult: (User?) -> Unit, onError: (String) -> Unit){
        dbRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                onResult(user);
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message);
            }

        })
    }
}