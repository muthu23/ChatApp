package com.dubaipolice.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPref {

    private var mSharedPref: SharedPreferences? = null
    private const val PREF_NAME = "karggo-customer"
    fun init(context: Context) {
        if (mSharedPref == null) mSharedPref = context.getSharedPreferences(
            context.packageName,
            Context.MODE_PRIVATE
        )
    }

    fun readString(key: String?): String? {
        return mSharedPref!!.getString(key, "")
    }

    fun writeString(key: String?, value: String?) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putString(key, value)
        prefsEditor.apply()
    }

    fun readBoolean(key: String?): Boolean {
        return mSharedPref!!.getBoolean(key, false)
    }

    fun writeBoolean(key: String?, value: Boolean) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putBoolean(key, value)
        prefsEditor.apply()
    }

    fun readInt(key: String?): Int {
        return mSharedPref!!.getInt(key, -1)
    }

    fun writeInt(key: String?, value: Int?) {
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.putInt(key, value!!).apply()
    }

    fun clearData() {
        //editor.remove("name");  //use this to remove specific key value
        //use clear to remove all the value from the shared preferences
        val prefsEditor = mSharedPref!!.edit()
        prefsEditor.clear()
        prefsEditor.apply()
    }
}
