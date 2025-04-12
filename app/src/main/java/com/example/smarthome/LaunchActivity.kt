package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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