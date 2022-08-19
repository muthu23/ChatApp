package com.dubaipolice.repository

import androidx.lifecycle.MutableLiveData
import com.dubaipolice.api.ApiClient
import com.dubaipolice.api.ApiRequests
import com.dubaipolice.api.ApiResponseCallback
import com.dubaipolice.api.GenricError
import com.dubaipolice.model.*
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.viewmodel.StreamListViewModel
import com.dubaipolice.wrapper.Resource
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import java.io.File

class Repository {

    private val apiInterface: ApiRequests? = null

    //without custom ApiResponseCallback
    /* fun login(
         phone: String?,
         password: String?
     ): MutableLiveData<Resource<LoginResponse?>>? {
         val data: MutableLiveData<Resource<LoginResponse?>> =
             MutableLiveData<Resource<LoginResponse?>>()
         //paas these data from activity to this function to call this function
         val call: Call<LoginResponse> = ApiClient.request!!.login(phone, password)
         call.enqueue(object : retrofit2.Callback<LoginResponse> {

             override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                 data.value= Resource.success(response.body())
             }

             override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                 data.value= Resource.error("hello")
             }


         })

         return data
     }*/

    //with custom ApiResponseCallback
    fun login(
        parameter: JsonObject?
    ): MutableLiveData<Resource<LoginResponse?>> {
        val data: MutableLiveData<Resource<LoginResponse?>> =
            MutableLiveData<Resource<LoginResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<LoginResponse> =
            ApiClient.request!!.login(Utils.getSelectedLanguage()!!, parameter!!)
        call.enqueue(object : ApiResponseCallback<LoginResponse?>() {

            override fun onSuccess(response: LoginResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun sendHelpNotification(
        parameter: JsonObject?
    ): MutableLiveData<Resource<CommonResponse?>> {
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<CommonResponse> =
            ApiClient.request!!.sendHelpNotification(Utils.getSelectedLanguage()!!,  SharedPref.readString(AppConstants.KEY_TOKEN)!!,parameter!!)
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }
    fun updateNotificationSound(
        parameter: JsonObject?
    ): MutableLiveData<Resource<CommonResponse?>> {
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<CommonResponse> =
            ApiClient.request!!.updateNotificationSound(Utils.getSelectedLanguage()!!,  SharedPref.readString(AppConstants.KEY_TOKEN)!!,parameter!!)
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun showNotification(
        parameter: JsonObject?
    ): MutableLiveData<Resource<CommonResponse?>> {
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<CommonResponse> =
            ApiClient.request!!.showNotification(Utils.getSelectedLanguage()!!,  SharedPref.readString(AppConstants.KEY_TOKEN)!!,parameter!!)
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {
            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }
            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun readNotification(
        parameter: JsonObject?,
        notificationId: Int?
    ): MutableLiveData<Resource<CommonResponse?>> {
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        val call: Call<CommonResponse> =
           // ApiClient.request!!.readNotification(Utils.getSelectedLanguage()!!,  SharedPref.readString(AppConstants.KEY_TOKEN)!!,notificationId.toString(),parameter!!)
            ApiClient.request!!.readNotification(Utils.getSelectedLanguage()!!, SharedPref.readString(AppConstants.KEY_TOKEN)!!, notificationId.toString(),parameter!!)
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }
    fun readAllNotification(
        parameter: JsonObject?,
    ): MutableLiveData<Resource<CommonResponse?>> {
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        val call: Call<CommonResponse> =
           // ApiClient.request!!.readNotification(Utils.getSelectedLanguage()!!,  SharedPref.readString(AppConstants.KEY_TOKEN)!!,notificationId.toString(),parameter!!)
            ApiClient.request!!.readAllNotification(Utils.getSelectedLanguage()!!, SharedPref.readString(AppConstants.KEY_TOKEN)!!,parameter!!)
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }
    fun verifyLoginUsingOtp(
        parameter: JsonObject?
    ): MutableLiveData<Resource<LoginResponse?>> {
        val data: MutableLiveData<Resource<LoginResponse?>> =
            MutableLiveData<Resource<LoginResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<LoginResponse> =
            ApiClient.request!!.verifyLoginUsingOtp(Utils.getSelectedLanguage()!!, parameter!!)
        call.enqueue(object : ApiResponseCallback<LoginResponse?>() {

            override fun onSuccess(response: LoginResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun createAndChangePasscode(
        parameter: JsonObject?
    ): MutableLiveData<Resource<CommonResponse?>> {
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<CommonResponse> = ApiClient.request!!.createAndChangePasscode(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            parameter!!
        )
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun updateProfile(
        filePath: File?
    ): MutableLiveData<Resource<ProfileResponse?>> {
        val data: MutableLiveData<Resource<ProfileResponse?>> =
            MutableLiveData<Resource<ProfileResponse?>>()
        //paas these data from activity to this function to call this function

        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)

        //builder.addFormDataPart(AppConstants.FIELD.NAME, name!!)

        if (filePath != null) {
            builder.addFormDataPart(
                AppConstants.FIELD.PROFILE_PIC,
                filePath.name,
                filePath.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }


        val requestBody = builder.build()

        val call: Call<ProfileResponse> = ApiClient.request!!.updateProfile(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            requestBody
        )
        call.enqueue(object : ApiResponseCallback<ProfileResponse?>() {

            override fun onSuccess(response: ProfileResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun updateLanguage(
        languageId: Int?
    ): MutableLiveData<Resource<ProfileResponse?>> {
        val data: MutableLiveData<Resource<ProfileResponse?>> =
            MutableLiveData<Resource<ProfileResponse?>>()
        //paas these data from activity to this function to call this function

        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)

        builder.addFormDataPart(AppConstants.FIELD.LANGUAGE_ID_TO_SAVE, languageId.toString())

        val requestBody = builder.build()

        val call: Call<ProfileResponse> = ApiClient.request!!.updateProfile(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            requestBody
        )
        call.enqueue(object : ApiResponseCallback<ProfileResponse?>() {

            override fun onSuccess(response: ProfileResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun verifyToken(
    ): MutableLiveData<Resource<VerifyTokenResponse?>> {
        val data: MutableLiveData<Resource<VerifyTokenResponse?>> =
            MutableLiveData<Resource<VerifyTokenResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<VerifyTokenResponse> = ApiClient.request!!.verifyToken(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!
        )
        call.enqueue(object : ApiResponseCallback<VerifyTokenResponse?>() {

            override fun onSuccess(response: VerifyTokenResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun verifyPasscode(
        parameter: JsonObject?
    ): MutableLiveData<Resource<CommonResponse?>>{
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<CommonResponse> = ApiClient.request!!.verifyPasscode(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            parameter!!
        )
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun forceUpgradeApp(
    ): MutableLiveData<Resource<ForceUpgradeResponse?>> {
        val data: MutableLiveData<Resource<ForceUpgradeResponse?>> =
            MutableLiveData<Resource<ForceUpgradeResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<ForceUpgradeResponse> = ApiClient.request!!.forceUpgradeApp(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!
        )
        call.enqueue(object : ApiResponseCallback<ForceUpgradeResponse?>() {

            override fun onSuccess(response: ForceUpgradeResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun updateFcmToken(
        parameter: JsonObject?
    ): MutableLiveData<Resource<CommonResponse?>>{
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<CommonResponse> = ApiClient.request!!.updateFcmToken(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            parameter!!
        )
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }


    fun getAllCms(
    ): MutableLiveData<Resource<GetAllCmsListResponse?>>{
        val data: MutableLiveData<Resource<GetAllCmsListResponse?>> =
            MutableLiveData<Resource<GetAllCmsListResponse?>>()
        val call: Call<GetAllCmsListResponse> = ApiClient.request!!.getAllCmsList(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,)
        call.enqueue(object : ApiResponseCallback<GetAllCmsListResponse?>() {
            override fun onSuccess(response: GetAllCmsListResponse?) {
                data.value = Resource.success(response)
            }
            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }
        })

        return data
    }






    fun getGroupList(
    ): MutableLiveData<Resource<GroupListResponse?>> {
        val data: MutableLiveData<Resource<GroupListResponse?>> =
            MutableLiveData<Resource<GroupListResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<GroupListResponse> = ApiClient.request!!.getGroupList(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            SharedPref.readInt(AppConstants.KEY_USER_ID).toString()
        )
        call.enqueue(object : ApiResponseCallback<GroupListResponse?>() {

            override fun onSuccess(response: GroupListResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun getGroupInfo(groupId: String?): MutableLiveData<Resource<GroupInfoResponse?>> {
        val data: MutableLiveData<Resource<GroupInfoResponse?>> =
            MutableLiveData<Resource<GroupInfoResponse?>>()
        val call: Call<GroupInfoResponse> = ApiClient.request!!.getGroupInfo(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            groupId
        )
        call.enqueue(object : ApiResponseCallback<GroupInfoResponse?>() {

            override fun onSuccess(response: GroupInfoResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun deleteGroupMember(
        groupId: String?,
        userId: String?
    ): MutableLiveData<Resource<CommonResponse?>> {
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        val call: Call<CommonResponse> = ApiClient.request!!.deleteGroupMember(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            groupId,
            userId
        )
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {
            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }
            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }
        })
        return data
    }


    fun getAllMember(
        groupId: String?,
        organization_id: String?
    ): MutableLiveData<Resource<UserListResponse?>> {
        val data: MutableLiveData<Resource<UserListResponse?> > =
            MutableLiveData<Resource<UserListResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<UserListResponse> = ApiClient.request!!.getAllMember(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            groupId,
            organization_id
        )
        call.enqueue(object : ApiResponseCallback<UserListResponse?>() {
            override fun onSuccess(response: UserListResponse?) {
                data.value = Resource.success(response)
            }
            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }
        })
        return data
    }


    fun addMemberList(
        users: AddMembers
    ): MutableLiveData<Resource<CommonResponse?>> {
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<CommonResponse> = ApiClient.request!!.addMemberList(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            users
        )
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {
            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }
            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }
        })
        return data
    }

    fun muteGroup(
        groupId: String?,
        parameter: JsonObject?
    ): MutableLiveData<Resource<CommonResponse?>>{
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<CommonResponse> = ApiClient.request!!.muteGroup(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            groupId!!,
            parameter!!
        )
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {
            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }
            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun updateGroup(
        groupId: String,
        groupName: String?,
        groupAlertLevel: String?,
        groupLogo: File?,
    ): MutableLiveData<Resource<UpdateGroupResponse?>> {
        val data: MutableLiveData<Resource<UpdateGroupResponse?>> =
            MutableLiveData<Resource<UpdateGroupResponse?>>()
        //paas these data from activity to this function to call this function

        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)

        if(groupName != null)
        {
            builder.addFormDataPart(AppConstants.FIELD.NAME, groupName)
        }

        if(groupAlertLevel != null)
        {
            builder.addFormDataPart(AppConstants.FIELD.ALERT_LEVEL, groupAlertLevel)
        }

        if (groupLogo != null) {
            builder.addFormDataPart(
                AppConstants.FIELD.PROFILE_PIC,
                groupLogo.name,
                groupLogo.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }


        val requestBody = builder.build()

        val call: Call<UpdateGroupResponse> = ApiClient.request!!.updateGroup(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            groupId,
            requestBody
        )
        call.enqueue(object : ApiResponseCallback<UpdateGroupResponse?>() {

            override fun onSuccess(response: UpdateGroupResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }



    fun getHLSLink(
        mediaLink: StreamListViewModel.Link
    ): MutableLiveData<Resource<HLSLinkResponse?>> {
        val data: MutableLiveData<Resource<HLSLinkResponse?>> =
            MutableLiveData<Resource<HLSLinkResponse?>>()
        val call: Call<HLSLinkResponse> = ApiClient.request!!.getHlsLink(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            mediaLink
        )
        call.enqueue(object : ApiResponseCallback<HLSLinkResponse?>() {
            override fun onSuccess(response: HLSLinkResponse?) {
                data.value = Resource.success(response)
            }
            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }
        })
        return data
    }

    fun stopStream(
        hlsLink: HLSLink
    ): MutableLiveData<Resource<CommonResponse?>> {
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        val call: Call<CommonResponse> = ApiClient.request!!.stopStream(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            hlsLink
        )
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {
            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }
            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }
        })
        return data
    }

    fun goLiveStreaming(
        groupId: String?
    ): MutableLiveData<Resource<GoLiveStreamingResponse?>> {
        val data: MutableLiveData<Resource<GoLiveStreamingResponse?>> =
            MutableLiveData<Resource<GoLiveStreamingResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<GoLiveStreamingResponse> = ApiClient.request!!.goLiveStreaming(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            groupId
        )
        call.enqueue(object : ApiResponseCallback<GoLiveStreamingResponse?>() {

            override fun onSuccess(response: GoLiveStreamingResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun stopLiveStreaming(
        groupId: String?
    ): MutableLiveData<Resource<GoLiveStreamingResponse?>> {
        val data: MutableLiveData<Resource<GoLiveStreamingResponse?>> =
            MutableLiveData<Resource<GoLiveStreamingResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<GoLiveStreamingResponse> = ApiClient.request!!.stopLiveStreaming(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            groupId
        )
        call.enqueue(object : ApiResponseCallback<GoLiveStreamingResponse?>() {

            override fun onSuccess(response: GoLiveStreamingResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun sendCallNotification(
        parameter: JsonObject?
    ): MutableLiveData<Resource<CommonResponse?>>{
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        //paas these data from activity to this function to call this function

        val call: Call<CommonResponse> = ApiClient.request!!.sendCallNotification(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            parameter!!
        )
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

    fun logout(userId: String?): MutableLiveData<Resource<CommonResponse?>> {
        val data: MutableLiveData<Resource<CommonResponse?>> =
            MutableLiveData<Resource<CommonResponse?>>()
        val call: Call<CommonResponse> = ApiClient.request!!.logout(
            Utils.getSelectedLanguage()!!,
            SharedPref.readString(AppConstants.KEY_TOKEN)!!,
            userId
        )
        call.enqueue(object : ApiResponseCallback<CommonResponse?>() {

            override fun onSuccess(response: CommonResponse?) {
                data.value = Resource.success(response)
            }

            override fun onError(msg: GenricError?) {
                data.value = Resource.error(msg!!.message)
            }


        })

        return data
    }

}