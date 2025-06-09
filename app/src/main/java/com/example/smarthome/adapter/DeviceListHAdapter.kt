package com.example.smarthome.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthome.R
import com.example.smarthome.model.Device
import com.example.smarthome.model.DeviceStatus
import com.example.smarthome.model.DeviceType
import com.example.smarthome.model.Home
import com.example.smarthome.model.Room

class DeviceListHAdapter(private val items:List<Device>, private val onItemClick:(Device) -> Unit): RecyclerView.Adapter<DeviceListHAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
        val txtName = itemView.findViewById<TextView>(R.id.txtName);
        val txtState = itemView.findViewById<TextView>(R.id.txtState);
        val imgDevice = itemView.findViewById<ImageView>(R.id.imgDevice);
        init {
            itemView.setOnClickListener {
                val item = items[adapterPosition]
                onItemClick(item)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.devicelist_home_layout,parent,false);
        return MyViewHolder(view);
    }

    override fun getItemCount(): Int {
        return items.size;
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position];
        holder.txtName.text = item.name;
        holder.txtState.text = item.status.toString();
        if(item.status == DeviceStatus.CONNECTED){
            holder.txtState.setTextColor(Color.parseColor("#22E20D"));
        }
        if(item.status == DeviceStatus.DISCONNECTED){
            holder.txtState.setTextColor(Color.parseColor("#FFDE0A2A"));
        }
        if(item.type == DeviceType.TV){
          holder.imgDevice.setImageResource(R.drawable.baseline_tv_24)
        }
        if(item.type == DeviceType.LIGHT){
            holder.imgDevice.setImageResource(R.drawable.baseline_lightbulb_outline_24)
        }
    }
}