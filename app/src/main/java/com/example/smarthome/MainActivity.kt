package com.example.smarthome

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthome.adapter.HomeListDetailAdapter
import com.example.smarthome.adapter.HomeListRecycleAdapter
import com.example.smarthome.adapter.RoomListAdapter
import com.example.smarthome.databinding.ActivityMainBinding
import com.example.smarthome.model.Home
import com.example.smarthome.model.Room
import com.example.smarthome.model.User
import com.example.smarthome.respository.DeviceRepo
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.RoomRepo
import com.example.smarthome.respository.UserRepo

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepo: UserRepo
    private lateinit var homeRepo: HomeRepo
    private lateinit var roomRepo: RoomRepo
    private lateinit var deviceRepo: DeviceRepo
    private var homeId:String = "gd"
    private var firstLoad = true;

    override fun onResume() {
        super.onResume()
        if (firstLoad) {
            firstLoad = false
        } else {
            showRoomList();
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

        setSelectedBar()
        setTabBar();
        checkNoti();
        setHomeCur();
        setAddDev();
    }

    private fun setAddDev() {
        binding.btnAddDev.setOnClickListener{
            val intent = Intent(this,AddDeviceActivity::class.java);
            intent.putExtra("homeId",homeId);
            startActivity(intent)
        }
    }

    private fun setHomeCur() {
        binding.loadingOverlay.visibility = View.VISIBLE
        userRepo.getUserCur(
            onResult = {user ->
                if(user != null){
                    homeRepo.getHomeById(user.homeCur,
                        onResult = {home ->
                            if(home != null){
                                homeId = home.id
                                showRoomList();
                            }else {

                                user.homeCur = user.homeList[0].first
                                homeId = user.homeCur
                                userRepo.updateUser(user){check ->
                                    if(check) showRoomList()
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
                        val intent = Intent(this,NotiActivity::class.java);
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
            binding.tabRoom.isChecked = true;
            binding.tabRoom.setTextColor(Color.parseColor("#000000"))
            binding.tabDevices.isChecked = false;
            binding.tabDevices.setTextColor(Color.parseColor("#FFAAAAAA"))
            binding.tabRoom.isEnabled=false;
            binding.tabDevices.isEnabled = true;
            showRoomList();
        }
        binding.tabDevices.setOnClickListener{
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
        binding.rvRoom.layoutManager = GridLayoutManager(this,2);


        homeRepo.getRoomListId(homeId,
            onResult = {roomListId ->
                roomRepo.getRoomList(roomListId,
                    onResult = { roomList ->
                        binding.rvRoom.adapter = RoomListAdapter(roomList){item ->
                            Toast.makeText(this, "Bạn đã chọn: ${item.name}", Toast.LENGTH_SHORT).show()
                        }
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
    private fun showDeviceList() {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.rvRoom.layoutManager = GridLayoutManager(this,2);


        homeRepo.getRoomListId(homeId,
            onResult = {roomListId ->
                roomRepo.getRoomList(roomListId,
                    onResult = { roomList ->
                        binding.rvRoom.adapter = RoomListAdapter(roomList){item ->
                            Toast.makeText(this, "Bạn đã chọn: ${item.name}", Toast.LENGTH_SHORT).show()
                        }

                    },
                    onError = {

                    }
                )
            },
            onError = {

            }
        )
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
            val intent = Intent(this,HomeManageActivity::class.java)
            startActivity(intent);
        }
    }

}