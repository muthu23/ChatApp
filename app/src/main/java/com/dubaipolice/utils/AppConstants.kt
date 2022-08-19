package com.dubaipolice.utils

import android.Manifest

object AppConstants {
    //Shared Preference Keys
    const val KEY_IS_LOGGEDIN = "isLoggedIn"
    const val KEY_USER_ID = "user_id"
    const val KEY_FIRST_NAME = "first_name"
    const val KEY_LAST_NAME = "last_name"
    const val KEY_USER_NAME = "user_name"
    const val KEY_PROFILE_PHOTO = "profile_photo"
    const val KEY_ROLE_ID = "role_id"
    const val KEY_ORGANIZATION_ID = "organization_id"
    const val KEY_TOKEN = "token"
    const val KEY_LANGUAGE = "language"
    const val KEY_IS_PASSCODE_GENERATED = "is_passcode_generated"
    const val KEY_IS_APP_EXIT_TIMEOUT = "is_app_exit_timeout"
    const val KEY_IS_APP_MAP_DOWNLOADED = "is_app_map_downloaded"
    const val KEY_FIREBASE_TOKEN = "firebase_token"
    const val FCM_TOKEN = "fcm_token"
    const val KEY_SOUND_FILE = "sound_file"
    const val KEY_IS_SHOW_NOTIFICATION = "is_show_notification"

    const val KEY_LAST_FETCHED_CHAT_TIMESTAMP = "lastFetchedChatTimestamp"

    const val KEY_XMPP_USER_JID = "xmpp_user_jid"
    const val KEY_XMPP_USER_PASSWORD = "xmpp_user_password"

    const val DEVICE_TYPE_ANDROID = "android"
    const val LANGUAGE_ENGLISH = "EN"
    const val LANGUAGE_ARABIC = "AR"
    const val LANGUAGE_ENGLISH_ID = 2
    const val LANGUAGE_ARABIC_ID = 1
    const val END_USER = 1
    const val IT_USER = 2

    const val PIP_ACTION_LOCAL_BROADCAST = "pip_action_local_broadcast"
    const val PIP_ACTION_MAIN_BROADCAST = "pip.actions"
    const val PIP_ACTION_TYPE = "action_type"
    const val PIP_ACTION_PLAY = "play"
    const val PIP_ACTION_PAUSE = "pause"
    const val PIP_ACTION_STOP = "stop"


    const val CHAT_UPLOADER_SERVICE = "chat_uploader_service"

    const val TYPE_TEXT_CHAT = "1"
    const val TYPE_IMAGE_CHAT = "2"
    const val TYPE_VIDEO_CHAT = "3"
    const val TYPE_AUDIO_CHAT = "4"
    const val TYPE_DOCUMENT_CHAT = "5"
    const val TYPE_LOCATION_CHAT = "6"
    const val TYPE_CONTACT_CHAT = "7"

    const val MUTE_GROUP = "mute_group"

    const val COLOR_SCHEME = "color_scheme"
    const val FONT_SCHEME = "font_scheme"

    /*For media Downloads*/
    const val workerName = "downloadWorker"
    const val notificationChannelName: String = "WorkManager Notifications"
    const val notificationChannelId = "work manager channel"

    const val notificationMapChannelName: String = "Map WorkManager Notifications"
    const val notificationMapChannelId = "map work manager channel"


    const val notificationChannelDescription = "Shows notifications whenever work events occur"
    const val notificationTitle = "Download Request Status"
    const val uploadNotificationChannelId = "upload channel"

    const val MEDIA_DOWNLOADED_BROADCAST = "media_downloaded_broadcast"
    const val MEDIA_DOWNLOADED_MEDIA_PATH = "media_downloaded_media_path"
    const val MEDIA_DOWNLOADED_MESSAGE_ID = "media_downloaded_message_id"
    const val MEDIA_DOWNLOADED_GROUP_JID = "media_downloaded_group_jid"

    const val PRESENCE_RECEIVED_BROADCAST = "presence_received_broadcast"
    const val REFRESH_GROUP_BROADCAST = "refresh_group_broadcast"

    const val MISSED_CALL_BROADCAST = "missed_call_broadcast"
    const val CALL_STARTED_BROADCAST = "call_started_broadcast"
    const val CALL_ENDED_BROADCAST = "call_ended_broadcast"

    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"

