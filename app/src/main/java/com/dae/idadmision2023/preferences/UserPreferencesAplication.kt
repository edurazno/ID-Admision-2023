package com.dae.idadmision2023.preferences

import android.app.Application

class UserPreferencesAplication : Application() {

    //para accesar a esta propiedad desde cualquier clase
    companion object {
        lateinit var prefs : Pref
    }

    override fun onCreate() {
        super.onCreate()
            prefs = Pref (applicationContext)
    }
}