package com.dubaipolice.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.dubaipolice.R

object MyProgressDialog {

    private const val LAYOUT_DIALOG_ID : Int = R.layout.layout_custom_dialog
    private var dialog : Dialog? = null

    private fun initializeDialog(context : Context) {

        //dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
        dialog = Dialog(context, R.style.FullScreenDialogWithStatusBarColorAccent)

        val view = View.inflate(
            context,
            LAYOUT_DIALOG_ID,
            null
        )

        dialog!!.setContentView(view)
        dialog!!.window!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
       /* dialog!!.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )*/
        dialog!!.setCancelable(false)
    }

    fun show(context: Context) {

        try {

            //dismiss dialog if already showing any
            dismiss()

            initializeDialog(context)
            dialog ?: return
            if (!dialog!!.isShowing) {
                dialog!!.show()
            }
        } catch (e : Exception) {
            print(e.stackTrace)
        }
    }

    fun dismiss() {

        try {
            if (dialog != null && dialog!!.isShowing) {
                dialog!!.dismiss()
                dialog = null
            }
        } catch (e : Exception) {
            print(e.stackTrace)
        }
    }
}