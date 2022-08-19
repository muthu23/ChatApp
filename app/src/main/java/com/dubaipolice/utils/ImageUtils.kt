package com.dubaipolice.utils

import android.content.Context
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class has image util functions
 */
object ImageUtils {

    /**
     * This function is used to create the file path for images
     */
    @Throws(IOException::class)
    fun createImageFileCache(context: Context): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.cacheDir
        return File.createTempFile(
            imageFileName,  /* prefix */".jpg",  /* suffix */storageDir /* directory */
        )
    }

    /**
     * This function is used to read binary stream from file
     */
    fun readImageVideoBinaryStream(stream: InputStream): ByteArray? {
        val buffer = ByteArrayOutputStream()
        try {
            var nRead: Int
            val data = ByteArray(1024)
            while (stream.read(data, 0, data.size).also { nRead = it } != -1) {
                buffer.write(data, 0, nRead)
            }
            buffer.flush()
            //byte[] byteArray = buffer.toByteArray();
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return buffer.toByteArray()
    }

    /**
     * This function is used to write binary stream to file
     */
    fun writeImageVideoFile(cachedFile: File?, data: ByteArray?): Boolean? {
        var output: BufferedOutputStream? = null
        try {
            output = BufferedOutputStream(FileOutputStream(cachedFile))
            output.write(data)
            output.flush()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                output!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }

}