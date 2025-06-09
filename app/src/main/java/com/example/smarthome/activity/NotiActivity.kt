package com.example.smarthome.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthome.adapter.MessListAdapter
import com.example.smarthome.databinding.ActivityNotiBinding
import com.example.smarthome.model.OnItemActionListener
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.UserRepo

class NotiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotiBinding
    private lateinit var userRepo: UserRepo;
    private lateinit var homeRepo: HomeRepo

    private lateinit var adapter: MessListAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Load lai khi keo xuong
        binding.swipeRefresh.setOnRefreshListener {
            showMess();
        }

        userRepo = UserRepo();
        homeRepo = HomeRepo();

        showMess();
        goBack();
    }

    private fun goBack() {
        binding.btnBackToHome.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent);
            finish();
        }
    }

    private fun showMess() {
        binding.rvMessList.layoutManager = LinearLayoutManager(this);
        userRepo.getUserCur(
            onResult = { user ->
                if (user != null) {
                    val homeInvites = mutableListOf<String>();
                    for (home in user.homeList) {
                        if (!home.second) {
                            homeInvites.add(home.first);
                        }
                    }
                    //adapter hien danh sach tin nhan
                    adapter = MessListAdapter(homeInvites, object : OnItemActionListener {
                        override fun onAcceptClick(position: Int) {
                            binding.loadingOverlay.visibility = View.VISIBLE
                            homeRepo.getHomeById(
                                homeInvites[position],
                                onResult = { home ->
                                    if (home != null) {
                                        //SUa lai trang thai cua home trong user
                                        val homeIndex = user.homeList.indexOfFirst {
                                            it.first.equals(home.id)
                                        }
                                        user.homeList[homeIndex].second = true;
                                        userRepo.updateUser(user) { suc ->
                                            if (suc) {
                                                //Sua lai trang thai user trong home
                                                val userIndex =
                                                    home.sharedUsers.indexOfFirst {
                                                        it.first.equals(user.id)
                                                    }
                                                home.sharedUsers[userIndex].second =
                                                    true;
                                                homeRepo.addHome(home) {cess ->
                                                    if(cess){
                                                        adapter.removeItem(position)
                                                        binding.loadingOverlay.visibility = View.GONE

                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                onError =
                                {
                                    binding.loadingOverlay.visibility = View.GONE
                                }
                            )
                        }

                        override fun onDeclineClick(position: Int) {
                            binding.loadingOverlay.visibility = View.VISIBLE
                            homeRepo.getHomeById(
                                homeInvites[position],
                                onResult = { home ->
                                    if (home != null) {

                                        //Xoa home trong user
                                        user.homeList.removeIf {
                                            it.first.equals(
                                                home.id
                                            )
                                        }
                                        userRepo.updateUser(user) { suc ->
                                            if (suc) {
                                                //Xoa user trong home
                                                home.sharedUsers.removeIf {
                                                    it.first.equals(
                                                        user.id
                                                    )
                                                }
                                                homeRepo.addHome(home) {cess ->
                                                    if(cess){
                                                        adapter.removeItem(position)
                                                        binding.loadingOverlay.visibility = View.GONE

                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                onError =
                                {
                                    binding.loadingOverlay.visibility = View.GONE
                                }
                            )
                        }
                    })
                    binding.rvMessList.adapter = adapter
                }
                binding.swipeRefresh.isRefreshing=false;
            },
            onError =
            {
                binding.swipeRefresh.isRefreshing=false;
            }
        )

    }
}