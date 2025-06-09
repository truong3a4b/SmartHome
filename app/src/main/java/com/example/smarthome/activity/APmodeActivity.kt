package com.example.smarthome.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smarthome.R
import com.example.smarthome.databinding.ActivityAddDeviceBinding
import com.example.smarthome.databinding.ActivityApmodeBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

class APmodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApmodeBinding
    private lateinit var homeId:String;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApmodeBinding.inflate(layoutInflater)
        setContentView(binding.root);

        homeId = intent.getStringExtra("homeId") ?: "";

        setBtnConnect();
        setBack();
    }

    private fun setBack() {
        binding.btnBackToHome.setOnClickListener{
            finish();
        }

    }

    private fun setBtnConnect() {
        binding.btnConnect.setOnClickListener{
            val ssid = binding.edtSSID.text.toString();
            val pass = binding.edtPass.text.toString();
            if(ssid.isNullOrEmpty()){
                binding.edtSSID.error = "Please enter name"
            } else if (pass.isNullOrEmpty()){
                binding.edtPass.error = "Please enter name"
            } else {
                binding.loadingOverlay.visibility = View.VISIBLE;
                sendWifiToDev(ssid,pass);

            }
        }
    }

    private fun sendWifiToDev(ssid: String, pass: String) {
        val url = "http://192.168.4.1/connect"
        val formBody = FormBody.Builder()
            .add("ssid",ssid)
            .add("password",pass)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object:Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HTTP","Failed")
                Handler(Looper.getMainLooper()).post {
                    binding.loadingOverlay.visibility = View.GONE;
                    Toast.makeText(this@APmodeActivity,"Kết nối không thành công",Toast.LENGTH_SHORT).show();
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("HTTP","Response: ${response.body?.string()}")
                listenResponse();
            }

        })
    }

    private var isListening = true
    private var udpThread: Thread? = null
    private fun listenResponse() {
        udpThread = Thread {
            val socket = DatagramSocket(4210, InetAddress.getByName("0.0.0.0"))
            socket.broadcast = true
            socket.soTimeout = 1000 // Mỗi lần chờ tối đa 1 giây
            val startTime = System.currentTimeMillis()
            val timeoutMillis = 25000

            try {
                while (isListening && System.currentTimeMillis() - startTime < timeoutMillis) {
                    try {
                        val buffer = ByteArray(1024)
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)
                        val message = String(packet.data, 0, packet.length)

                        Log.d("UDP", "ESP32 gửi IP: $message")
                        reportSuccess(message)

                        isListening = false
                        break
                    } catch (e: SocketTimeoutException) {
                        Log.d("UDP", "Chờ UDP...")
                    }
                }

                if (isListening) {
                    // Hết thời gian mà không nhận được
                    Handler(Looper.getMainLooper()).post {
                        binding.loadingOverlay.visibility = View.GONE
                        Toast.makeText(this, "Không nhận được phản hồi từ ESP32", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("UDP", "Lỗi nhận UDP: ${e.message}")
                Handler(Looper.getMainLooper()).post {
                    binding.loadingOverlay.visibility = View.GONE
                    Toast.makeText(this, "Lỗi khi nhận UDP", Toast.LENGTH_SHORT).show()
                }
            } finally {
                socket.close()
            }
        }

        udpThread?.start()
    }

    private fun reportSuccess(ip : String) {
        val url = "http://$ip/success"
        val formBody = FormBody.Builder()
            .add("res","OK")
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object:Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HTTP","Failed")
                Handler(Looper.getMainLooper()).post {
                    binding.loadingOverlay.visibility = View.GONE;
                    Toast.makeText(this@APmodeActivity,"Kết nối không thành công",Toast.LENGTH_SHORT).show();
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val cont:String = response.body?.string().toString();
                val mac = cont.substringBefore("###");
                val type = cont.substringAfter("###");
                Log.d("HTTP","Response: $mac");
                Handler(Looper.getMainLooper()).post {
                    binding.loadingOverlay.visibility = View.GONE;
                    Toast.makeText(this@APmodeActivity,"Kết nối thành công",Toast.LENGTH_SHORT).show();
                    val intent = Intent(this@APmodeActivity, SelectRoomActivity::class.java);
                    intent.putExtra("homeId",homeId)
                    intent.putExtra("MAC",mac);
                    intent.putExtra("IP",ip);
                    intent.putExtra("TYPE",type)
                    startActivity(intent);
                }
            }

        })
    }



    override fun onDestroy() {
        super.onDestroy()
        isListening = false
        udpThread?.interrupt()
    }
}
