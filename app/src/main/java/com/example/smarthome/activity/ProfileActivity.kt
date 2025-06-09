package com.example.smarthome.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smarthome.R
import com.example.smarthome.databinding.ActivityProfileBinding
import com.example.smarthome.respository.UserRepo
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepo: UserRepo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater);
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userRepo = UserRepo();

        setSignOut();
        setTabBar();
        setProfile();
        showIdUer();
        setEditUserName();
    }

    private fun setEditUserName() {
        binding.btnUserName.setOnClickListener{
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_name_user,null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create();

            val edtUserName = dialogView.findViewById<EditText>(R.id.edtUserName)
            val btnOut = dialogView.findViewById<ImageButton>(R.id.btnOut)
            val btnSaveNameEdit = dialogView.findViewById<Button>(R.id.btnSaveNameEdit)

            userRepo.getUserCur(
                onResult = {user ->
                    if(user != null){
                        edtUserName.setText(user.name)
                    }

                },
                onError = {

                }
            )
            btnSaveNameEdit.setOnClickListener{
                val newName = edtUserName.text.toString();
                if(newName.isNullOrEmpty()){
                    edtUserName.error = "Please enter name"
                }else{

                    userRepo.getUserCur(
                        onResult = {user ->
                            if(user != null){
                                user.name = newName;
                                userRepo.updateUser(user){success ->
                                    if(success){
                                        Toast.makeText(this, "Da doi ten ", Toast.LENGTH_SHORT).show()
                                        setProfile();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(this, "Doi ten that bai", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                        },
                        onError = {

                        }
                    )
                }
            }
            btnOut.setOnClickListener{
                dialog.dismiss();
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }

    private fun showIdUer() {
        binding.btnIdUser.setOnClickListener{
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_id_user,null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create();

            val txtIdUser = dialogView.findViewById<TextView>(R.id.txtIdUser)
            val btnOut = dialogView.findViewById<ImageButton>(R.id.btnOut)
            userRepo.getUserCur(
                onResult = {user ->
                    if(user != null){
                        txtIdUser.text = user.id
                    }

                },
                onError = {

                }
            )

            btnOut.setOnClickListener{
                dialog.dismiss();
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }


    }

    private fun setProfile() {
        binding.txtUserNameProfile.visibility = View.GONE
        userRepo.getUserCur(
            onResult = {user ->
                if(user != null){
                    binding.txtUserNameProfile.text = user.name
                    binding.txtUserNameProfile.visibility = View.VISIBLE
                }

            },
            onError = {

            }
        )
    }

    private fun setSignOut() {
        binding.btnLogout.setOnClickListener{
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun setTabBar() {
        binding.tabProfileatPro.isSelected = true;
        binding.tabHomeatPro.isSelected=false
        binding.tabHomeatPro.setOnClickListener{
            binding.tabProfileatPro.isSelected = false;
            binding.tabHomeatPro.isSelected = true;
            startActivity(Intent(this, MainActivity::class.java));
            finish();
        }

    }
}