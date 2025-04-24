package com.example.smarthome

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.espressif.iot.esptouch.EsptouchTask
import com.espressif.iot.esptouch.util.ByteUtil
import com.espressif.iot.esptouch2.provision.TouchNetUtil



import com.example.smarthome.databinding.ActivityTestBinding
import com.example.smarthome.model.WifiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID


class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding
    private lateinit var wifiManager: WifiManager;
    private lateinit var wifiState: WifiState
    private val esp32DeviceName = "ESP32"
    private var bluetoothSocket: BluetoothSocket? = null
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var outputStream: OutputStream? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager;
//        wifiState = WifiState();
//
//
//        binding.btnConnect.setOnClickListener{
//            if (checkPermission()){
//                if(checkWifi()){
//                    binding.txtSSID.text = wifiState.ssid;
//                    binding.txtBSSID.text = wifiState.bssid;
//                }else{
//                    binding.txtLog.text = "CheckWIfi false";
//                }
//            }else{
//                requestPermission();
//                binding.txtLog.text="Yeu cau permission"
//            }
//        }
//        binding.btnSend.setOnClickListener{
//            connectESP();
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )

            val missing = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (missing.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1)
            }
        }
        binding.btnConnect.setOnClickListener{
            connectToESP();
        }

        binding.btnSend.setOnClickListener{
            val ssidInput = binding.edtSSID.text.toString();
            val passInput = binding.edtPass.text.toString();
            val data = "SSID:${ssidInput}\nPASS:${passInput}\n"
            sendDataToESP32(data)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToESP() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        
        val device = pairedDevices.find { it.name == esp32DeviceName }
        if (device == null) {
            Toast.makeText(this, "Không tìm thấy ESP32", Toast.LENGTH_SHORT).show()
            return
        }

        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
        bluetoothSocket?.connect()
        outputStream = bluetoothSocket?.outputStream

        Toast.makeText(this, "Đã kết nối ESP32", Toast.LENGTH_SHORT).show()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Ứng dụng cần quyền Bluetooth để hoạt động", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun sendDataToESP32(data: String) {
        if (outputStream == null) {
            Toast.makeText(this, "Chưa kết nối Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        outputStream?.write(data.toByteArray())
        Toast.makeText(this, "Đã gửi dữ liệu", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothSocket?.close()
    }
    private fun checkPermission():Boolean{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED;
        }else return true;
    }
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }
   private fun checkWifi():Boolean{
       wifiState.wifiConnected = false;

       val wifiInfo = wifiManager.connectionInfo;
       val connected = TouchNetUtil.isWifiConnected(wifiManager);
       if(!connected){
           return false;
       }
       val ssid = TouchNetUtil.getSsidString(wifiInfo);
       wifiState.ssidBytes = TouchNetUtil.getRawSsidBytesOrElse(wifiInfo,ssid.toByteArray());
       wifiState.ssid = ssid;
       wifiState.wifiConnected = true;
       wifiState.is5G = TouchNetUtil.is5G(wifiInfo.frequency);
       wifiState.bssid = wifiInfo.bssid;
       wifiState.enable = wifiState.wifiConnected;
       return true;
   }

    private  fun connectESP(){
        val ssidByte = ByteUtil.getBytesByString(wifiState.ssid);
        val pwdStr = binding.edtPass.text.toString();
        val passByte = ByteUtil.getBytesByString(pwdStr);
        val bssidByte = TouchNetUtil.convertBssid2Bytes(wifiState.bssid)

        coroutineScope.launch {
            val task = EsptouchTask(ssidByte, bssidByte, passByte, applicationContext)
            task.setPackageBroadcast(true)
            task.setEsptouchListener {
                Log.d("ESP", "ESP connected: ${it.inetAddress.hostAddress}")
            }
            val results = task.executeForResults(1)

            withContext(Dispatchers.Main) {
                if (results.isNotEmpty() && results[0].isSuc) {
                    val result = results[0]
                    binding.txtLog.text = "IP: ${result.inetAddress.hostAddress}\nMAC: ${result.bssid}"
                } else {
                    binding.txtLog.text = "Kết nối thất bại"
                }

            }
        }
    }
}