    const val STREAM_TIME = "stream_time"
    const val STREAM_TIMER_BROADCAST = "stream_timer_broadcast"
    const val STREAM_STOP_BROADCAST = "stream_stop_broadcast"

    object StanzaConstants {

        //Message Stanza Parameters
        const val MESSAGE_ID = "messageId"
        const val TO = "to"
        const val GROUP_ID = "groupId"
        const val GROUP_JID = "groupJid"
        const val GROUP_IMAGE_URL = "groupImageUrl"
        const val TIMESTAMP = "timestamp"
        const val UNIXTIMESTAMP = "unixtimestamp"
        const val MEDIA_TYPE = "mediaType"
        const val SENDER_NAME = "senderName"
        const val SENDER_FIRST_NAME = "senderFirstName"
        const val SENDER_LAST_NAME = "senderLastName"
        const val SENDER_IMAGE_URL = "senderImageUrl"
        const val SENDER_JID = "senderJid"
        const val SENDER_ID = "senderId"

        const val DELIVERED = "delivered"

        const val DISPLAYED = "displayed"

        const val DATA = "data"
        const val DATA_JABBER = "data:jabber"

        const val MEDIA_DATA = "mediaData"
        const val MEDIA_DATA_JABBER = "mediadata:jabber"

        const val DOCUMENT_DATA = "docData"
        const val DOCUMENT_DATA_JABBER = "docData:jabber"

        const val LOCATION_DATA = "locationData"
        const val LOCATION_DATA_JABBER = "locationData:jabber"

        const val CONTACT_DATA = "contactData"
        const val CONTACT_DATA_JABBER = "contactData:jabber"

        //media
        const val MEDIA_URL = "mediaUrl"
        const val MEDIA_SIZE = "mediaSize"
        const val MEDIA_DURATION = "mediaDuration"
        const val MEDIA_THUMB_IMAGE_URL = "mediaThumbImageUrl"

        //document
        const val DOCUMENT_URL = "docUrl"
        const val DOCUMENT_SIZE = "docSize"
        const val DOCUMENT_TYPE = "docType"

        //location
        const val LOCATION_LATITUDE = "latitude"
        const val LOCATION_LONGITUDE = "longitude"

        //contact
        const val CONTACT_FIRST_NAME = "firstname"
        const val CONTACT_LAST_NAME = "lastname"
        const val CONTACT_EMAIL = "email"
        const val CONTACT_PHONE = "phone"
        const val CONTACT_MIDDLE_NAME = "middleName"

        const val MEDIA_BODY = "Media Message"
        const val DOCUMENT_BODY = "Document Message"
        const val LOCATION_BODY = "Location Message"
        const val CONTACT_BODY = "Contact Message"

        const val COMPOSING = "composing"
        const val COMPOSING_NAMESPACE = "http://jabber.org/protocol/chatstates"


        const val PRESENCE_STATUS = "presencestatus"
        const val PRESENCE_STATUS_JABBER = "presencestatus:jabber"
        const val PRESENCE_STATUS_REQUEST = "StatusRequest"
        const val PRESENCE_STATUS_RESPONSE = "StatusResponse"

        const val DATA_REFRESH_TRIGGER = "groupdatarefreshtrigger"
        const val DATA_REFRESH = "data"
        const val DATA_REFRESH_JABBER = "data:jabber"

    }


    object IntentConstants {
        //sign up and sign in activity
        const val GROUP_DATA = "group_Data"
        const val HLS_LINK_DATA = "hls_link_data"

        const val GROUP_ID = "group_id"

        const val GROUP_JID = "group_jid"

        const val CALLER_GROUP_JID = "call_group_jid"
        const val CALLER_GROUP_NAME = "call_group_name"
        const val CALLER_GROUP_ID = "call_group_id"
        const val CALLER_GROUP_SENDER_NAME = "call_sender_name"
        const val CALLER_DATA = "call_group_data"

        const val PRESENCE_USER_JID = "presence_user_jid"
        const val PRESENCE_USER_STATUS = "presence_user_status"
        const val PRESENCE_USER_ONLINE = "presence_user_online"
        const val PRESENCE_USER_OFFLINE = "presence_user_offline"

        const val CAMERA_IMAGE_PATH = "camera_image_path"
        const val PHONE_NUMBER = "phone"
        const val PHONE_COUNTRY_CODE = "phone_country_code"
        const val OTP_FORGOT_PASS = "otpforgotpass"
        const val TOKEN_RESET_PASS = "tokenresetpass"

