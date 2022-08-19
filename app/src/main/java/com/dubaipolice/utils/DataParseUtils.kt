package com.dubaipolice.utils

import com.google.gson.Gson
import java.lang.reflect.Type

object DataParseUtils {
    fun <T> parseJson(json: String?, tClass: Class<T>?): T {
        return Gson().fromJson(json, tClass)
    }

    fun <T> parseJson(json: String?, type: Type?): T {
        return Gson().fromJson(json, type)
    }

    fun getJson(profile: Any?): String {
        return Gson().toJson(profile)
    }
}