package com.example.smarthome.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthome.adapter.RoomListDetailAdapter
import com.example.smarthome.databinding.ActivitySelectRoomBinding
import com.example.smarthome.model.Device
import com.example.smarthome.model.DeviceType
import com.example.smarthome.model.Room
import com.example.smarthome.respository.DeviceRepo
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.RoomRepo

class SelectRoomActivity : AppCompatActivity() {
    private lateinit var binding :ActivitySelectRoomBinding;
    private lateinit var roomSelect:Room;
    private var roomPre:Room? = null;
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
        setBack();
    }

    private fun setBack() {
        binding.btnBackToHome.setOnClickListener{
            finish();
        }
    }

    private fun setDefault() {
        val nameDevice = intent.getStringExtra("name")?:"Noname"
        binding.edtDeviceName.setText(nameDevice);

        homeId = intent.getStringExtra("homeId") ?: "";
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.rvRoomListDetail.layoutManager = LinearLayoutManager(this)
        val macAddress = intent.getStringExtra("MAC") ?: "";
        deviceRepo.getDeviceById(macAddress,
            onResult = {dev ->
                if(dev != null){

                    roomRepo.getRoomById(dev.room,
                        onResult = {room ->
                            if(room!=null){
                                binding.edtDeviceRoom.setText(room.name);
                                roomSelect = room;
                                roomPre=room
                            }

                        },
                        onError = {

                        }
                    )
                }
                homeRepo.getRoomListId(homeId,
                    onResult = { roomListId ->

                        roomRepo.getRoomList(roomListId,
                            onResult = { roomList ->
                                if(dev==null){
                                    binding.edtDeviceRoom.setText(roomList[0].name);
                                    roomSelect = roomList[0];
                                    roomPre = null
                                }
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
            },
            onError = {}
        )

    }

    private fun setBtnAdd(){
        binding.btnAdd.setOnClickListener{
            binding.loadingOverlay.visibility = View.VISIBLE;

            val nameDevice = binding.edtDeviceName.text.toString();
            val macAddress = intent.getStringExtra("MAC") ?: "";
            val ipAddress = intent.getStringExtra("IP")?:"";
            val type = intent.getStringExtra("TYPE")?:"";
            var typeDevice = DeviceType.OTHER;
            Log.d("TYPE",type);
            if(type.equals("TV")){
                Log.d("TYPE","OKKKK");
                typeDevice = DeviceType.TV
            }else if(type.equals("LIGHT")){
                typeDevice = DeviceType.LIGHT
            };
            if(nameDevice.isNullOrEmpty()){
                binding.edtDeviceName.error=" Please enter name";
            }else{
                val idDevice = macAddress;
                val device = Device(idDevice,nameDevice,macAddress,ipAddress,roomSelect.id,typeDevice);
                deviceRepo.addDevice(device){suc ->
                    if(suc){

                        if(roomPre != null){
                            if(!roomSelect.id.equals(roomPre!!.id)){
                                roomPre!!.deviceList.removeIf { it.equals(macAddress) }
                                roomSelect.deviceList.add(device.id)
                                roomRepo.addRoom(roomPre!!){cess ->
                                    if(cess){
                                        roomRepo.addRoom(roomSelect){ce ->
                                            if(ce){
                                                binding.loadingOverlay.visibility = View.GONE;
                                                Toast.makeText(this,"Add Successfully",Toast.LENGTH_SHORT).show();
                                                startActivity(Intent(this, MainActivity::class.java))
                                                finishAffinity()
                                            }
                                        }
                                    }
                                }
                            }else{
                                binding.loadingOverlay.visibility = View.GONE;
                                Toast.makeText(this,"Add Successfully",Toast.LENGTH_SHORT).show();
                                startActivity(Intent(this, MainActivity::class.java))
                                finishAffinity()
                            }

                        }else{
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
}