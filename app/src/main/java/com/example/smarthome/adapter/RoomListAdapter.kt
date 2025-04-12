package com.example.smarthome.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthome.R
import com.example.smarthome.model.Room

class RoomListAdapter(private val items: List<Room>, private val onItemClick:(Room) -> Unit): RecyclerView.Adapter<RoomListAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val imgRoom: ImageView = itemView.findViewById(R.id.imgRoom);
        val txtRoom: TextView = itemView.findViewById(R.id.txtRoom);
        val txtDevOfRoom: TextView = itemView.findViewById(R.id.txtDevOfRoom);

        init {
            itemView.setOnClickListener {
                val item = items[adapterPosition]
                onItemClick(item)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.roomlist_layout,parent,false);
        return MyViewHolder(view);
    }

    override fun getItemCount(): Int {
        return items.size;
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position];
        holder.imgRoom.setImageResource(R.drawable.kitchen);
        holder.txtRoom.text = item.name;
        if(item.getNumDevice() < 2){
            holder.txtDevOfRoom.text = item.getNumDevice().toString()+" device";
        }else{
            holder.txtDevOfRoom.text = item.getNumDevice().toString()+" devices";
        }


    }
}