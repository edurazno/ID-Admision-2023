package com.dae.idadmision2023


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dae.idadmision2023.databinding.ActivityMainBinding
import com.dae.idadmision2023.preferences.ConfigPreferences
import com.dae.idadmision2023.preferences.UserPreferencesAplication
import com.dae.idadmision2023.preferences.UserPreferencesAplication.Companion.prefs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.integration.android.IntentIntegrator
import java.io.File
import java.io.IOException
import java.security.AccessController.getContext
import java.sql.Time
import java.time.Instant
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    var encontrado = false
    var tituloSede = prefs.getSede()

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showAppClosingDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        MyToolBar().show(this,"",false)

        when(tituloSede){
           "CC01" -> binding.tvTituloSede.text = "Centro de convenciones CU"
           "CC02" -> binding.tvTituloSede.text = "Complejo cultural universitario CCU"
           "CC03" -> binding.tvTituloSede.text = "Fac. de cultura física"
        }


        revisaPremisoStorage()

        //BOTON SCANEAR
        binding.btnBuscar.setOnClickListener {

            hideKeyBoard()

            if (!binding.inTxtNsolicitud.text.isNullOrBlank()) {
                borrarDatos()
                fnBuscarMatricula(binding.inTxtNsolicitud.text.toString())
            }else{
                borrarDatos()
                initScan()
            }

        }

        //cerrar aplicacion
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }

    private fun borrarDatos() {

        if (binding.tvCarrera.text.isNotEmpty() && binding.tvNombre.text.isNotEmpty() && binding.tvsede.text.isNotEmpty()){
        //if (binding.inTxtNsolicitud.text.isNullOrBlank()){

            binding.inTxtNsolicitud.setText("")
            binding.tvNoSolicitud.text  = ""
            binding.tvNombre.text       = ""
            binding.tvCarrera.text      = ""
            binding.tvsede.text         = ""
            binding.tvFecha.text        = ""

        }
    }

    private fun revisaPremisoStorage() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //permiso no aceptado por el momento
            requestStoragePermiso()
        }
    }

    private fun requestStoragePermiso() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
            //El usuario ya ha rechazado los permisos
            Toast.makeText(this, "Debes aceptar el permiso de almacenamiento en ajustes de la app", Toast.LENGTH_SHORT).show()
        }else{
            //pedir permiso
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 777)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 777){
            //nuestro permisos
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permiso aceptado", Toast.LENGTH_SHORT).show()
            }else{
                //el permiso no ha sido aceptado
                Toast.makeText(this, "Permiso rechazado", Toast.LENGTH_SHORT).show()
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
                borrarDatos()
            }

         }else {
            //Toast.makeText(this, "Ingresa los 9 dígitos de la matrícula", Toast.LENGTH_SHORT).show()
            initScan()
        }
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
            if (result.contents == null) {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }else{
                searchInJSON1(result.contents)
                //searchInJSON(result.contents)
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun searchInJSON1(matricula: String) : Boolean{

        val gson = Gson()
        val json = loadData("json_79k.json")
        val flag :Boolean
        val arrayAspirantesType = object : TypeToken<Array<dataAspirantes>>() {}.type
        val aspirantes: Array<dataAspirantes> = gson.fromJson(json, arrayAspirantesType)

        val encontrado = aspirantes.find{ it.matricula == matricula}

        if ( encontrado != null){

            revisaSede(encontrado.sede_code)
            revisaFechaDia(encontrado.fecha)
            binding.inTxtNsolicitud.setText(encontrado.matricula)
            binding.tvNoSolicitud.text  = encontrado.matricula
            binding.tvNombre.text       = encontrado.nombre
            binding.tvCarrera.text      = encontrado.carrera
            binding.tvsede.text         = encontrado.sede
            binding.tvFecha.text        = encontrado.fecha + " - " + encontrado.hora

            if (android.os.Build.VERSION.SDK_INT >= 29)   loadImage(encontrado.matricula)
            flag = true
        }else{
            Toast.makeText(this, "No. de Solicitud no válido.", Toast.LENGTH_SHORT).show()
            borrarDatos()
            flag = false
        }

        return flag
    }

    private fun revisaSede(sedeCode: String) {
        var sede = prefs.getSede()
        if (sede == sedeCode){
            binding.tvsede.setTextColor(ContextCompat.getColor(this, R.color.verdeModalidades))
            binding.inTxtNsolicitud.setTextColor(ContextCompat.getColor(this, R.color.verdeModalidades))
        }else{
            binding.inTxtNsolicitud.setTextColor(ContextCompat.getColor(this, R.color.naranjaNMS))
            binding.tvsede.setTextColor(ContextCompat.getColor(this, R.color.naranjaNMS))
        }
    }

    private fun revisaFechaDia(fecha:String) {
        val c = Calendar.getInstance()
        val day = c.get(Calendar.DATE)
        var mes = c.get(Calendar.MONTH)
        val anio = c.get(Calendar.YEAR)

        mes++ //numero mes inicia ENERO = 0
        //val fecha2 = "${day.toString()}/${mes.toString()}/${anio.toString()}"

        val diaInFileJson = fecha.substring(0,2)
        val mesInFileJson = fecha.substring(3,6)
        var nMes = mes_toNum(mesInFileJson)

        if (diaInFileJson.toInt() == day.toInt() && nMes == mes){
            binding.tvFecha.setTextColor(ContextCompat.getColor(this, R.color.verdeModalidades))
        }else{
            binding.tvFecha.setTextColor(ContextCompat.getColor(this, R.color.naranjaNMS))
        }
    }

    private fun mes_toNum(mesJson: String):Int {
        var nMes = 0
        when(mesJson){
            "ENE" -> nMes = 1
            "FEB" -> nMes = 2
            "MAR" -> nMes = 3
            "ABR" -> nMes = 4
            "MAY" -> nMes = 5
            "JUN" -> nMes = 6
            "JUL" -> nMes = 7
            "AGO" -> nMes = 8
            "SEP" -> nMes = 9
            "OCT" -> nMes = 10
            "NOV" -> nMes = 11
            "DIC" -> nMes = 12
        }
        return nMes
    }

    //@RequiresApi(Build.VERSION_CODES.Q)
    fun loadImage(matricula: String) {
        val path = "/storage/emulated/0/Download/fotos/${matricula}.jpg"
        val file = File(path)
        var imageUri = Uri.fromFile(file)

        Glide.with(this).load(imageUri).into(binding.ivFoto)
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
            Toast.makeText(this, "Error al cargar el archivo: ${e}", Toast.LENGTH_SHORT).show()
        }
        return tContents
    }

    //Opciones de Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_desplegable,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.it_ajustes -> {
                UserPreferencesAplication.prefs.deleteSede()
                startActivity(Intent (this, ConfigPreferences::class.java) )
                this.finish()
            } //borra el historial
            R.id.it_salir ->   { showAppClosingDialog() }                        //muestra ventana para salir de la App
        }
        return super.onOptionsItemSelected(item)
    }

    //Dialogo para cerrra App
    private fun showAppClosingDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Aviso")
            .setMessage("¿Realmente deseas salir de la aplicación?")
            .setPositiveButton("Sí") { _, _ -> this.finish() }
            .setNegativeButton("No", null)
            .show()
    }
}