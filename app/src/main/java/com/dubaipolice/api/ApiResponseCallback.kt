package com.dubaipolice.api

import android.content.Intent
import android.util.Log
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.utils.*
import com.dubaipolice.view.activity.LoginActivity
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class ApiResponseCallback<T> : Callback<T?> {
    private val TAG = ApiResponseCallback::class.java.simpleName
    override fun onResponse(
        call: Call<T?>,
        response: Response<T?>
    ) {
        Log.i(TAG, "RES : " + "Success")
        if (response.body() != null && response.raw().code == 200) {
            onSuccess(response.body())
        } else if (response.raw().code == 401) {
            try {
                val e1 = GenricError()
                e1.message= MainApplication.appContext.getString(R.string.session_expired)
                //e1.error= null
                e1.success= false
                onError(e1)
            } catch (e: Exception) {
                Log.d(TAG, "api exception: " + e.message)
                onError(defaultErrorMsg)
            }
            /*try {

                MyProgressDialog.dismiss()

                val unauthorisedResponse: UnauthorisedResponse = Gson().fromJson(response.errorBody()!!.string(), UnauthorisedResponse::class.java)

                SharedPref.writeBoolean(AppConstants.KEY_IS_LOGGEDIN, false)
                //Utils.showToastLong(MainApplication.appContext, MainApplication.appContext.getString(R.string.error_401))
                Utils.showToastLong(MainApplication.appContext, unauthorisedResponse.message!!)

                val i = Intent(MainApplication.appContext, LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                MainApplication.appContext.startActivity(i)

            } catch (e: Exception) {
                Log.d(TAG, "api exception: " + e.message)
                //onError(defaultErrorMsg)

                SharedPref.writeBoolean(AppConstants.KEY_IS_LOGGEDIN, false)
                Utils.showToastLong(MainApplication.appContext, MainApplication.appContext.getString(R.string.error_401))

                val i = Intent(MainApplication.appContext, LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                MainApplication.appContext.startActivity(i)

            }*/
        } else if (response.raw().code == 500) {
            try {
                val e1 = GenricError()
                e1.message= MainApplication.appContext.getString(R.string.error_500)
                //e1.error= null
                e1.success= false
                onError(e1)
            } catch (e: Exception) {
                Log.d(TAG, "api exception: " + e.message)
                onError(defaultErrorMsg)
            }
        }
        else if (response.raw().code == 400) {
            try {
                val e1 = GenricError()
                e1.message= MainApplication.appContext.getString(R.string.error_400)
                //e1.error= null
                e1.success= false
                onError(e1)
            } catch (e: Exception) {
                Log.d(TAG, "api exception: " + e.message)
                onError(defaultErrorMsg)
            }
        }
        /* else if (response.raw().code == 401) {
             try {
                 val e1 = GenricError()
                 e1.message= MainApplication.appContext.getString(R.string.error_401)
                 //e1.error= null
                 e1.success= false
                 onError(e1)
             } catch (e: Exception) {
                 Log.d(TAG, "api exception: " + e.message)
                 onError(defaultErrorMsg)
             }
         }*/
        else if (response.raw().code == 403) {
            try {
                val e1 = GenricError()
                e1.message= MainApplication.appContext.getString(R.string.error_403)
                //e1.error= null
                e1.success= false
                onError(e1)
            } catch (e: Exception) {
                Log.d(TAG, "api exception: " + e.message)
                onError(defaultErrorMsg)
            }
        }
        else if (response.raw().code == 404) {
            try {
                val e1 = GenricError()
                e1.message= MainApplication.appContext.getString(R.string.error_404)
                //e1.error= null
                e1.success= false
                onError(e1)
            } catch (e: Exception) {
                Log.d(TAG, "api exception: " + e.message)
                onError(defaultErrorMsg)
            }
        }
        else if (response.raw().code == 502) {
            try {
                val e1 = GenricError()
                e1.message= MainApplication.appContext.getString(R.string.error_502)
                //e1.error= null
                e1.success= false
                onError(e1)
            } catch (e: Exception) {
                Log.d(TAG, "api exception: " + e.message)
                onError(defaultErrorMsg)
            }
        }
        else {
            onError(defaultErrorMsg)
        }
       /* } else if (response.raw().code == 401) {
            try {
                val error: GenricError = DataParseUtils.parseJson(
                    response.errorBody()!!.string(),
                    GenricError::class.java
                )
                val e1 = GenricError()
                e1.message= error.message
                e1.message= MainApplication.appContext.getString(R.string.session_expired)
                e1.statusCode= error.statusCode
                e1.success= error.success
                onError(e1)
            } catch (e: Exception) {
                Log.d(TAG, "api exception: " + e.message)
                onError(defaultErrorMsg)
            }
        } else if (response.raw().code == 403 || response.raw().code == 400 || response.raw().code == 404
        ) {
            try {
                val error: GenricError = DataParseUtils.parseJson(
                    response.errorBody()!!.string(),
                    GenricError::class.java
                )
                val e1 = GenricError()
                e1.message= error.message
                e1.statusCode= error.statusCode
                e1.error= error.error
                e1.success= error.success
                onError(e1)
            } catch (e: Exception) {
                Log.d(TAG, "api exception: " + e.message)
                onError(defaultErrorMsg)
            }
        } else {
            onError(defaultErrorMsg)
        }*/
    }

    override fun onFailure(call: Call<T?>, t: Throwable) {
        Log.i(TAG, "ERROR FAIL : " + t.message)
        if (t is NetworkConnectionInterceptor.NoConnectivityException) {
            // show No Connectivity message to user or do whatever you want.
            onError(getNoConnectivityErrorMsg(t.message))
        } else {
            onError(defaultErrorMsg)
        }
    }

    abstract fun onSuccess(response: T?)
    abstract fun onError(msg: GenricError?)
    private val defaultErrorMsg: GenricError
        private get() {
            val error = GenricError()

            error.message = MainApplication.appContext.getString(R.string.something_went_wrong)

            onError(error)
            return error
        }

    private fun getNoConnectivityErrorMsg(msg: String?): GenricError {
        val error = GenricError()

        error.message= msg

        onError(error)
        return error
    }
}