package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthome.adapter.RoomListDetailAdapter
import com.example.smarthome.databinding.ActivitySelectRoomBinding
import com.example.smarthome.model.Device
import com.example.smarthome.model.Room
import com.example.smarthome.respository.DeviceRepo
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.RoomRepo

class SelectRoomActivity : AppCompatActivity() {
    private lateinit var binding :ActivitySelectRoomBinding;
    private lateinit var roomSelect:Room;
    private lateinit var homeId:String;
    private lateinit var roomRepo: RoomRepo;
    private lateinit var homeRepo:HomeRepo;
    private lateinit var deviceRepo:DeviceRepo;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectRoomBinding.inflate(layoutInflater);
        setContentView(binding.root);

        roomRepo = RoomRepo();
        homeRepo = HomeRepo();
        deviceRepo = DeviceRepo();
        setDefault();
        setBtnAdd();
    }

    private fun setDefault() {
        val nameDevice = intent.getStringExtra("name")?:"Noname"
        binding.edtDeviceName.setText(nameDevice);

        homeId = intent.getStringExtra("homeId") ?: "";
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.rvRoomListDetail.layoutManager = LinearLayoutManager(this)
        homeRepo.getRoomListId(homeId,
            onResult = { roomListId ->

                roomRepo.getRoomList(roomListId,
                    onResult = { roomList ->
                        binding.edtDeviceRoom.setText(roomList[0].name);
                        roomSelect = roomList[0];
                        binding.rvRoomListDetail.adapter = RoomListDetailAdapter(roomList) { item ->
                            binding.edtDeviceRoom.setText(item.name);
                            roomSelect = item;
                        }
                        binding.loadingOverlay.visibility = View.GONE

                    },
                    onError = { err ->
                        Log.e("Home", "Lỗi: $err")

                        binding.loadingOverlay.visibility = View.GONE
                    }
                )
            },
            onError = {err->
                Log.e("User", "Lỗi: $err")
                binding.loadingOverlay.visibility = View.GONE
            }
        )
    }

    private fun setBtnAdd(){
        binding.btnAdd.setOnClickListener{
            binding.loadingOverlay.visibility = View.VISIBLE;
            val nameDevice = binding.edtDeviceName.text.toString();
            val macAddress = intent.getStringExtra("MAC") ?: "";
            val ipAddress = intent.getStringExtra("IP")?:"";
            if(nameDevice.isNullOrEmpty()){
                binding.edtDeviceName.error=" Please enter name";
            }else{
                val idDevice = deviceRepo.getKey();
                val device = Device(idDevice,nameDevice,macAddress,ipAddress);
                deviceRepo.addDevice(device){suc ->
                    if(suc){
                        roomSelect.deviceList.add(device.id)
                        roomRepo.addRoom(roomSelect){cess ->
                            if(cess){
                                binding.loadingOverlay.visibility = View.GONE;
                                Toast.makeText(this,"Add Successfully",Toast.LENGTH_SHORT).show();
                                startActivity(Intent(this, MainActivity::class.java))
                                finishAffinity()
                            }
                        }
                    }
                }
            }
        }
    }
}