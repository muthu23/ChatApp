package com.dubaipolice.service

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dubaipolice.api.ApiClient
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

/**
 * This service is used to run in background to update fcm token
 */
class FCMUpdater(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        private val TAG = FCMUpdater::class.java.simpleName
    }

    override fun doWork(): Result {

        //get input parameters from where started work manager request and add this param there
        val fcmToken = inputData.getString(AppConstants.FCM_TOKEN)
        return if (fcmToken != null && !TextUtils.isEmpty(fcmToken)) {
            //synchronous api request using execute method instead of enqueue to return worker response status

            var parameter: JsonObject? = null

            try {
                parameter = JsonObject()
                parameter!!.addProperty(AppConstants.FIELD.FCM_TOKEN, fcmToken)
                Log.d("Parameter", parameter.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val call: Call<CommonResponse> = ApiClient.request!!.updateFcmToken(
                Utils.getSelectedLanguage()!!,
                SharedPref.readString(AppConstants.KEY_TOKEN)!!,
                parameter!!
            )
            try {
                val response: Response<CommonResponse> = call.execute()
                if (response.code() == 200) {
                    val data: CommonResponse? = response.body()
                    //TODO
                    //if data needs to be fetched
                } else {
                    return Result.retry()
                }
                Result.success()
            } catch (e: IOException) {
                if (e.message != null) {
                    Log.e("Error", e.message!!)
                }
                Result.retry()
            }
        } else {
            Result.failure()
        }
    }

}