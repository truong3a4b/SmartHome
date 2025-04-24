package com.example.smarthome

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Visibility
import com.example.smarthome.adapter.DeviceListAdapter
import com.example.smarthome.databinding.ActivityAddDeviceBinding
import com.example.smarthome.model.Device
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.jvm.Throws

class AddDeviceActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddDeviceBinding
    private lateinit var homeId:String;
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var outputStream: OutputStream? = null
    private var deviceSelected:Device?=null;
    private lateinit var deviceAdapter: DeviceListAdapter
    private val deviceList = mutableListOf<Device>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root);

        homeId = intent.getStringExtra("homeId") ?: "";
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        checkBluetoothPermissions();
        checkEnableBluetooth();
        showDeviceDectected();
        setBtnDetect();
        setBack();
    }



    @SuppressLint("MissingPermission")
    private fun setBtnDetect() {
        binding.btnDetect.setOnClickListener{
            val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
            pairedDevices.forEach{item ->
                val name = item.name;
                val address = item.address;
                val device = Device("",name,address);
                if (deviceList.none { it.macAddress == address }) {
                    deviceList.add(device);
                }
            }
            startBluetoothScan();
            binding.btnDetect.isClickable=false;
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            Toast.makeText(this, "Bạn cần cấp quyền Bluetooth để ứng dụng hoạt động", Toast.LENGTH_LONG).show()
        }
    }

    //Kiem tra quyen bluetooth
    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )

            val missing = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (missing.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, missing.toTypedArray(), 2001)
            }
        } else {
            // Android 11 trở xuống cần quyền location để scan Bluetooth
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    2002
                )
            }
        }
    }

    //Kiem tra xem da bat bluetooth chua
    @SuppressLint("MissingPermission")
    private fun checkEnableBluetooth(){
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ Bluetooth", Toast.LENGTH_SHORT).show()
        } else if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1001) // Bắt người dùng bật Bluetooth
        }
    }

    //Dang ky broadcastReceiver de lang nghe xem co thiet bi moi ko
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action ?: return


            if (action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) ?: return

                val name = device.name ?: "Unknow"
                val address = device.address
                val item = Device("",name,address);

                if (deviceList.none { it.macAddress == address }) {
                    deviceList.add(item)
                    deviceAdapter.notifyItemInserted(deviceList.size - 1)
                }
            }
        }
    }

    private fun showDeviceDectected() {
        deviceAdapter = DeviceListAdapter(deviceList) { device ->
            deviceSelected = device;
            connectToDevice(device.macAddress)
        }
        binding.rvDeviceList.layoutManager = LinearLayoutManager(this);
        binding.rvDeviceList.adapter = deviceAdapter;
    }

    private fun startBluetoothScan() {


        // 1. Kiểm tra quyền BLUETOOTH_SCAN
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                1
            )
            return
        }

        // 2. Hủy quét cũ nếu đang quét
        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.cancelDiscovery()
        }

        // 3. Đăng ký BroadcastReceiver để lắng nghe thiết bị mới tìm được
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        // 4. Bắt đầu quét thiết bị xung quanh
        bluetoothAdapter!!.startDiscovery()
        Toast.makeText(this, "Đang quét...", Toast.LENGTH_SHORT).show()
    }

    //Ham ket nối tới thết bị
    @SuppressLint("MissingPermission")
    private fun connectToDevice(address:String){
        val device = bluetoothAdapter!!.getRemoteDevice(address);
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            // Hủy quét nếu đang quét
            if (bluetoothAdapter!!.isDiscovering) {
                bluetoothAdapter!!.cancelDiscovery()
            }

            // Kết nối
            bluetoothSocket?.connect()

            Toast.makeText(this, "Đã kết nối tới ${device.name}", Toast.LENGTH_SHORT).show()
            //Hien dialog de ket noi wifi cho thiet bi
            showDialog();
        }
        catch (e: IOException){
            e.printStackTrace()
            Toast.makeText(this, "Kết nối thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            try {
                bluetoothSocket?.close()
            } catch (closeException: IOException) {
                closeException.printStackTrace()
            }
        }

        binding.loadingOverlay.visibility = View.GONE
    }

    //Hien dialog de nhap thong tin wifi
    private fun showDialog(){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_device,null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create();

        val btnOutDialog = dialogView.findViewById<ImageButton>(R.id.btnOutDialog);
        val edtSSID = dialogView.findViewById<EditText>(R.id.edtSSID);
        val edtPass = dialogView.findViewById<EditText>(R.id.edtPass);
        val btnConnect = dialogView.findViewById<Button>(R.id.btnConnect);

        btnConnect.setOnClickListener{
            val ssid = edtSSID.text.toString();
            val pass = edtPass.text.toString();
            if(ssid.isNullOrEmpty()){
                edtSSID.error = "Please enter name"
            } else if (pass.isNullOrEmpty()){
                edtPass.error = "Please enter name"
            } else{
                try {
                    val data = ssid+";"+pass+"\n";
                    bluetoothSocket?.outputStream?.write(byteArrayOf(0x01))
                    bluetoothSocket?.outputStream?.write(data.toByteArray())

                    Toast.makeText(this, "Đã gửi", Toast.LENGTH_SHORT).show()
                    val inputStream = bluetoothSocket?.inputStream;
                    dialog.dismiss();
                    binding.loadingOverlay.visibility = View.VISIBLE
                    startListening(inputStream);
                } catch (e: IOException) {
                    Toast.makeText(this, "Lỗi khi gửi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        btnOutDialog.setOnClickListener{
            dialog.dismiss();
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }


    //Ham nay de lang nghe du lieu nhan vao tu bluetooth
    //Neu nhan dc thong bao ket noi thanh cong thi chuyen sang act moi
    private fun startListening(inputStream: InputStream?) {
        if (inputStream == null) return

        Thread {
            val buffer = ByteArray(1024)
            var bytes: Int
            var state = false;
            val messageBuilder = StringBuilder()
            while(true){
                try {
                    val signal = inputStream.read()
                    if(signal == 0x02){
                        state = true;
                        break;
                    } else if(signal == 0x03){
                        runOnUiThread {
                            Toast.makeText(this, "ESP32: Connect Fail!", Toast.LENGTH_SHORT).show()
                            binding.loadingOverlay.visibility = View.GONE
                        }
                        break;
                    }
                }catch (e:IOException){
                    e.printStackTrace()
                    break
                }
            }

            while (true) {
                try {
                    if(state){
                        bytes = inputStream.read(buffer);
                        val incoming = String(buffer, 0, bytes)
                        messageBuilder.append(incoming);
                        if (incoming.contains("\n")) {
                            val fullMessage = messageBuilder.toString().trim()
                            messageBuilder.clear()

                            runOnUiThread {
                                Toast.makeText(this, "ESP32: $fullMessage", Toast.LENGTH_SHORT).show()
                                deviceSelected!!.ipAddress = fullMessage;
                                val intent = Intent(this,SelectRoomActivity::class.java);
                                intent.putExtra("homeId",homeId);
                                intent.putExtra("name",deviceSelected!!.name);
                                intent.putExtra("MAC",deviceSelected!!.macAddress);
                                intent.putExtra("IP",deviceSelected!!.ipAddress);
                                startActivity(intent);
                                binding.loadingOverlay.visibility = View.GONE

                            }
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }.start()
    }

    private fun setBack() {
        binding.btnBackToHome.setOnClickListener{
            finish();
        }
    }
    //Huy receiver
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {}
    }
}