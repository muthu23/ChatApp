package com.dubaipolice.callback

import android.view.View
import com.dubaipolice.db.model.MessageInfoTable
import com.dubaipolice.model.*

interface HandleClick {
    fun clickHandle(v: View)
}

interface HandleChatItemClick {
    fun itemClickHandle(messageInfoTable: MessageInfoTable)
}

interface HandleUserItemClick {
    fun itemClickHandle(user: Users)
}

interface HandleMediaItemClick {
    fun itemClickHandle(media: Attachment)
}

interface HandleVideoStreamItemClick {
    fun itemClickHandle(media: VideoItem?)
}

interface HandleLiveStreamItemClick {
    fun itemClickHandle(media: Stream?)
}
interface HandleNotificationSoundItemClick {
    fun itemClickHandle(item: NotificationTune?, isChecked: Boolean?, position :Int)
}
interface HandleGroupHelpItemChecked {
    fun itemClickHandle(item: GroupHelpItem?, isChecked: Boolean?, position :Int)
}
interface HandleNotificationListItemClick {
    fun itemClickHandle(isVisible:Boolean,notifications :Notifications?)
}