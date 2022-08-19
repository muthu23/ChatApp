package com.dubaipolice.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.Window
import androidx.core.content.res.ResourcesCompat
import com.dubaipolice.MainApplication
import com.dubaipolice.R

object ThemeUtility {

    /**
     * create drawable using gradient colors and set to view
     */
    fun setGradientThemeColor(view: View, cornerRadius: Float) {

        /*val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.argb(255, 66, 133, 244), Color.argb(255, 15, 157, 88))
        )*/
        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor(ColorConstants.BLUE), Color.parseColor(ColorConstants.RED))
        )
        gd.cornerRadius = cornerRadius

        view.background= gd

    }

    /**
     * Gets shape as it as and set colors to drawable file
     */
    fun setGradientThemeColorUsingDrawable(mContext: Context, drawableFile: Int) {

        val colorSchemes = SharedPref.readString(AppConstants.COLOR_SCHEME)!!.split(",".toRegex()).toTypedArray()
        val firstColor= colorSchemes[0]
        val secondColor= colorSchemes[1]

        var background = ResourcesCompat.getDrawable(mContext.resources, drawableFile, null) as GradientDrawable
        var colorList = intArrayOf(Color.parseColor(secondColor), Color.parseColor(firstColor))
        //var colorList = intArrayOf(Color.parseColor(ColorConstants.BLUE), Color.parseColor(ColorConstants.RED))
        //background.mutate() // Mutate the drawable so changes don't affect every other drawable
        // background.setGradientCenter(0.1F, 0.5F) // Reset the center of the gradient (default is 0.5F, 0.5F)) - Only works for radial and sweep types

        background.setColors(colorList)

        //use this if background.mutate is using
        //binding.toolbar.background= background

    }

    fun setStatusBarThemeColor(window: Window) {

        val colorSchemes = SharedPref.readString(AppConstants.COLOR_SCHEME)!!.split(",".toRegex()).toTypedArray()
        val firstColor= colorSchemes[0]
        val secondColor= colorSchemes[1]
        ///activity.window.statusBarColor = ContextCompat.getColor(mContext, R.color.color_name)
        //window.statusBarColor = Color.parseColor(ColorConstants.BLUE)
        window.statusBarColor = Color.parseColor(firstColor)


    }


    object ColorConstants {

        const val RED = "#FF0000"
        const val GREEN = "#00FF00"
        const val BLUE = "#0000FF"

    }

}