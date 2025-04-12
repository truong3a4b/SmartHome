package com.example.smarthome.model



interface OnItemActionListener {
    fun onAcceptClick(position: Int);
    fun onDeclineClick(position: Int)
}