package com.example.smarthome.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthome.R
import com.example.smarthome.adapter.RoomListDetailAdapter
import com.example.smarthome.databinding.ActivityRoomManageBinding
import com.example.smarthome.model.Room
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.RoomRepo
import com.example.smarthome.respository.UserRepo

class RoomManageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoomManageBinding
    private lateinit var roomRepo:RoomRepo
    private lateinit var homeId: String
    private lateinit var homeRepo:HomeRepo
    private lateinit var userRepo:UserRepo
    private var firstLoad = true;
    override fun onResume() {
        super.onResume()
        if (firstLoad) {
            firstLoad = false
        } else {
            showRoomListDetail()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomManageBinding.inflate(layoutInflater);
        setContentView(binding.root)

        homeId = intent.getStringExtra("homeId") ?: "";

        roomRepo = RoomRepo();
        homeRepo = HomeRepo();
        userRepo = UserRepo();

        binding.swipeRefresh.setOnRefreshListener {
            showRoomListDetail()
        }

        checkOwner();
        showAddRoomDialog();
        showRoomListDetail()
        setBack();
    }

    private fun showRoomListDetail() {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.rvRoomListDetail.layoutManager = LinearLayoutManager(this)
        homeRepo.getRoomListId(homeId,
            onResult = { roomListId ->
                roomRepo.getRoomList(roomListId,
                    onResult = { roomList ->
                        binding.rvRoomListDetail.adapter = RoomListDetailAdapter(roomList) { item ->
                            val intent = Intent(this, RoomDetailActivity::class.java);
                            intent.putExtra("roomId",item.id);
                            intent.putExtra("homeId",homeId);
                            intent.putExtra("numRoom",roomList.size.toString());
                            startActivity(intent);
                        }
                        binding.loadingOverlay.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false;
                    },
                    onError = { err ->
                        Log.e("Home", "Lỗi: $err")
                        binding.swipeRefresh.isRefreshing = false;
                        binding.loadingOverlay.visibility = View.GONE
                    }
                )
            },
            onError = {err->
                Log.e("User", "Lỗi: $err")
                binding.swipeRefresh.isRefreshing=false;
                binding.loadingOverlay.visibility = View.GONE
            }
        );
    }

    private fun checkOwner() {
        homeRepo.getHomeById(homeId,
            onResult = {home ->
                if(home != null){
                    //La chu nha
                    if(userRepo.getUserIdCur().equals(home.ownerId)){
                        binding.btnAddRoom.visibility = View.VISIBLE

                    }else{
                        //Ko phai chu nha
                        binding.btnAddRoom.visibility = View.GONE

                    }
                }

            },
            onError = {

            }
        )
    }

    private fun setBack() {
        binding.btnBackToHome.setOnClickListener{
            finish();
        }
    }

    private fun showAddRoomDialog() {
        binding.btnAddRoom.setOnClickListener{
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_room,null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create();

            val edtRoomName = dialogView.findViewById<EditText>(R.id.edtRoomName);
            val btnSaveAdd = dialogView.findViewById<Button>(R.id.btnSaveHomeAdd);
            val btnOutAddRoomDialog = dialogView.findViewById<ImageButton>(R.id.btnOutAddRoomDialog)

            //Luu ten phong vao database
            btnSaveAdd.setOnClickListener{
                val name = edtRoomName.text.toString();
                if(name.isNullOrEmpty()){
                    edtRoomName.error = "Please enter name"
                }else{
                    val idRoom = roomRepo.getKey();
                    val room = Room(idRoom,name, mutableListOf())
                    roomRepo.addRoom(room){ success ->
                        if(success){
                            Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show()
                            //update room vao home
                            homeRepo.getHomeById(homeId,
                                onResult = {home ->
                                    if(home != null){
                                        home.roomList.add(room.id);
                                        homeRepo.addHome(home){check ->
                                            if (check) showRoomListDetail()
                                        }
                                    }
                                },
                                onError = {

                                }
                            )


                        } else {
                            Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show()
                        }
                    }


                    dialog.dismiss();
                }
            }

            btnOutAddRoomDialog.setOnClickListener{
                dialog.dismiss();
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }
}