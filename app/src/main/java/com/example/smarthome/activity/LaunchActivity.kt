package com.example.smarthome.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.smarthome.R
import com.google.firebase.auth.FirebaseAuth

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_launch)
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // User đã đăng nhập
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Chưa đăng nhập
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // kết thúc Splash để không quay lại được nữa
    }
}