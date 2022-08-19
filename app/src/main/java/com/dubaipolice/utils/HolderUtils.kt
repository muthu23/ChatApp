package com.dubaipolice.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dubaipolice.R
import com.dubaipolice.db.model.MessageInfoTable
import java.io.File

fun ImageView.generateStatus(itemChatItem: MessageInfoTable) {
    when {
        !itemChatItem.displayedAt.isNullOrBlank() -> setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_chat_read_blue
            )
        )
        !itemChatItem.deliveredAt.isNullOrBlank() -> setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_chat_delivered_white
            )
        )
        !itemChatItem.sentAt.isNullOrBlank() -> setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_chat_sent_white
            )
        )
        else -> setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_chat_pending_white
            )
        )
    }
}


fun ImageView.setDownloadVisibility(chat: MessageInfoTable) {
    visibility =
        if (!chat.mediaUrlLocal.isNullOrBlank() && File(chat.mediaUrlLocal!!).exists())
            View.GONE
        else View.VISIBLE
}

fun setPlayVisibility(chat: MessageInfoTable): Int {
    return if (!chat.mediaUrlLocal.isNullOrBlank() && File(chat.mediaUrlLocal!!).exists())
        View.VISIBLE
    else View.GONE
}

fun Context.openLink(url: String?) {
    Intent(Intent.ACTION_VIEW).apply {
        data = url?.toUri()
        startActivity(this)
    }
}

fun TextView.setDateVisibility(
    position: Int,
    itemCurrentItem: MessageInfoTable,
    itemLastItem: ArrayList<MessageInfoTable>
) {
    val date: String? = DateUtils.getChatDateFromTimestamp(itemCurrentItem.unixTimestamp!!)
    visibility = when {
        position > 0 -> if (date.equals(
                DateUtils.getChatDateFromTimestamp(itemLastItem[position - 1].unixTimestamp!!),
                ignoreCase = true
            )
        ) View.GONE else View.VISIBLE
        else -> View.VISIBLE
    }
}

fun setUserImage(mContext: Context?, senderImageUrl: String?, imgSenderImage: ImageView) {
    Glide.with(mContext!!).load(senderImageUrl).placeholder(
        AppCompatResources.getDrawable(mContext, R.drawable.ic_profile)
    )
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(imgSenderImage)

}

fun setThumbMediaImage(
    mContext: Context?,
    mediaThumbImageUrl: String?,
    imgMedia: ImageView
) {
    Glide.with(mContext!!).load(mediaThumbImageUrl).placeholder(
        AppCompatResources.getDrawable(mContext, R.drawable.progress_animation)
    )
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(imgMedia)

}

fun setThumbDocImage(
    mContext: Context?,
    imgMedia: ImageView
) {
    Glide.with(mContext!!).load(R.drawable.ic_docs)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(imgMedia)

}

fun setThumbVoiceImage(
    mContext: Context?,
    imgMedia: ImageView
) {
    Glide.with(mContext!!).load(R.drawable.audio_file)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(imgMedia)

}

fun Context.openMediaIntent(mediaFile: String?, type: String) {

    try {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = FileProvider.getUriForFile(
            this,
            "com.dubaipolice.fileprovider", File(mediaFile!!)
        )
        intent.setDataAndType(uri, type)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //DO NOT FORGET THIS EVER
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        //startActivity(Intent.createChooser(intent, "Select Player"))
    } catch (e: Exception) {
        e.printStackTrace()
        Utils.showToast(applicationContext, "Audio Player not Found!")
    }
}

fun Context.openMapIntent(latitude: String, longitude: String) {
    try {
        val mapUri = Uri.parse("geo:0,0?q=$latitude,$longitude(Location)")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    } catch (e: Exception) {
        e.printStackTrace()
        Utils.showToast(applicationContext, "Google Maps not Found!")
    }
}

fun Context.openContactIntent(name: String, phone: String) {
    try {
        val addContactIntent = Intent(Intent.ACTION_INSERT)
        addContactIntent.type = ContactsContract.Contacts.CONTENT_TYPE
        addContactIntent.putExtra(ContactsContract.Intents.Insert.NAME, name)
        addContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, phone)
        startActivity(addContactIntent)
    } catch (e: Exception) {
        e.printStackTrace()
        Utils.showToast(applicationContext, "Contact app not Found!")
    }
}
fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}
