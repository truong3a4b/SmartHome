package com.example.smarthome.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthome.R
import com.example.smarthome.model.OnItemActionListener
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.UserRepo

class MessListAdapter(private val items: MutableList<String>, private val listener: OnItemActionListener) :
    RecyclerView.Adapter<MessListAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMess: TextView = itemView.findViewById(R.id.txtMess);
        val btnAccept = itemView.findViewById<Button>(R.id.btnAccept)
        val btnDecline = itemView.findViewById<Button>(R.id.btnDecline)
        val txtUserSend = itemView.findViewById<TextView>(R.id.txtUserSend)

        init {
            btnAccept.setOnClickListener {
                listener.onAcceptClick(adapterPosition)
            }
            btnDecline.setOnClickListener {
                listener.onDeclineClick(adapterPosition);
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.messlist_layout, parent, false);
        return MyViewHolder(view);
    }

    override fun getItemCount(): Int {
        return items.size;
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = items[position];
        val userRepo = UserRepo();
        val homeRepo = HomeRepo();

        //Lay nha tu homeID
        homeRepo.getHomeById(item,
            onResult = { home ->
                if (home != null) {

                    //Lay nguoi gui loi moi
                    userRepo.getUserById(home.ownerId,
                        onResult = { userSend ->
                            if (userSend != null) {
                                holder.txtUserSend.text = userSend.name
                                val mess = "Invite you into ${home.name}";
                                holder.txtMess.text = mess
                            }
                        },
                        onError = {

                        }
                    )


                }
            },
            onError = {

            }
        )

    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}