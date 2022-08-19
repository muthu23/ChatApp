package com.dubaipolice.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.devlomi.record_view.OnRecordListener
import com.devlomi.record_view.RecordButton
import com.devlomi.record_view.RecordView
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.callback.HandleChatItemClick
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityChatBinding
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.db.model.MemberInfoTable
import com.dubaipolice.db.model.MessageInfoTable
import com.dubaipolice.downloader.CustomDownloadManager
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.model.Contact
import com.dubaipolice.pickers.contact.ContactPicker
import com.dubaipolice.service.RtpService
import com.dubaipolice.utils.*
import com.dubaipolice.utils.AppConstants.Permissions.PICK_IMAGE_REQUEST_CODE
import com.dubaipolice.utils.AppConstants.Permissions.PICK_IMAGE_VIDEO_REQUEST_CODE
import com.dubaipolice.utils.AppConstants.Permissions.PICK_PDF_FILE_REQUEST_CODE
import com.dubaipolice.utils.AppConstants.Permissions.PICK_VIDEO_REQUEST_CODE
import com.dubaipolice.utils.AppConstants.Permissions.REQUEST_CHECK_LOCATION_SETTINGS
import com.dubaipolice.utils.FileUtils
import com.dubaipolice.utils.Utils.bitmapToFile
import com.dubaipolice.utils.Utils.formatMilliSecond
import com.dubaipolice.utils.Utils.getBitmapFromRes
import com.dubaipolice.utils.Utils.getFileName
import com.dubaipolice.utils.Utils.getMediaDuration
import com.dubaipolice.utils.Utils.getMimeType
import com.dubaipolice.view.adapter.ChatAdapter
import com.dubaipolice.viewmodel.ChatActivityViewModel
import com.dubaipolice.wrapper.Resource
import com.dubaipolice.xmpp.XmppChatCallback
import com.github.piasy.rxandroidaudio.AudioRecorder
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.*
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.io.*
import java.net.URL
import kotlin.math.roundToInt


