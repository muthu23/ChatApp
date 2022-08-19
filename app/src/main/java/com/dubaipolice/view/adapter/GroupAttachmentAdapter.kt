package com.dubaipolice.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dubaipolice.R
import com.dubaipolice.callback.HandleMediaItemClick
import com.dubaipolice.databinding.ItemDocsListBinding
import com.dubaipolice.databinding.ItemMediaListBinding
import com.dubaipolice.databinding.ItemVoiceListBinding
import com.dubaipolice.model.Attachment
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.DateUtils.getChatTimeFromTimestamp
import com.dubaipolice.utils.DateUtils.getFormattedDate
import com.dubaipolice.utils.Utils
import com.dubaipolice.utils.Utils.replaceNull
import com.dubaipolice.utils.setThumbDocImage
import com.dubaipolice.utils.setThumbMediaImage
import com.dubaipolice.view.activity.OnlineDocumentViewerActivity
import com.dubaipolice.view.activity.OnlineMediaPlayerActivity
import com.stfalcon.imageviewer.StfalconImageViewer
import java.util.*

internal class GroupAttachmentAdapter(
    private val context: Context,
    private val handleUserItemClick: HandleMediaItemClick
) : PagingDataAdapter<Attachment, RecyclerView.ViewHolder>(AttachmentComparator) {

    private val IMAGE_VIDEO_VIEW: Int = 1
    private val AUDIO_VIEW: Int = 2
    private val DOCUMENT_VIEW: Int = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            IMAGE_VIDEO_VIEW ->
                MediaViewHolder(
                    ItemMediaListBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            DOCUMENT_VIEW ->
                DocsViewHolder(
                    ItemDocsListBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            AUDIO_VIEW ->
                VoiceViewHolder(
                    ItemVoiceListBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )


            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        with(getItem(position)) {
            when (viewHolder) {
                is MediaViewHolder -> viewHolder.bind(this)
                is DocsViewHolder -> viewHolder.bind(this)
                is VoiceViewHolder -> viewHolder.bind(this)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)?.media_type) {
            "image" -> {
                IMAGE_VIDEO_VIEW
            }
            "video" -> {
                IMAGE_VIDEO_VIEW
            }
            "audio" -> {
                AUDIO_VIEW
            }
            "docs" -> {
                DOCUMENT_VIEW
            }
            else -> 0
        }
    }

    inner class MediaViewHolder(val binding: ItemMediaListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(attachment: Attachment?) {
            attachment?.let {
                setThumbMediaImage(context, attachment.attachmentThumbUrl, binding.imgMedia)
                binding.imgPlay.visibility =
                    if (attachment.media_type == "video") View.VISIBLE else View.GONE

                binding.layoutMedia.setOnClickListener {
                    if (attachment.media_type == "video")
                        moveToPlayer(attachment.attachmentUrl)
                    else openImage(attachment.attachmentUrl)
                }
            }
        }

        private fun openImage(url: String) {
            val images = mutableListOf<String>()
            images.add(url)
            if (url.isNotEmpty() && Utils.isNetConnected(context))
                StfalconImageViewer.Builder(context, images) { view, image ->
                    Glide.with(context).load(image)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(view)
                }.show()
            else Utils.showToast(context, context.getString(R.string.no_group_photo))
        }

    }

    inner class DocsViewHolder(val binding: ItemDocsListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(attachment: Attachment?) {
            attachment?.let {
               // setThumbDocImage(context, binding.docThump)
                try {
                    binding.tvDocName.text  = attachment.attachmentUrl.substringAfter("chat/")
                    binding.docName.text = Utils.getFileExtension(attachment.attachmentUrl.substringAfter("-"))
                        .uppercase(Locale.getDefault())
                    binding.tvDate.text = getFormattedDate(attachment.created_at)
                } catch (e: Exception) {
                }
                binding.lnCnt.setOnClickListener {
                    moveToViewer(attachment.attachmentUrl)
                }
            }
        }
    }

    inner class VoiceViewHolder(val binding: ItemVoiceListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(attachment: Attachment?) {
            attachment?.let {
         //setThumbVoiceImage(context, binding.voiceThump)
                try {
                    binding.txtTitle.text = attachment.attachmentUrl.substringAfter("chat/")
                    binding.audioDuration.text = replaceNull(attachment.duration)
                    binding.txtTime.text =getFormattedDate(attachment.created_at)
                } catch (e: Exception) {
                }

                binding.lnPlay.setOnClickListener {
                    moveToPlayer(attachment.attachmentUrl)
                }
            }
        }
    }

    object AttachmentComparator : DiffUtil.ItemCallback<Attachment>() {
        override fun areItemsTheSame(oldItem: Attachment, newItem: Attachment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Attachment, newItem: Attachment): Boolean {
            return oldItem == newItem
        }
    }


    private fun moveToPlayer(hlsLink: String) {
        val i = Intent(context, OnlineMediaPlayerActivity::class.java)
        i.putExtra(AppConstants.IntentConstants.HLS_LINK_DATA, hlsLink)
        context.startActivity(i)
    }

    private fun moveToViewer(url: String) {
        val i = Intent(context, OnlineDocumentViewerActivity::class.java)
        i.putExtra(AppConstants.IntentConstants.HLS_LINK_DATA, url)
        context.startActivity(i)
    }


}