package com.dae.idadmision2023.preferences


import android.content.Context


class Pref (val context: Context) {

    val SHARED_NAME = "MyDB"
    val SHARED_SEDE = "sedeSeleccionado"

    val storage = context.getSharedPreferences(SHARED_NAME, 0)

    //guarda el dato nivelSeleccionado
    fun saveSede(sedeSeleccionado:String){
        storage.edit().putString(SHARED_SEDE, sedeSeleccionado).apply()
    }

    //treae el dato guardado
    fun getSede():String{
        return storage.getString(SHARED_SEDE,"")!!
    }

    //borrar Nivel
    fun deleteSede(){
        storage.edit().clear().apply()
    }

}