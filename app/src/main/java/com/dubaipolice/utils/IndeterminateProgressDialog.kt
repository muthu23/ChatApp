package com.dubaipolice.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import com.dubaipolice.R

class IndeterminateProgressDialog (context: Context) : AlertDialog(context) {

    private val messageTextView: TextView
    private val mProgress: ProgressBar

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_loading_dialog, null)
        messageTextView = view.findViewById(R.id.message)
        mProgress = view.findViewById(R.id.progressBar)
        setView(view)
        messageTextView.setOnClickListener {
           onClose()
        }
    }

     fun setProgress(progress:Int){
        mProgress.progress = progress
    }

    override fun setMessage(message: CharSequence?) {
        this.messageTextView.text = message.toString()
    }

     private fun onClose(){
        this.dismiss()
    }

}