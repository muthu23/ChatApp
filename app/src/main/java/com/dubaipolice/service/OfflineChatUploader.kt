package com.dubaipolice.service

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dubaipolice.MainApplication
import com.dubaipolice.api.ApiClient
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

class OfflineChatUploader (context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        private val TAG = OfflineChatUploader::class.java.simpleName
    }

    override fun doWork(): Result {

        //get input parameters from where started work manager request and add this param there
        val input = inputData.getString(AppConstants.CHAT_UPLOADER_SERVICE);

        if(input != null && !TextUtils.isEmpty(input))
        {

            var messageInfoTable= AppDatabase.getAppDatabase(applicationContext)
                ?.messageInfoTableDao()
                ?.getUnuploadedChatList(SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!)

            //if list size grater than 0
            if(messageInfoTable != null && messageInfoTable.isNotEmpty())
            {

                if(Utils.isNetConnected(MainApplication.appContext))
                {

                    messageInfoTable.forEachIndexed { index, e ->

                        println("$e at $index")

                        if(MainApplication.connection != null)
                        {

                            if(MainApplication.connection!!.isConnected() &&
                                MainApplication.connection!!.isAuthenticated())
                            {
                                MainApplication.connection?.sendMessageMultiUser(e)
                            }

                        }

                    }

                    return Result.success()

                }
                else
                {
                    return Result.retry()
                }

            }
            else
            {
                return Result.success()
            }

        } else {
            return Result.failure()
        }

    }

}