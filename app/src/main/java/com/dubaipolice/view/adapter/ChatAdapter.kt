package com.dubaipolice.view.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dubaipolice.R
import com.dubaipolice.callback.HandleChatItemClick
import com.dubaipolice.db.model.MessageInfoTable
import com.dubaipolice.utils.*
import com.dubaipolice.view.activity.OnlineMediaPlayerActivity
import com.stfalcon.imageviewer.StfalconImageViewer
import java.io.File
import java.util.HashMap


@Suppress("PrivatePropertyName")
class ChatAdapter(
    context: Context,
    chatList: ArrayList<MessageInfoTable>,
    clickHandle: HandleChatItemClick
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //identify current and other user, the set layout accordingly.
    private val TYPE_EMPTY_DEFAULT = 0
    private val TYPE_MSG_TEXT: Int = 1
    private val TYPE_MSG_IMAGE: Int = 2
    private val TYPE_MSG_VIDEO: Int = 3
    private val TYPE_MSG_AUDIO: Int = 4
    private val TYPE_MSG_DOCUMENT: Int = 5
    private val TYPE_MSG_LOCATION: Int = 6
    private val TYPE_MSG_CONTACT: Int = 7

    //so on
    private val TYPE_MSG_TEXT_LEFT = 10
    private val TYPE_MSG_TEXT_RIGHT = 11
    private val TYPE_MSG_IMAGE_LEFT = 12
    private val TYPE_MSG_IMAGE_RIGHT = 13
    private val TYPE_MSG_VIDEO_LEFT = 14
    private val TYPE_MSG_VIDEO_RIGHT = 15
    private val TYPE_MSG_AUDIO_LEFT = 16
    private val TYPE_MSG_AUDIO_RIGHT = 17
    private val TYPE_MSG_DOCUMENT_LEFT = 18
    private val TYPE_MSG_DOCUMENT_RIGHT = 19
    private val TYPE_MSG_LOCATION_LEFT = 20
    private val TYPE_MSG_LOCATION_RIGHT = 21
    private val TYPE_MSG_CONTACT_LEFT = 22
    private val TYPE_MSG_CONTACT_RIGHT = 23

    var mContext: Context? = null
    private var clickHandle: HandleChatItemClick
    var chatList: ArrayList<MessageInfoTable> = ArrayList()

    var currentAudioPlayingPosition= -1

    val mediaPlayer: MediaPlayer? = MediaPlayer()

    /**
     * Used these lists to assign atleast single color to all users
     * after then duplicate colors
     */
    private var colorsList: ArrayList<Int> = ArrayList()
    private var unAssignedColorsList: ArrayList<Int> = ArrayList()

    /**
     * can use this in place of unAssignedColorList as it will
     * color the user name randomly even duplicate color
     */
    val colors = arrayOf(
        Color.parseColor("#e28743"),
        Color.parseColor("#eab676"),
        Color.parseColor("#1e81b0"),
        Color.parseColor("#FF3377"),
        Color.parseColor("#F933FF"),
        Color.parseColor("#0AB511"),
    )



    val assignedColorsList: MutableMap<String, Int> = HashMap()

    init {
        this.mContext = context
        this.chatList = chatList
        this.clickHandle = clickHandle

        colorsList.add(Color.parseColor("#e28743"))
        colorsList.add(Color.parseColor("#eab676"))
        colorsList.add(Color.parseColor("#1e81b0"))
        colorsList.add(Color.parseColor("#FF3377"))
        colorsList.add(Color.parseColor("#F933FF"))
        colorsList.add(Color.parseColor("#0AB511"))

        unAssignedColorsList.addAll(colorsList)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?
        when (viewType) {
            TYPE_MSG_TEXT_LEFT -> {
                view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_chat_left, parent, false)
                return ChatLeftViewHolder(view)
            }
            TYPE_MSG_TEXT_RIGHT -> {
                view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_chat_right, parent, false)
                return ChatRightViewHolder(view)
            }
            TYPE_MSG_IMAGE_LEFT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_left_images, parent, false)
                return ChatLeftImageViewHolder(view)
            }
            TYPE_MSG_IMAGE_RIGHT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_right_images, parent, false)
                return ChatRightImageViewHolder(view)
            }
            TYPE_MSG_VIDEO_LEFT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_left_videos, parent, false)
                return ChatLeftVideoViewHolder(view)
            }
            TYPE_MSG_VIDEO_RIGHT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_right_videos, parent, false)
                return ChatRightVideoViewHolder(view)
            }
            TYPE_MSG_AUDIO_LEFT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_left_audios, parent, false)
                return ChatLeftAudioViewHolder(view)
            }
            TYPE_MSG_AUDIO_RIGHT -> {
                Log.e("ItemViewType", "AudioRight")
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_right_audios, parent, false)
                return ChatRightAudioViewHolder(view)
            }
            TYPE_MSG_DOCUMENT_LEFT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_left_documents, parent, false)
                return ChatLeftDocumentViewHolder(view)
            }
            TYPE_MSG_DOCUMENT_RIGHT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_right_documents, parent, false)
                return ChatRightDocumentViewHolder(view)
            }
            TYPE_MSG_LOCATION_LEFT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_left_location, parent, false)
                return ChatLeftLocationViewHolder(view)
            }
            TYPE_MSG_LOCATION_RIGHT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_right_location, parent, false)
                return ChatRightLocationViewHolder(view)
            }
            TYPE_MSG_CONTACT_LEFT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_left_contact, parent, false)
                return ChatLeftContactViewHolder(view)
            }
            TYPE_MSG_CONTACT_RIGHT -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_right_contact, parent, false)
                return ChatRightContactViewHolder(view)
            }
            else -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_default_empty, parent, false)
                return DefaultEmptyViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_MSG_TEXT_LEFT -> {
                (holder as ChatLeftViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_TEXT_RIGHT -> {
                (holder as ChatRightViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_IMAGE_LEFT -> {
                (holder as ChatLeftImageViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_IMAGE_RIGHT -> {
                (holder as ChatRightImageViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_VIDEO_LEFT -> {
                (holder as ChatLeftVideoViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_VIDEO_RIGHT -> {
                (holder as ChatRightVideoViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_AUDIO_LEFT -> {
                (holder as ChatLeftAudioViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_AUDIO_RIGHT -> {
                (holder as ChatRightAudioViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_DOCUMENT_LEFT -> {
                (holder as ChatLeftDocumentViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_DOCUMENT_RIGHT -> {
                (holder as ChatRightDocumentViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_LOCATION_LEFT -> {
                (holder as ChatLeftLocationViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_LOCATION_RIGHT -> {
                (holder as ChatRightLocationViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_CONTACT_LEFT -> {
                (holder as ChatLeftContactViewHolder).bind(
                    position
                )
            }
            TYPE_MSG_CONTACT_RIGHT -> {
                (holder as ChatRightContactViewHolder).bind(
                    position
                )
            }
            else -> (holder as DefaultEmptyViewHolder).bind(position)
        }

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        when (chatList[position].type) {
            TYPE_MSG_TEXT.toString() -> {
                return if (SharedPref.readString(AppConstants.KEY_XMPP_USER_JID) == chatList[position].senderJid) {
                    TYPE_MSG_TEXT_RIGHT
                } else {
                    TYPE_MSG_TEXT_LEFT
                }
            }
            TYPE_MSG_IMAGE.toString() -> {
                return if (SharedPref.readString(AppConstants.KEY_XMPP_USER_JID) == chatList[position].senderJid) {
                    TYPE_MSG_IMAGE_RIGHT
                } else {
                    TYPE_MSG_IMAGE_LEFT
                }
            }
            TYPE_MSG_VIDEO.toString() -> {
                return if (SharedPref.readString(AppConstants.KEY_XMPP_USER_JID) == chatList[position].senderJid) {
                    TYPE_MSG_VIDEO_RIGHT
                } else {
                    TYPE_MSG_VIDEO_LEFT
                }
            }
            TYPE_MSG_AUDIO.toString() -> {
                Log.e("ItemViewType", "Audio")
                return if (SharedPref.readString(AppConstants.KEY_XMPP_USER_JID) == chatList[position].senderJid) {
                    TYPE_MSG_AUDIO_RIGHT
                } else {
                    TYPE_MSG_AUDIO_LEFT
                }
            }
            TYPE_MSG_DOCUMENT.toString() -> {
                return if (SharedPref.readString(AppConstants.KEY_XMPP_USER_JID) == chatList[position].senderJid) {
                    TYPE_MSG_DOCUMENT_RIGHT
                } else {
                    TYPE_MSG_DOCUMENT_LEFT
                }
            }
            TYPE_MSG_LOCATION.toString() -> {
                return if (SharedPref.readString(AppConstants.KEY_XMPP_USER_JID) == chatList[position].senderJid) {
                    TYPE_MSG_LOCATION_RIGHT
                } else {
                    TYPE_MSG_LOCATION_LEFT
                }
            }
            TYPE_MSG_CONTACT.toString() -> {
                return if (SharedPref.readString(AppConstants.KEY_XMPP_USER_JID) == chatList[position].senderJid) {
                    TYPE_MSG_CONTACT_RIGHT
                } else {
                    TYPE_MSG_CONTACT_LEFT
                }
            }
            else -> {
                return TYPE_EMPTY_DEFAULT
            }
        }

    }

    inner class ChatRightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvMessage: TextView = itemView.findViewById(R.id.txt_message)
        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgSeenStatus: ImageView = itemView.findViewById(R.id.imgSeenStatus)
        private lateinit var chat: MessageInfoTable

        override fun onClick(v: View) {
        }


        fun bind(position: Int) {
            chat = chatList[position]

            tvMessage.text = chat.messageText
            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)

            imgSeenStatus.generateStatus(chat)
            tvDate.setDateVisibility(position, chat, chatList)
        }

    }

    inner class ChatLeftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvMessage: TextView = itemView.findViewById(R.id.txt_message)
        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var tvSenderName: TextView = itemView.findViewById(R.id.txt_sender_name)
        private var senderProfile: ImageView = itemView.findViewById(R.id.sender_profile)
        private lateinit var chat: MessageInfoTable

        override fun onClick(v: View) {}

        fun bind(position: Int) {
            chat = chatList[position]

            tvMessage.text = chat.messageText
            tvSenderName.text = chat.senderName

            addColorToSenderName(tvSenderName, chat)

            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)

            tvDate.setDateVisibility(position, chat, chatList)
            setUserImage(mContext, chat.senderImageUrl, senderProfile)

        }

    }

    private fun addColorToSenderName(tvSenderName: TextView, chat: MessageInfoTable) {

        if(assignedColorsList.isEmpty())
        {
            if(unAssignedColorsList.isNotEmpty())
            {
                var randomColor= unAssignedColorsList.random()
                assignedColorsList.put(chat.senderJid!!, randomColor)
                tvSenderName.setTextColor(randomColor)
                unAssignedColorsList.remove(randomColor)
            }
            else
            {
                var randomColor= colorsList.random()
                assignedColorsList.put(chat.senderJid!!, randomColor)
                tvSenderName.setTextColor(randomColor)
            }
        }
        else
        {
            var isColorAssigned= false
            var assignedColor= 0
            assignedColorsList.forEach { (key, value) ->
                if(key.equals(chat.senderJid, ignoreCase = true))
                {
                    isColorAssigned= true
                    assignedColor= value
                }
                //println("$key = $value")
            }

            if(isColorAssigned)
            {
                tvSenderName.setTextColor(assignedColor)
            }
            else
            {
                if(unAssignedColorsList.isNotEmpty())
                {
                    var randomColor= unAssignedColorsList.random()
                    assignedColorsList.put(chat.senderJid!!, randomColor)
                    tvSenderName.setTextColor(randomColor)
                    unAssignedColorsList.remove(randomColor)
                }
                else
                {
                    var randomColor= colorsList.random()
                    assignedColorsList.put(chat.senderJid!!, randomColor)
                    tvSenderName.setTextColor(randomColor)
                }

            }

        }

    }

    /* Image right (for Me) view holder*/
    inner class ChatRightImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgSeenStatus: ImageView = itemView.findViewById(R.id.imgSeenStatus)
        private var imgMessage: ImageView = itemView.findViewById(R.id.img_message_right)

        private var imgDownload: ImageView = itemView.findViewById(R.id.imgDownload)
        private var tvOpenImage: TextView = itemView.findViewById(R.id.txt_open_images_right)

        private lateinit var chat: MessageInfoTable

        init {
            tvOpenImage.setOnClickListener(this)
            imgDownload.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            chat = chatList[bindingAdapterPosition]
            when (v.id) {
                R.id.txt_open_images_right -> {

                    val images = mutableListOf<String>()
                    chat.mediaUrlLocal?.let {
                        images.add(chat.mediaUrlLocal.toString())
                    }
                    if (images.isNotEmpty())
                    {
                        StfalconImageViewer.Builder(mContext, images) { view, image ->
                            Glide.with(mContext!!).load(image)
                                .into(view)
                        }.show()
                    }

                    //mContext!!.openMediaIntent(chat.mediaUrlLocal.toString(), "image/*")
                }
                R.id.imgDownload -> {
                    imgDownload.visibility = View.GONE
                    clickHandle.itemClickHandle(chat)
                }
            }

        }

        fun bind(position: Int) {

            chat = chatList[position]

            imgDownload.setDownloadVisibility(chat)
            tvOpenImage.visibility = setPlayVisibility(chat)

            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)

            imgSeenStatus.generateStatus(chat)
            tvDate.setDateVisibility(position, chat, chatList)
            setThumbMediaImage(mContext, chat.mediaThumbImageUrl, imgMessage)

        }
    }

    /* Image left (for Others) view holder*/
    inner class ChatLeftImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvSenderName: TextView = itemView.findViewById(R.id.txt_sender_name)
        private var senderProfile: ImageView = itemView.findViewById(R.id.sender_profile)
        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgMessage: ImageView = itemView.findViewById(R.id.img_message_left)

        private var imgDownload: ImageView = itemView.findViewById(R.id.imgDownload)
        private var tvOpenImage: TextView = itemView.findViewById(R.id.txt_open_images_left)

        private lateinit var chat: MessageInfoTable

        init {
            tvOpenImage.setOnClickListener(this)
            imgDownload.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            chat = chatList[bindingAdapterPosition]
            when (v.id) {
                R.id.txt_open_images_left -> {

                    val images = mutableListOf<String>()
                    chat.mediaUrlLocal?.let {
                        images.add(chat.mediaUrlLocal.toString())
                    }
                    if (images.isNotEmpty())
                    {
                        StfalconImageViewer.Builder(mContext, images) { view, image ->
                            Glide.with(mContext!!).load(image)
                                .into(view)
                        }.show()
                    }

                    //mContext!!.openMediaIntent(chat.mediaUrlLocal.toString(), "image/*")
                }
                R.id.imgDownload -> {
                    imgDownload.visibility = View.GONE
                    clickHandle.itemClickHandle(chat)
                }
            }

        }

        fun bind(position: Int) {
            chat = chatList[position]

            imgDownload.setDownloadVisibility(chat)
            tvOpenImage.visibility = setPlayVisibility(chat)

            tvSenderName.text = chat.senderName

            addColorToSenderName(tvSenderName, chat)

            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)

            tvDate.setDateVisibility(position, chat, chatList)
            setUserImage(mContext, chat.senderImageUrl, senderProfile)
            setThumbMediaImage(mContext, chat.mediaThumbImageUrl, imgMessage)

        }
    }

    /* Location right (for Me) view holder*/
    inner class ChatRightLocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgSeenStatus: ImageView = itemView.findViewById(R.id.imgSeenStatus)
        private var imgMessage: ImageView = itemView.findViewById(R.id.img_message_right)
        private lateinit var chat: MessageInfoTable

        init {
            imgMessage.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            when (v.id) {
                R.id.img_message_right -> {
                    chat = chatList[bindingAdapterPosition]
                    mContext!!.openMapIntent(chat.latitude.toString(), chat.longitude.toString())
                }

            }

        }

        fun bind(position: Int) {

            chat = chatList[position]
            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)

            imgSeenStatus.generateStatus(chat)
            tvDate.setDateVisibility(position, chat, chatList)

        }
    }

    /* Location left (for Others) view holder*/
    inner class ChatLeftLocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvSenderName: TextView = itemView.findViewById(R.id.txt_sender_name)
        private var senderProfile: ImageView = itemView.findViewById(R.id.sender_profile)
        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgMessage: ImageView = itemView.findViewById(R.id.img_message_left)
        private lateinit var chat: MessageInfoTable

        init {
            imgMessage.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            when (v.id) {
                R.id.img_message_left -> {
                    chat = chatList[bindingAdapterPosition]
                    mContext!!.openMapIntent(chat.latitude.toString(), chat.longitude.toString())

                }
            }
        }

        fun bind(position: Int) {
            chat = chatList[position]
            tvSenderName.text = chat.senderName

            addColorToSenderName(tvSenderName, chat)

            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)

            tvDate.setDateVisibility(position, chat, chatList)
            setUserImage(mContext, chat.senderImageUrl, senderProfile)

        }
    }

    /* Video right (for Me) view holder*/
    inner class ChatRightVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgSeenStatus: ImageView = itemView.findViewById(R.id.imgSeenStatus)
        private var imgMessage: ImageView = itemView.findViewById(R.id.video_thumbnail_right)

        private var imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)
        private var imgDownload: ImageView = itemView.findViewById(R.id.imgDownload)
        private lateinit var chat: MessageInfoTable

        init {
            imgPlay.setOnClickListener(this)
            imgDownload.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            chat = chatList[bindingAdapterPosition]
            when (v.id) {
                R.id.imgPlay -> {
                    val i = Intent(mContext, OnlineMediaPlayerActivity::class.java)
                    i.putExtra(AppConstants.IntentConstants.HLS_LINK_DATA, chat.mediaUrlLocal.toString())
                    mContext!!.startActivity(i)
                    //mContext!!.openMediaIntent(chat.mediaUrlLocal.toString(), "video/*")
                }
                R.id.imgDownload -> {
                    imgDownload.visibility = View.GONE
                    clickHandle.itemClickHandle(chat)
                }
            }

        }

        fun bind(position: Int) {
            chat = chatList[position]

            imgPlay.visibility =  setPlayVisibility(chat)
            imgDownload.setDownloadVisibility(chat)

            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)

            imgSeenStatus.generateStatus(chat)
            tvDate.setDateVisibility(position, chat, chatList)
            setThumbMediaImage(mContext, chat.mediaThumbImageUrl, imgMessage)


        }
    }

    /* Video left (for Others) view holder*/
    inner class ChatLeftVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvSenderName: TextView = itemView.findViewById(R.id.txt_sender_name)
        private var senderProfile: ImageView = itemView.findViewById(R.id.sender_profile)
        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgMessage: ImageView = itemView.findViewById(R.id.video_thumbnail_left)

        private var imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)
        private var imgDownload: ImageView = itemView.findViewById(R.id.imgDownload)
        private lateinit var chat: MessageInfoTable

        init {
            imgPlay.setOnClickListener(this)
            imgDownload.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            chat = chatList[bindingAdapterPosition]
            when (v.id) {
                R.id.imgPlay -> {
                    val i = Intent(mContext, OnlineMediaPlayerActivity::class.java)
                    i.putExtra(AppConstants.IntentConstants.HLS_LINK_DATA, chat.mediaUrlLocal.toString())
                    mContext!!.startActivity(i)
                    //mContext!!.openMediaIntent(chat.mediaUrlLocal.toString(), "video/*")
                }
                R.id.imgDownload -> {
                    imgDownload.visibility = View.GONE
                    clickHandle.itemClickHandle(chat)
                }
            }

        }

        fun bind(position: Int) {
            chat = chatList[position]
            imgPlay.visibility = setPlayVisibility(chat)
            imgDownload.setDownloadVisibility(chat)

            tvSenderName.text = chat.senderName

            addColorToSenderName(tvSenderName, chat)

            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)

            tvDate.setDateVisibility(position, chat, chatList)
            setUserImage(mContext, chat.senderImageUrl, senderProfile)
            setThumbMediaImage(mContext, chat.mediaThumbImageUrl, imgMessage)

        }
    }

    /* Audio right (for Me) view holder*/
    inner class ChatRightAudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgSeenStatus: ImageView = itemView.findViewById(R.id.img_seen_status)
        private var imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)
        private var imgStop: ImageView = itemView.findViewById(R.id.imgStop)
        private var imgDownload: ImageView = itemView.findViewById(R.id.imgDownload)
        private var tvAudioDuration: TextView = itemView.findViewById(R.id.audio_duration)
        private lateinit var chat: MessageInfoTable

        init {
            imgPlay.setOnClickListener(this)
            imgStop.setOnClickListener(this)
            imgDownload.setOnClickListener(this)

        }

        override fun onClick(v: View) {
            chat = chatList[bindingAdapterPosition]
            when (v.id) {
                R.id.imgPlay -> {
                    //mContext!!.openMediaIntent(chat.mediaUrlLocal, "audio/*")
                    imgPlay.visibility = View.GONE
                    imgStop.visibility= View.VISIBLE

                    playAudio(chat.mediaUrlLocal, bindingAdapterPosition)
                }
                R.id.imgStop -> {
                    //mContext!!.openMediaIntent(chat.mediaUrlLocal, "audio/*")
                    imgPlay.visibility = View.VISIBLE
                    imgStop.visibility= View.GONE
                    stopAudio()
                }
                R.id.imgDownload -> {
                    imgDownload.visibility = View.GONE
                    clickHandle.itemClickHandle(chat)
                }
            }

        }

        fun bind(position: Int) {
            chat = chatList[bindingAdapterPosition]

            imgStop.visibility= View.GONE

            tvAudioDuration.text= chat.mediaDuration

            imgPlay.visibility = setPlayVisibility(chat)
            imgDownload.setDownloadVisibility(chat)

            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            imgSeenStatus.generateStatus(chat)
            val date: String? = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = date
            tvDate.setDateVisibility(position, chat, chatList)

        }
    }

    private fun playAudio(mediaUrlLocal: String?, selectedAudioPosition: Int) {

        if(currentAudioPlayingPosition != -1)
        {
            if(currentAudioPlayingPosition != selectedAudioPosition)
            {
                notifyItemChanged(currentAudioPlayingPosition)
            }
        }
        currentAudioPlayingPosition= selectedAudioPosition

        val audioUri: Uri = FileProvider.getUriForFile(
            mContext!!,
            "com.dubaipolice.fileprovider", File(mediaUrlLocal!!)
        )

        if(mediaPlayer != null){
            mediaPlayer.stop()
            mediaPlayer.reset()
            //mediaPlayer.release()
            //audio is stopped here
        }

/*        mediaPlayer?.setOnCompletionListener(object: MediaPlayer.OnCompletionListener{
            override fun onCompletion(mp: MediaPlayer?) {
                //notifyItemChanged(position)
            }
        })*/

        mediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        mediaPlayer?.setDataSource(mContext!!, audioUri)
        mediaPlayer?.prepare()

        mediaPlayer?.setOnCompletionListener(object:  MediaPlayer.OnCompletionListener {
            override fun onCompletion(myMedia: MediaPlayer?) {

                notifyItemChanged(currentAudioPlayingPosition)

            }


        })

        mediaPlayer?.start()

    }

    private fun stopAudio() {

        if(mediaPlayer != null){
            mediaPlayer.stop()
            mediaPlayer.reset()
            //mediaPlayer.release()
            //audio is stopped here
        }

    }

    /* Audio Left (for Me) view holder*/
    inner class ChatLeftAudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvSenderName: TextView = itemView.findViewById(R.id.txt_sender_name)
        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgSenderImage: ImageView = itemView.findViewById(R.id.sender_image)
        private var imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)
        private var imgStop: ImageView = itemView.findViewById(R.id.imgStop)
        private var imgDownload: ImageView = itemView.findViewById(R.id.imgDownload)
        private var tvAudioDuration: TextView = itemView.findViewById(R.id.audio_duration)
        private lateinit var chat: MessageInfoTable

        init {
            imgPlay.setOnClickListener(this)
            imgStop.setOnClickListener(this)
            imgDownload.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            chat = chatList[bindingAdapterPosition]
            when (v.id) {
                R.id.imgPlay -> {
                    //mContext!!.openMediaIntent(chat.mediaUrlLocal, "audio/*")
                    imgPlay.visibility = View.GONE
                    imgStop.visibility= View.VISIBLE
                    playAudio(chat.mediaUrlLocal, bindingAdapterPosition)
                }
                R.id.imgStop -> {
                    //mContext!!.openMediaIntent(chat.mediaUrlLocal, "audio/*")
                    imgPlay.visibility = View.VISIBLE
                    imgStop.visibility= View.GONE
                    stopAudio()
                }
                R.id.imgDownload -> {
                    imgDownload.visibility = View.GONE
                    clickHandle.itemClickHandle(chat)
                }
            }
        }

        fun bind(position: Int) {
            chat = chatList[position]

            imgStop.visibility= View.GONE

            tvAudioDuration.text= chat.mediaDuration

            imgPlay.visibility = setPlayVisibility(chat)
            imgDownload.setDownloadVisibility(chat)

            tvSenderName.text = chat.senderName

            addColorToSenderName(tvSenderName, chat)

            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            val date: String? = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = date
            tvDate.setDateVisibility(position, chat, chatList)
            setUserImage(mContext, chat.senderImageUrl, imgSenderImage)

        }
    }

    /* Contact right (for Me) view holder*/
    inner class ChatRightContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgSeenStatus: ImageView = itemView.findViewById(R.id.img_seen_status)
        private var imgContactProfile: ImageView = itemView.findViewById(R.id.img_contact_user_right)
        private var tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name_right)
        private var tvContactPhone: TextView = itemView.findViewById(R.id.tv_contact_phone_right)
        private var lnrContactLayout: LinearLayout = itemView.findViewById(R.id.layout_contact_right)

        private lateinit var chat: MessageInfoTable

        init {
            lnrContactLayout.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            chat = chatList[bindingAdapterPosition]
            when (v.id) {
                R.id.layout_contact_right -> {
                    mContext!!.openContactIntent(Utils.replaceNull(chat.contactFirstName).plus(" "+Utils.replaceNull(chat.contactMiddleName)).plus(" "+Utils.replaceNull(chat.contactLastName)), chat.contactPhone!!)
                }
            }

        }

        fun bind(position: Int) {
            chat = chatList[bindingAdapterPosition]
            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            imgSeenStatus.generateStatus(chat)
            val date: String? = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = date
            tvDate.setDateVisibility(position, chat, chatList)

            tvContactName.text = Utils.replaceNull(chat.contactFirstName).plus(" "+Utils.replaceNull(chat.contactMiddleName)).plus(" "+Utils.replaceNull(chat.contactLastName))
            tvContactPhone.text = chat.contactPhone


        }
    }

    /* Contact Left (for Me) view holder*/
    inner class ChatLeftContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvSenderName: TextView = itemView.findViewById(R.id.txt_sender_name)
        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgSenderImage: ImageView = itemView.findViewById(R.id.sender_profile)
        private var imgContactProfile: ImageView = itemView.findViewById(R.id.img_contact_user_left)
        private var tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name_left)
        private var tvContactPhone: TextView = itemView.findViewById(R.id.tv_contact_phone_left)
        private var lnrContactLayout: RelativeLayout = itemView.findViewById(R.id.layout_contact_left)
        private lateinit var chat: MessageInfoTable

        init {
            lnrContactLayout.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            chat = chatList[bindingAdapterPosition]
            when (v.id) {
                R.id.layout_contact_left -> {
                    mContext!!.openContactIntent(Utils.replaceNull(chat.contactFirstName).plus(" "+Utils.replaceNull(chat.contactMiddleName)).plus(" "+Utils.replaceNull(chat.contactLastName)), chat.contactPhone!!)
                }
            }
        }

        fun bind(position: Int) {
            chat = chatList[position]

            tvSenderName.text = chat.senderName

            addColorToSenderName(tvSenderName, chat)

            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            val date: String? = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = date
            tvDate.setDateVisibility(position, chat, chatList)
            setUserImage(mContext, chat.senderImageUrl, imgSenderImage)

            tvContactName.text = Utils.replaceNull(chat.contactFirstName).plus(" "+Utils.replaceNull(chat.contactMiddleName)).plus(" "+Utils.replaceNull(chat.contactLastName))
            tvContactPhone.text = chat.contactPhone

        }
    }



    /* Video right (for Me) view holder*/
    inner class ChatRightDocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgSeenStatus: ImageView = itemView.findViewById(R.id.imgSeenStatus)
        private var imgMessage: ImageView = itemView.findViewById(R.id.docs_thumbnail_right)

        private var imgDownload: ImageView = itemView.findViewById(R.id.imgDownload)
        private var tvOpenDocs: TextView = itemView.findViewById(R.id.txt_open_docs_right)

        private lateinit var chat: MessageInfoTable

        init {
            tvOpenDocs.setOnClickListener(this)
            imgDownload.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            chat = chatList[bindingAdapterPosition]
            when (v.id) {
                R.id.txt_open_docs_right -> {
                    mContext!!.openMediaIntent(chat.mediaUrlLocal.toString(), "application/*")
                }
                R.id.imgDownload -> {
                    imgDownload.visibility = View.GONE
                    clickHandle.itemClickHandle(chat)
                }
            }

        }

        fun bind(position: Int) {
            chat = chatList[position]

            imgDownload.setDownloadVisibility(chat)
            tvOpenDocs.visibility = setPlayVisibility(chat)
            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)

            imgSeenStatus.generateStatus(chat)
            tvDate.setDateVisibility(position, chat, chatList)
            setThumbDocImage(mContext, imgMessage)


        }
    }

    /* Video left (for Others) view holder*/
    inner class ChatLeftDocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var tvSenderName: TextView = itemView.findViewById(R.id.txt_sender_name)
        private var senderProfile: ImageView = itemView.findViewById(R.id.sender_profile)
        private var tvMessageTime: TextView = itemView.findViewById(R.id.txt_message_time)
        private var tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private var imgMessage: ImageView = itemView.findViewById(R.id.docs_thumbnail_left)

        private var imgDownload: ImageView = itemView.findViewById(R.id.imgDownload)
        private var tvOpenDocs: TextView = itemView.findViewById(R.id.txt_open_docs_left)

        private lateinit var chat: MessageInfoTable

        init {
            tvOpenDocs.setOnClickListener(this)
            imgDownload.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            chat = chatList[bindingAdapterPosition]
            when (v.id) {
                R.id.txt_open_docs_left -> {
                    mContext!!.openMediaIntent(chat.mediaUrlLocal.toString(), "application/*")
                }
                R.id.imgDownload -> {
                    imgDownload.visibility = View.GONE
                    clickHandle.itemClickHandle(chat)
                }
            }

        }

        fun bind(position: Int) {
            chat = chatList[position]
            imgDownload.setDownloadVisibility(chat)
            tvOpenDocs.visibility = setPlayVisibility(chat)
            tvSenderName.text = chat.senderName

            addColorToSenderName(tvSenderName, chat)

            tvMessageTime.text = DateUtils.getChatTimeFromTimestamp(chat.unixTimestamp!!)
            tvDate.text = DateUtils.getChatDateFromTimestamp(chat.unixTimestamp!!)

            tvDate.setDateVisibility(position, chat, chatList)
            setUserImage(mContext, chat.senderImageUrl, senderProfile)
            setThumbDocImage(mContext, imgMessage)

        }
    }


    /* Default empty view holder */
    inner class DefaultEmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        lateinit var tvMessage: TextView
        lateinit var tvMessageTime: TextView
        lateinit var tvDate: TextView
        lateinit var imgSeenStatus: ImageView
        override fun onClick(v: View) {}

        fun bind(position: Int) {

            Log.e("ItemViewType", position.toString())

        }

    }

}