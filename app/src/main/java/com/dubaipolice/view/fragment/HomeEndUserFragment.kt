package com.dubaipolice.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.dubaipolice.MainApplication
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.FragmentHomeEndUserBinding
import com.dubaipolice.view.activity.ChatActivity
import com.dubaipolice.view.activity.HomeEndUserActivity
import com.dubaipolice.view.activity.VideoStreamActivity
import com.dubaipolice.xmpp.XmppCustomConnection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jivesoftware.smack.*
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.MessageBuilder
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.packet.StandardExtensionElement
import org.jivesoftware.smack.roster.PresenceEventListener
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterEntry
import org.jivesoftware.smack.roster.RosterListener
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.mam.MamManager
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jxmpp.jid.BareJid
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.FullJid
import org.jxmpp.jid.Jid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart
import java.io.IOException
import java.net.InetAddress
import java.util.*
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import org.jxmpp.stringprep.XmppStringprepException

import org.jxmpp.jid.DomainBareJid

class HomeEndUserFragment : Fragment(), HandleClick {

    //lateinit var homeViewModel: HomeViewModel? = null
    lateinit var binding: FragmentHomeEndUserBinding
    lateinit var mContext: Context

    lateinit var connection: AbstractXMPPConnection

    private var multiUserChatManager: MultiUserChatManager? = null
    private lateinit var mamManager: MamManager
    private var multiUserChat: MultiUserChat? = null
    private lateinit var nickName: Resourcepart

    private val TAG = HomeEndUserFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_end_user, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = binding.root.context

        //homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        binding.lifecycleOwner = this@HomeEndUserFragment
        //binding.setHomeViewModel(homeViewModel)
        binding.clickHandle = this

        //createXmppConnection()

