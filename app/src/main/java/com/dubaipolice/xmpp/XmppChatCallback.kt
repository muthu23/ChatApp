package com.dubaipolice.xmpp

import android.view.View
import com.dubaipolice.db.model.MessageInfoTable
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.MessageBuilder

interface XmppChatCallback {

    fun receivedMessage(message: MessageInfoTable)
    fun sentMessage(message: MessageInfoTable)
    fun deliveredMessage(message: MessageInfoTable)
    fun displayedMessage(message: MessageInfoTable)
    fun typingIndicator(message: MessageInfoTable)

}