package com.dae.idadmision2023

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.*
import com.dae.idadmision2023.databinding.ActivityMainBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanOptions

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
        val result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data)
        if(result != null){
            if(result.contents == null){
                Toast.makeText(this, "cancelado",Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(this, "el valor escaneado es: ${result.contents}",Toast.LENGTH_SHORT).show()
                binding.inTxtNsolicitud.setText(result.contents)
            }
        }else{
             super.onActivityResult(requestCode, resultCode, data)
        }
    }

}