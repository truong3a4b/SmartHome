package com.example.smarthome.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthome.R
import com.example.smarthome.model.Device
import com.example.smarthome.model.DeviceStatus
import com.example.smarthome.model.Home
import com.example.smarthome.model.Room

class DeviceListRoomAdapter(private val items:List<Device>, private val onItemClick:(Device) -> Unit): RecyclerView.Adapter<DeviceListRoomAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
        val txtNameRoom = itemView.findViewById<TextView>(R.id.txtNameRoom);
        val txtState = itemView.findViewById<TextView>(R.id.txtState);

        init {
            itemView.setOnClickListener {
                val item = items[adapterPosition]
                onItemClick(item)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.devicelist_room_layout,parent,false);
        return MyViewHolder(view);
    }

    override fun getItemCount(): Int {
        return items.size;
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position];
        holder.txtNameRoom.text = item.name;
        holder.txtState.text = item.status.toString();
        if(item.status == DeviceStatus.CONNECTED){
            holder.txtState.setTextColor(Color.parseColor("#22E20D"));
        }
        if(item.status == DeviceStatus.DISCONNECTED){
            holder.txtState.setTextColor(Color.parseColor("#FFDE0A2A"));
        }
    }
}