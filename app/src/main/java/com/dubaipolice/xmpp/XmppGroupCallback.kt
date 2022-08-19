package com.dubaipolice.xmpp

import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.db.model.MessageInfoTable
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.MessageBuilder

interface XmppGroupCallback {

    fun receivedMessage(message: MessageInfoTable)
    fun sentMessage(message: MessageInfoTable)
    fun groupUpdated(isUpdated: Boolean)

}