        //PickActivity
        const val LOCATION_TYPE = "location_type"
        const val PICKUP_LOCATION = "pickup_location"
        const val DESTINATION_LOCATION = "destination_location"
        const val PRIMARY_TEXT = "primary_text"
        const val SECONDARY_TEXT = "secondary_text"
        const val LATITUDE = "lat"
        const val LONGITUDE = "lang"

        //data carry through screens on homepage
        const val SOURCE_LAT = "source_lat"
        const val SOURCE_LANG = "source_lang"
        const val DESTINATION_LAT = "destination_lat"
        const val DESTINATION_LANG = "destination_lang"
        const val SOURCE_ADDRESS = "source_address"
        const val DESTINATION_ADDRESS = "destination_address"
        const val TRUCK_TYPE = "truck_type"
        const val ITEM_DESCRIPTION = "item_description"
        const val SPECIAL_INSTRUCTION = "special_instruction"
        const val ITEM_IMAGES = "item_images"
        const val CONFIRMING_DRIVER = "confirming_driver"
        const val RIDE_ID = "ride_id"
        const val DRIVER_ID = "driver_id"
        const val END_PIN = "end_pin"
        const val DRIVER_NAME = "driver_name"
        const val DRIVER_PHOTO_URL = "driver_photo_url"
        const val IS_FROM_HOME_TO_DRIVER_DETAILS = "is_from_home_to_driver_details"

        //Driver ride status
        const val RIDE_STATUS_SEARCHING_FOR_DRIVER = 1
        const val RIDE_STATUS_DRIVER_ACCEPTED = 2
        const val RIDE_STATUS_DRIVER_ARRIVED = 3
        const val RIDE_STATUS_RIDE_STARTED = 4
        const val RIDE_STATUS_RIDE_CANCELLED = 5
        const val RIDE_STATUS_RIDE_COMPLETED = 6
    }

    object Permissions {
        const val GALLERY_REQUEST_CODE = 1
        const val CAMERA_REQUEST_CODE = 2
        const val AUTOCOMPLETE_PICKUP_REQUEST_CODE = 23
        const val AUTOCOMPLETE_DESTINATION_REQUEST_CODE = 24
        const val AUTOCOMPLETE_GOOGLE_MAP_REQUEST_CODE = 27
        const val REQUEST_CHECK_LOCATION_SETTINGS = 25
        const val REQUEST_CHECK_GOOGLE_PLAY_SERVICE = 26
        const val LOCATION_PERMISSION_REQUEST_CODE = 102
        const val READ_WRITE_PERMISSION_REQUEST_CODE = 103
        const val CAMERA_PERMISSION_REQUEST_CODE = 104
        const val PHONE_STATE_PERMISSION_REQUEST_CODE = 105
        const val PERMISSION_ALL_REQUEST_CODE = 101

        const val PICK_PDF_FILE_REQUEST_CODE = 241
        const val PICK_IMAGE_VIDEO_REQUEST_CODE = 242
        const val PICK_IMAGE_REQUEST_CODE = 243
        const val PICK_VIDEO_REQUEST_CODE = 244

        val PERMISSIONS_ALL = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE
        )

    }

    interface FIELD {

        companion object {
            const val EMAIL = "email"
            const val OTP = "otp"
            const val DEVICE_OS_TYPE = "device_os_type"
            const val DEVICE_OS_VERSION = "device_os_version"
            const val LANGUAGE_ID = "language-id"
            const val LANGUAGE_ID_TO_SAVE = "languageId"
            const val AUTHORIZATION = "Authorization"
            const val OLD_PASSCODE = "old_passcode"
            const val NEW_PASSCODE = "new_passcode"
            const val PROFILE_PIC = "profile_pic"
            const val NAME = "name"
            const val PASSCODE = "passcode"
            const val FCM_TOKEN = "fcm_token"
            const val MUTE = "mute"
            const val ALERT_LEVEL = "alert_level"

            const val LATITUDE = "latitude"
            const val LONGITUDE = "longitude"
            const val USER_ID = "user_id"

            const val LIVE_STREAM_LINK = "live_stream_link"
            const val GROUP_ID = "group_id"
            const val TITLE = "title"
            const val SOUND_FILE = "sound_file"
            const val GROUP_IDS = "group_ids"
            const val ISREAD = "is_read"
            const val NOTIFICATION = "notification"



        }
    }

}
