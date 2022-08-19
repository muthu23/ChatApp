package com.dubaipolice.service

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dubaipolice.api.ApiClient
import com.dubaipolice.api.ApiResponseCallback
import com.dubaipolice.api.GenricError
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.google.gson.JsonObject
import retrofit2.Call

class LocationUpdaterToServer(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        private val TAG = LocationUpdaterToServer::class.java.simpleName
    }

    override fun doWork(): Result {

        //get input parameters from where started work manager request and add this param there
        val latitude = inputData.getDouble(AppConstants.LATITUDE, 0.0)
        val longitude = inputData.getDouble(AppConstants.LONGITUDE, 0.0)

        if (latitude != null && longitude != null) {
            if (SharedPref.readBoolean(AppConstants.KEY_IS_LOGGEDIN)) {
                sendLocationToServer(
                    latitude,
                    longitude,
                )
            }
        }

        return Result.success()

    }

    private fun sendLocationToServer(
        latitude: Double,
        longitude: Double,
    ) {

        var parameter: JsonObject? = null

        try {
            parameter = JsonObject()
            parameter!!.addProperty(AppConstants.FIELD.LATITUDE, latitude.toString())
            parameter!!.addProperty(AppConstants.FIELD.LONGITUDE, longitude.toString())
            parameter!!.addProperty(AppConstants.FIELD.USER_ID, SharedPref.readInt(AppConstants.KEY_USER_ID).toString())
            Log.d("Parameter", parameter.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //String myLocation= "["+location.getLatitude() + "," + location.getLongitude() + "]";
        val call: Call<CommonResponse> =
            ApiClient.request!!.sendLocationToServer(Utils.getSelectedLanguage()!!, SharedPref.readString(AppConstants.KEY_TOKEN)!!, parameter!!)
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

            override fun onSuccess(response: CommonResponse?) {
                if (response != null) {
                    if (response.isSuccess) {
                        Log.e(TAG, "SuccessLocation")
                    }
                }
            }

            override fun onError(msg: GenricError?) {
                if (msg != null && !TextUtils.isEmpty(msg.message)) {
                    Log.e(TAG, msg.message!!)
                }
            }


        })

    }

}