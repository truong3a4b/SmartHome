package com.example.smarthome.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smarthome.R
import com.example.smarthome.model.User
import com.example.smarthome.respository.HomeRepo
import com.example.smarthome.respository.UserRepo

class UserListAdapter(private val items: List<User>,private val homeId:String, private val onItemClick: (User) -> Unit) :
    RecyclerView.Adapter<UserListAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val txtUserNameHome: TextView = itemView.findViewById(R.id.txtUserNameHome);
        val txtVaitro: TextView = itemView.findViewById(R.id.txtVaitro);

        init {
            itemView.setOnClickListener {
                val item = items[adapterPosition]
                onItemClick(item)

            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserListAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.userlist_layout,parent,false);
        return MyViewHolder(view);
    }

    override fun onBindViewHolder(holder: UserListAdapter.MyViewHolder, position: Int) {
        val item = items[position];

        val userRepo = UserRepo();
        val homeRepo = HomeRepo();

        holder.txtUserNameHome.text = item.name;
        homeRepo.getHomeById(homeId,
            onResult = {home ->
                if(home != null){
                    if(item.id.equals(home.ownerId)){
                        holder.txtVaitro.text = "Owner"

                    }else{
                        holder.txtVaitro.text = "Member"

                    }
                }

            },
            onError = {

            }
        )

        homeRepo.getHomeById(homeId,
            onResult = {home ->
                if(home != null){
                    if(userRepo.getUserIdCur().equals(home.ownerId)){
                        holder.itemView.isClickable=true
                    }else{
                        holder.itemView.isClickable=false
                    }
                }

            },
            onError = {

            }
        )

    }

    override fun getItemCount(): Int {
        return items.size;
    }

}