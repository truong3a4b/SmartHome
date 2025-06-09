package com.example.smarthome.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthome.R
import com.example.smarthome.model.Room

class RoomListAdapter(private val items: List<Room>, private val onItemClick:(Room) -> Unit,private val onAddClick:()-> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_ITEM = 0
    private val TYPE_BUTTON = 1

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size) TYPE_BUTTON else TYPE_ITEM
    }
    inner class ItemViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val imgRoom: ImageView = itemView.findViewById(R.id.imgRoom);
        val txtRoom: TextView = itemView.findViewById(R.id.txtRoom);
        val txtDevOfRoom: TextView = itemView.findViewById(R.id.txtDevOfRoom);

        fun bind(item:Room){
            imgRoom.setImageResource(R.drawable.kitchen);
            txtRoom.text = item.name;
            if(item.getNumDevice() < 2){
                txtDevOfRoom.text = item.getNumDevice().toString()+" device";
            }else{
                txtDevOfRoom.text = item.getNumDevice().toString()+" devices";
            }
        }
        init {
            itemView.setOnClickListener {
                val item = items[adapterPosition]
                onItemClick(item)

            }
        }
    }

    class ButtonViewHolder(itemView: View,private val onAddClick: () -> Unit) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            // Xử lý nút Add
            val btnAdd = itemView.findViewById<Button>(R.id.buttonAdd)
            btnAdd.setOnClickListener{
                onAddClick()
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.roomlist_layout, parent, false)
            ItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.button_add_room_layout, parent, false)
            ButtonViewHolder(view, onAddClick)
        }
    }

    override fun getItemCount(): Int {
        return items.size+1;
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val item = items[position]
            holder.bind(item)
        } else if (holder is ButtonViewHolder) {
            holder.bind()
        }
    }
}