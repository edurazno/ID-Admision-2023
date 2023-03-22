package com.dae.idadmision2023

import androidx.appcompat.app.AppCompatActivity

class MyToolBar {
    fun show (activities:AppCompatActivity, title:String, upButton:Boolean){
        activities.setSupportActionBar(activities.findViewById(R.id.toolbar))
        activities.supportActionBar?.title = title
        activities.supportActionBar?.setDisplayHomeAsUpEnabled(upButton)
    }
}