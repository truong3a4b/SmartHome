package com.example.smarthome.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smarthome.R
import com.example.smarthome.adapter.DeviceListHAdapter
import com.example.smarthome.adapter.HomeListRecycleAdapter
import com.example.smarthome.adapter.RoomListAdapter
import com.example.smarthome.databinding.ActivityMainBinding
import com.example.smarthome.model.Device
import com.example.smarthome.model.DeviceStatus
import com.example.smarthome.model.DeviceType
import com.example.smarthome.model.RetrofitClient
import com.example.smarthome.model.Room
import com.example.smarthome.model.WeatherResponse
import com.example.smarthome.respository.DeviceRepo
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.RoomRepo
import com.example.smarthome.respository.UserRepo
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import retrofit2.Callback as RetrofitCallback
import retrofit2.Call as RetrofitCall// Đây là Callback của Retrofit
import retrofit2.Response as RetrofitResponse
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepo: UserRepo
    private lateinit var homeRepo: HomeRepo
    private lateinit var roomRepo: RoomRepo
    private lateinit var deviceRepo: DeviceRepo
    private var homeId:String = "gd"
    private val apiKey = "ee5c4cb8508212000c1a92103daf0738"//api key thoi tiet
    private var tabRoom = true;
    private lateinit var deviceAdapter:DeviceListHAdapter;
    private val client = OkHttpClient()

    //Load lai du lieu khi vao quay lai act
    private var firstLoad = true;
    override fun onResume() {
        super.onResume()
        if (firstLoad) {
            firstLoad = false
        } else {
            if(tabRoom) showRoomList();
            else showDeviceList();
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepo = UserRepo();
        homeRepo = HomeRepo();
        roomRepo = RoomRepo();
        deviceRepo = DeviceRepo();
        //Load lai khi vuot xuong
        binding.swipeRefresh.setOnRefreshListener {
            checkNoti();
            setHomeCur()
        }

        setSelectedBar()//room or device
        setTabBar();//home or profile
        checkNoti();
        setHomeCur();
        setAddDev();
        getWeather("Hanoi")
    }


    private fun getWeather(city: String) {
        RetrofitClient.instance.getCurrentWeather(city, apiKey)
            .enqueue(object : RetrofitCallback<WeatherResponse> {
                override fun onResponse(
                    call: RetrofitCall<WeatherResponse>,
                    response: RetrofitResponse<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val weather = response.body()
                        val temp = weather?.main?.temp
                        val cityName = weather?.name
                        val condition = weather?.weather?.get(0)?.main
                        val iconCode = weather?.weather?.get(0)?.icon
                        val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                        binding.txtWeather.text = condition;
                        binding.txtTemp.text = temp?.let { "%.1f".format(it) } ?: "N/A"
                        binding.txtLocation.text = cityName;
                        Glide.with(this@MainActivity)
                            .load(iconUrl)
                            .into(binding.imgWeather)
                    }
                }

                override fun onFailure(call: RetrofitCall<WeatherResponse>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Lỗi: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    private fun setAddDev() {
        binding.btnAddDev.setOnClickListener{
            val popupView = layoutInflater.inflate(R.layout.popup_add_device,null);
            val popupWindow = PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            val btnAddBlt = popupView.findViewById<LinearLayout>(R.id.btnAddBlt)
            val btnAddWifi = popupView.findViewById<LinearLayout>(R.id.btnAddWifi)

            popupWindow.isFocusable = true;
            popupWindow.showAsDropDown(it,0,0);

            btnAddBlt.setOnClickListener{
                val intent = Intent(this, AddDeviceActivity::class.java);
                intent.putExtra("homeId",homeId);
                startActivity(intent)
            }

            btnAddWifi.setOnClickListener{
                val intent = Intent(this, APmodeActivity::class.java);
                intent.putExtra("homeId",homeId);
                startActivity(intent)
            }
        }
    }

    //Kiem tra nha hien tai, load lai phong hoac nha
    private fun setHomeCur() {
        binding.loadingOverlay.visibility = View.VISIBLE
        userRepo.getUserCur(
            onResult = {user ->
                if(user != null){
                    homeRepo.getHomeById(user.homeCur,
                        onResult = {home ->
                            if(home != null){
                                homeId = home.id
                                binding.txtHome.text = home.name
                                if(tabRoom)showRoomList();
                                else showDeviceList();
                            }else {

                                user.homeCur = user.homeList[0].first
                                homeId = user.homeCur
                                userRepo.updateUser(user){check ->
                                    if(check){
                                        if(tabRoom) showRoomList()
                                        else showDeviceList();
                                    }

                                }
                            }
                            setHomePopup();
                        },
                        onError = {

                        }
                    )
                }

            },
            onError = {

            }
        )
    }


    private fun checkNoti() {
        userRepo.getUserCur(
            onResult = {user ->
                if(user != null){
                    val homeInvites = mutableListOf<String>();
                    for(home in user.homeList){
                        if(!home.second){
                            homeInvites.add(home.first);
                        }
                    }
                    if(homeInvites.size > 0){
                        binding.badgeDot.visibility = View.VISIBLE
                    }else{
                        binding.badgeDot.visibility = View.GONE;
                    }

                    binding.btnNoti.setOnClickListener{
                        val intent = Intent(this, NotiActivity::class.java);
                        startActivity(intent)
                    }
                }

            },
            onError = {

            }
        )
    }


    private fun setSelectedBar() {
        binding.tabRoom.isChecked = true;
        binding.tabDevices.isChecked = false;
        binding.tabRoom.setOnClickListener{
            tabRoom = true;
            binding.tabRoom.isChecked = true;
            binding.tabRoom.setTextColor(Color.parseColor("#000000"))
            binding.tabDevices.isChecked = false;
            binding.tabDevices.setTextColor(Color.parseColor("#FFAAAAAA"))
            binding.tabRoom.isEnabled=false;
            binding.tabDevices.isEnabled = true;
            showRoomList();
        }
        binding.tabDevices.setOnClickListener{
            tabRoom = false;
            binding.tabRoom.isChecked = false;
            binding.tabRoom.setTextColor(Color.parseColor("#FFAAAAAA"))
            binding.tabDevices.isChecked = true;
            binding.tabDevices.setTextColor(Color.parseColor("#000000"))
            binding.tabRoom.isEnabled=true;
            binding.tabDevices.isEnabled = false;
            showDeviceList();
        }
    }


    private fun setTabBar() {
        binding.tabHome.isSelected = true;
        binding.tabProfile.setOnClickListener{
            binding.tabProfile.isSelected = true;
            binding.tabHome.isSelected = false;
            startActivity(Intent(this, ProfileActivity::class.java));
            finish();
        }
    }

    private fun showRoomList() {
        binding.loadingOverlay.visibility = View.VISIBLE

        val gridLayoutManager = GridLayoutManager(this,2)


        homeRepo.getRoomListId(homeId,
            onResult = {roomListId ->
                roomRepo.getRoomList(roomListId,
                    onResult = { roomList ->

                        binding.rvRoom.layoutManager = gridLayoutManager;
                        binding.rvRoom.adapter = RoomListAdapter(roomList,
                            onItemClick = {item ->
                                val intent = Intent(this, RoomDetailActivity::class.java);
                                intent.putExtra("homeId",homeId);
                                intent.putExtra("roomId",item.id);
                                intent.putExtra("numRoom",roomList.size.toString())
                                startActivity(intent);
                            },
                            onAddClick = {
                                showDialogAddRoom();
                            })
                        binding.loadingOverlay.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing=false;
                    },
                    onError = {

                    }
                )
            },
            onError = {

            }
        )


    }

    private fun showDialogAddRoom() {
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
                                        if (check) showRoomList()
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


    private fun showDeviceList() {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.rvRoom.layoutManager = GridLayoutManager(this,2);
        val deviceList = mutableListOf<Device>()
        deviceAdapter = DeviceListHAdapter(deviceList){device ->
            var intent:Intent? = null;
            if(device.type==DeviceType.LIGHT){
                intent = Intent(this,LightDeviceActivity::class.java);
            }
            if(device.type == DeviceType.TV){
                intent = Intent(this,TVControlActivity::class.java);
            }
            if(intent!=null){
                intent.putExtra("IP",device.ipAddress);
                intent.putExtra("MAC",device.macAddress);
                startActivity(intent);
            }



        }
        binding.rvRoom.adapter = deviceAdapter;
        homeRepo.getRoomListId(homeId,
            onResult = {roomListId ->
                roomRepo.getRoomList(roomListId,
                    onResult = { roomList ->

                        for(room in roomList){
                            deviceRepo.getDeviceList(room.deviceList,
                                onResult = {itemList ->
                                    checkDevice(itemList);
                                    deviceList.addAll(itemList);
                                    deviceAdapter.notifyItemInserted(deviceList.size-1);
                                },
                                onError = {

                                }
                            )
                        }
                        binding.loadingOverlay.visibility = View.GONE


                    },
                    onError = {

                    }
                )
            },
            onError = {

            }
        )
    }
    private fun checkDevice(devices : List<Device>) {
        for(dev in devices){
            val ip = dev.ipAddress;
            val url = "http://$ip/check";

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread{
                        if(dev.status.compareTo(DeviceStatus.DISCONNECTED)!=0){
                            dev.status = DeviceStatus.DISCONNECTED;
                            deviceRepo.addDevice(dev){}
                            deviceAdapter.notifyDataSetChanged()
                        }

                    }
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()

                    runOnUiThread{
                        if(response.isSuccessful){
                            if(dev.status.compareTo(DeviceStatus.CONNECTED)!=0){
                                dev.status = DeviceStatus.CONNECTED;
                                deviceRepo.addDevice(dev){}
                                deviceAdapter.notifyDataSetChanged()
                            }

                        }else{
                            if(dev.status.compareTo(DeviceStatus.DISCONNECTED)!=0){
                                dev.status = DeviceStatus.DISCONNECTED;
                                deviceRepo.addDevice(dev){}
                                deviceAdapter.notifyDataSetChanged()

                            }
                        }

                    }
                    response.close();
                }

            })
        }
    }
    fun setHomePopup(){
        binding.btnShowHome.setOnClickListener{
            showCustomPopup(it);
        }

    }
    fun showCustomPopup(view: View){
        val popupView = layoutInflater.inflate(R.layout.popup_home_menu,null);
        val popupWindow = PopupWindow(popupView,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        popupWindow.isFocusable = true;
        popupWindow.showAsDropDown(view,0,0);

        val rvHomeList:RecyclerView = popupView.findViewById(R.id.rvHomeList);
        val txtHomeManager:LinearLayout = popupView.findViewById(R.id.txtHomeManage);

        rvHomeList.layoutManager = LinearLayoutManager(this);

        userRepo.getHomeListId(
            onResult = { itemsHome ->
                val homeListId = mutableListOf<String>();
                for(item in itemsHome){
                    if(item.second){
                        homeListId.add(item.first);
                        Log.d("home","${item.first}")
                    }

                }
                homeRepo.getHomeList(homeListId,
                    onResult = {homeList ->
                        val selectionIndex = homeListId.indexOfFirst { it.equals(homeId) }
                        rvHomeList.adapter= HomeListRecycleAdapter(homeList,selectionIndex){ item ->
                            homeId = item.id;
                            userRepo.getUserCur(
                                onResult = { user ->
                                    if(user != null){
                                        user.homeCur = item.id
                                        userRepo.updateUser(user){c ->
                                            if(c) setHomeCur()
                                        }
                                    }
                                },
                                onError = {

                                }
                            )
                        };

                    },
                    onError = {err ->
                        Log.e("Home", "Lỗi: $err")

                    }
                )
            },
            onError = {err->
                Log.e("User", "Lỗi: $err")

            }
        );




        txtHomeManager.setOnClickListener{
            val intent = Intent(this, HomeManageActivity::class.java)
            startActivity(intent);
        }
    }

}