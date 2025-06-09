package com.example.smarthome.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smarthome.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        setLogin();
        setGoSignup();
    }

    private fun setGoSignup() {
        binding.txtSignup.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
            finish();
        }
    }

    private fun setLogin() {
        binding.btnLogin.setOnClickListener{
            binding.loadingOverlay.visibility = View.VISIBLE
            val email = binding.edtEmailLogin.text.toString().trim();
            val pass = binding.edtPasswordLogin.text.toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                binding.loadingOverlay.visibility = View.GONE
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        binding.loadingOverlay.visibility = View.GONE
                        startActivity(Intent(this, MainActivity::class.java));
                        finish();
                    } else {
                        binding.loadingOverlay.visibility = View.GONE
                        when (task.exception) {
                            is FirebaseAuthInvalidUserException -> {
                                Toast.makeText(this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show()
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(this, "Sai mật khẩu", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this, "Lỗi đăng nhập: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }
    }
}