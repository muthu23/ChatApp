package com.dubaipolice.xmpp

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.api.ApiClient
import com.dubaipolice.db.AppDatabase
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.db.model.MemberInfo
import com.dubaipolice.db.model.MemberInfoTable
import com.dubaipolice.db.model.MessageInfoTable
import com.dubaipolice.model.MediaUploadResponse
import com.dubaipolice.service.OfflineChatUploader
import com.dubaipolice.service.UploaderRequestBody
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.DateUtils
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import de.datlag.mimemagic.MimeData
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jivesoftware.smack.*
import org.jivesoftware.smack.packet.*
import org.jivesoftware.smack.roster.PresenceEventListener
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterListener
import org.jivesoftware.smack.roster.SubscribeListener
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.chatstates.ChatState
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension
import org.jivesoftware.smackx.muc.MucEnterConfiguration
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jivesoftware.smackx.ping.PingManager
import org.jxmpp.jid.BareJid
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.FullJid
import org.jxmpp.jid.Jid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory


@OptIn(DelicateCoroutinesApi::class)
class XmppCustomConnection(
    context: Context?,
    private val userName: String?,//here 'userName' is jabberId
    private val password: String?,
) : ConnectionListener, StanzaListener {

    private var connection: XMPPTCPConnection? = null
    private var configBuilder: XMPPTCPConnectionConfiguration.Builder
    private var multiUserChatManager: MultiUserChatManager? = null
    private var multiUserChat: MultiUserChat? = null
    private var multiUserChatList: ArrayList<MultiUserChat> = ArrayList()
    private lateinit var nickName: Resourcepart
    private var context: Context? = null

    private var xmppChatCallback: XmppChatCallback? = null
    private var xmppGroupCallback: XmppGroupCallback? = null

    companion object {
        internal var isConnectedFirstTime = false
        internal var isConnecting: Boolean? = true
        private var currentXmppResource = ""
        private val TAG = XmppCustomConnection::class.java.simpleName
        private const val RECONNECT_TIME_INTERVAL: Long = 10000

    }

    init {
        this.context = context

        isConnecting = true

        val inputStream = context!!.resources.openRawResource(R.raw.my_cert_bks)
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(inputStream, "dbchatdev".toCharArray())

        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
        trustManagerFactory.init(keyStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagerFactory.trustManagers, null)

        configBuilder = XMPPTCPConnectionConfiguration.builder()
        configBuilder.setUsernameAndPassword(this.userName, this.password)
        //configBuilder.setXmppDomain("n1.iworklab.com") //main
        //configBuilder.setHost("n1.iworklab.com")  //main
        configBuilder.setXmppDomain("dbchatdev.iworklab.com") //main
        configBuilder.setHost("dbchatdev.iworklab.com")  //main
        //configBuilder.setXmppDomain(serviceName) //extra
        //configBuilder.setHostAddress(addr)  //extra
        //configBuilder.setHostnameVerifier(verifier)  //extra
        configBuilder.setPort(5223)
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
        configBuilder.setSocketFactory(sslContext.socketFactory)
        configBuilder.enableDefaultDebugger()
        currentXmppResource = "resource"
        //configBuilder.setResource(currentXmppResource)
        configBuilder.setSendPresence(true)
    }

    fun doConnect() {

        GlobalScope.launch {
            try {
                connect("XmppConnection constructor")
            } catch (e: XMPPException) {
                Log.e(TAG, "INIT XMPP EXCEPTION:" + e.message)
            } catch (e: IOException) {
                Log.e(TAG, "INIT IO EXCEPTION:" + e.message)
            } catch (e: SmackException) {
                Log.e(TAG, "INIT SMACK EXCEPTION:" + e.message)
            } catch (e: InterruptedException) {
                Log.e(TAG, "INIT Interrupted EXCEPTION:" + e.message)
            }
            /**To return on main thread
             *  uiThread {
            toast(result)
            }*/
        }

    }


    @Throws(XMPPException::class, IOException::class, SmackException::class)
    private fun connect(tag: String) {

        //removing the listener
        if (connection != null) {
            //connection!!.removeAsyncStanzaListener(this@XmppConnection)
            connection?.removeConnectionListener(this@XmppCustomConnection)
            connection?.removeStanzaAcknowledgedListener(this@XmppCustomConnection)
            connection = null
        }
        connection = XMPPTCPConnection(configBuilder.build())
        //connection!!.packetReplyTimeout = 30000
        connection!!.addConnectionListener(this@XmppCustomConnection)
        connection!!.addStanzaAcknowledgedListener(this@XmppCustomConnection)
        connection!!.addAsyncStanzaListener(stanzaListener, null)

        try {
            connection!!.connect()

            if (connection!!.isConnected) {
                Log.e("app", "conn done")
            }

            // Log into the server
            connection!!.login()

            //will return us whether the user has been authenticated with the server.
            if (connection!!.isAuthenticated) {
                Log.e("app", "Auth done")
            }

            //All of the connection and auth call are wrapped
            //in a try catch so that we can catch any failure and take proper action.

            // Disconnect from the server
            //connection.disconnect();

            if (connection!!.isConnected && connection!!.isAuthenticated) {
                Log.e("app", "Connection and Auth done")
                //now send message and receive message code here

                sendOnlinePresence()
                //multiUserChat()
                multiUserChatWithList()
                //receiveMessageOneToOne()
                getRoaster()

                //add ping manager
                val pingManager = PingManager.getInstanceFor(connection)
                pingManager.pingInterval = 5 //in seconds
                pingManager.pingMyServer()

            }


            if (tag == "reconnect") {
                //reconnectedListener.onXMPPReConnected()
            }
        } catch (e: XMPPException) {
            Log.e(TAG, "INIT XMPP EXCEPTION:" + e.message)
            if (!isConnectedFirstTime) {
                isConnectedFirstTime = true
                reConnect()
            }
        } catch (e: IOException) {
            Log.e(TAG, "INIT IO EXCEPTION:" + e.message)
            if (!isConnectedFirstTime) {
                isConnectedFirstTime = true
                reConnect()
            }
        } catch (e: SmackException) {
            Log.e(TAG, "INIT SMACK EXCEPTION:" + e.message)
            if (!isConnectedFirstTime) {
                isConnectedFirstTime = true
                reConnect()
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "INIT Interrupted EXCEPTION:" + e.message)
            if (!isConnectedFirstTime) {
                isConnectedFirstTime = true
                reConnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "--Connect Exception:" + e.message)
            //reconnectedListener.onXMPPConnectionError(e.message)
        }

        /*if (connection != null && connection!!.isConnected && connection!!.isAuthenticated) {
            Log.i(TAG, "----login succesful....")
            //We'll save login user's credentials (jabberId, password etc.)here,
            // currently I'm saving on login button click, which is not recommended.
        }*/

        //ProviderManager.addIQProvider("ping", "urn:xmpp:ping", PingProvider())
        //sendOnlinePresence()

    }

    private fun reConnect() {
        if (connection != null && connection!!.isConnected && connection!!.isAuthenticated) {
            Log.i(TAG, "Xmpp already connected and authenticated")
            return
        }
        Log.i(TAG, "Xmpp reconnect attempt")
        //here we send flag to disconnect to stop wait bar in chat window
        /*try {
            this.connection!!.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Xmpp reconnect attempt error " + e.message)
        }*/

        GlobalScope.launch {
            while (true) {
                try {
                    if (connection != null && connection!!.isConnected && connection!!.isAuthenticated) {
                        Log.i(TAG, "Xmpp already connected and authenticated")
                        return@launch
                    }

                    if (isConnecting == false) {
                        Log.i(TAG, "Xmpp disconnected")
                        return@launch
                    }

                    Thread.sleep(RECONNECT_TIME_INTERVAL)
                    connect("reconnect")
                } catch (e: XMPPException) {
                    Log.e(TAG, "Xmpp reconnecting error " + e.message)
                    if (isConflictError(e.message)) {
                        Log.d(TAG, "is XMPP conflict error occurred")
                    }
                } catch (e: SmackException.NoResponseException) {
                    Log.e(TAG, "NO RESPONSE EXCEPTION")
                } catch (e: SmackException) {
                    Log.e(TAG, "Smack reconnecting error " + e.message)
                    //PdDriver.sleep(RECONNECT_TIME_INTERVAL);
                } catch (e: IOException) {
                    Log.e(TAG, "Smack reconnecting error " + e.message)
                }
            }
        }
    }

    private fun isConflictError(errorMessage: String?): Boolean {
        var isError = false
        Log.d(TAG, "is XMPP conflict error: $errorMessage")
        when (errorMessage?.contains("<conflict")) {

            true -> isError = true
            false -> isError = false
            else -> {}
        }
        return isError
    }

    fun multiUserChat() {
        //userName can be your name by which you want to join the room
        //nickName is the name which will be shown on group chat
        nickName =
            Resourcepart.from(Utils.getUserFromJid(SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!))

        val roomJid: EntityBareJid = JidCreate.entityBareFrom("open@conference.n1.iworklab.com")
        // User2 joins the new room (as a visitor)
        multiUserChatManager = MultiUserChatManager.getInstanceFor(connection)
        multiUserChat = multiUserChatManager!!.getMultiUserChat(roomJid)
        /*val mucEnterConfiguration = multiUserChat?.getEnterConfigurationBuilder(nickName)!!
            .requestNoHistory()
            .build()*/
        val mucEnterConfiguration: MucEnterConfiguration?
        if (SharedPref.readString(AppConstants.KEY_LAST_FETCHED_CHAT_TIMESTAMP) != null &&
            !TextUtils.isEmpty(SharedPref.readString(AppConstants.KEY_LAST_FETCHED_CHAT_TIMESTAMP))
        ) {

            val lastChatFetchedDate =
                DateUtils.getDateObjectFromStringTimestamp(SharedPref.readString(AppConstants.KEY_LAST_FETCHED_CHAT_TIMESTAMP)!!)

            mucEnterConfiguration = multiUserChat?.getEnterConfigurationBuilder(nickName)!!
                .requestHistorySince(lastChatFetchedDate) //timestamp from your last message
                .build()

        } else {
            //(currenmilliseconds - saved timestamp milliseconds) / 1000
            mucEnterConfiguration = multiUserChat?.getEnterConfigurationBuilder(nickName)!!
                .requestMaxStanzasHistory(300)
                .build()
        }

        SharedPref.writeString(
            AppConstants.KEY_LAST_FETCHED_CHAT_TIMESTAMP,
            System.currentTimeMillis().toString()
        )

        if (!multiUserChat!!.isJoined) {
            //multiUserChat?.join(nickName)
            multiUserChat?.join(mucEnterConfiguration)
        }

        // For listening incoming message
        multiUserChat?.addMessageListener(incomingMessageListener)
        // incomingMessageListener is implemented below.

/*        multiUserChat?.addPresenceInterceptor { object: PresenceListener {
            override fun processPresence(presence: Presence?) {
                TODO("Not yet implemented")
            }

        } }*/


        /*val dm = DeliveryReceiptManager
            .getInstanceFor(connection)
        dm.autoReceiptMode = DeliveryReceiptManager.AutoReceiptMode.always*/
        /*dm.addReceiptReceivedListener(object : ReceiptReceivedListener {

            override fun onReceiptReceived(
                fromJid: Jid?,
                toJid: Jid?,
                receiptId: String?,
                receipt: Stanza?
            ) {

                TODO("Not yet implemented")
            }
        })*/

    }

    private fun multiUserChatWithList() {
        val myGroupDbList = AppDatabase.getAppDatabase(MainApplication.appContext)
            ?.groupInfoTableDao()
            ?.getAll()
        multiUserChatList.clear()
        if (myGroupDbList != null && myGroupDbList.isNotEmpty()) {
            Log.e("DBSize", myGroupDbList.size.toString())
            myGroupDbList.forEachIndexed { _, group ->
                val muc = joinMultiUserChat(group.groupJid!!)
                multiUserChatList.add(muc)
            }
            SharedPref.writeString(AppConstants.KEY_LAST_FETCHED_CHAT_TIMESTAMP, System.currentTimeMillis().toString())
            uploadOfflineChat()
        }

    }

    private fun joinMultiUserChat(roomJidString: String): MultiUserChat {
        //userName can be your name by which you want to join the room
        //nickName is the name which will be shown on group chat
        nickName =
            Resourcepart.from(Utils.getUserFromJid(SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!))

        val roomJid = JidCreate.entityBareFrom(roomJidString)
        // User2 joins the new room (as a visitor)
        val multiUserChatManager = MultiUserChatManager.getInstanceFor(connection)
        val multiUserChat = multiUserChatManager!!.getMultiUserChat(roomJid)
        /*val mucEnterConfiguration = multiUserChat?.getEnterConfigurationBuilder(nickName)!!
            .requestNoHistory()
            .build()*/
        val mucEnterConfiguration: MucEnterConfiguration?
        if (SharedPref.readString(AppConstants.KEY_LAST_FETCHED_CHAT_TIMESTAMP) != null &&
            !TextUtils.isEmpty(SharedPref.readString(AppConstants.KEY_LAST_FETCHED_CHAT_TIMESTAMP))
        ) {

            /*var lastChatFetchedDate= DateUtils.getDateObjectFromStringTimestamp(SharedPref.readString(AppConstants.KEY_LAST_FETCHED_CHAT_TIMESTAMP)!!)

            mucEnterConfiguration = multiUserChat?.getEnterConfigurationBuilder(nickName)!!
                .requestHistorySince(lastChatFetchedDate) //timestamp from your last message
                .build()*/

            val lastChatFetchedTimestamp =
                SharedPref.readString(AppConstants.KEY_LAST_FETCHED_CHAT_TIMESTAMP)!!.toLong()
            val seconds = (System.currentTimeMillis() - lastChatFetchedTimestamp) / 1000

            Log.e("myseconds", seconds.toInt().toString())


            mucEnterConfiguration = multiUserChat?.getEnterConfigurationBuilder(nickName)!!
                .requestHistorySince(seconds.toInt())
                .build()

        } else {
            /*mucEnterConfiguration = multiUserChat?.getEnterConfigurationBuilder(nickName)!!
                .requestMaxStanzasHistory(300)
                .build()*/

            val seconds = (System.currentTimeMillis() - 0) / 1000

            Log.e("myseconds", seconds.toInt().toString())

            mucEnterConfiguration = multiUserChat?.getEnterConfigurationBuilder(nickName)!!
                .requestHistorySince(seconds.toInt())
                .build()

        }

        //SharedPref.writeString(AppConstants.KEY_LAST_FETCHED_CHAT_TIMESTAMP, System.currentTimeMillis().toString())

        if (!multiUserChat.isJoined) {
            //multiUserChat?.join(nickName)
            multiUserChat.join(mucEnterConfiguration)
        }

        // For listening incoming message
        multiUserChat.addMessageListener(incomingMessageListener)
        // incomingMessageListener is implemented below.

        return multiUserChat

        /*multiUserChat?.addPresenceInterceptor { object: PresenceListener {
            override fun processPresence(presence: Presence?) {
                TODO("Not yet implemented")
            }

        } }*/


        /*val dm = DeliveryReceiptManager
            .getInstanceFor(connection)
        dm.autoReceiptMode = DeliveryReceiptManager.AutoReceiptMode.always*/
        /*dm.addReceiptReceivedListener(object : ReceiptReceivedListener {

            override fun onReceiptReceived(
                fromJid: Jid?,
                toJid: Jid?,
                receiptId: String?,
                receipt: Stanza?
            ) {

                TODO("Not yet implemented")
            }
        })*/

    }

    fun addToMultiUserChatList(roomJidString: String) {
        if (connection != null) {
            if (isConnected() && isAuthenticated()) {
                val muc = joinMultiUserChat(roomJidString)
                multiUserChatList.add(muc)
            }
        }
    }

    fun unJoinAndRemoveFromMultiUserChatList(roomJidString: String) {
        if (multiUserChatList.isNotEmpty()) {
            val listMucCloned = multiUserChatList.toMutableList()

            listMucCloned.forEachIndexed { index, muc ->
                val roomJid: EntityBareJid = JidCreate.entityBareFrom(roomJidString)
                if (muc.room == roomJid) {
                    muc.leave()
                    multiUserChatList.removeAt(index)
                    return@forEachIndexed
                }

            }

        }

    }

    private val stanzaListener = StanzaListener { packet ->
        if (packet is Message) {
            //normal message
            if (packet.body == null) {
                Log.e("Received", "Received")
                if (packet.getExtension(AppConstants.StanzaConstants.PRESENCE_STATUS_JABBER) != null) {
                    val presenceExt =
                        packet.getExtension(AppConstants.StanzaConstants.PRESENCE_STATUS_JABBER) as StandardExtensionElement
                    Log.e("presenceExt", presenceExt.toXML().toString())

                    val senderJid =
                        presenceExt.getFirstElement(AppConstants.StanzaConstants.SENDER_JID).text

                    if (presenceExt.getFirstElement(AppConstants.StanzaConstants.PRESENCE_STATUS_REQUEST) != null) {
                        Log.e("presenceExtRequestReceived", "")
                        sendMessageOneToOne(senderJid, true, "online")
                    }

                    if (presenceExt.getFirstElement(AppConstants.StanzaConstants.PRESENCE_STATUS_RESPONSE) != null) {
                        Log.e("presenceExtResponseReceived", "")

                        val presenceStatus= presenceExt.getFirstElement(AppConstants.StanzaConstants.PRESENCE_STATUS_RESPONSE).text
                        val intent =
                            Intent(AppConstants.PRESENCE_RECEIVED_BROADCAST)
                        intent.putExtra(
                            AppConstants.IntentConstants.PRESENCE_USER_JID,
                            senderJid
                        )
                        if(presenceStatus != null)
                        {
                            if(presenceStatus == "online")
                            {
                                intent.putExtra(
                                    AppConstants.IntentConstants.PRESENCE_USER_STATUS,
                                    AppConstants.IntentConstants.PRESENCE_USER_ONLINE
                                )
                            }
                            else
                            {
                                intent.putExtra(
                                    AppConstants.IntentConstants.PRESENCE_USER_STATUS,
                                    AppConstants.IntentConstants.PRESENCE_USER_OFFLINE
                                )
                            }
                        }
                        else
                        {
                            intent.putExtra(
                                AppConstants.IntentConstants.PRESENCE_USER_STATUS,
                                AppConstants.IntentConstants.PRESENCE_USER_OFFLINE
                            )
                        }
                        context!!.sendBroadcast(intent)
                    }

                }
                if (packet.getExtension(AppConstants.StanzaConstants.DATA_REFRESH_JABBER) != null) {

                    val messageInfoTable = MessageInfoTable()

                    val repExt =
                        packet.getExtension(AppConstants.StanzaConstants.DATA_REFRESH_JABBER) as StandardExtensionElement
                    Log.e("refreshExt", repExt.toXML().toString())
                    if (repExt.getFirstElement(AppConstants.StanzaConstants.DATA_REFRESH_TRIGGER) != null) {
                        xmppGroupCallback!!.groupUpdated(true)
                        val intent =
                            Intent(AppConstants.REFRESH_GROUP_BROADCAST)
                        context!!.sendBroadcast(intent)
                        Log.e("REFRESHGROUP", "REFERESHGROUP1")
                    }//delivered
                    else if (repExt.getFirstElement(AppConstants.StanzaConstants.DELIVERED) != null) {

                            /*if(repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_JID).text == SharedPref.readString(AppConstants.KEY_XMPP_USER_JID))
                                {*/

                            Log.e("DeliveryReceipt", "myReceipt")

                            messageInfoTable.messageId =
                                repExt.getFirstElement(AppConstants.StanzaConstants.MESSAGE_ID).text
                            messageInfoTable.groupJid =
                                repExt.getFirstElement(AppConstants.StanzaConstants.GROUP_JID).text
                            messageInfoTable.senderJid =
                                repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_JID).text
                            messageInfoTable.deliveredAt =
                                repExt.getFirstElement(AppConstants.StanzaConstants.DELIVERED).text

                            if (messageInfoTable.deliveredAt != null && messageInfoTable.messageId != null) {
                                Log.e("DeliveryReceipt", "notnull")
                                updateDeliveredAt(
                                    messageInfoTable.deliveredAt!!,
                                    messageInfoTable.messageId!!
                                )

                                if (xmppChatCallback != null) {
                                    xmppChatCallback!!.deliveredMessage(messageInfoTable)
                                }

                            }

                            //}

                        } //displayed
                        else if (repExt.getFirstElement(AppConstants.StanzaConstants.DISPLAYED) != null) {

                            /*if(repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_JID).text == SharedPref.readString(AppConstants.KEY_XMPP_USER_JID))
                                {*/

                            Log.e("DisplayedReceipt", "myReceipt")

                            messageInfoTable.messageId =
                                repExt.getFirstElement(AppConstants.StanzaConstants.MESSAGE_ID).text
                            messageInfoTable.groupJid =
                                repExt.getFirstElement(AppConstants.StanzaConstants.GROUP_JID).text
                            messageInfoTable.senderJid =
                                repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_JID).text
                            messageInfoTable.displayedAt =
                                repExt.getFirstElement(AppConstants.StanzaConstants.DISPLAYED).text

                            if (messageInfoTable.displayedAt != null && messageInfoTable.messageId != null) {
                                Log.e("DisplayedReceipt", "notnull")
                                updateDisplayedAt(messageInfoTable)

                                //update unread chat count
                                /*Log.e("displayedoutside", "")
                                    if(messageInfoTable.senderJid!! != SharedPref.readString(AppConstants.KEY_XMPP_USER_JID))
                                    {
                                        Log.e("displayedinside", "")
                                        GlobalScope.launch {

                                            AppDatabase.getAppDatabase(MainApplication.appContext)
                                                ?.messageInfoTableDao()
                                                ?.updateMessageReadForSenderSide(messageInfoTable.messageId!!)

                                            updateUnreadChatCount(messageInfoTable)

                                        }
                                    }*/

                                if (xmppChatCallback != null) {
                                    xmppChatCallback!!.displayedMessage(messageInfoTable)
                                }

                            }

                            //}

                        }

                }

            }

        }

    }

    private val incomingMessageListener = MessageListener { message ->
        val messageInfoTable = MessageInfoTable()
        /*  messageInfoTable.createdAt= time.toString()
             messageInfoTable.deliveredAt= time.toString()
             messageInfoTable.displayedAt= time.toString()
             messageInfoTable.groupId= "1"
             messageInfoTable.groupImageUrl= "https://abc.com"
             messageInfoTable.groupJid= "open@conference.n1.iworklab.com"
             messageInfoTable.groupName= "Open Discussion"
             messageInfoTable.isUnread= true
             messageInfoTable.messageId= "2"
             messageInfoTable.messageText= message
             messageInfoTable.receivedAt= time.toString()
             messageInfoTable.refrenceUserId= 1
             messageInfoTable.senderId= "2"
             messageInfoTable.senderImageUrl= "https://abc.com"
             messageInfoTable.senderJid= "firoz@iworklab.com"
             messageInfoTable.senderName= "Firoz"
             (messageInfoTable.sentAt= time.toString)
             messageInfoTable.unixTimestamp= time.toString()*/

        //normal message
        if (message!!.body != null) {
            Log.e("MsgReceived", message.body)
            if (message.getExtension(AppConstants.StanzaConstants.DATA_JABBER) != null) {
                val repExt =
                    message.getExtension(AppConstants.StanzaConstants.DATA_JABBER) as StandardExtensionElement
                Log.e("RepExtMessage", repExt.toXML().toString())

                val messageList = AppDatabase.getAppDatabase(MainApplication.appContext)
                    ?.messageInfoTableDao()
                    ?.checkMessageDuplicate(repExt.getFirstElement(AppConstants.StanzaConstants.MESSAGE_ID).text)

                if (messageList != null && messageList.isNotEmpty()) {
                    //data exist with this message id so we can update here
                } else {
                    //data does not exist with this message id so add in db

                    messageInfoTable.messageText = message.body
                    messageInfoTable.messageId =
                        repExt.getFirstElement(AppConstants.StanzaConstants.MESSAGE_ID).text
                    messageInfoTable.groupName =
                        repExt.getFirstElement(AppConstants.StanzaConstants.TO).text
                    messageInfoTable.groupId =
                        repExt.getFirstElement(AppConstants.StanzaConstants.GROUP_ID).text
                    messageInfoTable.groupJid =
                        repExt.getFirstElement(AppConstants.StanzaConstants.GROUP_JID).text
                    messageInfoTable.groupImageUrl =
                        repExt.getFirstElement(AppConstants.StanzaConstants.GROUP_IMAGE_URL).text
                    messageInfoTable.timestamp =
                        repExt.getFirstElement(AppConstants.StanzaConstants.TIMESTAMP).text
                    messageInfoTable.unixTimestamp =
                        repExt.getFirstElement(AppConstants.StanzaConstants.UNIXTIMESTAMP).text
                    messageInfoTable.type =
                        repExt.getFirstElement(AppConstants.StanzaConstants.MEDIA_TYPE).text
                    messageInfoTable.senderName =
                        repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_NAME).text
                    messageInfoTable.senderFirstName =
                        repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_FIRST_NAME).text
                    messageInfoTable.senderLastName =
                        repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_LAST_NAME).text
                    messageInfoTable.senderImageUrl =
                        repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_IMAGE_URL).text
                    messageInfoTable.senderJid =
                        repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_JID).text
                    messageInfoTable.senderId =
                        repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_ID).text
                    messageInfoTable.receivedAt =
                        DateUtils.getDateTimeWithTimeZoneFromTimestamp(
                            System.currentTimeMillis().toString()
                        ) // current formatted time

                    if (message.getExtension(AppConstants.StanzaConstants.MEDIA_DATA_JABBER) != null) {
                        val mediaExt =
                            message.getExtension(AppConstants.StanzaConstants.MEDIA_DATA_JABBER) as StandardExtensionElement
                        messageInfoTable.mediaUrl =
                            mediaExt.getFirstElement(AppConstants.StanzaConstants.MEDIA_URL).text

                        if (messageInfoTable.type == AppConstants.TYPE_IMAGE_CHAT ||
                            messageInfoTable.type == AppConstants.TYPE_VIDEO_CHAT
                        ) {
                            messageInfoTable.mediaThumbImageUrl =
                                mediaExt.getFirstElement(AppConstants.StanzaConstants.MEDIA_THUMB_IMAGE_URL).text
                        }
                        messageInfoTable.mediaSize =
                            mediaExt.getFirstElement(AppConstants.StanzaConstants.MEDIA_SIZE).text
                        messageInfoTable.mediaDuration =
                            mediaExt.getFirstElement(AppConstants.StanzaConstants.MEDIA_DURATION).text
                    }

                    if (message.getExtension(AppConstants.StanzaConstants.DOCUMENT_DATA_JABBER) != null) {
                        val documentExt =
                            message.getExtension(AppConstants.StanzaConstants.DOCUMENT_DATA_JABBER) as StandardExtensionElement
                        messageInfoTable.mediaUrl =
                            documentExt.getFirstElement(AppConstants.StanzaConstants.DOCUMENT_URL).text
                        messageInfoTable.mediaSize =
                            documentExt.getFirstElement(AppConstants.StanzaConstants.DOCUMENT_SIZE).text
                        messageInfoTable.docMimeType =
                            documentExt.getFirstElement(AppConstants.StanzaConstants.DOCUMENT_TYPE).text
                    }

                    if (message.getExtension(AppConstants.StanzaConstants.LOCATION_DATA_JABBER) != null) {
                        val locationExt =
                            message.getExtension(AppConstants.StanzaConstants.LOCATION_DATA_JABBER) as StandardExtensionElement
                        messageInfoTable.latitude =
                            locationExt.getFirstElement(AppConstants.StanzaConstants.LOCATION_LATITUDE).text
                        messageInfoTable.longitude =
                            locationExt.getFirstElement(AppConstants.StanzaConstants.LOCATION_LONGITUDE).text
                    }

                    if (message.getExtension(AppConstants.StanzaConstants.CONTACT_DATA_JABBER) != null) {
                        val contactExt =
                            message.getExtension(AppConstants.StanzaConstants.CONTACT_DATA_JABBER) as StandardExtensionElement
                        messageInfoTable.contactFirstName =
                            contactExt.getFirstElement(AppConstants.StanzaConstants.CONTACT_FIRST_NAME)?.text
                        messageInfoTable.contactLastName =
                            contactExt.getFirstElement(AppConstants.StanzaConstants.CONTACT_LAST_NAME)?.text
                        messageInfoTable.contactPhone =
                            contactExt.getFirstElement(AppConstants.StanzaConstants.CONTACT_PHONE)?.text
                        messageInfoTable.contactEmail =
                            contactExt.getFirstElement(AppConstants.StanzaConstants.CONTACT_EMAIL)?.text
                    }

                    //for read status
                    if (MainApplication.activeRoomJid != null && MainApplication.activeRoomJid == messageInfoTable.groupJid) {
                        messageInfoTable.readStatus = 1
                    } else {
                        messageInfoTable.readStatus = 0
                    }

                    /*Log.e("--->", " ---  LOG REPLY EXTENSION ---")
                        Log.e("msto>", repExt.getFirstElement("messageId").text)
                        Log.e("msto>", repExt.getFirstElement("to").text)*/
                    //Log.e(javaClass.simpleName, repExt.getValue("rText"))
                    //Log.e(javaClass.simpleName, repExt.getAttributeValue("rText"))

                    /*if(messageInfoTable.senderJid != SharedPref.readString(AppConstants.KEY_XMPP_USER_JID).toString())
                        {*/

                    if (xmppChatCallback != null) {
                        xmppChatCallback!!.receivedMessage(messageInfoTable)
                    }
                    if (xmppGroupCallback != null) {
                        xmppGroupCallback!!.receivedMessage(messageInfoTable)
                    }

                    addMessageToDb(messageInfoTable)
                    updateLastMessageInGroup(
                        messageInfoTable.messageText!!,
                        messageInfoTable.unixTimestamp!!,
                        messageInfoTable.messageId!!,
                        messageInfoTable.type!!,
                        messageInfoTable.senderJid!!,
                        messageInfoTable.senderFirstName!!,
                        messageInfoTable.groupJid!!
                    )

                    //send delivered status
                    sendDelivered(messageInfoTable)
                    //send read status
                    if (messageInfoTable.readStatus == 1) {
                        sendDisplayed(messageInfoTable)
                    }
                    /*else
                        {*/

                    GlobalScope.launch {

                        updateUnreadChatCount(messageInfoTable)

                    }

                    //}

                    //}

                }

            }
        } else {
            Log.e("MsgReceived", "NULL")
            //other informatic data
            if (message.getExtension(AppConstants.StanzaConstants.DATA_JABBER) != null) {
                val repExt =
                    message.getExtension(AppConstants.StanzaConstants.DATA_JABBER) as StandardExtensionElement

                    //typing
                    if (message.getExtension(AppConstants.StanzaConstants.COMPOSING_NAMESPACE) != null) {
                        //val composeExt = messag   e.getExtension(AppConstants.StanzaConstants.COMPOSING_NAMESPACE) as ChatStateExtension

                        Log.e("Typing", "typing")

                        if (repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_JID).text != SharedPref.readString(
                                AppConstants.KEY_XMPP_USER_JID
                            )
                        ) {

                            Log.e("Typing", "other")

                            messageInfoTable.groupJid =
                                repExt.getFirstElement(AppConstants.StanzaConstants.GROUP_JID).text
                            messageInfoTable.senderJid =
                                repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_JID).text
                            messageInfoTable.senderName =
                                repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_NAME).text
                            messageInfoTable.senderFirstName =
                                repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_FIRST_NAME).text
                            messageInfoTable.senderLastName =
                                repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_LAST_NAME).text

                            if (messageInfoTable.senderFirstName != null) {

                                if (xmppChatCallback != null) {
                                    xmppChatCallback!!.typingIndicator(messageInfoTable)
                                }

                            }

                        }

                    }


            }
        }

        /*if (!TextUtils.isEmpty(message?.body)) {
                //addIncomingMessageInRecycler(message!!)
                Log.e("MSG", message?.body!!)
            }*/

        //Log.e("messagereceived --->", message!!.toXML().toString())

        /*val repExt= message!!.getExtension("data:jabber") as StandardExtensionElement
            if (repExt != null) {
                Log.e("RepExt", repExt.toXML().toString())
                Log.e("--->", " ---  LOG REPLY EXTENSION ---")
                Log.e("msto>", repExt.getFirstElement("messageId").text)
                Log.e("msto>", repExt.getFirstElement("to").text)
                //Log.e(javaClass.simpleName, repExt.getValue("rText"))
                //Log.e(javaClass.simpleName, repExt.getAttributeValue("rText"))
            }*/

        /*val repExt= message!!.getExtension("shayan:replyOuter") as StandardExtensionElement
            if (repExt != null) {
                Log.e("--->", " ---  LOG REPLY EXTENSION ---")
                //Log.e(javaClass.simpleName, repExt.getValue("rText"))
                Log.e(javaClass.simpleName, repExt.getAttributeValue("rText"))
            }*/
    }

    fun sendMessageOneToOne(sendToJid: String, isReceived: Boolean, presenceStatus: String) {

        var messageBuilder: Message?= null

        val elementBuilder = StandardExtensionElement.builder(
            AppConstants.StanzaConstants.PRESENCE_STATUS,
            AppConstants.StanzaConstants.PRESENCE_STATUS_JABBER
        )
        val outerStatusRequest = StandardExtensionElement.builder(
            AppConstants.StanzaConstants.PRESENCE_STATUS_REQUEST,
            "statusrequest"
        )
        val outerStatusResponse = StandardExtensionElement.builder(
            AppConstants.StanzaConstants.PRESENCE_STATUS_RESPONSE,
            presenceStatus
        )
        elementBuilder.addElement(
            AppConstants.StanzaConstants.SENDER_JID,
            SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)
        )

        if (isReceived) {
            elementBuilder.addElement(
                AppConstants.StanzaConstants.PRESENCE_STATUS_RESPONSE,
                presenceStatus
            )

            messageBuilder = MessageBuilder.buildMessage()
                .to(sendToJid)
                .ofType(Message.Type.chat)
                .addExtension(elementBuilder.build())
                .addExtension(outerStatusResponse.build())
                .build()

        } else {
            elementBuilder.addElement(
                AppConstants.StanzaConstants.PRESENCE_STATUS_REQUEST,
                "statusrequest"
            )

            messageBuilder = MessageBuilder.buildMessage()
                .to(sendToJid)
                .ofType(Message.Type.chat)
                .addExtension(elementBuilder.build())
                .addExtension(outerStatusRequest.build())
                .build()

        }

        Log.e("message --->", messageBuilder!!.toXML().toString())

        try {
            connection?.sendStanza(messageBuilder)
        } catch (e: Exception) {
            Log.e(TAG, "Error in sending presence status:" + e.message)
        }

    }

    fun sendStanzaToReceiveGrpUpdate(
        groupJid: String,
        groupId: String,
        membersJidList: List<String?>
    ) {

        val elementBuilder = StandardExtensionElement.builder(
            AppConstants.StanzaConstants.DATA_REFRESH,
            AppConstants.StanzaConstants.DATA_REFRESH_JABBER
        )
        elementBuilder.addElement(
            AppConstants.StanzaConstants.DATA_REFRESH_TRIGGER,
            AppConstants.StanzaConstants.DATA_REFRESH_TRIGGER
        )
        elementBuilder.addElement(AppConstants.StanzaConstants.GROUP_ID, groupId)
        elementBuilder.addElement(AppConstants.StanzaConstants.GROUP_JID, groupJid)

        membersJidList.let {
            it.forEachIndexed { _, memberJid ->
                if(memberJid != SharedPref.readString(AppConstants.KEY_XMPP_USER_JID))
                {

                    val messageBuilder = MessageBuilder.buildMessage()
                        .to(memberJid)
                        .ofType(Message.Type.chat)
                        .addExtension(elementBuilder.build())
                        .build()
                    Log.e("message --->", messageBuilder.toXML().toString())
                    try {
                        connection!!.sendStanza(messageBuilder)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in sending presence status:" + e.message)
                    }

                }
            }
        }
    }

    private fun getRoaster() {

        //val userJid= JidCreate.bareFrom("firoznew1645687895547@dbchatdev.iworklab.com")
        val entityUserJid: EntityBareJid =
            JidCreate.entityBareFrom("firoznew1645687895547@dbchatdev.iworklab.com")

        val roster = Roster.getInstanceFor(connection)

        roster.subscriptionMode= Roster.SubscriptionMode.accept_all

        /*roster.addSubscribeListener(object: SubscribeListener{
            override fun processSubscribe(
                from: Jid?,
                subscribeRequest: Presence?
            ): SubscribeListener.SubscribeAnswer {
                TODO("Not yet implemented")
            }

        })*/

        roster.addRosterListener(object : RosterListener {
            override fun entriesAdded(addresses: MutableCollection<Jid>?) {
                //TODO("Not yet implemented")
            }

            override fun entriesUpdated(addresses: MutableCollection<Jid>?) {
                //TODO("Not yet implemented")
            }

            override fun entriesDeleted(addresses: MutableCollection<Jid>?) {
                //TODO("Not yet implemented")
            }

            override fun presenceChanged(presence: Presence?) {
                Log.e("PresenceListener2", "Presence Changed")
                println("Presence changed: " + presence!!.from + " " + presence)
                presence.type.let {
                    println("Presence type:" + presence.type.name)
                }
                if(presence.type?.name == Presence.Type.available.name)
                {
                    //user online
                    val intent =
                        Intent(AppConstants.PRESENCE_RECEIVED_BROADCAST)
                    intent.putExtra(
                        AppConstants.IntentConstants.PRESENCE_USER_JID,
                        Utils.getJidFromFullJid(presence.from.asUnescapedString())
                    )
                    intent.putExtra(
                        AppConstants.IntentConstants.PRESENCE_USER_STATUS,
                        AppConstants.IntentConstants.PRESENCE_USER_ONLINE
                    )
                    context!!.sendBroadcast(intent)
                }
                else
                {
                    //user offline
                    val intent =
                        Intent(AppConstants.PRESENCE_RECEIVED_BROADCAST)
                    intent.putExtra(
                        AppConstants.IntentConstants.PRESENCE_USER_JID,
                        Utils.getJidFromFullJid(presence.from.asUnescapedString())
                    )
                    intent.putExtra(
                        AppConstants.IntentConstants.PRESENCE_USER_STATUS,
                        AppConstants.IntentConstants.PRESENCE_USER_OFFLINE
                    )
                    context!!.sendBroadcast(intent)
                }

            }

        })

     /*   roster.addPresenceEventListener(object : PresenceEventListener {
            override fun presenceAvailable(address: FullJid?, availablePresence: Presence?) {
                Log.e("PresenceListener", "Available")

            }

            override fun presenceUnavailable(address: FullJid?, presence: Presence?) {
                Log.e("PresenceListener", "UnAvailable")
            }

            override fun presenceError(address: Jid?, errorPresence: Presence?) {

            }

            override fun presenceSubscribed(address: BareJid?, subscribedPresence: Presence?) {

            }

            override fun presenceUnsubscribed(address: BareJid?, unsubscribedPresence: Presence?) {

            }

        })*/

        //val presence= roster.getPresence(entityUserJid)
        /*if(presence.mode == Presence.Mode.available)
        {
            Log.e("UserStatus", "online")
        }
        else
        {
            Log.e("UserStatus", "offline")
        }*/
        /*val entries: Collection<RosterEntry> = roster.entries
        for (entry in entries)
        {
            println(entry)
        }*/

    }

    fun subscribeUser(membersList: List<MemberInfoTable>) {

        //val userJid= JidCreate.bareFrom("firoznew1645687895547@dbchatdev.iworklab.com")
        /*val entityUserJid: EntityBareJid =
            JidCreate.entityBareFrom("firoznew1645687895547@dbchatdev.iworklab.com")
*/
        val roster = Roster.getInstanceFor(connection)

        /*// user jid, nickname, null
        //roster.createEntry(userJid, Utils.getUserFromJid(userJid.toString()), null)
        roster.createItemAndRequestSubscription(userJid, Utils.getUserFromJid(userJid.toString()), null)
*/
        membersList.forEachIndexed { _, membersDetail ->
            if(membersDetail.userJid != SharedPref.readString(AppConstants.KEY_XMPP_USER_JID))
            {
                val userJid= JidCreate.bareFrom(membersDetail.userJid!!)
                // user jid, nickname, null
                //roster.createEntry(userJid, Utils.getUserFromJid(userJid.toString()), null)
                roster.createItemAndRequestSubscription(userJid, Utils.getUserFromJid(userJid.toString()), null)

            }
        }

    }

    fun sendOnlineOfflineCustomStanza(membersList: List<MemberInfo>, userPresence: String) {

        membersList.forEachIndexed { _, membersDetail ->
            if(membersDetail.user_jid != SharedPref.readString(AppConstants.KEY_XMPP_USER_JID))
            {

                sendMessageOneToOne(membersDetail.user_jid!!, true, userPresence)

            }
        }

    }

        fun sendMessageMultiUser(messageInfoTable: MessageInfoTable) {

        if (multiUserChatList.isNotEmpty()) {
            multiUserChatList.forEachIndexed { _, muc ->

                val roomJid: EntityBareJid = JidCreate.entityBareFrom(messageInfoTable.groupJid!!)

                if (muc.room == roomJid) {


                    val elementBuilder = StandardExtensionElement.builder(
                        AppConstants.StanzaConstants.DATA,
                        AppConstants.StanzaConstants.DATA_JABBER
                    )
                        .addElement(
                            AppConstants.StanzaConstants.MESSAGE_ID,
                            messageInfoTable.messageId
                        )
                        .addElement(AppConstants.StanzaConstants.TO, messageInfoTable.groupName)
                        .addElement(AppConstants.StanzaConstants.GROUP_ID, messageInfoTable.groupId)
                        .addElement(
                            AppConstants.StanzaConstants.GROUP_JID,
                            messageInfoTable.groupJid
                        )
                        .addElement(
                            AppConstants.StanzaConstants.GROUP_IMAGE_URL,
                            messageInfoTable.groupImageUrl
                        )
                        .addElement(
                            AppConstants.StanzaConstants.TIMESTAMP,
                            messageInfoTable.timestamp
                        )
                        .addElement(
                            AppConstants.StanzaConstants.UNIXTIMESTAMP,
                            messageInfoTable.unixTimestamp
                        )
                        .addElement(AppConstants.StanzaConstants.MEDIA_TYPE, messageInfoTable.type)
                        .addElement(
                            AppConstants.StanzaConstants.SENDER_NAME,
                            messageInfoTable.senderName
                        )
                        .addElement(
                            AppConstants.StanzaConstants.SENDER_FIRST_NAME,
                            messageInfoTable.senderFirstName
                        )
                        .addElement(
                            AppConstants.StanzaConstants.SENDER_LAST_NAME,
                            messageInfoTable.senderLastName
                        )
                        .addElement(
                            AppConstants.StanzaConstants.SENDER_IMAGE_URL,
                            messageInfoTable.senderImageUrl
                        )
                        .addElement(
                            AppConstants.StanzaConstants.SENDER_JID,
                            messageInfoTable.senderJid
                        )
                        .addElement(
                            AppConstants.StanzaConstants.SENDER_ID,
                            messageInfoTable.senderId
                        )
                        .build()

                    val messageBuilder = MessageBuilder.buildMessage(messageInfoTable.messageId)
                        .to(messageInfoTable.groupJid)
                        .setBody(messageInfoTable.messageText)
                        .ofType(Message.Type.groupchat)
                        .addExtension(elementBuilder)

                    if (messageInfoTable.type == AppConstants.TYPE_TEXT_CHAT) {
                    } else if (messageInfoTable.type == AppConstants.TYPE_IMAGE_CHAT ||
                        messageInfoTable.type == AppConstants.TYPE_VIDEO_CHAT
                    ) {

                        val mediaElementBuilder = StandardExtensionElement.builder(
                            AppConstants.StanzaConstants.MEDIA_DATA,
                            AppConstants.StanzaConstants.MEDIA_DATA_JABBER
                        )
                            .addElement(
                                AppConstants.StanzaConstants.MEDIA_URL,
                                messageInfoTable.mediaUrl
                            )
                            .addElement(
                                AppConstants.StanzaConstants.MEDIA_SIZE,
                                messageInfoTable.mediaSize
                            )
                            .addElement(
                                AppConstants.StanzaConstants.MEDIA_DURATION,
                                messageInfoTable.mediaDuration
                            )
                            .addElement(
                                AppConstants.StanzaConstants.MEDIA_THUMB_IMAGE_URL,
                                messageInfoTable.mediaThumbImageUrl
                            )
                            .build()

                        messageBuilder.addExtension(mediaElementBuilder)

                    } else if (messageInfoTable.type == AppConstants.TYPE_AUDIO_CHAT
                    ) {

                        val mediaElementBuilder = StandardExtensionElement.builder(
                            AppConstants.StanzaConstants.MEDIA_DATA,
                            AppConstants.StanzaConstants.MEDIA_DATA_JABBER
                        )
                            .addElement(
                                AppConstants.StanzaConstants.MEDIA_URL,
                                messageInfoTable.mediaUrl
                            )
                            .addElement(
                                AppConstants.StanzaConstants.MEDIA_SIZE,
                                messageInfoTable.mediaSize
                            )
                            .addElement(
                                AppConstants.StanzaConstants.MEDIA_DURATION,
                                messageInfoTable.mediaDuration
                            )
                            .build()

                        messageBuilder.addExtension(mediaElementBuilder)

                    } else if (messageInfoTable.type == AppConstants.TYPE_DOCUMENT_CHAT) {

                        val documentElementBuilder = StandardExtensionElement.builder(
                            AppConstants.StanzaConstants.DOCUMENT_DATA,
                            AppConstants.StanzaConstants.DOCUMENT_DATA_JABBER
                        )
                            .addElement(
                                AppConstants.StanzaConstants.DOCUMENT_URL,
                                messageInfoTable.mediaUrl
                            )
                            .addElement(
                                AppConstants.StanzaConstants.DOCUMENT_SIZE,
                                messageInfoTable.mediaSize
                            )
                            .addElement(
                                AppConstants.StanzaConstants.DOCUMENT_TYPE,
                                messageInfoTable.docMimeType
                            )
                            .build()

                        messageBuilder.addExtension(documentElementBuilder)

                    } else if (messageInfoTable.type == AppConstants.TYPE_LOCATION_CHAT) {

                        val locationElementBuilder = StandardExtensionElement.builder(
                            AppConstants.StanzaConstants.LOCATION_DATA,
                            AppConstants.StanzaConstants.LOCATION_DATA_JABBER
                        )
                            .addElement(
                                AppConstants.StanzaConstants.LOCATION_LATITUDE,
                                messageInfoTable.latitude
                            )
                            .addElement(
                                AppConstants.StanzaConstants.LOCATION_LONGITUDE,
                                messageInfoTable.longitude
                            )
                            .build()

                        messageBuilder.addExtension(locationElementBuilder)

                    } else if (messageInfoTable.type == AppConstants.TYPE_CONTACT_CHAT) {

                        val contactElementBuilder = StandardExtensionElement.builder(
                            AppConstants.StanzaConstants.CONTACT_DATA,
                            AppConstants.StanzaConstants.CONTACT_DATA_JABBER
                        )
                            .addElement(
                                AppConstants.StanzaConstants.CONTACT_FIRST_NAME,
                                messageInfoTable.contactFirstName
                            )
                            .addElement(
                                AppConstants.StanzaConstants.CONTACT_LAST_NAME,
                                messageInfoTable.contactLastName
                            )
                            .addElement(
                                AppConstants.StanzaConstants.CONTACT_PHONE,
                                messageInfoTable.contactPhone
                            )
                            .addElement(
                                AppConstants.StanzaConstants.CONTACT_EMAIL,
                                messageInfoTable.contactEmail
                            )
                            .build()

                        messageBuilder.addExtension(contactElementBuilder)

                    }

                    if (isConnected() && isAuthenticated()) {
                        muc.sendMessage(messageBuilder)
                    }
                    Log.e("messages", messageBuilder.build().toXML().toString())

                    return@forEachIndexed

                }

            }

        }

    }

    private fun sendDelivered(messageInfoTable: MessageInfoTable) {

                val elementBuilder = StandardExtensionElement.builder(
                    AppConstants.StanzaConstants.DATA,
                    AppConstants.StanzaConstants.DATA_JABBER
                )
                    .addElement(
                        AppConstants.StanzaConstants.MESSAGE_ID,
                        messageInfoTable.messageId
                    )
                    .addElement(
                        AppConstants.StanzaConstants.GROUP_JID,
                        messageInfoTable.groupJid
                    )
                    .addElement(
                        AppConstants.StanzaConstants.SENDER_JID,
                        SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)
                    )
                    .addElement(
                        AppConstants.StanzaConstants.DELIVERED,
                        DateUtils.getDateTimeWithTimeZoneFromTimestamp(
                            System.currentTimeMillis().toString()
                        )
                    )
                    .build()

                val messageBuilder = MessageBuilder.buildMessage(Utils.generateRandomUniqueId())
                    .to(messageInfoTable.senderJid)
                    .ofType(Message.Type.chat)
                    .addExtension(elementBuilder)
                    .build()


        if (isConnected() && isAuthenticated()) {
            try {
                connection!!.sendStanza(messageBuilder)
            } catch (e: Exception) {
                Log.e(TAG, "Error in sending presence status:" + e.message)
            }
        }
                    //Log.e("message --->", message.toXML().toString())
                    Log.e("myDelivered --->", messageBuilder.toXML().toString())

    }

    //need to send this stanza after msg seen
    //all other code implemented with db
    fun sendDisplayed(messageInfoTable: MessageInfoTable) {

                    val elementBuilder = StandardExtensionElement.builder(
                        AppConstants.StanzaConstants.DATA,
                        AppConstants.StanzaConstants.DATA_JABBER
                    )
                        .addElement(
                            AppConstants.StanzaConstants.MESSAGE_ID,
                            messageInfoTable.messageId
                        )
                        .addElement(
                            AppConstants.StanzaConstants.GROUP_JID,
                            messageInfoTable.groupJid
                        )
                        .addElement(
                            AppConstants.StanzaConstants.SENDER_JID,
                            SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)
                        )
                        .addElement(
                            AppConstants.StanzaConstants.DISPLAYED,
                            DateUtils.getDateTimeWithTimeZoneFromTimestamp(
                                System.currentTimeMillis().toString()
                            )
                        )
                        .build()

        val messageBuilder = MessageBuilder.buildMessage(Utils.generateRandomUniqueId())
            .to(messageInfoTable.senderJid)
            .ofType(Message.Type.chat)
            .addExtension(elementBuilder)
            .build()


        if (isConnected() && isAuthenticated()) {
            try {
                connection!!.sendStanza(messageBuilder)
            } catch (e: Exception) {
                Log.e(TAG, "Error in sending presence status:" + e.message)
            }
        }
                    //Log.e("message --->", message.toXML().toString())
                    Log.e("myDisplayed --->", messageBuilder.toXML().toString())


    }

    fun sendTyping(messageInfoTable: MessageInfoTable) {

        if (multiUserChatList.isNotEmpty()) {
            multiUserChatList.forEachIndexed { _, muc ->

                /* Log.e("RoomJidActive", muc.room.toString())
                 Log.e("RoomJidGroup", messageInfoTable.groupJid!!)
 */
                val roomJid: EntityBareJid = JidCreate.entityBareFrom(messageInfoTable.groupJid!!)

                if (muc.room == roomJid) {

                    /*Log.e("RoomJidActive", "INSIDELOOP")
                    Log.e("RoomJidGroup", "INSIDELOOP")
*/
                    val chatStateExtension = ChatStateExtension(ChatState.composing)

                    val elementBuilder = StandardExtensionElement.builder(
                        AppConstants.StanzaConstants.DATA,
                        AppConstants.StanzaConstants.DATA_JABBER
                    )
                        .addElement(
                            AppConstants.StanzaConstants.GROUP_JID,
                            messageInfoTable.groupJid
                        )
                        .addElement(
                            AppConstants.StanzaConstants.SENDER_JID,
                            messageInfoTable.senderJid
                        )
                        .addElement(
                            AppConstants.StanzaConstants.SENDER_NAME,
                            messageInfoTable.senderName
                        )
                        .addElement(
                            AppConstants.StanzaConstants.SENDER_FIRST_NAME,
                            messageInfoTable.senderFirstName
                        )
                        .addElement(
                            AppConstants.StanzaConstants.SENDER_LAST_NAME,
                            messageInfoTable.senderLastName
                        )
                        .build()

                    /*var elementComposingBuilder= StandardExtensionElement.builder(AppConstants.StanzaConstants.COMPOSING, AppConstants.StanzaConstants.COMPOSING_NAMESPACE)
                        .build()*/

                    val messageBuilder = MessageBuilder.buildMessage(Utils.generateRandomUniqueId())
                        .to(messageInfoTable.groupJid)
                        .ofType(Message.Type.groupchat)
                        .addExtension(elementBuilder)
                        .addExtension(chatStateExtension)

                    if (isConnected() && isAuthenticated()) {
                        muc.sendMessage(messageBuilder)
                    }
                    //Log.e("message --->", message.toXML().toString())
                    Log.e("mycomposing --->", messageBuilder.build().toXML().toString())

                    return@forEachIndexed

                }

            }

        }

    }

    fun addMessageToDb(messageInfoTable: MessageInfoTable) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.messageInfoTableDao()
                ?.insert(messageInfoTable)

        }
    }

    fun addGroupToDb(groupInfoTable: GroupInfoTable) {
        GlobalScope.launch {
            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.insert(groupInfoTable)

        }
    }

    fun removeGroupFromDb(groupJid: String) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.deleteGroup(groupJid)

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.messageInfoTableDao()
                ?.deleteChatForDeletedGroup(groupJid)

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.memberInfoTableDao()
                ?.deleteMemberForDeletedGroup(groupJid)

        }
    }

    private fun updateSentAt(sentAt: String, messageId: String) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.messageInfoTableDao()
                ?.updateMessageSent(sentAt, messageId)

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateMessageSent(sentAt, messageId)

        }
    }

    private fun updateDeliveredAt(deliveredAt: String, messageId: String) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.messageInfoTableDao()
                ?.updateMessageDelivered(deliveredAt, messageId)

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateMessageDelivered(deliveredAt, messageId)


        }
    }

    private fun updateDisplayedAt(messageInfoTable: MessageInfoTable) {
        GlobalScope.launch {

            //update displayed at
            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.messageInfoTableDao()
                ?.updateMessageDisplayed(
                    messageInfoTable.displayedAt!!,
                    messageInfoTable.messageId!!
                )

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateMessageDisplayed(
                    messageInfoTable.displayedAt!!,
                    messageInfoTable.messageId!!
                )

        }
    }

    fun updateUnreadChatCount(messageInfoTable: MessageInfoTable) {

        //get unread count
        val unreadCount = AppDatabase.getAppDatabase(MainApplication.appContext)
            ?.messageInfoTableDao()
            ?.getUnreadChatCount(
                SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!,
                messageInfoTable.groupJid!!
            )

        //update unread count
        if (unreadCount != null && !TextUtils.isEmpty(unreadCount)) {
            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateGroupUnreadChatCount(unreadCount.toInt(), messageInfoTable.groupJid!!)
        } else {
            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateGroupUnreadChatCount(0, messageInfoTable.groupJid!!)
        }

    }

    fun updateLastMessageInGroup(
        lastMessage: String,
        timestamp: String,
        messageId: String,
        messageType: String,
        senderJid: String,
        senderName: String,
        groupJid: String
    ) {
        GlobalScope.launch {
            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateMessage(
                    lastMessage,
                    timestamp,
                    messageId,
                    messageType,
                    senderJid,
                    senderName,
                    groupJid
                )
        }
    }

    fun updateGroupDb(image: String?, groupName: String?, alertLevel: String, groupJid: String) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateGroupData(image, groupName, alertLevel, groupJid)

        }
    }

    fun updateGroupCallStatus(isCallActive: Boolean, groupJid: String) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateGroupCallStatus(isCallActive.toString(), groupJid)

        }
    }

    fun updateGroupActiveStatus(isGroupActive: String, groupJid: String) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateGroupActiveStatus(isGroupActive, groupJid)

        }
    }

    fun updateAlertLevel(alertLevel: String, groupJid: String) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.groupInfoTableDao()
                ?.updateGroupAlertLevel(alertLevel, groupJid)

        }
    }

    private fun deleteMessageHavingNoFile(messageId: String) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.messageInfoTableDao()
                ?.deleteMessageHavingNoFile(messageId)
        }
    }

    private fun uploadOfflineChat() {
        GlobalScope.launch {
            val messageInfoTable = AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.messageInfoTableDao()
                ?.getUnuploadedChatList(SharedPref.readString(AppConstants.KEY_XMPP_USER_JID)!!)

            Log.e("MessageOffline", "w")

            //if list size grater than 0
            if (messageInfoTable != null && messageInfoTable.isNotEmpty()) {

                Log.e("MessageOfflineSize", messageInfoTable.size.toString())

                messageInfoTable.forEachIndexed { index, e ->

                    println("$e at $index")

                    if (connection != null) {
                        if (isConnected() &&
                            isAuthenticated()
                        ) {
                            if (e.type == AppConstants.TYPE_IMAGE_CHAT ||
                                e.type == AppConstants.TYPE_VIDEO_CHAT ||
                                e.type == AppConstants.TYPE_AUDIO_CHAT ||
                                e.type == AppConstants.TYPE_DOCUMENT_CHAT
                            ) {
                                if (e.mediaUrl != null)
                                    sendMessageMultiUser(e)
                                else
                                    uploadMediaFiles(e)
                            } else
                                sendMessageMultiUser(e)


                        }
                    }

                }

            }

        }
    }

    fun updateUploadedMediaUrl(mediaUrl: String, mediaThumbUrl: String, messageId: String) {
        GlobalScope.launch {

            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.messageInfoTableDao()
                ?.updateUploadedMediaUrl(mediaUrl, mediaThumbUrl, messageId)

        }
    }


    fun updateDownloadedMediaUrl(mediaUrl: String, messageId: String) {
        GlobalScope.launch {
            AppDatabase.getAppDatabase(MainApplication.appContext)
                ?.messageInfoTableDao()
                ?.updateDownloadedMediaUrl(mediaUrl, messageId)
        }
    }

    fun uploadOfflineChatWithWorkManager() {

        val sendOfflineChatUploadWorkRequest =
            OneTimeWorkRequest.Builder(OfflineChatUploader::class.java) //pass data to work manager
                .setInputData(
                    Data.Builder()
                        .putString(
                            AppConstants.CHAT_UPLOADER_SERVICE,
                            AppConstants.CHAT_UPLOADER_SERVICE
                        )
                        .build()
                )
                .build()

        WorkManager
            .getInstance(MainApplication.appContext)
            .enqueueUniqueWork(
                AppConstants.CHAT_UPLOADER_SERVICE,
                ExistingWorkPolicy.REPLACE,
                sendOfflineChatUploadWorkRequest
            )

    }

    fun isAuthenticated(): Boolean {

        return (connection != null && connection!!.isAuthenticated)
    }

    fun isConnected(): Boolean {

        return (connection != null && connection!!.isConnected)
    }

    fun disconnectXmpp(tag: String) {

        Log.i(TAG, "----call disconnecting XMPP:$tag")
        if (connection != null) {
            //            ProviderManager.removeIQProvider("ping", "urn:xmpp:ping");
            sendOfflinePresence()

            connection!!.removeAsyncStanzaListener(stanzaListener)
            connection!!.removeConnectionListener(this@XmppCustomConnection)
            connection!!.removeStanzaAcknowledgedListener(this@XmppCustomConnection)

            GlobalScope.launch {

                connection!!.disconnect()
                connection = null
                isConnecting = false

            }

        }
    }

    override fun connectionClosed() {
        Log.i(TAG, "connection closed.")

        /*if (Utils.isNetConnected(context!!)) {
            reConnect()
        }*/
    }

    override fun connectionClosedOnError(e: Exception) {
        Log.e(TAG, "connection closed on error:" + e.stackTrace)

        if (isConflictError(e.message)) {
            return
        }

        Log.e(TAG, "reconnect after closed on error")
        reConnect()
    }

    override fun connecting(connection: XMPPConnection?) {
        super.connecting(connection)
        Log.e(TAG, "XMPP Connecting in")
    }

    override fun connected(connection: XMPPConnection?) {
        super.connected(connection)
        Log.e(TAG, "XMPP Connected")
    }

    override fun authenticated(connection: XMPPConnection?, resumed: Boolean) {
        super.authenticated(connection, resumed)
        Log.e(TAG, "XMPP Authenticated")
        //uploadOfflineChat()
        //uploadOfflineChatWithWorkManager()
    }

    //below methods used to send online/offline status of user.
    fun sendOnlinePresence() {

        val presence = connection!!.stanzaFactory
            .buildPresenceStanza()
            .setPriority(1)
            .ofType(Presence.Type.available)
            //.setMode(Presence.Mode.available)
            .setStatus("online")
            .build()

        try {
            connection!!.sendStanza(presence)
        } catch (e: Exception) {
            Log.e(TAG, "Error in sending online presence:" + e.message)
        }

    }

    fun sendOfflinePresence() {

        val presence = connection!!.stanzaFactory
            .buildPresenceStanza()
            .setPriority(1)
            .ofType(Presence.Type.unavailable)
            //.setMode(Presence.Mode.available)
            .setStatus("offline")
            .build()

        try {
            connection!!.sendStanza(presence)
        } catch (e: Exception) {
            Log.e(TAG, "Error in sending offline presence:" + e.message)
        }

    }

    fun setXmppCallbackForChat(xmppChatCallback: XmppChatCallback) {
        this.xmppChatCallback = xmppChatCallback
    }

    fun setXmppCallbackForGroups(xmppGroupCallback: XmppGroupCallback) {
        this.xmppGroupCallback = xmppGroupCallback
    }

    //sent msg receipt using stanza acknowledge listener
    override fun processStanza(packet: Stanza?) {
        if (packet is Message) {
            Log.e("sentsuccess", "sent")

            //normal message
            if (packet.body != null) {
                if (packet.getExtension(AppConstants.StanzaConstants.DATA_JABBER) != null) {
                    val repExt =
                        packet.getExtension(AppConstants.StanzaConstants.DATA_JABBER) as StandardExtensionElement
                    Log.e("RepExt", repExt.toXML().toString())

                    val messageInfoTable = MessageInfoTable()

                    messageInfoTable.messageText = packet.body
                    messageInfoTable.messageId =
                        repExt.getFirstElement(AppConstants.StanzaConstants.MESSAGE_ID).text
                    messageInfoTable.groupName =
                        repExt.getFirstElement(AppConstants.StanzaConstants.TO).text
                    messageInfoTable.groupId =
                        repExt.getFirstElement(AppConstants.StanzaConstants.GROUP_ID).text
                    messageInfoTable.groupJid =
                        repExt.getFirstElement(AppConstants.StanzaConstants.GROUP_JID).text
                    messageInfoTable.groupImageUrl =
                        repExt.getFirstElement(AppConstants.StanzaConstants.GROUP_IMAGE_URL).text
                    messageInfoTable.timestamp =
                        repExt.getFirstElement(AppConstants.StanzaConstants.TIMESTAMP).text
                    messageInfoTable.unixTimestamp =
                        repExt.getFirstElement(AppConstants.StanzaConstants.UNIXTIMESTAMP).text
                    messageInfoTable.type =
                        repExt.getFirstElement(AppConstants.StanzaConstants.MEDIA_TYPE).text
                    messageInfoTable.senderName =
                        repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_NAME).text
                    messageInfoTable.senderImageUrl =
                        repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_IMAGE_URL).text
                    messageInfoTable.senderJid =
                        repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_JID).text
                    messageInfoTable.senderId =
                        repExt.getFirstElement(AppConstants.StanzaConstants.SENDER_ID).text
                    messageInfoTable.sentAt = DateUtils.getDateTimeWithTimeZoneFromTimestamp(
                        System.currentTimeMillis().toString()
                    )

                    if (messageInfoTable.sentAt != null && messageInfoTable.messageId != null) {

                        updateSentAt(messageInfoTable.sentAt!!, messageInfoTable.messageId!!)

                        if (xmppChatCallback != null) {
                            xmppChatCallback!!.sentMessage(messageInfoTable)
                        }
                        if (xmppGroupCallback != null) {
                            xmppGroupCallback!!.sentMessage(messageInfoTable)
                        }

                    }

                }
            }

        }
    }

    //upload media
    fun uploadMediaFiles(messageInfoTable: MessageInfoTable) {

        // val selectedUri: Uri = Uri.parse(messageInfoTable.mediaUrlLocal!!)
        val groupId = messageInfoTable.groupId!!
        val thumbFile = File(messageInfoTable.mediaThumbImageUrl!!)
        val file = File(messageInfoTable.mediaUrlLocal!!)

        if (file.exists() && thumbFile.exists()) {
            val fileMime = MimeData.fromFile(file, context!!)
            val fileMimeString = fileMime.mimeType.toString()
            Log.v("mimeData", fileMime.mimeType.toString())
            val bodyToSend = UploaderRequestBody(
                file,
                fileMimeString,
                System.currentTimeMillis().toInt(),
                context!!,
                false
            )

            val thumbFileMime = MimeData.fromFile(thumbFile, context!!)

            Log.v("mimeDataThumb", thumbFileMime.mimeType.toString())
            val thumbBodyToSend = UploaderRequestBody(
                thumbFile,
                thumbFileMime.mimeType.toString(),
                System.currentTimeMillis().toInt(),
                context!!,
                true
            )

            val partGroupId =
                groupId.toRequestBody("text/plain;charset=utf-8".toMediaType())

            var partMediaDuration: RequestBody?= null
            if(messageInfoTable.mediaDuration != null)
            {
                partMediaDuration = messageInfoTable.mediaDuration!!.toRequestBody("text/plain;charset=utf-8".toMediaType())
            }

            var thumbMultipart: MultipartBody.Part? = null
            if (messageInfoTable.type == AppConstants.TYPE_IMAGE_CHAT ||
                messageInfoTable.type == AppConstants.TYPE_VIDEO_CHAT
            ) {

                thumbMultipart = MultipartBody.Part.createFormData(
                    "attachment_file_thumb",
                    thumbFile.name,
                    thumbBodyToSend
                )
            }

            ApiClient.request!!.uploadAttachments(
                partGroupId,
                MultipartBody.Part.createFormData(
                    "attachment_file",
                    file.name,
                    bodyToSend
                ),
                thumbMultipart,
                partMediaDuration,
                Utils.getSelectedLanguage()!!,
                SharedPref.readString(AppConstants.KEY_TOKEN)!!
            ).enqueue(object : Callback<MediaUploadResponse> {
                override fun onResponse(
                    call: Call<MediaUploadResponse>,
                    response: Response<MediaUploadResponse>
                ) {
                    response.body()?.let {
                        if (response.body()!!.success) {

                            Log.v("onResponse", it.toString())
                            Log.v("onResponse", it.data.toString())
                            val mediaUrl = response.body()!!.data.attachmentUrl
                            val mediaThumbUrl = response.body()!!.data.attachmentThumbUrl
                            Log.e("MediaUrl", mediaUrl)
                            Log.e("MediaThumbUrl", mediaThumbUrl)
                            updateUploadedMediaUrl(
                                mediaUrl,
                                mediaThumbUrl,
                                messageInfoTable.messageId!!
                            )
                            messageInfoTable.mediaUrl = mediaUrl
                            messageInfoTable.mediaThumbImageUrl = mediaThumbUrl
                            Log.e("Upload done", "")
                            sendMessageMultiUser(messageInfoTable)
                            //    progress_bar.progress = 100

                        }
                    }
                }

                override fun onFailure(call: Call<MediaUploadResponse>, t: Throwable) {
                    Log.v("onFailure", t.message.toString())
                }
            })
        } else {
            deleteMessageHavingNoFile(messageInfoTable.messageId!!)
        }

    }
}