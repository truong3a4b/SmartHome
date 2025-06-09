package com.example.smarthome.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smarthome.R
import com.example.smarthome.databinding.ActivityLightDeviceBinding
import com.example.smarthome.databinding.ActivityTvcontrolBinding
import com.example.smarthome.respository.DeviceRepo
import com.example.smarthome.respository.RoomRepo
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class TVControlActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTvcontrolBinding
    private lateinit var ipAddress:String;
    private lateinit var macAddress:String;
    private val client = OkHttpClient()
    private lateinit var roomRepo: RoomRepo
    private lateinit var deviceRepo: DeviceRepo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTvcontrolBinding.inflate(layoutInflater);
        setContentView(binding.root)

        ipAddress = intent.getStringExtra("IP")?: "";
        macAddress = intent.getStringExtra("MAC")?: "";

        roomRepo = RoomRepo();
        deviceRepo = DeviceRepo();

        setDeviceName();
        setBtnPower();
        setBack();
        setBtnChanel();
        setBtnSetting();
    }

    private fun setBtnSetting() {
        binding.btnSetting.setOnClickListener{
            val popupView = layoutInflater.inflate(R.layout.popup_device_setting,null);
            val popupWindow = PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

            val btnRename: LinearLayout = popupView.findViewById(R.id.btnRename);
            val btnMove: LinearLayout = popupView.findViewById(R.id.btnMove);
            val btnDelete: LinearLayout = popupView.findViewById(R.id.btnDelete);
            popupWindow.isFocusable = true;
            popupWindow.showAsDropDown(it,0,0);

            btnRename.setOnClickListener{
                popupWindow.dismiss()
                showDialogEditName();
            }
            btnDelete.setOnClickListener{
                popupWindow.dismiss()
                showDialogDelete();
            }
        }
    }
    private fun showDialogEditName() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_device_name, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create();

        val edtDeviceName = dialogView.findViewById<EditText>(R.id.edtDeviceName);
        val btnSaveHomeEdit = dialogView.findViewById<Button>(R.id.btnSaveHomeEdit);
        val btnOutEditHomeDialog = dialogView.findViewById<ImageButton>(R.id.btnOutEditHomeDialog)

        deviceRepo.getDeviceById(macAddress,
            onResult = {device ->
                if(device!=null){
                    edtDeviceName.setText(device.name)
                }
            },
            onError = {}
        )
        btnOutEditHomeDialog.setOnClickListener{
            dialog.dismiss();
        }
        btnSaveHomeEdit.setOnClickListener{
            val name = edtDeviceName.text.toString();
            if (name.isNullOrEmpty()) {
                edtDeviceName.error = "Please enter name"
            } else {
                deviceRepo.getDeviceById(macAddress,
                    onResult = { device ->
                        if (device != null) {
                            device.name = name;
                            deviceRepo.addDevice(device){ success ->
                                if (success) {
                                    Toast.makeText(this, "Sua thành công", Toast.LENGTH_SHORT)
                                        .show()
                                    setDeviceName();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(this, "sua thất bại", Toast.LENGTH_SHORT)
                                        .show()
                                }}
                        }
                    },
                    onError = {}
                )
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showDialogDelete() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete")
        builder.setMessage("Are you sure you want to delete?")
        builder.setPositiveButton("Yes") { dialog, which ->
            binding.loadingOverlay.visibility = View.VISIBLE
            deviceRepo.deleteDevice(macAddress) { success ->
                if (success) {
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(this, "Da xoa", Toast.LENGTH_SHORT).show()
                    finish();
                } else {
                    return@deleteDevice
                }
            }
        }
        builder.setNegativeButton("No") { dialog, which ->
            // Xử lý khi nhấn No (đóng dialog)
            dialog.dismiss()
        }
        builder.show()
    }

    private fun setBtnChanel() {
        binding.btnUpChannel.setOnClickListener{
            senCmdToDev("tv/chup");
        }
        binding.btnDownChanel.setOnClickListener{
            senCmdToDev("tv/chdown");
        }
    }

    private fun setBack() {
        binding.btnBackToHome.setOnClickListener{
            finish();
        }
    }


    private fun setDeviceName() {
        deviceRepo.getDeviceById(macAddress,
            onResult = {device ->
                if(device!=null){
                    binding.txtDeviceName.text = device.name;
                }
            },
            onError = {}
        )
    }
    private fun setBtnPower() {
        binding.btnPower.setOnClickListener{
            senCmdToDev("tv/power");
        }
    }

    private fun senCmdToDev(cmd: String) {
        val url = "http://$ipAddress/$cmd";

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread{
                    Toast.makeText(this@TVControlActivity,"Lỗi kết nối", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread{
                    if(response.isSuccessful){
                        Toast.makeText(this@TVControlActivity,"Ok", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@TVControlActivity,"Lỗi yêu cầu", Toast.LENGTH_SHORT).show()
                    }

                }
                response.close();
            }

        })
    }

}