package com.dae.idadmision2023

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dae.idadmision2023.databinding.ActivitySplashAppBinding
import com.dae.idadmision2023.preferences.ConfigPreferences

class SplashApp : AppCompatActivity() {

    lateinit var binding : ActivitySplashAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Thread.sleep(1500)
        startActivity(Intent(this, ConfigPreferences::class.java))
        this.finish()

    }
}