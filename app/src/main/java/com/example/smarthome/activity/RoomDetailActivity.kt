package com.example.smarthome.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthome.R
import com.example.smarthome.adapter.DeviceListRoomAdapter
import com.example.smarthome.databinding.ActivityRoomDetailBinding
import com.example.smarthome.respository.DeviceRepo
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.RoomRepo
import com.example.smarthome.respository.UserRepo


class RoomDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoomDetailBinding
    private lateinit var roomId:String
    private lateinit var homeId:String
    private lateinit var roomRepo: RoomRepo
    private lateinit var homeRepo: HomeRepo;
    private lateinit var userRepo: UserRepo;
    private lateinit var deviceRepo: DeviceRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomDetailBinding.inflate(layoutInflater);
        setContentView(binding.root);

        roomId = intent.getStringExtra("roomId")?:""
        homeId = intent.getStringExtra("homeId") ?: "";

        roomRepo = RoomRepo();
        homeRepo = HomeRepo();
        userRepo = UserRepo();
        deviceRepo = DeviceRepo();


        setRoomName();
        checkOwner();
        showEditNameRoom();
        setBtnAddDev();
        setDeleteBtn();
        setBack();
    }

    private fun setBtnAddDev() {
        binding.btnAddDev.setOnClickListener{
            val intent = Intent(this,AddDeviceActivity::class.java);
            intent.putExtra("homeId",homeId)
            startActivity(intent);
        }
    }


    private fun setBack() {
        binding.btnBackToRoomManage.setOnClickListener{
            finish()
        }
    }

    private fun setDeleteBtn() {
        binding.btnDeleteRoom.setOnClickListener {
            binding.loadingOverlay.visibility = View.VISIBLE
            roomRepo.deleteRoom(roomId,homeId) { success ->
                if (success) {
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(this, "Da xoa", Toast.LENGTH_SHORT).show()
                    finish();
                } else {
                    return@deleteRoom
                }
            }
        }
    }

    private fun showEditNameRoom() {
        binding.btnEditRoomName.setOnClickListener{
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_room, null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create();

            val edtRoomName = dialogView.findViewById<EditText>(R.id.edtRoomName);
            val btnSaveRoomEdit = dialogView.findViewById<Button>(R.id.btnSaveRoomEdit);
            val btnOutDialog = dialogView.findViewById<ImageButton>(R.id.btnOutDialog)
            val loadingOverlay = dialogView.findViewById<FrameLayout>(R.id.loadingOverlay1)

            roomRepo.getRoomById(roomId,
                onResult = {room ->
                    if(room != null){
                        edtRoomName.setText(room.name);
                    }

                },
                onError = {

                }
            )

            btnSaveRoomEdit.setOnClickListener {
                loadingOverlay.visibility = View.VISIBLE
                val name = edtRoomName.text.toString();
                if (name.isNullOrEmpty()) {
                    edtRoomName.error = "Please enter name"
                } else {

                    roomRepo.getRoomById(roomId,
                        onResult = { room ->
                            if (room != null) {
                                room.name = name;
                                roomRepo.addRoom(room) { success ->
                                    if (success) {
                                        Toast.makeText(this, "Sua thành công", Toast.LENGTH_SHORT)
                                            .show()
                                        loadingOverlay.visibility = View.GONE
                                        dialog.dismiss();
                                    } else {
                                        loadingOverlay.visibility = View.GONE
                                        Toast.makeText(this, "sua thất bại", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            }
                        },
                        onError = {
                            loadingOverlay.visibility = View.VISIBLE
                        }
                    )

                }
            }

            btnOutDialog.setOnClickListener {
                dialog.dismiss();
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }

    private fun checkOwner() {
        binding.loadingOverlay.visibility = View.VISIBLE
        homeRepo.getHomeById(homeId,
            onResult = {home ->
                if(home != null){
                    //La chu nha
                    if(userRepo.getUserIdCur().equals(home.ownerId)){
                        binding.btnAddDev.visibility = View.VISIBLE
                        binding.btnEditRoomName.isClickable = true
                        //NEu so luong =1 thi ko cho xoa
                        checkNumRoom()
                    }else{
                        //Ko phai chu nha
                        binding.btnAddDev.visibility = View.GONE
                        binding.btnDeleteRoom.visibility = View.GONE
                        binding.btnEditRoomName.isClickable = false
                    }
                }
                binding.loadingOverlay.visibility = View.GONE
            },
            onError = {

            }
        )
    }
    fun checkNumRoom(){
        val num = intent.getStringExtra("numRoom")?:""
        val numRoom = num.toInt();
        if(numRoom < 2){
            binding.btnDeleteRoom.visibility = View.GONE
        }else{
            binding.btnDeleteRoom.visibility = View.VISIBLE
        }
    }
    private fun setRoomName() {
        roomRepo.getRoomById(roomId,
            onResult = {room ->
                if(room != null){
                    binding.txtRoomName.text = room.name
                    showDeviceList(room.deviceList)
                }
            },
            onError = {

            }
        )
    }
    private fun showDeviceList(deviceListId:List<String>) {
        binding.rvDeviceList.layoutManager = LinearLayoutManager(this)
        deviceRepo.getDeviceList(deviceListId,
            onResult = {deviceList ->
                binding.rvDeviceList.adapter = DeviceListRoomAdapter(deviceList){item ->
                    val intent = Intent(this, LightDeviceActivity::class.java)
                    intent.putExtra("IP",item.ipAddress);
                    intent.putExtra("MAC",item.macAddress);
                    startActivity(intent)
                }
                binding.loadingOverlay.visibility = View.GONE
            },
            onError = {

            }
        )
    }
}