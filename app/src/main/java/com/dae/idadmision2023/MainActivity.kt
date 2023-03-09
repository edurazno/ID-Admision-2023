package com.dae.idadmision2023


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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


        //BOTON SCANEAR
        binding.btnBuscar.setOnClickListener {

            hideKeyBoard()
            if (!binding.inTxtNsolicitud.text.isNullOrBlank()) {

                fnBuscarMatricula(binding.inTxtNsolicitud.text.toString())
            }else{
                initScan()
            }
        }

    }

    fun hideKeyBoard(){
        val view = this.currentFocus
        if(view != null){
            val hideMe = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken,0)
        }else{
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }
    }

    private fun fnBuscarMatricula(matricula: String) {
        //202228541
        if (matricula.length == 9) {
            //Toast.makeText(this, matricula, Toast.LENGTH_SHORT).show()

            //encontrado = searchInJSON(matricula)
            encontrado = searchInJSON1(matricula)

            //Revisa si se encotró información del aspirante.
            if (!encontrado){

                Toast.makeText(this, "No. de Solicitud \n ¡No encontrado!", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, android.os.Build.VERSION.SDK_INT.toString(),Toast.LENGTH_SHORT).show()
                binding.tvCarrera.text = ""
                binding.tvFecha.text = ""
                binding.tvsede.text = ""
                binding.tvNoSolicitud.text = ""
                binding.tvNombre.text = ""

            }

         }else {
            //Toast.makeText(this, "Ingresa los 9 dígitos de la matrícula", Toast.LENGTH_SHORT).show()
            initScan()
        }
    }

     @RequiresApi(Build.VERSION_CODES.Q)
     fun loadImage(matricula: String) {
         //val path = "storage/emulated/0/Download/fotos/${matricula}.jpg"
         val path = "/storage/emulated/0/Download/fotos/${matricula}.jpg"
         val file = File(path)
         val imageUri = Uri.fromFile(file)

         Glide.with(this).load(imageUri).into(binding.ivFoto)
    }

    private fun initScan() {
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("\"Escanea el código de barras del formato de asignación de examen\"")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        //integrator.setTorchEnabled(true) //flash
        integrator.initiateScan()
    }

    //SCANNER
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
                Toast.makeText(this, "cancelado", Toast.LENGTH_SHORT).show()
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
                    loadImage(asp.matricula)
                }
                flag = true
            }
        }

        return flag
    }

    private fun searchInJSON1(matricula: String) : Boolean{

        val gson = Gson()
        val json = loadData("aspirantes5.json")
        val flag :Boolean

        val arrayAspirantesType = object : TypeToken<Array<dataAspirantes>>() {}.type
        val aspirantes: Array<dataAspirantes> = gson.fromJson(json, arrayAspirantesType)

        val encontrado = aspirantes.find{ it.matricula == matricula}

        if ( encontrado != null){
            binding.tvNoSolicitud.text = encontrado.matricula
            binding.tvNombre.text = encontrado.nombre
            binding.tvCarrera.text = encontrado.carrera
            binding.tvsede.text = encontrado.sede
            binding.tvFecha.text = encontrado.fecha + " / " + encontrado.hora
            if (android.os.Build.VERSION.SDK_INT >= 29)   loadImage(encontrado.matricula)

            flag = true
        }else{
            Toast.makeText(this, "Matricula no encontrada", Toast.LENGTH_SHORT).show()
            flag = false
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