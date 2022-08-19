package com.dubaipolice.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {

    fun getChatDateFromTimestamp(timestamp: String): String? {
        return try {
            val format = SimpleDateFormat("dd-MMMM-yyyy")

            format.format(Date(timestamp.toLong()))
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e("Error", e.message!!)
            }
            ""
        }
    }

    fun getChatDateFromTimestampTwoDigit(timestamp: String): String? {
        return try {
            val format = SimpleDateFormat("MM-dd-yy")

            format.format(Date(timestamp.toLong()))
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e("Error", e.message!!)
            }
            ""
        }
    }

    fun getChatTimeFromTimestamp(timestamp: String): String? {
        return try {
            val format = SimpleDateFormat("HH:mm")

            format.format(Date(timestamp.toLong()))
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e("Error", e.message!!)
            }
            ""
        }
    }

    fun getDateTimeWithTimeZoneFromTimestamp(timestamp: String): String? {
        return try {
            //var format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            var format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS.sss'Z'")
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.format(Date(timestamp.toLong()))
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e("Error", e.message!!)
            }
            ""
        }
    }

    fun getDateObjectFromStringTimestamp(timestamp: String): Date? {
        return try {
            Date(timestamp.toLong())
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e("Error", e.message!!)
            }
            Date()
        }
    }

    //input: 2021-08-24 12:55:00  //return timestamp
    //convert utc timezone to current timezone
    fun getTimestampFromDateTimeString(datetime: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'")
        format.timeZone = TimeZone.getTimeZone("UTC")
        try {
            val date = format.parse(datetime)
            System.out.println(date)
            return date.time
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
            //return null
        }


    }

    fun getTime12FromTimestamp(timestamp: String): String? {
        return try {
            val format = SimpleDateFormat("hh:mm aa")

            format.format(Date(timestamp.toLong()))
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e("Error", e.message!!)
            }
            ""
        }
    }
    fun getFormattedDate(dateStr: String): String {
        val inputFormatter = SimpleDateFormat(Constants.YYYY_MM_DD_SERVER_RESPONSE_FORMAT)
        if (dateStr.trim().isNotEmpty()) {
            val date = inputFormatter.parse(dateStr)
            date?.let {
                var dateFomate = Constants.DD_MM_YYYY_FORMAT
                val formatter = SimpleDateFormat(dateFomate)
                return formatter.format(date)
            }
        }
        return dateStr
    }

    fun getSecondsBetweenTimestamp(timestamp: String): Long {
        try {
            val receivedDateTime = Date(timestamp.toLong())
            val currentDateTime = Date()
            Log.e("receivedDateTime", receivedDateTime.toString())
            Log.e("currentDateTime", currentDateTime.toString())
            val milliseconds = currentDateTime.time - receivedDateTime.time
            //milliseconds to seconds
            return TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        } catch (e: java.lang.Exception) {
            //e.printStackTrace();
            if (e.message != null) {
                Log.e("Error", e.message!!)
            }
            return 0
        }
    }

}