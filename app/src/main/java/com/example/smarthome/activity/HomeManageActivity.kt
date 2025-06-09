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
import com.example.smarthome.adapter.HomeListDetailAdapter
import com.example.smarthome.databinding.ActivityHomeManageBinding
import com.example.smarthome.model.Cupbo
import com.example.smarthome.model.Home
import com.example.smarthome.model.Room
import com.example.smarthome.model.User
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.RoomRepo
import com.example.smarthome.respository.UserRepo


class HomeManageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeManageBinding
    private lateinit var homeRepo:HomeRepo
    private lateinit var userRepo: UserRepo
    private lateinit var roomRepo: RoomRepo
    private var firstLoad = true;

    override fun onResume() {
        super.onResume()
        if (firstLoad) {
            firstLoad = false
        } else {
            showHomeListDetail()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeManageBinding.inflate(layoutInflater)
        setContentView(binding.root);

        binding.swipeRefresh.setOnRefreshListener {
            showHomeListDetail()
        }
        //khoi tao classHomeRepo
        homeRepo = HomeRepo();
        userRepo = UserRepo();
        roomRepo = RoomRepo();
        setBackToHome();
        showAddHomeDialog();
        showHomeListDetail()

    }

    private fun showHomeListDetail() {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.rvHomeListDetail.layoutManager = LinearLayoutManager(this)
        userRepo.getHomeListId(
            onResult = { itemsHome ->
                val homeListId = mutableListOf<String>();
                for(item in itemsHome){
                    if(item.second){
                        homeListId.add(item.first);
                    }

                }
                homeRepo.getHomeList(homeListId,
                    onResult = {homeList ->
                        binding.rvHomeListDetail.adapter = HomeListDetailAdapter(homeList){item ->
                            val intent = Intent(this, HomeDetailActivity::class.java)
                            intent.putExtra("homeId",item.id);
                            intent.putExtra("numHome",homeList.size.toString())
                            startActivity(intent)
                        }
                        binding.loadingOverlay.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing=false;
                    },
                    onError = {err ->
                        binding.swipeRefresh.isRefreshing=false;
                        binding.loadingOverlay.visibility = View.GONE
                    }
                )
            },
            onError = {err->

                binding.swipeRefresh.isRefreshing=false;
                binding.loadingOverlay.visibility = View.GONE
            }
        );



    }


    private fun showAddHomeDialog() {
        binding.btnAddHome.setOnClickListener{
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_home,null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create();

            val edtHomeName = dialogView.findViewById<EditText>(R.id.edtHomeName);
            val btnSaveAdd = dialogView.findViewById<Button>(R.id.btnSaveHomeAdd);
            val btnOutAddHomeDialog = dialogView.findViewById<ImageButton>(R.id.btnOutAddHomeDialog)

            //Luu ten nha vao database
            btnSaveAdd.setOnClickListener{
                val name = edtHomeName.text.toString();
                if(name.isNullOrEmpty()){
                    edtHomeName.error = "Please enter name"
                }else{
                    val idHome = homeRepo.getKey();
                    val idUser = userRepo.getUserIdCur();
                    val room = Room(roomRepo.getKey(),"Room", mutableListOf());
                    roomRepo.addRoom(room){suc ->
                        if(suc){
                            val home = Home(idHome,name,idUser, mutableListOf(room.id), mutableListOf(Cupbo(idUser,true)))
                            homeRepo.addHome(home){success ->
                                if(success){
                                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show()
                                    //update home vao user
                                    userRepo.addHometoUser(idHome,true){check ->
                                        if (check) showHomeListDetail()
                                    }

                                } else {
                                    Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    dialog.dismiss();
                }
            }

            btnOutAddHomeDialog.setOnClickListener{
                dialog.dismiss();
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }



    private fun setBackToHome() {
        binding.btnBackToHome.setOnClickListener{
            finish();
        }
    }
}