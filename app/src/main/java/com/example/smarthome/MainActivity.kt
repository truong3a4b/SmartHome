package com.example.smarthome

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthome.adapter.HomeListRecycleAdapter
import com.example.smarthome.adapter.RoomListAdapter
import com.example.smarthome.databinding.ActivityMainBinding
import com.example.smarthome.model.Home
import com.example.smarthome.model.Room

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setHomePopup();
        setSelectedBar()
        showRoomList();
        setTabBar();
    }

    private fun setSelectedBar() {
        binding.tabRoom.isChecked = true;
        binding.tabDevices.isChecked = false;
        binding.tabRoom.setOnClickListener{
            binding.tabRoom.isChecked = true;
            binding.tabRoom.setTextColor(Color.parseColor("#000000"))
            binding.tabDevices.isChecked = false;
            binding.tabDevices.setTextColor(Color.parseColor("#FFAAAAAA"))

            Toast.makeText(this,"Ban chon Room",Toast.LENGTH_SHORT).show();
        }
        binding.tabDevices.setOnClickListener{
            binding.tabRoom.isChecked = false;
            binding.tabRoom.setTextColor(Color.parseColor("#FFAAAAAA"))
            binding.tabDevices.isChecked = true;
            binding.tabDevices.setTextColor(Color.parseColor("#000000"))
            Toast.makeText(this,"Ban chon Devices",Toast.LENGTH_SHORT).show();
        }
    }

    private fun setTabBar() {
        binding.tabHome.isSelected = true;
        binding.tabHome.setOnClickListener{
            binding.tabProfile.isSelected = false;
            binding.tabHome.isSelected = true;
            Toast.makeText(this,"Ban chon Home",Toast.LENGTH_SHORT).show()
        }
        binding.tabProfile.setOnClickListener{
            binding.tabProfile.isSelected = true;
            binding.tabHome.isSelected = false;
            Toast.makeText(this,"Ban chon Profile",Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRoomList() {
        binding.rvRoom.layoutManager = GridLayoutManager(this,2);
        val roomList = mutableListOf<Room>();
        roomList.add(Room("1","kitchen",R.drawable.kitchen, mutableListOf("1","2")));
        roomList.add(Room("1","kitchen",R.drawable.kitchen, mutableListOf("1","2")));
        roomList.add(Room("1","kitchen",R.drawable.kitchen, mutableListOf("1","2")));
        roomList.add(Room("1","kitchen",R.drawable.kitchen, mutableListOf("1","2")));
        roomList.add(Room("1","kitchen",R.drawable.kitchen, mutableListOf("1","2")));
        roomList.add(Room("1","kitchen",R.drawable.kitchen, mutableListOf("1","2")));
        roomList.add(Room("1","kitchen",R.drawable.kitchen, mutableListOf("1","2")));
        roomList.add(Room("1","kitchen",R.drawable.kitchen, mutableListOf("1","2")));


        binding.rvRoom.adapter = RoomListAdapter(roomList){item ->
            Toast.makeText(this, "Bạn đã chọn: ${item.name}", Toast.LENGTH_SHORT).show()
        }
    }

    fun setHomePopup(){
        binding.txtHome.setOnClickListener{
            showCustomPopup(it);
        }

    }
    fun showCustomPopup(view: View){
        val popupView = layoutInflater.inflate(R.layout.popup_home_menu,null);
        val popupWindow = PopupWindow(popupView,LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        popupWindow.isFocusable = true;
        popupWindow.showAsDropDown(view,0,0);

        val rvHomeList:RecyclerView = popupView.findViewById(R.id.rvHomeList);
        val txtHomeManager:TextView = popupView.findViewById(R.id.txtHomeManage);

        rvHomeList.layoutManager = LinearLayoutManager(this);

        val items = mutableListOf<Home>();
        items.add(Home("1","Home1","Truong", mutableListOf("1","2"), mutableListOf("1","2")));
        items.add(Home("1","Home1","Truong", mutableListOf("1","2"), mutableListOf("1","2")));
        items.add(Home("1","Home1","Truong", mutableListOf("1","2"), mutableListOf("1","2")));
        items.add(Home("1","Home1","Truong", mutableListOf("1","2"), mutableListOf("1","2")));
        items.add(Home("1","Home1","Truong", mutableListOf("1","2"), mutableListOf("1","2")));

        rvHomeList.adapter= HomeListRecycleAdapter(items){ item ->
            Toast.makeText(this, "Bạn đã chọn: ${item.name}", Toast.LENGTH_SHORT).show()
        };


        txtHomeManager.setOnClickListener{
            Toast.makeText(this, "Bạn đã chọn: home manage", Toast.LENGTH_SHORT).show()
        }
    }
}