      /*  myConnection = XmppCustomConnection("firoz", "firoz")

        myConnection!!.doConnect()*/

    }

    private fun createXmppConnection()
    {

        GlobalScope.launch {

        var addr= InetAddress.getByName("n1.iworklab.com")
        val verifier = HostnameVerifier { hostName, sslSession ->
            println("DEBUG getHostNameVerified verify entered")
            false
        }

            var serviceName: DomainBareJid? = null
            try {
                serviceName = JidCreate.domainBareFrom("n1.iworklab.com")
            } catch (e: XmppStringprepException) {
                e.printStackTrace()
            }

        // Create a connection to the jabber.org server on a specific port.
        val config = XMPPTCPConnectionConfiguration.builder()
            .setUsernameAndPassword("firoz", "firoz")
            .setXmppDomain("n1.iworklab.com") //main
            .setHost("n1.iworklab.com")  //main
            //.setXmppDomain(serviceName) //extra
            //.setHostAddress(addr)  //extra
            //.setHostnameVerifier(verifier)  //extra
            .setPort(5222)
            .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
            .enableDefaultDebugger()
            .build()

        connection = XMPPTCPConnection(config)


            //connect to the server
            try {
                connection.connect()
                if(connection.isConnected) {
                    Log.e("app", "conn done");
                }

                // Log into the server
                connection.login()

                //will return us whether the user has been authenticated with the server.
                if(connection.isAuthenticated) {
                    Log.e("app", "Auth done");
                }

                //All of the connection and auth call are wrapped
                //in a try catch so that we can catch any failure and take proper action.

                // Disconnect from the server
                //connection.disconnect();

                if(connection.isConnected && connection.isAuthenticated)
                {
                    Log.e("app", "Connection and Auth done");
                    //now send message and receive message code here

                    multiUserChat()
                    //receiveMessageOneToOne()

                }

            } catch (e: XMPPException) {
                Log.e(TAG, "INIT XMPP EXCEPTION:" + e.message)
            } catch (e: IOException) {
                Log.e(TAG, "INIT IO EXCEPTION:" + e.message)
            } catch (e: SmackException) {
                Log.e(TAG, "INIT SMACK EXCEPTION:" + e.message)
            } catch (e: InterruptedException) {
                Log.e(TAG, "INIT Interrupted EXCEPTION:" + e.message)
            }

        }



    }

    private fun multiUserChat()
    {

        //userName can be your name by which you want to join the room
        //nickName is the name which will be shown on group chat
        nickName = Resourcepart.from("testbot2")

        var roomJid: EntityBareJid = JidCreate.entityBareFrom("open@conference.n1.iworklab.com");
        // User2 joins the new room (as a visitor)
        multiUserChatManager= MultiUserChatManager.getInstanceFor(connection)
        multiUserChat= multiUserChatManager!!.getMultiUserChat(roomJid)
        /*val mucEnterConfiguration = multiUserChat?.getEnterConfigurationBuilder(nickName)!!
            .requestNoHistory()
            .build()*/
        if (!multiUserChat!!.isJoined) {
            multiUserChat?.join(nickName)
        }

        // For listening incoming message
        multiUserChat?.addMessageListener(incomingMessageListener)
        // incomingMessageListener is implemented below.

        multiUserChat?.addPresenceInterceptor { object: PresenceListener{
            override fun processPresence(presence: Presence?) {
                TODO("Not yet implemented")
            }

        } }
        
    }

    private val incomingMessageListener = object : MessageListener {
        override fun processMessage(message: Message?) {
            if (!TextUtils.isEmpty(message?.body)) {
                //addIncomingMessageInRecycler(message!!)
                Log.e("MSGIncoming", message?.body!!)
            }
        }
    }

    private fun sendMessageMultiUser() {

//===============================================
        //adding custom reply extension
        /*<message id='923442621149' type='chat'><body>shanraisshan</body>
        <reply xmlns='shayan:reply'><rText>this is custom attribute</rText></reply>
        </message>*/

        val attributes: MutableMap<String, String> = HashMap()
        attributes["lat"] = "latitude"
        attributes["long"] = "longitude"


        var elementBuilder= StandardExtensionElement.builder("reply", "shayan:reply")
            .addAttributes(attributes)
            .setText("hello")
            .build()

        var elementBuilder2= StandardExtensionElement.builder("replyOuter", "shayan:replyOuter")
            .addElement(elementBuilder)
            .build()

        val message = connection.stanzaFactory
            .buildMessageStanza()
            .to("jsmith@jivesoftware.com")
            .setBody("Howdy! How are you?")
            .ofType(Message.Type.chat)
            .addExtension(elementBuilder2)
            .build()

        var messageBuilder= MessageBuilder.buildMessage()
            .to("open@conference.n1.iworklab.com")
            .setBody("Howdy! How are you?")
            .ofType(Message.Type.chat)
            .addExtension(elementBuilder2)

        multiUserChat?.sendMessage(messageBuilder)
        //Log.e("message --->", message.toXML().toString())
        //Log.e("message --->", messageBuilder.build().toXML().toString())

    }

    private fun getRoaster() {

        val roster = Roster.getInstanceFor(connection)
        val entries: Collection<RosterEntry> = roster.entries
        for (entry in entries)
        {
            println(entry)
        }

    }

    private fun rosterListener(roster: Roster)
    {
        roster.addRosterListener(object : RosterListener {
            override fun entriesAdded(addresses: MutableCollection<Jid>?) {
                TODO("Not yet implemented")
            }

            override fun entriesUpdated(addresses: MutableCollection<Jid>?) {
                TODO("Not yet implemented")
            }

            override fun entriesDeleted(addresses: MutableCollection<Jid>?) {
                TODO("Not yet implemented")
            }

            override fun presenceChanged(presence: Presence?) {
                println("Presence changed: " + presence!!.from + " " + presence)
            }

        })

        roster.addPresenceEventListener(object : PresenceEventListener {
            override fun presenceAvailable(address: FullJid?, availablePresence: Presence?) {
                TODO("Not yet implemented")
            }

            override fun presenceUnavailable(address: FullJid?, presence: Presence?) {
                TODO("Not yet implemented")
            }

            override fun presenceError(address: Jid?, errorPresence: Presence?) {
                TODO("Not yet implemented")
            }

            override fun presenceSubscribed(address: BareJid?, subscribedPresence: Presence?) {
                TODO("Not yet implemented")
            }

            override fun presenceUnsubscribed(address: BareJid?, unsubscribedPresence: Presence?) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun sendMessageOneToOne() {
//========================================
        /*val presence= connection.stanzaFactory
            .buildPresenceStanza()
            .setPriority(1)
            .ofType(Presence.Type.available)
            .setMode(Presence.Mode.available)
            .setStatus("online")
            .build()

        connection.sendStanza(presence)*/

//========================================================
        /*val message = connection.stanzaFactory
            .buildMessageStanza()
            .to("jsmith@jivesoftware.com")
            .setBody("Howdy! How are you?")
            .ofType(Message.Type.chat)
            .build()
        connection.sendStanza(message)*/

//===============================================
        //adding custom reply extension
        /*<message id='923442621149' type='chat'><body>shanraisshan</body>
        <reply xmlns='shayan:reply'><rText>this is custom attribute</rText></reply>
        </message>*/

        val attributes: MutableMap<String, String> = HashMap()
        attributes["lat"] = "latitude"
        attributes["long"] = "longitude"


        var elementBuilder= StandardExtensionElement.builder("reply", "shayan:reply")
        .addAttributes(attributes)
        .setText("hello")
        .build()

        var elementBuilder2= StandardExtensionElement.builder("replyOuter", "shayan:replyOuter")
            .addElement(elementBuilder)
            .build()

        val message = connection.stanzaFactory
            .buildMessageStanza()
            .to("jsmith@jivesoftware.com")
            .setBody("Howdy! How are you?")
            .ofType(Message.Type.chat)
            .addExtension(elementBuilder2)
            .build()
        connection.sendStanza(message)
        Log.e("message --->", message.toXML().toString())

//===================================================

        //DeliveryReceiptRequest.addTo(message);
        /*val dm = DeliveryReceiptManager
            .getInstanceFor(connection)
        dm.autoReceiptMode = AutoReceiptMode.always
        dm.addReceiptReceivedListener(object : ReceiptReceivedListener {

            override fun onReceiptReceived(
                fromJid: Jid?,
                toJid: Jid?,
                receiptId: String?,
                receipt: Stanza?
            ) {

                TODO("Not yet implemented")
            }
        })*/

//========================================================
        /*var jid2: EntityBareJid = JidCreate.entityBareFrom("jsmith@jivesoftware.com");
        val message = Message()
        message.body = "msg_body"
        message.type = Message.Type.chat
        message.to = jid2
        message.subject = "Subject"
        connection.sendStanza(message)*/

        // Assume we've created an XMPPConnection name "connection"._
        /*var chatManager: ChatManager = ChatManager.getInstanceFor(connection)

        //entitty bare jid consists of username to whom
        //you want to send message and domain of server
        var jid: EntityBareJid = JidCreate.entityBareFrom("jsmith@jivesoftware.com");
        var chat: Chat = chatManager.chatWith(jid);
        chat.send("Howdy!");*/
        //custom send
        /*val newMessage = Message()
        newMessage.setBody("Howdy!")
        // Additional modifications to the message Stanza.
        //JivePropertiesManager.addProperty(newMessage, "favoriteColor", "red")
        chat.send(newMessage)*/

    }

    private fun receiveMessageOneToOne() {
        // Assume we've created an XMPPConnection name "connection"._
        /*var chatManager: ChatManager = ChatManager.getInstanceFor(connection)

        //EntityBareJid from, Message message, Chat chat
        chatManager.addIncomingListener { from, message, chat ->


            println("New message from " + from + ": " + message.body)

        }*/

        var chatManager: ChatManager = ChatManager.getInstanceFor(connection)

        //EntityBareJid from, Message message, Chat chat
        chatManager.addIncomingListener { from, message, chat ->

            /*val repExt= message.getExtension("shayan:replyOuter") as StandardExtensionElement
            if (repExt != null) {
                Log.e("--->", " ---  LOG REPLY EXTENSION ---")
                //Log.e(javaClass.simpleName, repExt.getValue("rText"))
                Log.e(javaClass.simpleName, repExt.getAttributeValue("rText"))
            }*/
            println("New message from " + from + ": " + message.body)

        }

    }

    override fun clickHandle(v: View) {
        when(v.id) {

            R.id.imgMenu -> {

                /*val activity: HomeEndUserActivity? = activity as HomeEndUserActivity?
                activity?.openDrawer()*/

                //sendMessageOneToOne()
                //sendMessageMultiUser()

                startActivity(Intent(mContext, ChatActivity::class.java))

                //sendMessageMultiUser()

            }

        }
    }
}