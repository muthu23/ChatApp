package com.dubaipolice.api

import com.dubaipolice.model.*
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.viewmodel.StreamListViewModel
import com.google.gson.JsonObject
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiRequests {
    interface FIELD {
        companion object {
            const val COUNTRY_CODE = "country_code"
            const val PHONE = "phone_no"
            const val EMAIL = "email"
            const val EMAIL_OR_PHONE = "emailOrPhone"
            const val NAME = "name"
            const val OLD_PASSWORD = "old_password"
            const val PASSWORD = "password"
            const val ROLE = "role"
            const val FCM_TOKEN = "fcm_token"
            const val OTP = "otp"
            const val TOKEN = "token"
            const val AUTHORIZATION = "authorization"
            const val ACCESS_TOKEN = "access_token"
            const val LOGIN_PLATFORM = "login_platform"

            //Ride parameters
            const val PICKUP_ADDRESS = "pick_up_address"
            const val PICKUP_LOCATION = "pick_up_location"
            const val DROP_ADDRESS = "drop_address"
            const val DROP_LOCATION = "drop_location"
            const val ITEM_DESCRIPTION = "item_description"
            const val SPECIAL_INSTRUCTION = "special_instruction"
            const val TRUCK_TYPE_ID = "truck_type_id"
            const val ESTIMATED_FEE = "estimated_fee"
        }
    }

    @Headers("Content-Type: application/json")
    @POST("mobile/login")
    fun login(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Body requestBody: JsonObject
    ): Call<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("mobile/send-help-notification")
    fun sendHelpNotification(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>

    @Headers("Content-Type: application/json")
    @PATCH("mobile/sound-file-update")
    fun updateNotificationSound(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>

    @Headers("Content-Type: application/json")
    @PATCH("mobile/show-notification")
    fun showNotification(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>


    @PATCH("mobile/update-read/{id}")
    fun readNotification(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("id") userId: String?,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>

    @POST("mobile/mass-read-update-notification")
    fun readAllNotification(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>

    @Headers("Content-Type: application/json")
    @POST("mobile/verify-login")
    fun verifyLoginUsingOtp(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Body requestBody: JsonObject
    ): Call<LoginResponse>

    @Headers("Content-Type: application/json")
    @POST("mobile/change-passcode")
    fun createAndChangePasscode(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>

    @PATCH("mobile/update-user")
    fun updateProfile(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body file: RequestBody
    ): Call<ProfileResponse>

    @GET("verify-token")
    fun verifyToken(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String
    ): Call<VerifyTokenResponse>

    @Headers("Content-Type: application/json")
    @POST("mobile/verify-passcode")
    fun verifyPasscode(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>

    @GET("mobile/force-upgrade")
    fun forceUpgradeApp(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String
    ): Call<ForceUpgradeResponse>

    @Headers("Content-Type: application/json")
    @PATCH("mobile/firebase-token-update")
    fun updateFcmToken(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>

    @GET("mobile/get-all-cms")
    fun getAllCmsList(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
    ): Call<GetAllCmsListResponse>


    @GET("mobile/group-list/{id}")
    fun getGroupList(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("id") userId: String?
    ): Call<GroupListResponse>

    @Multipart
    @POST("mobile/upload-attachment")
    fun uploadAttachments(
        @Part("group_id") group_id: RequestBody,
        @Part uploadFile: MultipartBody.Part,
        @Part fileThumb: MultipartBody.Part?,
        @Part("duration") duration: RequestBody?,
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String
    ): Call<MediaUploadResponse>

    @GET("mobile/group/{id}")
    fun getGroupInfo(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("id") groupId: String?
    ): Call<GroupInfoResponse>

    @GET("mobile/group/{id}")
    fun getGroupData(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("id") groupId: String?
    ): Observable<GroupInfoResponse>

    //delete group member
    @DELETE("mobile/group-member/{group_id}/{user_id}")
    fun deleteGroupMember(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("group_id") groupId: String?,
        @Path("user_id") userId: String?
    ): Call<CommonResponse>

    @GET("mobile/users/{group_id}/{organization_id}")
    fun getAllMember(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("group_id") groupId: String?,
        @Path("organization_id") organizationId: String?
    ): Call<UserListResponse>

    @GET("mobile/users/{group_id}/{organization_id}")
    suspend fun getAllMemberPaging(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("group_id") groupId: String?,
        @Path("organization_id") organizationId: String?,
        @Query("page") page: Int,
        @Query("search") search: String?,
        @Query("size") size: Int,
        ): UserListResponse

    @GET("mobile/attachment-list/{group_id}")
    suspend fun getMediaListPaging(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("group_id") groupId: String?,
        @Query("media_type") mediaType: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): GroupMediaResponse

    //add group members
    @POST("mobile/group-members")
    fun addMemberList(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body requestBody: AddMembers
    ): Call<CommonResponse>

    @Headers("Content-Type: application/json")
    @PATCH("mobile/mute-group/{id}")
    fun muteGroup(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("id") groupId: String?,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>

    @PATCH("mobile/update-group/{id}")
    fun updateGroup(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("id") groupId: String?,
        @Body file: RequestBody
    ): Call<UpdateGroupResponse>

    @Headers("Content-Type: application/json")
    @POST("mobile/add-location")
    fun sendLocationToServer(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>


    @GET("mobile/go-live/{groupid}")
    fun goLiveStreaming(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("groupid") groupId: String?
    ): Call<GoLiveStreamingResponse>

    @GET("mobile/stop-live-stream/{groupid}")
    fun stopLiveStreaming(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("groupid") groupId: String?
    ): Call<GoLiveStreamingResponse>

    @Headers("Content-Type: application/json")
    @POST("mobile/store-live-stream")
    fun saveLiveStreaming(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>

    @Headers("Content-Type: application/json")
    @POST("mobile/rtsp-conversion")
    fun getHlsLink(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body link: StreamListViewModel.Link
    ): Call<HLSLinkResponse>

    @Headers("Content-Type: application/json")
    @POST("mobile/stop-conversion")
    fun stopStream(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body hlsLink: HLSLink
    ): Call<CommonResponse>

    @GET("mobile/rtsp-streams/{group_id}")
    suspend fun getVideoStreamPaging(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("group_id") groupId: String?,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): VideoStreamResponse

    @GET("mobile/get-live-streams/{group_id}")
    suspend fun getLiveStreamList(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("group_id") groupId: String?,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): LiveStreamListResponse

    @GET("get-notifications-list")
    suspend fun getNotificationList(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): NotificationsListResponse

    @Headers("Content-Type: application/json")
    @POST("mobile/send-call-notification")
    fun sendCallNotification(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Body requestBody: JsonObject
    ): Call<CommonResponse>

    @GET("mobile/logout/{userid}")
    fun logout(
        @Header(AppConstants.FIELD.LANGUAGE_ID) language: String,
        @Header(AppConstants.FIELD.AUTHORIZATION) token: String,
        @Path("userid") userId: String?
    ): Call<CommonResponse>

}
