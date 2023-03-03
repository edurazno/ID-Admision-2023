package com.dae.idadmision2023

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.dae.idadmision2023.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import java.io.File
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    /*val getContent = registerForActivityResult(GetContent()){
            uri :Uri? -> {
                if (uri == null){
                    Toast.makeText(this@MainActivity,"Cancelado", Toast.LENGTH_SHORT)
                }
                else{
                    Toast.makeText(this@MainActivity,"Scanead", Toast.LENGTH_SHORT)
                }
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBuscar.setOnClickListener { initScan() }

        binding.btnBuscar2.setOnClickListener {
            val matricula: String = binding.inTxtNsolicitud.text.toString()
            fnBuscarMatricula(matricula)
        }

    }

    private fun fnBuscarMatricula(matricula: String) {

        //202228541

        if (matricula.length == 9) {
            Toast.makeText(this, matricula, Toast.LENGTH_SHORT).show()
            searchInJSON(matricula)
        } else {
            Toast.makeText(this, "Ingresa los 9 dígitos de la matrícula", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchInJSON(matricula: String) {

        val gson = Gson()
        val json = loadData()
        val aspirante = gson.fromJson(json, dataAspirantes::class.java)

        Log.d("res", aspirante.id.toString())

    }

    fun loadData(): String {
        var tContents : String = ""
        try {
            val stream = assets.open("aspirantes.json")
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            tContents = String(buffer)
        } catch (e: IOException) {
            Toast.makeText(this, "Error: ${e}", Toast.LENGTH_SHORT).show()
        }
        return tContents
    }

    private fun initScan() {

        /* val options : ScanOptions = ScanOptions()
         options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
         options.setPrompt(("Escanea el QR del formato de asignación de examen"))
         options.setCameraId(0)
         options.setBeepEnabled(true)
         //options.setOrientationLocked(false)
         options.setBarcodeImageEnabled(true)*/

        IntentIntegrator(this).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val matricula: String
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "cancelado", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "${result.contents}", Toast.LENGTH_SHORT).show()
                matricula = result.contents.toString()
                searchInJSON(matricula)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}