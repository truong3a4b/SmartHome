package com.example.smarthome.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smarthome.R
import com.example.smarthome.adapter.UserListAdapter
import com.example.smarthome.databinding.ActivityHomeDetailBinding
import com.example.smarthome.model.Cupbo
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.UserRepo

class HomeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeDetailBinding
    private lateinit var homeRepo: HomeRepo;
    private lateinit var userRepo: UserRepo;
    private lateinit var homeId: String

    private var firstLoad = true;

    override fun onResume() {
        super.onResume()
        if (firstLoad) {
            firstLoad = false
        } else {
            showMember()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeRepo = HomeRepo();
        userRepo = UserRepo();


        homeId = intent.getStringExtra("homeId") ?: "";

        binding.swipeRefresh.setOnRefreshListener {
            showMember();
        }
        setHomeName();
        showMember();
        setBack();
        setDeleteBtn();
        setLeaveBtn()
        showEditHomeName();
        checkOwner();
        setInviteMember();
        setRoomBtn();
    }



    private fun setRoomBtn() {
        binding.btnRoomDev.setOnClickListener {
            val intent = Intent(this, RoomManageActivity::class.java);
            intent.putExtra("homeId", homeId);
            startActivity(intent);
        }
    }

    private fun setLeaveBtn() {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.btnLeaveHome.setOnClickListener {
            userRepo.getUserCur(
                onResult = { user ->
                    if (user != null) {
                        homeRepo.getHomeById(homeId,
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
                                            homeRepo.addHome(home) { cess ->
                                                if (cess) {
                                                    binding.loadingOverlay.visibility = View.GONE
                                                    Toast.makeText(
                                                        this,
                                                        "Đã rời",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    finish();
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            onError = {
                                binding.loadingOverlay.visibility = View.GONE
                            }
                        )
                    }
                },
                onError = {
                    binding.loadingOverlay.visibility = View.GONE
                }
            )
        }
    }

    private fun checkOwner() {
        homeRepo.getHomeById(homeId,
            onResult = { home ->
                if (home != null) {
                    //La chu nha
                    if (userRepo.getUserIdCur().equals(home.ownerId)) {
                        binding.btnInvite.visibility = View.VISIBLE
                        binding.btnLeaveHome.visibility = View.GONE
                        binding.btnEditHomeName.isClickable = true
                        checkNumHome();
                    } else {
                        //Ko phai chu nha
                        binding.btnInvite.visibility = View.GONE
                        binding.btnLeaveHome.visibility = View.VISIBLE
                        binding.btnEditHomeName.isClickable = false
                        binding.btnDeleteHome.visibility = View.GONE
                    }
                }

            },
            onError = {

            }
        )
    }
    private fun checkNumHome() {
        val num = intent.getStringExtra("numHome") ?: ""
        val numHome = num.toInt();
        if (numHome < 2) {
            binding.btnDeleteHome.visibility = View.GONE
        } else{
            binding.btnDeleteHome.visibility = View.VISIBLE
        }
    }
    private fun setInviteMember() {

        binding.btnInvite.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_invite, null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create();

            val edtUserId = dialogView.findViewById<EditText>(R.id.edtUserId);
            val txtUserName = dialogView.findViewById<TextView>(R.id.txtUserName);
            val btnInvite = dialogView.findViewById<Button>(R.id.btnInvite);
            val btnOut = dialogView.findViewById<ImageButton>(R.id.btnOut);
            val lyUserName = dialogView.findViewById<ConstraintLayout>(R.id.lyUserName)
            val txtResult = dialogView.findViewById<TextView>(R.id.txtResult)
            val loadingOverlay = dialogView.findViewById<FrameLayout>(R.id.loadingOverlay1)

            lyUserName.visibility = View.GONE
            txtResult.visibility = View.GONE


            //Tim nguoi dung sau khi nhan tim kiem
            edtUserId.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val text = edtUserId.text.toString()
                    //  Xử lý tìm kiếm
                    userRepo.getUserById(text,
                        onResult = { user ->
                            if (user != null) {
                                txtUserName.text = user.name

                                //Nhan nut moi thanh vien
                                btnInvite.setOnClickListener {
                                    loadingOverlay.visibility = View.VISIBLE
                                    //them thanh vien vao nha nhung o trang thai chua dong y
                                    homeRepo.getHomeById(homeId,
                                        onResult = { home ->
                                            if (home != null) {
                                                home.sharedUsers.add(Cupbo(user.id, false));
                                                homeRepo.addHome(home) { suc ->
                                                    if (suc) {
                                                        //them nha vao thanh vien o trang thai dc moi
                                                        user.homeList.add(Cupbo(homeId, false));
                                                        userRepo.updateUser(user) { cess ->
                                                            if (cess) {
                                                                //sua lai nut bam
                                                                btnInvite.setText("Invited")
                                                                btnInvite.isClickable = false;
                                                                btnInvite.setBackgroundResource(R.drawable.bg_google_button)
                                                                btnInvite.setTextColor(
                                                                    Color.parseColor(
                                                                        "#000000"
                                                                    )
                                                                )
                                                                btnInvite.visibility = View.VISIBLE

                                                                loadingOverlay.visibility =
                                                                    View.GONE
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onError = {

                                        }
                                    )

                                }

                                //Kiem tra trang thai nha cua nguoi dc moi
                                val homeOfUser = user.homeList.find { it.first.equals(homeId) }
                                if (homeOfUser != null) {
                                    if (homeOfUser.second) {
                                        btnInvite.visibility = View.GONE
                                    } else {
                                        btnInvite.setText("Invited")
                                        btnInvite.isClickable = false;
                                        btnInvite.setBackgroundResource(R.drawable.bg_google_button)
                                        btnInvite.setTextColor(Color.parseColor("#000000"))
                                        btnInvite.visibility = View.VISIBLE
                                    }
                                } else {
                                    btnInvite.setText("Invite")
                                    btnInvite.setBackgroundResource(R.drawable.bg_button3)
                                    btnInvite.setTextColor(Color.parseColor("#FFFFFF"))
                                    btnInvite.isClickable = true;
                                    btnInvite.visibility = View.VISIBLE
                                }
                                lyUserName.visibility = View.VISIBLE
                            } else {
                                txtResult.visibility = View.VISIBLE
                            }

                        },
                        onError = {
                            txtResult.visibility = View.GONE
                        }
                    )
                    true
                } else {
                    false
                }
            }

            //Nut out
            btnOut.setOnClickListener {
                dialog.dismiss();
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }


    private fun setHomeName() {
        homeRepo.getHomeById(homeId,
            onResult = { home ->
                if (home != null) {
                    binding.txtHomeName.text = home.name;
                }
            },
            onError = {

            }
        )

    }

    private fun showEditHomeName() {
        binding.btnEditHomeName.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_home, null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create();

            val edtHomeName = dialogView.findViewById<EditText>(R.id.edtHomeName);
            val btnSaveHomeEdit = dialogView.findViewById<Button>(R.id.btnSaveHomeEdit);
            val btnOutEditHomeDialog =
                dialogView.findViewById<ImageButton>(R.id.btnOutEditHomeDialog)

            homeRepo.getHomeById(homeId,
                onResult = { home ->
                    if (home != null) {
                        edtHomeName.setText(home.name);
                    }
                },
                onError = {

                }
            )

            btnSaveHomeEdit.setOnClickListener {
                val name = edtHomeName.text.toString();
                if (name.isNullOrEmpty()) {
                    edtHomeName.error = "Please enter name"
                } else {

                    homeRepo.getHomeById(homeId,
                        onResult = { home ->
                            if (home != null) {
                                home.name = name;
                                homeRepo.addHome(home) { success ->
                                    if (success) {
                                        Toast.makeText(this, "Sua thành công", Toast.LENGTH_SHORT)
                                            .show()
                                        setHomeName();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(this, "sua thất bại", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            }
                        },
                        onError = {

                        }
                    )


                }
            }

            btnOutEditHomeDialog.setOnClickListener {
                dialog.dismiss();
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }

    private fun setDeleteBtn() {
        homeRepo.getHomeById(homeId,
            onResult = { home ->
                if (home != null) {
                    binding.btnDeleteHome.setOnClickListener {
                        binding.loadingOverlay.visibility = View.VISIBLE
                        homeRepo.deleteHome(homeId) { success ->
                            if (success) {
                                binding.loadingOverlay.visibility = View.GONE
                                Toast.makeText(this, "Da xoa", Toast.LENGTH_SHORT).show()
                                finish();
                            } else {
                                return@deleteHome
                            }
                        }
                    }

                }
            },
            onError = {

            }
        )
    }

    private fun setBack() {
        binding.btnBackToHomeManage.setOnClickListener {
            finish()
        }
    }

    private fun showMember() {
        binding.rvUserList.layoutManager = LinearLayoutManager(this)

        homeRepo.getUserListId(homeId,
            onResult = { userList ->
                val userListId = mutableListOf<String>();
                for (item in userList) {
                    if (item.second) {
                        userListId.add(item.first);
                    }

                }
                userRepo.getUserList(userListId,
                    onResult = { userList ->
                        binding.rvUserList.adapter = UserListAdapter(userList, homeId) { user ->
                            Toast.makeText(this, "You click", Toast.LENGTH_SHORT).show()
                        }
                        binding.loadingOverlay.visibility = View.GONE
                    },
                    onError = { err ->
                        Log.e("Home", "Lỗi: $err")
                        binding.loadingOverlay.visibility = View.GONE
                    }
                )


            },
            onError = {
                binding.loadingOverlay.visibility = View.GONE
            }
        )
        binding.swipeRefresh.isRefreshing = false
    }
}