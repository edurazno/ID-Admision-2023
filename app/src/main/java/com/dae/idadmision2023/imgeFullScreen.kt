package com.dae.idadmision2023

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.dae.idadmision2023.databinding.ActivityImgeFullScreenBinding

class imgeFullScreen : AppCompatActivity() {

    lateinit var binding : ActivityImgeFullScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImgeFullScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
   /*
        val imagen = intent.getStringExtra("img")
        Log.d("img",imagen.toString())
        Glide.with(this)
             .load(imagen)
             .into(binding.ivFoto)
   */
   }
}