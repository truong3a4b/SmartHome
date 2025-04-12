package com.example.smarthome.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthome.R
import com.example.smarthome.model.Home
import com.example.smarthome.model.Room

class HomeListDetailAdapter(private val items:List<Home>, private val onItemClick:(Home) -> Unit): RecyclerView.Adapter<HomeListDetailAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
        val txtNameHome = itemView.findViewById<TextView>(R.id.txtNameHome);
        val txtRoomOfHome = itemView.findViewById<TextView>(R.id.txtRoomOfHome);
        val txtDevOfHome = itemView.findViewById<TextView>(R.id.txtDevOfHome);

        init {
            itemView.setOnClickListener {
                val item = items[adapterPosition]
                onItemClick(item)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.homelist_detail_layout,parent,false);
        return MyViewHolder(view);
    }

    override fun getItemCount(): Int {
        return items.size;
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position];
        holder.txtNameHome.text = item.name;
    }
}