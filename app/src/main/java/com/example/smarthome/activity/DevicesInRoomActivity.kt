package com.example.smarthome.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthome.R
import com.example.smarthome.adapter.DeviceListAdapter
import com.example.smarthome.adapter.DeviceListRoomAdapter
import com.example.smarthome.adapter.HomeListDetailAdapter
import com.example.smarthome.databinding.ActivityDevicesInRoomBinding
import com.example.smarthome.respository.DeviceRepo
import com.example.smarthome.respository.RoomRepo

class DevicesInRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDevicesInRoomBinding;
    private lateinit var roomRepo: RoomRepo
    private lateinit var deviceRepo: DeviceRepo
    private lateinit var roomId:String
    private lateinit var homeId:String
    //Load lai du lieu khi vao quay lai act
    private var firstLoad = true;
    override fun onResume() {
        super.onResume()
        if (firstLoad) {
            firstLoad = false
        } else {
            setRoomName()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevicesInRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomRepo = RoomRepo();
        deviceRepo = DeviceRepo();

        roomId = intent.getStringExtra("roomId")?:""
        homeId = intent.getStringExtra("homeId")?:""
        setRoomName();
        setBtnAddDevice();
        setBack();
    }

    private fun setBack() {
        binding.btnBackToHome.setOnClickListener{
            finish();
        }
    }

    private fun setBtnAddDevice() {
        binding.btnAddDevice.setOnClickListener {
            val popupView = layoutInflater.inflate(R.layout.popup_add_device, null);
            val popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );

            val btnAdd = popupView.findViewById<LinearLayout>(R.id.btnAdd)

            popupWindow.isFocusable = true;
            popupWindow.showAsDropDown(it,0,0);

            btnAdd.setOnClickListener{
                popupWindow.dismiss();
                val intent = Intent(this,AddDeviceActivity::class.java);
                intent.putExtra("homeId",homeId)
                startActivity(intent);
            }
        }
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

    private fun setRoomName() {
        binding.loadingOverlay.visibility = View.VISIBLE;
        roomRepo.getRoomById(roomId,
            onResult = {room ->
                if(room != null){
                    binding.texRoomName.text = room.name
                    showDeviceList(room.deviceList)
                }
            },
            onError = {

            }
        )
    }
}