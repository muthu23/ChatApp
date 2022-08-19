package com.dubaipolice.callservices

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telecom.VideoProfile
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.dubaipolice.R
import com.dubaipolice.model.CallerData
import com.dubaipolice.utils.AppConstants


class CallHandler(context: Context) {
    var callManagerContext = context;
    var telecomManager: TelecomManager =
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    lateinit var phoneAccountHandle: PhoneAccountHandle

    @SuppressLint("MissingPermission")
    fun init() {
        val componentName = ComponentName(callManagerContext, CallConnectionService::class.java)
        phoneAccountHandle = PhoneAccountHandle(componentName, "123456")
        val phoneAccount = PhoneAccount.builder(phoneAccountHandle, callManagerContext.getString(R.string.app_name))
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER).build()
        telecomManager.registerPhoneAccount(phoneAccount)

    }

     fun checkIsAccountEnabled():Boolean{
            if (ActivityCompat.checkSelfPermission(callManagerContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                val enabledAccounts: List<PhoneAccountHandle> =
                    telecomManager.callCapablePhoneAccounts
                for (account in enabledAccounts) {
                    if (account.componentName.className == CallConnectionService::class.java.canonicalName) {
                         return true
                    }
                }
            }
          return false
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun startOutgoingCall() {
        val extras = Bundle()
        extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
        val componentName = ComponentName(
            callManagerContext.packageName,
            CallConnectionService::class.java.name
        )
        val phoneAccountHandle = PhoneAccountHandle(componentName, "estosConnectionServiceId")
        val test = Bundle()
        val number = "09999999999"
        extras.putString("NAME", "Muthu")
        extras.putString("SESSIONID", "")
        extras.putString("RECEIVERTYPE", "")
        extras.putString("CALLTYPE", "Audio")
        extras.putString("RECEIVERID", "")

        test.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
        test.putInt(
            TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE,
            VideoProfile.STATE_BIDIRECTIONAL
        )
        test.putParcelable(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, extras)
        try {
            if (callManagerContext.checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                telecomManager.placeCall(Uri.parse("tel:$number"), test)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun startIncomingCall(callerData: CallerData) {
        if (callManagerContext.checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            val extras = Bundle()
            val uri = Uri.fromParts(
                PhoneAccount.SCHEME_TEL, "9920200133",
                null
            )
            extras.putString("CALLTYPE", "Audio")
            extras.putString("NAME", "Firoz")
            extras.putString(AppConstants.IntentConstants.CALLER_GROUP_JID, callerData.groupJid)
            extras.putString(AppConstants.IntentConstants.CALLER_GROUP_NAME, callerData.groupName)
            extras.putString(AppConstants.IntentConstants.CALLER_GROUP_ID, callerData.groupId)
            extras.putString(AppConstants.IntentConstants.CALLER_GROUP_SENDER_NAME, callerData.senderName)

            extras.putInt(TelecomManager.EXTRA_INCOMING_VIDEO_STATE, VideoProfile.STATE_AUDIO_ONLY)

            extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri)
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
            extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
            var isCallPermitted: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telecomManager.isIncomingCallPermitted(phoneAccountHandle)
            } else {
                true
            }
            if(checkIsAccountEnabled()) {
                Log.e("startIncomingCall",extras.toString()+"\n"+isCallPermitted)
                telecomManager.addNewIncomingCall(phoneAccountHandle, extras)
            }else {
                val intent = Intent()
                intent.action = TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS
                val componentName = ComponentName(
                    "com.android.server.telecom",
                    "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
                )
                intent.component = componentName
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                callManagerContext.startActivity(intent)
            }

               /* try {
                    Log.e("startIncomingCall: ", extras.toString() + "\n" + isCallPermitted)
                    telecomManager.addNewIncomingCall(phoneAccountHandle, extras)
                } catch (e: SecurityException) {
                    val intent = Intent()
                    intent.action = TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS
                    val componentName = ComponentName(
                        "com.android.server.telecom",
                        "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
                    )
                    intent.component = componentName
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    callManagerContext.startActivity(intent)
                    e.printStackTrace()
                } catch (e: Exception) {
                    Toast.makeText(
                        callManagerContext,
                        "Error occured:" + e.message,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }*/

        } else {
            Log.e("startIncomingCall: ", "Permission not granted")
        }
    }
}