@DelicateCoroutinesApi
@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity(), HandleClick, XmppChatCallback,
    HandleChatItemClick {

    lateinit var chatActivityViewModel: ChatActivityViewModel
    lateinit var binding: ActivityChatBinding

    lateinit var mContext: Context

    private var chatList: ArrayList<MessageInfoTable> = ArrayList()
    private var chatAdapter: ChatAdapter? = null

    private val handler = Handler(Looper.getMainLooper())


    private lateinit var bottomSheetDialog: BottomSheetDialog

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    private var broadcastReceiver: BroadcastReceiver? = null

    var mAudioRecorder: AudioRecorder?= null
    var mAudioFile: File?= null

    companion object {
        var groupData: GroupInfoTable? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this@ChatActivity, R.layout.activity_chat)
        chatActivityViewModel = ViewModelProvider(this)[ChatActivityViewModel::class.java]

        binding.lifecycleOwner = this@ChatActivity
        //binding.loginViewModel= loginViewModel
        binding.clickHandle = this

        mContext = this

        getExtras()

        MainApplication.activeRoomJid = groupData!!.groupJid!!

        initRecyclerView()

        addSwipeToRecyclerView()

        receiveMessage()

        getUnreadMessages()

        initTypingIndicatorWithEdittext()

        registerBroadcastReceiver()

        val recordView = findViewById<View>(R.id.record_view) as RecordView
        val recordButton = findViewById<View>(R.id.record_button) as RecordButton

//IMPORTANT

//IMPORTANT
        recordButton.setRecordView(recordView)

        recordView.setLockEnabled(true);
        recordView.setRecordLockImageView(findViewById(R.id.record_lock));

        recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart")
                startAudio()
                binding.layoutSend.visibility= View.INVISIBLE
            }

            override fun onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel")
                binding.layoutSend.visibility= View.VISIBLE
                GlobalScope.launch { stopAudio() }
            }

            override fun onFinish(recordTime: Long, limitReached: Boolean) {
                //Stop Recording..
                //limitReached to determine if the Record was finished when time limit reached.
                //val time: String = getHumanTimeText(recordTime)
                Log.d("RecordView", "onFinish")
                binding.layoutSend.visibility= View.VISIBLE

                GlobalScope.launch {

                    stopAudio()

                    val audioUri = FileProvider.getUriForFile(
                        mContext,
                        "com.dubaipolice.fileprovider", mAudioFile!!)

                    val audioFile = mAudioFile
                    val thumbFilePath = getThumbForFiles(mContext, audioUri, audioFile!!.name)
                    Log.e("AudiRe", audioUri.toString())
                    Log.e("AudiRem", audioFile.toString())
                    sendMessage(
                        AppConstants.TYPE_AUDIO_CHAT,
                        audioFile.toString(),
                        thumbFilePath.toString(),
                        getFileSize(audioFile!!),
                        formatMilliSecond(getMediaFileDuration(audioFile!!)),
                        null,
                        null,
                        null,
                        null
                    )

                }


                //Log.d("RecordTime", time)
            }

            override fun onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond")
                binding.layoutSend.visibility= View.VISIBLE
                GlobalScope.launch { stopAudio() }
            }
        })

        recordView.setOnBasketAnimationEndListener {
            Log.d(
                "RecordView",
                "Basket Animation Finished"
            )
        }

        requestPresenceSubscription()

    }

    private fun requestPresenceSubscription() {

        //var membersList: List<MemberInfoTable>? = emptyList()
        GlobalScope.launch {

            val membersList = AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.memberInfoTableDao()
                ?.getAllMembers(groupData!!.groupId.toString())

            if(membersList != null && membersList.size > 0)
            {
                MainApplication.connection?.subscribeUser(membersList)
            }

        }

    }

    fun startAudio()
    {

        GlobalScope.launch {

            mAudioRecorder= null
            mAudioRecorder = AudioRecorder.getInstance();
            val recorderDirectory = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "/SoundRecorder/")
            recorderDirectory.mkdirs()
            if (recorderDirectory.exists()) {
                val count = recorderDirectory.listFiles().size
                mAudioFile = File(recorderDirectory.path + "/recording$count.m4a")
                Log.v("path", mAudioFile.toString())
            }
            mAudioRecorder!!.prepareRecord(
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.AudioEncoder.AAC,
                mAudioFile)
            mAudioRecorder!!.startRecord()

        }

    }

    fun stopAudio()
    {
        mAudioRecorder!!.stopRecord()
    }

    /**
     * This function is used to register for broadcase events
     * to be received from background media downloads worker
     *
     *
     */
    private fun registerBroadcastReceiver() {

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                Log.i("Receiver", "Broadcast received: $action")
                if (action == AppConstants.MEDIA_DOWNLOADED_BROADCAST) {

                    val extras = intent.extras

                    if (extras != null) {
                        val mediaPath = extras.getString(AppConstants.MEDIA_DOWNLOADED_MEDIA_PATH)
                        val groupJid = extras.getString(AppConstants.MEDIA_DOWNLOADED_GROUP_JID)
                        val messageId = extras.getString(AppConstants.MEDIA_DOWNLOADED_MESSAGE_ID)

                        if (mediaPath != null && groupJid != null && messageId != null) {
                            refreshDownloadMedia(mediaPath, groupJid, messageId)
                        }

                    }

                } else if (action == AppConstants.CALL_STARTED_BROADCAST) {

                    val extras = intent.extras

                    if (extras != null) {
                        val groupJid = extras.getString(AppConstants.IntentConstants.GROUP_JID)

                        if (groupJid == groupData?.groupJid) {
                            groupData?.callActive= "true"
                            loadGroupData()
                        }

                    }

                } else if (action == AppConstants.CALL_ENDED_BROADCAST) {

                    val extras = intent.extras

                    if (extras != null) {
                        val groupJid = extras.getString(AppConstants.IntentConstants.GROUP_JID)

                        if (groupJid == groupData?.groupJid) {
                            groupData?.callActive= "false"
                            loadGroupData()
                        }

                    }

                }

            }
        }


        //registering the broadcast receiver
        val intentFilter = IntentFilter()
        intentFilter.addAction(AppConstants.MEDIA_DOWNLOADED_BROADCAST)
        intentFilter.addAction(AppConstants.CALL_STARTED_BROADCAST)
        intentFilter.addAction(AppConstants.CALL_ENDED_BROADCAST)

        registerReceiver(broadcastReceiver, intentFilter)
        /*registerReceiver(
            broadcastReceiver,
            IntentFilter(AppConstants.MEDIA_DOWNLOADED_BROADCAST)
        )*/
    }

    /**
     * This function is used to unregister broadcast receiver
     */
    private fun unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
    }

    /**
     * It is used to initialize location services
     *
     */
    private fun startLocationServices() {
        if (Utils.isGooglePlayServicesAvailable(
                this,
                AppConstants.Permissions.REQUEST_CHECK_GOOGLE_PLAY_SERVICE
            )
        ) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
            createLocationRequest()
            createLocationCallback()
            checkLocationSettings()
        }
    }

    /**
     * It is used to check if gps is enabled or not
     * if not enabled request to enable gps
     *
     */
    private fun checkLocationSettings() {

        //Check location settings based on location request above
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(mContext)
        val task = client.checkLocationSettings(builder.build())

        //if location settings matched with location request (GPS On)
        task.addOnSuccessListener { startLocationUpdates() }

        //if location settings not matched with location request (GPS Off)
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    //use this from activity
                    e.startResolutionForResult(
                        this@ChatActivity,
                        REQUEST_CHECK_LOCATION_SETTINGS
                    )
                    //use this from fragment
                    //startIntentSenderForResult(resolvable.getResolution().getIntentSender(), REQUEST_CHECK_LOCATION_SETTINGS, null, 0, 0, 0, null);
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    /**
     * This function is called when a location is fetched
     *
     */
    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                stopLocationUpdates()
                MyProgressDialog.dismiss()
                val location = locationResult.lastLocation
                sendMessage(
                    AppConstants.TYPE_LOCATION_CHAT,
                    null,
                    null,
                    null,
                    null,
                    null,
                    location.latitude.toString(),
                    location.longitude.toString(),
                    null
                )
                bottomSheetDialog.dismiss()

            }
        }
    }

    /**
     *
     * callback function when location permission is granted
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == AppConstants.Permissions.LOCATION_PERMISSION_REQUEST_CODE) {

            if (ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                MyProgressDialog.show(mContext)
                startLocationServices()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    AppConstants.Permissions.LOCATION_PERMISSION_REQUEST_CODE
                )
            }

            return
        }
    }

    /**
     * It is used to create location request
     *
     */
    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * This function is used to request for location
     *
     */
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    /**
     * This function is used to stop the request for location
     *
     */
    private fun stopLocationUpdates() {
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }

    private fun getUnreadMessages() {

        val unreadChatList = AppDatabase.getAppDatabase(applicationContext)
            ?.messageInfoTableDao()
            ?.getUnreadChats(
                SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!,
                groupData!!.groupJid!!
            )

        //Log.e("UnreadList", unreadChatList!!.size.toString())

        if (unreadChatList != null && unreadChatList.isNotEmpty()) {
            unreadChatList.forEachIndexed { _, chats ->

                //MainApplication.connection!!.updateDisplayedAt(DateUtils.getDateTimeWithTimeZoneFromTimestamp(System.currentTimeMillis().toString())!!, chats.messageId!!)
                MainApplication.connection!!.sendDisplayed(chats!!)

                //update unread chat count
                GlobalScope.launch {
                    AppDatabase.getAppDatabase(MainApplication.appContext)
                        ?.messageInfoTableDao()
                        ?.updateMessageReadForSenderSide(chats.messageId!!)
                    MainApplication.connection!!.updateUnreadChatCount(chats)

                }

            }
        }

    }

    private fun getExtras() {

        val extras = intent.extras
        Log.e("GetExtra", "get")
        if (extras != null) {

            groupData =
                extras.getSerializable(AppConstants.IntentConstants.GROUP_DATA) as GroupInfoTable

            loadGroupData()

        } else {
            Log.e("Groupname", "NULL")
        }


    }

    private fun loadGroupData() {
        binding.tvGroupName.text = groupData?.groupName

        /*  Log.e("Groupname", "NOTNULL")
          Log.e("Groupname", groupData!!.groupName!!)
          Log.e("Groupjid", groupData!!.groupJid!!)*/

        Glide.with(mContext).load(groupData?.imageUrl).placeholder(
            AppCompatResources.getDrawable(
                mContext,
                R.drawable.ic_profile
            )
        ).into(binding.groupImage)

        if(groupData!!.callActive == "true")
        {
            binding.imgAudioCall.visibility= View.GONE
            binding.tvJoinCall.visibility= View.VISIBLE
        }
        else
        {
            binding.tvJoinCall.visibility= View.GONE
            binding.imgAudioCall.visibility= View.VISIBLE
        }

        if(groupData?.groupActiveStatus == "true")
        {
            binding.tvReadOnlyMode.visibility= View.GONE
            binding.layoutSend.visibility= View.VISIBLE
            binding.recordButton.visibility= View.VISIBLE
        }
        else
        {
            binding.tvReadOnlyMode.visibility= View.VISIBLE
            binding.layoutSend.visibility= View.GONE
            binding.recordButton.visibility= View.GONE
        }

    }

    override fun onResume() {
        super.onResume()
        loadGroupData()
    }

    private fun initTypingIndicatorWithEdittext() {

        binding.etChat.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (s.toString().trim() == "") {
                    binding.imgSend.visibility = View.GONE
                    binding.recordButton.visibility = View.VISIBLE
                    binding.imgCameraCapture.visibility = View.VISIBLE
                } else {
                    binding.imgSend.visibility = View.VISIBLE
                    binding.recordButton.visibility = View.INVISIBLE
                    binding.imgCameraCapture.visibility = View.GONE
                }

                // Cancel any previous place prediction requests
                handler.removeCallbacksAndMessages(null)

                // Start a new place prediction request in 300 ms
                // handler.postDelayed({

                val messageInfoTable = MessageInfoTable()
                messageInfoTable.groupJid = groupData!!.groupJid!!
                messageInfoTable.senderJid =
                    SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)
                messageInfoTable.senderName =
                    SharedPref.readString(AppConstants.KEY_FIRST_NAME) + " " + SharedPref.readString(
                        AppConstants.KEY_LAST_NAME
                    )
                messageInfoTable.senderFirstName =
                    SharedPref.readString(AppConstants.KEY_FIRST_NAME)
                messageInfoTable.senderLastName =
                    SharedPref.readString(AppConstants.KEY_LAST_NAME)

                MainApplication.connection!!.sendTyping(messageInfoTable)


                // }, 300)

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        /*binding.etChat.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                Log.e("Has Focus", "")
            } else {
                Log.e("Has Focus", "")
            }
        }*/

    }

    private fun receiveMessage() {
        MainApplication.connection!!.setXmppCallbackForChat(this)
    }

    private fun initRecyclerView() {

        val myChatList = AppDatabase.getAppDatabase(applicationContext)
            ?.messageInfoTableDao()
            ?.getChatList(groupData!!.groupJid!!)

        if (myChatList != null && myChatList.isNotEmpty()) {
            chatList.addAll(myChatList.toMutableList())
        }

        Log.e("ChatSize", chatList.size.toString())


        binding.recyclerView.layoutManager = LinearLayoutManager(
            mContext,
            LinearLayoutManager.VERTICAL,
            false
        )

        chatAdapter = ChatAdapter(mContext, chatList, this)
        binding.recyclerView.adapter = chatAdapter
        scrollToBottom()

    }

    private fun addSwipeToRecyclerView() {

        val messageSwipeController = MessageSwipeController(this, object : SwipeControllerActions {
            override fun showReplyUI(position: Int) {
                //showQuotedMessage(messageList[position])
                showQuotedMessage(chatList[position])
            }
        })

        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

    }

    private fun showQuotedMessage(message: MessageInfoTable) {
        binding.etChat.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager?.showSoftInput(binding.etChat, InputMethodManager.SHOW_IMPLICIT)
        binding.tvQuotedUserName.text = message.senderName
        binding.tvQuotedMsg.text = message.messageText
        binding.replyLayout.visibility = View.VISIBLE

    }

    override fun clickHandle(v: View) {
        when (v.id) {
            R.id.back -> {
                onBackPressed()
            }
            R.id.btnCancelReply -> {
                binding.replyLayout.visibility = View.GONE
            }
            R.id.imgAttachment -> {
                showBottomSheet()
            }
            R.id.lnr_camera -> {
                val i = Intent(mContext, CameraActivity::class.java)
                //i.putExtra("options", options)
                startActivityForResult(i, PICK_IMAGE_VIDEO_REQUEST_CODE)
                bottomSheetDialog.dismiss()
            }
            R.id.lnr_photos_videos -> {
                openImageAndVideoPicker()
                /*FilePickerBuilder.instance
                    .setMaxCount(1) //optional
                    .enableImagePicker(true)
                    .enableVideoPicker(true)
                    .enableCameraSupport(false)
                    .setActivityTheme(droidninja.filepicker.R.style.LibAppTheme) //optional
                    .pickPhoto(this, FilePickerConst.REQUEST_CODE_PHOTO_VIDEO)*/
                bottomSheetDialog.dismiss()
            }
            R.id.lnr_document -> {
                openDocumentPicker()
                bottomSheetDialog.dismiss()
                /*FilePickerBuilder.instance
                    .setMaxCount(1) //optional
                    .enableImagePicker(false)
                    .enableVideoPicker(false)
                    .enableCameraSupport(false)
                    .enableDocSupport(true)
                    .setActivityTheme(droidninja.filepicker.R.style.LibAppTheme) //optional
                    .pickFile(this, FilePickerConst.REQUEST_CODE_DOC)
                bottomSheetDialog.dismiss()*/
            }
            R.id.lnr_location -> {

                bottomSheetDialog.dismiss()

                if (ContextCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    MyProgressDialog.show(mContext)

                    startLocationServices()
                } else {

                    ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS,
                        AppConstants.Permissions.LOCATION_PERMISSION_REQUEST_CODE
                    )
                }

                /* val intent = PlacePicker.IntentBuilder()
                         .setLatLong(40.748672, -73.985628)
                         .showLatLong(true)
                         .setMapRawResourceStyle(R.raw.map_style)
                         .setMapType(MapType.NORMAL)
                         .build(this)
                     startActivityForResult(intent, Constants.PLACE_PICKER_REQUEST)*/
            }
            R.id.lnr_contact -> {

                val contactPicker: ContactPicker? = ContactPicker.create(
                    activity = this,
                    onContactPicked = {
                        Log.v("contactPicker ", it.name + " " + it.number)
                        val contact = Contact()
                        contact.firstName = it.name
                        contact.lastName = ""
                        contact.phone = it.number
                        contact.email = ""
                        sendMessage(
                            AppConstants.TYPE_CONTACT_CHAT,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            contact
                        )
                        bottomSheetDialog.dismiss()
                    },
                    onFailure = { Log.v("contactPicker ", "" + it.localizedMessage) })
                contactPicker?.pick()
            }
            R.id.cancelAttachment -> {
                bottomSheetDialog.dismiss()

            }
            R.id.imgSend -> {
                if (!TextUtils.isEmpty(binding.etChat.text.toString().trim())) {
                    sendMessage(
                        AppConstants.TYPE_TEXT_CHAT,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
                }
            }

            R.id.imgCameraCapture -> {
                val i = Intent(mContext, CameraActivity::class.java)
                //i.putExtra("options", options)
                startActivityForResult(i, PICK_IMAGE_VIDEO_REQUEST_CODE)

              /*  val videoOption = VideoOptions()
                videoOption.videoDurationLimitInSeconds = 60
                val options = Options().apply {
                    ratio = Ratio.RATIO_AUTO                                    //Image/video capture ratio
                    count = 1                                                   //Number of images to restrict selection count
                    spanCount = 4                                               //Number for columns in grid
                    path = "Pix/Camera"                                         //Custom Path For media Storage
                    isFrontFacing = false                                       //Front Facing camera on start
                    videoOptions = videoOption              //Duration for video recording
                    mode = Mode.All                                             //Option to select only pictures or videos or both
                    flash = Flash.Auto                                          //Option to select flash type
                    preSelectedUrls = ArrayList() //Pre selected Image Urls
                }
                showCameraActivity(options)*/
            }

            R.id.layoutGroupHeader -> {
                val i = Intent(mContext, GroupInfoActivity::class.java)
                i.putExtra(AppConstants.IntentConstants.GROUP_DATA, groupData)
                mContext.startActivity(i)

            }
            R.id.img_video_call -> {
                /*val callManager = CallHandler(applicationContext)
                if (callManager.checkIsAccountEnabled()) {
                    callManager.init()
                    callManager.startIncomingCall()
                } else {
                    val intent = Intent()
                    intent.action = TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS
                    val componentName = ComponentName(
                        "com.android.server.telecom",
                        "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
                    )
                    intent.component = componentName
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    mContext.startActivity(intent)
                }*/

             /*   val i = Intent(mContext, VideoStreamGoLiveBackgroundActivity::class.java)
                i.putExtra(AppConstants.IntentConstants.GROUP_ID, groupData!!.groupId.toString())
                mContext.startActivity(i)*/

                if(groupData?.groupActiveStatus == "true")
                {

                    if (isMyServiceRunning(RtpService::class.java)) {

                        RtpService.groupId.let {

                            if(RtpService.groupId == groupData!!.groupId.toString())
                            {
                                val i = Intent(mContext, VideoStreamGoLiveBackgroundActivity::class.java)
                                i.putExtra(AppConstants.IntentConstants.GROUP_ID, groupData!!.groupId.toString())
                                mContext.startActivity(i)
                            }
                            else
                            {
                                Utils.showToast(mContext, "Live streaming already in progress in other group")
                            }

                        }

                    } else {
                        val i = Intent(mContext, VideoStreamGoLiveBackgroundActivity::class.java)
                        i.putExtra(AppConstants.IntentConstants.GROUP_ID, groupData!!.groupId.toString())
                        mContext.startActivity(i)
                    }

                }
                else
                {
                    Utils.showToast(mContext, getString(R.string.group_is_in_read_only_mode))
                }

            }

            R.id.imgAudioCall -> {

               /* val i = Intent(mContext, AudioConferenceActivity::class.java)
                i.putExtra(AppConstants.IntentConstants.GROUP_DATA, groupData!!)
                mContext.startActivity(i)*/
                //jitsiMeetAudioConference()

                if(groupData?.groupActiveStatus == "true")
                {
                    sendCallNotification()
                }
                else
                {
                    Utils.showToast(mContext, getString(R.string.group_is_in_read_only_mode))
                }


                //this function to be called from push notification
                //when call started notification received
               /* var callerData= CallerData()
                callerData.groupName= groupData!!.groupName!!
                callerData.groupJid= groupData!!.groupJid!!
                callerData.groupId= groupData!!.groupId!!
                callerData.senderName= "Firoz"

                val callManager = CallHandler(applicationContext)
                if (callManager.checkIsAccountEnabled()) {
                    callManager.init()
                    callManager.startIncomingCall(callerData)
                } else {
                    val intent = Intent()
                    intent.action = TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS
                    val componentName = ComponentName(
                        "com.android.server.telecom",
                        "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
                    )
                    intent.component = componentName
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    mContext.startActivity(intent)
                }*/

            }

            R.id.tvJoinCall -> {

                jitsiMeetAudioConference()

            }


        }
    }

    private fun jitsiMeetAudioConference() {

       /* val hangupExistingCallBroadcastIntent = Intent("org.jitsi.meet.HANG_UP")
        //muteBroadcastIntent.putExtra("muted", muted)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(hangupExistingCallBroadcastIntent)
*/

        MainApplication.activeCallGroupJid = groupData!!.groupJid!!

        val userInfo= JitsiMeetUserInfo()
        userInfo.displayName= String.format(
            "%s %s", SharedPref.readString(
                AppConstants.KEY_FIRST_NAME
            ), SharedPref.readString(AppConstants.KEY_LAST_NAME)
        )
        if(!TextUtils.isEmpty(SharedPref.readString(AppConstants.KEY_PROFILE_PHOTO)))
        {
            userInfo.avatar= URL(SharedPref.readString(AppConstants.KEY_PROFILE_PHOTO))
        }

        val options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL("https://dbvoicedev.iworklab.com"))
            .setRoom(Utils.getGroupFromJid(groupData!!.groupJid!!))
            //.setRoom("subhash0983")
            //.setServerURL(URL("https://meet.jit.si"))
            //.setRoom("firozaudio")
            //.setRoom("https://meet.jit.si/firozaudi")
            .setAudioMuted(false)
            .setVideoMuted(true)
            .setAudioOnly(true)
            //.setConfigOverride("requireDisplayName", true)
            //.setConfigOverride("disableInviteFunctions", false)
            .setFeatureFlag("invite.enabled", false)
            .setFeatureFlag("chat.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            .setFeatureFlag("video-mute.enabled", false)
            .setFeatureFlag("video-share.enabled", false)
            .setFeatureFlag("welcomepage.enabled", false)
            .setFeatureFlag("live-streaming.enabled", false)
            .setFeatureFlag("call-integration.enabled", false)
            .setUserInfo(userInfo)
            .setSubject(groupData!!.groupName)
            .build()

        JitsiMeetActivity.launch(this, options)

    }

    @Suppress("DEPRECATION")
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun openDocumentPicker() {
        val mimeTypes = arrayOf(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .doc & .docx
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // .ppt & .pptx
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xls & .xlsx
            "text/plain",
            "application/pdf",
            "application/zip"
        )

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

            addCategory(Intent.CATEGORY_OPENABLE)
            //type = "application/pdf"
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

        }

        startActivityForResult(intent, PICK_PDF_FILE_REQUEST_CODE)
    }

    private fun openImageAndVideoPicker() {

//        val mimeTypes = arrayOf(
//            "image/*",
//            "video/*"
//        )
//
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//
//            addCategory(Intent.CATEGORY_OPENABLE)
//            //type = "application/pdf"
//            type = "*/*"
//            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
//
//        }
//
//        startActivityForResult(intent, AppConstants.Permissions.PICK_IMAGE_VIDEO_REQUEST_CODE)

//        val mimeTypes = arrayOf(
//            "image/*",
//            "video/*"
//        )
//
//        val intent = Intent(Intent.ACTION_PICK, android.provider
//            .MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
//
//            type = "video/* image/*"
//            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
//
//
//        }
//
//        startActivityForResult(intent, AppConstants.Permissions.PICK_IMAGE_VIDEO_REQUEST_CODE)

        val items = arrayOf<CharSequence>(
            getString(R.string.select_photo),
            getString(R.string.select_video)
        )
        val builder = AlertDialog.Builder(this@ChatActivity)
        builder.setTitle(getString(R.string.choose_options))
        builder.setCancelable(true)
        builder.setItems(items) { _, item ->
            if (item == 0) {
                pickImage()
            } else if (item == 1) {
                pickVideo()
            }
        }
        builder.show()

    }

    private fun pickImage() {
        val intent = Intent(
            Intent.ACTION_PICK, android.provider
                .MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)


    }

    private fun pickVideo() {

        val intent = Intent(
            Intent.ACTION_PICK, android.provider
                .MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).apply {

            type = "video/*"

        }

        startActivityForResult(intent, PICK_VIDEO_REQUEST_CODE)


    }


    override fun onDestroy() {
        super.onDestroy()
        MainApplication.activeRoomJid = null
        unregisterBroadcastReceiver()
    }

    private fun sendMessage(
        type: String,
        fileUrl: String?,
        fileThumbnail: String?,
        fileSizeInBytes: String?,
        mediaDuration: String?,
        docType: String?,
        latitude: String?,
        longitude: String?,
        contact: Contact?
    ) {

        val message = binding.etChat.text.toString().trim()

        val unixUtcTime = System.currentTimeMillis().toString()
        val formattedTime =
            DateUtils.getDateTimeWithTimeZoneFromTimestamp(System.currentTimeMillis().toString())

        val messageInfoTable = MessageInfoTable()
        messageInfoTable.createdAt = formattedTime
        messageInfoTable.deliveredAt = null
        messageInfoTable.displayedAt = null
        messageInfoTable.groupId = groupData?.groupId.toString()
        messageInfoTable.groupImageUrl = groupData?.imageUrl.toString()
        messageInfoTable.groupJid = groupData?.groupJid
        messageInfoTable.groupName = groupData?.groupName
        messageInfoTable.readStatus = 0
        messageInfoTable.type = type
        messageInfoTable.messageId = Utils.generateRandomUniqueId()
        messageInfoTable.messageText = message
        messageInfoTable.receivedAt = null
        messageInfoTable.refrenceUserId = SharedPref.readInt(AppConstants.KEY_USER_ID)
        messageInfoTable.senderId = SharedPref.readInt(AppConstants.KEY_USER_ID).toString()
        messageInfoTable.senderImageUrl = SharedPref.readString(AppConstants.KEY_PROFILE_PHOTO)
        messageInfoTable.senderJid = SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)
        messageInfoTable.senderName =
            SharedPref.readString(AppConstants.KEY_FIRST_NAME) + " " + SharedPref.readString(
                AppConstants.KEY_LAST_NAME
            )
        messageInfoTable.senderFirstName = SharedPref.readString(AppConstants.KEY_FIRST_NAME)
        messageInfoTable.senderLastName = SharedPref.readString(AppConstants.KEY_LAST_NAME)
        messageInfoTable.sentAt = null //update time in utc after msg sent
        messageInfoTable.timestamp = formattedTime
        messageInfoTable.unixTimestamp = unixUtcTime

        //media
        if (type == AppConstants.TYPE_IMAGE_CHAT ||
            type == AppConstants.TYPE_VIDEO_CHAT ||
            type == AppConstants.TYPE_AUDIO_CHAT
        ) {

            messageInfoTable.messageText = AppConstants.StanzaConstants.MEDIA_BODY
            messageInfoTable.mediaUrlLocal = fileUrl
            messageInfoTable.mediaThumbImageUrl = fileThumbnail
            messageInfoTable.mediaSize = fileSizeInBytes
            messageInfoTable.mediaDuration = mediaDuration

        }

        if (type == AppConstants.TYPE_DOCUMENT_CHAT
        ) {

            messageInfoTable.messageText = AppConstants.StanzaConstants.DOCUMENT_BODY
            messageInfoTable.mediaUrlLocal = fileUrl
            messageInfoTable.mediaThumbImageUrl = fileThumbnail
            messageInfoTable.mediaSize = fileSizeInBytes
            messageInfoTable.docMimeType = docType

        }

        if (type == AppConstants.TYPE_LOCATION_CHAT
        ) {

            messageInfoTable.messageText = AppConstants.StanzaConstants.LOCATION_BODY
            messageInfoTable.latitude = latitude
            messageInfoTable.longitude = longitude

        }

        if (type == AppConstants.TYPE_CONTACT_CHAT
        ) {

            messageInfoTable.messageText = AppConstants.StanzaConstants.CONTACT_BODY
            messageInfoTable.contactFirstName = contact?.firstName
            messageInfoTable.contactLastName = contact?.lastName
            messageInfoTable.contactPhone = contact?.phone
            messageInfoTable.contactEmail = contact?.email

        }

        addChatToList(messageInfoTable)

        MainApplication.connection!!.addMessageToDb(messageInfoTable)
        MainApplication.connection!!.updateLastMessageInGroup(
            messageInfoTable.messageText!!,
            messageInfoTable.unixTimestamp!!,
            messageInfoTable.messageId!!,
            messageInfoTable.type!!,
            messageInfoTable.senderJid!!,
            messageInfoTable.senderFirstName!!,
            messageInfoTable.groupJid!!
        )

        //media and doc
        if (type == AppConstants.TYPE_IMAGE_CHAT ||
            type == AppConstants.TYPE_VIDEO_CHAT ||
            type == AppConstants.TYPE_AUDIO_CHAT ||
            type == AppConstants.TYPE_DOCUMENT_CHAT
        ) {
            MainApplication.connection!!.uploadMediaFiles(messageInfoTable)
        } else {
            MainApplication.connection!!.sendMessageMultiUser(messageInfoTable)
            binding.etChat.setText("")
        }


    }

    private fun showReceivedMessage(message: MessageInfoTable) {

        addChatToList(message)

    }

    private fun addChatToList(message: MessageInfoTable) {
        chatList.add(message)

        //chatAdapter.notifyDataSetChanged();

        runOnUiThread {
            chatAdapter!!.notifyItemInserted(chatAdapter!!.itemCount)
        }

        scrollToBottom()
    }

    private fun scrollToBottom() {
        runOnUiThread {
            binding.recyclerView.scrollToPosition(chatAdapter!!.itemCount - 1)
        }
    }

    override fun receivedMessage(message: MessageInfoTable) {

        if (message.groupJid == groupData?.groupJid) {

            showReceivedMessage(message)

        }

    }

    override fun sentMessage(message: MessageInfoTable) {
        //addIncomingMessageInRecycler(message!!)
        Log.e("MSGCHATSent", "sent")

        if (message.groupJid == groupData?.groupJid) {

            chatList.forEachIndexed { index, e ->

                println("$e at $index")

                if (e.messageId == message.messageId) {
                    chatList[index].sentAt = message.sentAt
                    runOnUiThread {
                        Log.e("messageIdMatched2 at", index.toString())
                        chatAdapter!!.notifyItemChanged(index)

                    }
                    return@forEachIndexed
                }

            }

        }

    }

    fun refreshDownloadMedia(mediaUrl: String, groupJid: String, messageId: String) {
        //addIncomingMessageInRecycler(message!!)
        Log.e("MSGDownload", "Download")

        if (groupJid == groupData?.groupJid) {

            chatList.forEachIndexed { index, e ->

                println("$e at $index")

                if (e.messageId == messageId) {
                    chatList[index].mediaUrlLocal = mediaUrl
                    runOnUiThread {
                        Log.e("messageIdMatched2 at", index.toString())
                        chatAdapter!!.notifyItemChanged(index)

                    }
                    return@forEachIndexed
                }

            }

        }

    }

    override fun deliveredMessage(message: MessageInfoTable) {

        if (message.groupJid == groupData?.groupJid) {

            chatList.forEachIndexed { index, e ->

                println("$e at $index")

                if (e.messageId == message.messageId) {
                    chatList[index].deliveredAt = message.deliveredAt
                    runOnUiThread {
                        Log.e("messageIdMatched at", index.toString())
                        chatAdapter!!.notifyItemChanged(index)

                    }

                    return@forEachIndexed
                }

            }

        }

    }

    override fun displayedMessage(message: MessageInfoTable) {


        if (message.groupJid == groupData?.groupJid) {

            chatList.forEachIndexed { index, e ->

                println("$e at $index")

                if (e.messageId == message.messageId) {
                    chatList[index].displayedAt = message.displayedAt
                    runOnUiThread {
                        Log.e("messageIdMatched at", index.toString())
                        chatAdapter!!.notifyItemChanged(index)

                    }

                    return@forEachIndexed
                }

            }

        }

    }

    override fun typingIndicator(message: MessageInfoTable) {

        if (message.groupJid == groupData?.groupJid) {

            runOnUiThread {
                binding.tvTypingStatus.text =
                    message.senderFirstName.plus(" " + getString(R.string.is_typing))

                Handler(Looper.getMainLooper()).postDelayed({

                    binding.tvTypingStatus.text = ""

                }, 5000)

            }

        }

    }


    @SuppressLint("InflateParams")
    private fun showBottomSheet() {
        try {
            bottomSheetDialog = BottomSheetDialog(this)
            val bottomSheetView =
                this.layoutInflater.inflate(R.layout.attachment_bottom_sheet, null)
            bottomSheetDialog.setContentView(bottomSheetView)
            val bottomSheetDialogFrameLayout =
                bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetDialogFrameLayout?.background = null
            bottomSheetDialog.show()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createFileFromUri(selectedUri: Uri): File {
        val parcelFileDescriptor =
            contentResolver.openFileDescriptor(selectedUri, "r", null)
        val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
        val file: File =
            FileUtils.createFile(mContext, contentResolver.getFileName(selectedUri, mContext))!!
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        return file

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_LOCATION_SETTINGS -> if (resultCode == RESULT_OK && data != null) {
                startLocationUpdates()

            }
            PICK_PDF_FILE_REQUEST_CODE -> if (resultCode == RESULT_OK && data != null) {

                data.data?.also { uri ->
                    // Perform operations on the document using its URI.

                    GlobalScope.launch {

                        val selectedFile = createFileFromUri(uri)
                        Log.e("FilePath", selectedFile.toString())
                        Log.e("FileUri", uri.toString())
                        Log.e("FileExtension", Utils.getFileExtension(selectedFile.name))
                        val thumbFilePath = getThumbForFiles(mContext, uri, selectedFile.name)
                        sendMessage(
                            AppConstants.TYPE_DOCUMENT_CHAT,
                            selectedFile.toString(),
                            thumbFilePath?.path.toString(),
                            getFileSize(selectedFile),
                            formatMilliSecond(getMediaFileDuration(selectedFile)),
                            Utils.getFileExtension(selectedFile.name),
                            null,
                            null,
                            null
                        )

                    }

                }

            }
            PICK_IMAGE_REQUEST_CODE -> if (resultCode == RESULT_OK && data != null) {

                data.data?.also { uri ->
                    // Perform operations on the document using its URI.

                    GlobalScope.launch {

                        var selectedFile = createFileFromUri(uri)
                        Log.e("FilePath", selectedFile.toString())
                        Log.e("FileUri", uri.toString())

                        //compress image
                        val valueDeferred = GlobalScope.async {

                            Compressor.compress(mContext, selectedFile) {
                                default()
                                destination(selectedFile.absoluteFile)
                            }

                        }

                        runBlocking {

                            selectedFile = valueDeferred.await()

                        }

                        sendMessage(
                            AppConstants.TYPE_IMAGE_CHAT,
                            selectedFile.toString(),
                            selectedFile.toString(),
                            getFileSize(selectedFile),
                            "0.0",
                            null,
                            null,
                            null,
                            null
                        )

                    }

                }

            }
            PICK_VIDEO_REQUEST_CODE -> if (resultCode == RESULT_OK && data != null) {
                data.data?.also { uri ->
                    // Perform operations on the document using its URI.
                    GlobalScope.launch {
                        val fileSize = getFileSizeFromUri(uri)
                        //if video less than 50 mb than upload
                        if (fileSize < 52428800) {

                            val selectedFile = createFileFromUri(uri)
                            Log.e("FilePath", selectedFile.toString())
                            Log.e("FileUri", uri.toString())

                            //val fileSize= getFileSize(selectedFile)
                            //Log.e("FileSizeInBytes", fileSize)

                            var thumbFilePath =
                                getThumbForFiles(mContext, uri, selectedFile.name)

                            //compress thumbnail image
                            val valueDeferred = GlobalScope.async {
                                Compressor.compress(mContext, thumbFilePath!!)
                            }
                            runBlocking {
                                thumbFilePath = valueDeferred.await()
                            }

                            sendMessage(
                                AppConstants.TYPE_VIDEO_CHAT,
                                selectedFile.toString(),
                                thumbFilePath.toString(),
                                getFileSize(selectedFile),
                                formatMilliSecond(getMediaFileDuration(selectedFile)),
                                null,
                                null,
                                null,
                                null
                            )

                        } else {
                            runOnUiThread {
                                Utils.showToast(
                                    mContext,
                                    resources.getString(R.string.video_limit_msg)
                                )
                            }
                        }

                    }

                }

            }
            PICK_IMAGE_VIDEO_REQUEST_CODE -> if (resultCode == RESULT_OK && data != null) {
                val isImage = data.getBooleanExtra("isImage", false)
                val selectedImage = data.getStringExtra("selectedFilePath")
                ///Log.e("TAG", "showCameraFragment: $selectedUri")

                selectedImage?.let { selectedImage ->
                    // Perform operations on the document using its URI.
                    if (isImage) {
                        GlobalScope.launch {
                            var selectedFile = File(selectedImage)
                            Log.e("FilePath", selectedFile.toString())

                            //compress image
                            val valueDeferred = GlobalScope.async {
                                Compressor.compress(mContext, selectedFile) {
                                    default()
                                    destination(selectedFile.absoluteFile)
                                }
                            }
                            runBlocking {
                                selectedFile = valueDeferred.await()
                            }
                            sendMessage(
                                AppConstants.TYPE_IMAGE_CHAT,
                                selectedFile.toString(),
                                selectedFile.toString(),
                                getFileSize(selectedFile),
                                "0.0",
                                null,
                                null,
                                null,
                                null
                            )

                        }
                    } else {
                        GlobalScope.launch {
                            val fileSize = getFileSize(File(selectedImage)).toLong()
                            //if video less than 50 mb than upload
                            if (fileSize < 52428800) {

                                val selectedFile = File(selectedImage)
                                Log.e("FilePath", selectedFile.toString())

                                //val fileSize= getFileSize(selectedFile)
                                //Log.e("FileSizeInBytes", fileSize)
                                val uri = FileProvider.getUriForFile(
                                    mContext,
                                    "com.dubaipolice.fileprovider", selectedFile)

                                var thumbFilePath =
                                    getThumbForFiles(mContext, uri, selectedFile.name)

                                //compress thumbnail image
                                val valueDeferred = GlobalScope.async {
                                    Compressor.compress(mContext, thumbFilePath!!)
                                }
                                runBlocking {
                                    thumbFilePath = valueDeferred.await()
                                }

                                sendMessage(
                                    AppConstants.TYPE_VIDEO_CHAT,
                                    selectedFile.toString(),
                                    thumbFilePath.toString(),
                                    getFileSize(selectedFile),
                                    formatMilliSecond(getMediaFileDuration(selectedFile)),
                                    null,
                                    null,
                                    null,
                                    null
                                )

                            } else {
                                runOnUiThread {
                                    Utils.showToast(
                                        mContext,
                                        resources.getString(R.string.video_limit_msg)
                                    )
                                }
                            }

                        }
                    }
                }

            }
        }

    }

    private fun getFileSizeFromUri(uri: Uri): Long {

        try {
            val cursor = contentResolver.query(uri, null, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {

                //val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                //Log.e("FileName", cursor.getString(nameIndex))
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                var byteSize = -1L
                if (!cursor.isNull(sizeIndex)) {
                    byteSize = cursor.getLong(sizeIndex)
                }
                if (byteSize > -1) {
                    Log.e("FileSize", byteSize.toString())
                    cursor.close()
                    return byteSize
                }

            } else {

                Log.e("FileName", "null")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0

    }

    private fun getMediaFileDuration(file: File): Long {
        return when {
            getMimeType(file.name).contains("video") || getMimeType(file.name).contains("audio") -> {
                file.getMediaDuration(mContext)
            }
            else -> {
                0
            }
        }
    }

    private fun getFileSize(file: File): String {
        val fileSize = file.length()
        val sizeInMb = fileSize / (1024.0 * 1024.0)
        val sizeInMbStr = "%.2f".format(sizeInMb)
        Log.d("getFileSizeAndDuration", "Size=${sizeInMb}MB")
        Log.d("getFileSizeAndDuration", "Size=${sizeInMbStr}MB")
        Log.d("getFileSizeAndDuration", "Size=${sizeInMb.roundToInt()}MB")
        return fileSize.toString()
    }

    private fun getThumbForFiles(context: Context, videoUri: Uri, filename: String): File? {
        Log.e("mimetype", getMimeType(filename))
        Log.e("mimetypefilename", filename)
        val image: Bitmap?
        when {
            getMimeType(filename).contains("video") -> {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, videoUri)
                image =
                    retriever.frameAtTime
                return image?.let { bitmapToFile(context, it, filename) }
            }


            getMimeType(filename).contains("audio") -> {
                image = getBitmapFromRes(mContext, R.drawable.ic_audio_mp3)
                return image?.let { bitmapToFile(context, it, filename) }
            }
            else -> {
                image = getBitmapFromRes(mContext, R.drawable.ic_docs_svgrepo_com)
                return image?.let { bitmapToFile(context, it, filename) }
            }

        }

    }

    private fun checkIsImage(context: Context, uri: Uri): Boolean {
        val contentResolver: ContentResolver = context.contentResolver
        val type: String? = contentResolver.getType(uri)
        if (type != null) {
            return type.startsWith("image/")
        } else {
            var inputStream: InputStream? = null
            try {
                inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val options: BitmapFactory.Options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeStream(inputStream, null, options)
                    return options.outWidth > 0 && options.outHeight > 0
                }
            } catch (e: IOException) {
            } finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    inputStream?.close()
                }
            }
        }
        return false
    }

    override fun itemClickHandle(messageInfoTable: MessageInfoTable) {
        val fileName: String = messageInfoTable.mediaUrl.toString()
            .substring(messageInfoTable.mediaUrl.toString().lastIndexOf("/") + 1)
        val customDownloadManager = CustomDownloadManager(
            notificationId = messageInfoTable.id,
            messageId = messageInfoTable.messageId!!,
            url = messageInfoTable.mediaUrl.toString(),
            fileName = fileName,
            groupJid = messageInfoTable.groupJid!!,
            context = applicationContext
        )
        customDownloadManager.startDownload()
    }

    private fun sendCallNotification()
    {

            MyProgressDialog.show(mContext);

            chatActivityViewModel.groupId.value= groupData?.groupId.toString()

            chatActivityViewModel.sendCallNotificationResponse!!.observe(
                this@ChatActivity,
                Observer<Resource<CommonResponse?>> { commonResponseResource ->
                    MyProgressDialog.dismiss()
                    when (commonResponseResource.status) {
                        Resource.Status.SUCCESS -> {
                            if (commonResponseResource.data!!.isSuccess) {

                                updateGroupCallStatus(
                                    true,
                                    groupData!!.groupJid!!
                                )

                                val intent =
                                    Intent(AppConstants.CALL_STARTED_BROADCAST)
                                intent.putExtra(AppConstants.IntentConstants.GROUP_JID, groupData!!.groupJid!!)
                                MainApplication.appContext.sendBroadcast(intent)

                                jitsiMeetAudioConference()

                            } else {
                                Utils.showToast(mContext, commonResponseResource.data.message!!)
                            }
                        }
                        Resource.Status.LOADING -> {
                        }
                        Resource.Status.ERROR -> {
                            Utils.showToast(mContext, commonResponseResource.message!!)
                        }
                    }
                })

    }

    fun updateGroupCallStatus(isCallActive: Boolean, groupJid: String) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateGroupCallStatus(isCallActive.toString(), groupJid)

        }
    }

}