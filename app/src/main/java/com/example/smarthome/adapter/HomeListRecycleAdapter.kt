package com.example.smarthome.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthome.R
import com.example.smarthome.model.Home

class HomeListRecycleAdapter(private val items:List<Home>, private val onItemClick: (Home) -> Unit) : RecyclerView.Adapter<HomeListRecycleAdapter.MyViewHolder>(){
    private var selectedPosition = 0
    inner class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val txtHomeName: TextView = itemView.findViewById(R.id.txtHomeName);
        init {
            itemView.setOnClickListener {
                val item = items[adapterPosition]
                onItemClick(item)
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                // Cập nhật 2 item: cái cũ và cái mới
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.homelist_layout,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position];
        holder.txtHomeName.text = item.name;

        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFBB86FC")) // Màu tím nhạt
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

}