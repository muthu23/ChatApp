package com.dubaipolice.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.IOException
import java.net.URL

object FileUtils {

    fun createFile(context: Context, fileName: String): File? {
        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "MediaDownloads"
        )
        try {
            var success = true
            if (!dir.exists()) {
                success = dir.mkdirs()
            }
            return if (success) {
                val file = File(
                    dir.absolutePath
                            + File.separator
                            + fileName
                )
                if (file.exists()) {
                    file.delete()
                }
                file
            } else {
                null
            }
        } catch (e: IOException) {
            return null
        } catch (e: SecurityException) {
            return null
        }
    }

    fun createFileForUploads(context: Context, fileName: String): File? {
        val dir = File(
            context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "MediaUploads"
        )
        try {
            var success = true
            if (!dir.exists()) {
                success = dir.mkdirs()
            }
            return if (success) {
                val file = File(
                    dir.absolutePath
                            + File.separator
                            + fileName
                )
                if (file.exists()) {
                    file.delete()
                }
                file
            } else {
                null
            }
        } catch (e: IOException) {
            return null
        } catch (e: SecurityException) {
            return null
        }
    }
     fun bytesIntoHumanReadable(bytes: Long): String {
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024
        return when {
            bytes in 0 until kilobyte -> {
                "$bytes B"
            }
            bytes in kilobyte until megabyte -> {
                (bytes / kilobyte).toString() + " KB"
            }
            bytes in megabyte until gigabyte -> {
                (bytes / megabyte).toString() + " MB"
            }
            bytes in gigabyte until terabyte -> {
                (bytes / gigabyte).toString() + " GB"
            }
            bytes >= terabyte -> {
                (bytes / terabyte).toString() + " TB"
            }
            else -> {
                "$bytes Bytes"
            }
        }
    }

}