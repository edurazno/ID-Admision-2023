package com.dae.idadmision2023

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.dae.idadmision2023.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.integration.android.IntentIntegrator
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    var encontrado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //SCANEAR
        binding.btnBuscar.setOnClickListener { initScan() }

        //BUSCAR
        binding.btnBuscar2.setOnClickListener {
            val matricula: String = binding.inTxtNsolicitud.text.toString()
            fnBuscarMatricula(matricula)
        }

    }

    private fun fnBuscarMatricula(matricula: String) {
        //202228541
        if (matricula.length == 9) {
            //Toast.makeText(this, matricula, Toast.LENGTH_SHORT).show()

            encontrado = searchInJSON(matricula)

            //Revisa si se encotró información del aspirante.
            if (!encontrado){

                Toast.makeText(this, "No. de Solicitud \n ¡No encontrado!", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, android.os.Build.VERSION.SDK_INT.toString(),Toast.LENGTH_SHORT).show()
                binding.tvCarrera.text = ""
                binding.tvFecha.text = ""
                binding.tvsede.text = ""
                binding.tvNoSolicitud.text = ""
                binding.tvNombre.text = ""

                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
                    loadImage()
                }*/

            }
        } else {
            //Toast.makeText(this, "Ingresa los 9 dígitos de la matrícula", Toast.LENGTH_SHORT).show()
            initScan()
        }
    }

     @RequiresApi(Build.VERSION_CODES.Q)
     fun loadImage() {
         val path = "/storage/emulated/0/Download/fotos/202228568.jpg"
         val file = File(path)
         val imageUri = Uri.fromFile(file)

         Glide.with(this).load(imageUri).into(binding.ivFoto)
    }

    private fun initScan() {

        val integrator = IntentIntegrator(this)
        integrator.setPrompt("\"Escanea el código de barras del formato de asignación de examen\"")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setTorchEnabled(true) //flash

        if (binding.inTxtNsolicitud.text == null){
            integrator.initiateScan()
        }else{
            //busca lo ingresado en la caja de texto
            fnBuscarMatricula(binding.inTxtNsolicitud.text.toString())
        }
    }

    //SCANNER
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val matricula: String
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "cancelado", Toast.LENGTH_SHORT).show()
            } else {
                //Toast.makeText(this, "${result.contents}", Toast.LENGTH_SHORT).show()
                matricula = result.contents.toString()
                searchInJSON(matricula)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun searchInJSON(matricula: String) : Boolean{

        var flag = false
        val gson = Gson()
        val json = loadData("aspirantes.json")

        val arrayAspirantesType = object : TypeToken<Array<dataAspirantes>>() {}.type
        val aspirantes: Array<dataAspirantes> = gson.fromJson(json, arrayAspirantesType)

        aspirantes.forEachIndexed { idx, asp ->
            if (asp.matricula == matricula) {
                //Toast.makeText(this, asp.matricula, Toast.LENGTH_SHORT).show()
                binding.tvCarrera.text = asp.carrera
                binding.tvFecha.text = asp.fecha + " / " +asp.hora
                binding.tvsede.text = asp.sede
                binding.tvNoSolicitud.text = asp.matricula
                binding.tvNombre.text = asp.nombre

                if (android.os.Build.VERSION.SDK_INT >= 29){
                    loadImage()
                }

                flag = true
            }
        }
        return flag
    }

    fun loadData(inFile: String): String {
        var tContents: String = ""
        try {
            val stream = assets.open(inFile)
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
}