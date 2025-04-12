package com.example.smarthome.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthome.R
import com.example.smarthome.model.Home
import com.example.smarthome.model.Room

class RoomListDetailAdapter(private val items:List<Room>, private val onItemClick:(Room) -> Unit): RecyclerView.Adapter<RoomListDetailAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
        val txtNameRoom = itemView.findViewById<TextView>(R.id.txtNameRoom);
        val txtDevOfRoom = itemView.findViewById<TextView>(R.id.txtDevOfHome);

        init {
            itemView.setOnClickListener {
                val item = items[adapterPosition]
                onItemClick(item)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.roomlist_detail_layout,parent,false);
        return MyViewHolder(view);
    }

    override fun getItemCount(): Int {
        return items.size;
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position];
        holder.txtNameRoom.text = item.name;
    }
}