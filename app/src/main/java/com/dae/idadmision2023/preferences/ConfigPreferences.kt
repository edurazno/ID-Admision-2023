package com.dae.idadmision2023.preferences

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast


import com.dae.idadmision2023.MainActivity
import com.dae.idadmision2023.R
import com.dae.idadmision2023.databinding.ActivityConfigPreferencesBinding
import com.dae.idadmision2023.preferences.UserPreferencesAplication.Companion.prefs


class ConfigPreferences : AppCompatActivity() {
    //variable para viewBinding
    private lateinit var binding: ActivityConfigPreferencesBinding
    private var sedeSeleccionado: String = "NO_CHECKED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfigPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide();
        checkUserValues()
        guardaValores()
        //this.finish()
    }

    //Busca valores guardados si hay los compara para ir al nivel que corresponde
    private fun checkUserValues() {
        var sede = prefs.getSede()
        if (sede.isNotEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            this.finish()
        }
    }

    //Guarda valores seleccionados en los radioButtons
    private fun guardaValores() {
        binding.btnGuardarPref.setOnClickListener {

            if (sedeSeleccionado != "NO_CHECKED") {
                prefs.saveSede(sedeSeleccionado)
                startActivity(Intent(this, MainActivity::class.java))
                this.finish()
            } else {
                Toast.makeText(
                    this,
                    "Debes seleccionar la sede antes de continuar.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    //Carga valoeres de radioButtons
    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked
            when (view.getId()) {
                R.id.rb_CU -> {
                    if (checked) {
                        sedeSeleccionado = "CC01" //CU
                    }
                }
                R.id.rb_CCU -> {
                    if (checked) {
                        sedeSeleccionado = "CC02" //CCU
                    }
                }
                R.id.rb_CulturaFisica -> {
                    if (checked) {
                        sedeSeleccionado = "CC03" //Cultura Física
                    }
                }
            }
        } /*else {
            //
        }*/
    }
}