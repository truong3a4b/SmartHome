package com.example.smarthome.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smarthome.databinding.ActivityRegisterBinding
import com.example.smarthome.model.Cupbo
import com.example.smarthome.model.Home
import com.example.smarthome.model.Room
import com.example.smarthome.model.User
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.RoomRepo
import com.example.smarthome.respository.UserRepo
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepo: UserRepo
    private lateinit var homeRepo: HomeRepo
    private lateinit var roomRepo: RoomRepo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userRepo = UserRepo();
        homeRepo = HomeRepo();
        roomRepo = RoomRepo();


        setCreateAcc();
        setGoLogin();
    }

    private fun setCreateAcc() {
        binding.btnSignup.setOnClickListener{
            binding.loadingOverlay.visibility = View.VISIBLE
            val email = binding.edtEmailRegis.text.toString()
            val pass = binding.edtPasswordRegis.text.toString()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                binding.loadingOverlay.visibility = View.GONE
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnCompleteListener
                        val room = Room(roomRepo.getKey(),"Room", mutableListOf());
                        roomRepo.addRoom(room){suc ->
                            if(suc){
                                val home = Home(homeRepo.getKey(),"Home",uid, mutableListOf(room.id), mutableListOf(Cupbo(uid,true)))
                                homeRepo.addHome(home){cess ->
                                    val username = email.substringBefore("@")
                                    val user = User(uid,email,username, mutableListOf(Cupbo(home.id,true)))
                                    userRepo.createUser(user){};
                                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                                    binding.loadingOverlay.visibility = View.GONE
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                            }
                        }


                    } else {
                        binding.loadingOverlay.visibility = View.GONE
                        Toast.makeText(this, "Lỗi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }

        }

    }

    private fun setGoLogin() {
        binding.txtLogin.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
            finish();
        }
    }
}