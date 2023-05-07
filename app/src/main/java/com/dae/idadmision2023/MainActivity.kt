package com.dae.idadmision2023


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import java.util.*
import kotlin.math.min


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
           "CC03" -> binding.tvTituloSede.text = "Facultad de cultura física"
        }

        revisaPremisoStorage()
        revisaPermisoAllFiles()


        val numSolicitud = binding.inTxtNsolicitud.text
        //BOTON SCANEAR
        binding.btnBuscar.setOnClickListener {

            hideKeyBoard()

            if (!numSolicitud.isNullOrBlank()) {
                borrarDatos()
                fnBuscarMatricula(numSolicitud.toString())
            }else{
                borrarDatos()
                initScan()
            }
        }

        //cerrar aplicacion
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }

    private fun revisaPermisoAllFiles() {
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                val getpermission = Intent()
                getpermission.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(getpermission)
            }
        }
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

        val aspirantes : Array<dataAspirantes> = gson.fromJson(json, arrayAspirantesType)

        val encontrado = aspirantes.find{ it.matricula == matricula}

        if ( encontrado != null){

            revisaSede(encontrado.sede_code)
            revisaFechaDia(encontrado.fecha)
            revisaTurno(encontrado.hora)

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

    private fun revisaTurno(hora : String) {
        val c = Calendar.getInstance()
        var horaSistema = c.get(Calendar.HOUR_OF_DAY)
        val minutoSis = c.get(Calendar.MINUTE)

        val horaSis = horaSistema  //horaSistema.toString().substring(0,2).toInt()
        val horaExamen = hora.substring(0,2).toInt()

        Log.d("hoa", horaSistema.toString())
        Log.d("hoa", horaExamen.toString())
        Log.d("hoa", minutoSis.toString())

        checaTurno(horaExamen,minutoSis, horaSis)

    }

    private fun checaTurno(horaExamen:Int, minutoSis:Int, horaSistema:Int) {

        if (horaExamen == 7 && horaSistema < horaExamen  ) {
            if (minutoSis <= 55) {
                if (horaSistema < horaExamen ) {
                    //Toast.makeText(this, "Turno 1 a tiempo:$horaExamen hora sistema: $horaSistema:$minutoSis VERDE", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "VERDE", Toast.LENGTH_SHORT).show()
                } else {
                    //Toast.makeText(this, "Hora de examen: $horaExamen: hora sistema: $horaSistema$minutoSis ROJO", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "ROJO", Toast.LENGTH_SHORT).show()
                }
            } else {
                //Toast.makeText(this, "Hora de examen: $horaExamen:$minutoSis rojo", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "ROJO", Toast.LENGTH_SHORT).show()
            }

        //TURNO 2 11:00
        } else if (horaExamen == 11 && horaSistema < horaExamen  ) {
            if (minutoSis <= 55) {
                if (horaSistema < horaExamen ) {
                    Toast.makeText(this, "Turno 2 a tiempo:$horaExamen hora sistema: $horaSistema:$minutoSis verde", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Hora de examen: $horaExamen:$minutoSis rojo", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Hora de examen: $horaExamen:$minutoSis rojo", Toast.LENGTH_SHORT).show()
            }

        //TURNO 3 15:00
        } else if ((horaExamen == 15 || horaExamen == 15 ) && horaSistema < horaExamen  ) {
            if (minutoSis <= 55) {
                if (horaSistema < horaExamen ) {
                    Toast.makeText(this, "Turno 3 a tiempo:$horaExamen hora sistema: $horaSistema:$minutoSis verde", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Hora de examen: $horaExamen:$minutoSis rojo", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Hora de examen: $horaExamen:$minutoSis rojo", Toast.LENGTH_SHORT).show()
            }

        //TURNO DE PRUEBA
        } else if ((horaExamen == 17) && horaSistema < horaExamen  ) {
            if (minutoSis <= 59 && horaSistema < horaExamen ) {
                if (horaSistema < horaExamen ) {
                    Toast.makeText(this, "Turno PRUEBA a tiempo:$horaExamen hora sistema: $horaSistema:$minutoSis verde", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Hora de examen: $horaExamen:$minutoSis rojo  - $horaSistema:$minutoSis", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Hora de examen: $horaExamen:$minutoSis rojo - $horaSistema:$minutoSis", Toast.LENGTH_SHORT).show()
            }

        } else if (horaSistema >= horaExamen){
            Toast.makeText(this, "Hora de examen: $horaSistema:$minutoSis hora ya paso $horaExamen", Toast.LENGTH_SHORT).show()

        //HORA DE EXAMEN INVALIDA
        }else{
            Toast.makeText(this, "Hora de examen: $horaSistema:$minutoSis hora no corresponde", Toast.LENGTH_SHORT).show()
        }

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
        //val path = "/storage/emulated/0/Download/fotos/${matricula}.jpg"
        val path = "/storage/emulated/0/Download/fotos/${matricula}.webp"
        val file = File(path)
        var imageUri = Uri.fromFile(file)

        Glide.with(this).load(imageUri).into(binding.ivFoto)
        //Glide.with(this).load("https://mathiasbynens.be/demo/animated-webp-supported.webp").into(binding.ivFoto)

       /* binding.ivFoto.setOnClickListener {
            val intent = Intent(this, imgeFullScreen::class.java)
            intent.putExtra("img", imageUri)
            this.startActivity(intent)
        }
        */
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