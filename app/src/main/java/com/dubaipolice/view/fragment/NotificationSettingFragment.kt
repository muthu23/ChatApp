package com.dubaipolice.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.callback.HandleNotificationSoundItemClick
import com.dubaipolice.databinding.FragmentNotificationSettingBinding
import com.dubaipolice.model.NotificationTune
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.adapter.NotificationsSoundAdapter
import com.dubaipolice.viewmodel.NotificationViewModel
import com.dubaipolice.wrapper.Resource
import java.io.File
import java.io.IOException


class NotificationSettingFragment : Fragment(), HandleClick, HandleNotificationSoundItemClick {

    lateinit var notificationViewModel: NotificationViewModel

    lateinit var binding: FragmentNotificationSettingBinding
    lateinit var mContext: Context
    val arrayList = ArrayList<NotificationTune>()
    var dialog: AlertDialog? = null
    private var notificationsSoundAdapter: NotificationsSoundAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification_setting, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = binding.root.context
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        binding.lifecycleOwner = this@NotificationSettingFragment
        //binding.setHomeViewModel(homeViewModel)
        binding.clickHandle = this
        binding.tvSelectedSound.text = getNotificationFile()
        binding.switchNotifications.isChecked=SharedPref.readBoolean(AppConstants.KEY_IS_SHOW_NOTIFICATION)
        binding.switchNotifications.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            showNotification(isChecked)
        })
    }

    override fun clickHandle(v: View) {

        when(v.id) {

            R.id.back -> {

                activity?.onBackPressed()

            }

            R.id.layoutSelectSound -> {

              //  pickSound()
                popupNotificationSound()
            }

        }

    }

    private fun pickSound() {

        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
            RingtoneManager.TYPE_NOTIFICATION)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")

        // for existing ringtone
        val urie = RingtoneManager.getActualDefaultRingtoneUri(
                mContext, RingtoneManager.TYPE_NOTIFICATION)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, urie)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val path= File(urie.path.toString()).name
        Log.e("Name", path)
        Log.e("Name2", File(defaultSoundUri.path.toString()).name)

        startActivityForResult(intent, 5)

    }


    private fun showNotification(isShowNotification:Boolean) {
        notificationViewModel.showNotification.value=isShowNotification
        MyProgressDialog.show(mContext)
        notificationViewModel.notificationShowResponse!!.observe(
            requireActivity()
        ) { commonResponse ->
            MyProgressDialog.dismiss()
            when (commonResponse.status) {
                Resource.Status.SUCCESS -> {
                    if (commonResponse.data!!.isSuccess) {
                        Utils.showToast(mContext, commonResponse.data.message!!)
                        SharedPref.writeBoolean(AppConstants.KEY_IS_SHOW_NOTIFICATION, isShowNotification)

                    } else {
                        Utils.showToast(mContext, commonResponse.data.message!!)
                    }

                }
                Resource.Status.LOADING -> {
                }
                Resource.Status.ERROR -> {
                    Utils.showToast(mContext, commonResponse.message!!)
                }
            }
        }
    }

    private fun popupNotificationSound() {
        val builder = AlertDialog.Builder(mContext)
        val customLayout: View = layoutInflater.inflate(R.layout.popup_notification_sound, null)
        builder.setView(customLayout)

        val recyclerNotificationSound: androidx.recyclerview.widget.RecyclerView = customLayout.findViewById(R.id.recyclerNotificationSound)
        val btnSave: Button = customLayout.findViewById(R.id.btnSave)
        val btnCancel: Button = customLayout.findViewById(R.id.btnCancel)
        arrayList.clear()
        getAllFilesInAssetByExtension(requireContext(), "", ".mp3")?.forEach {
            if(getNotificationFile()==it){
                arrayList.add(NotificationTune(it,true))

            }else{
                arrayList.add(NotificationTune(it,false))

            }
        }
        recyclerNotificationSound.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        notificationsSoundAdapter = NotificationsSoundAdapter(mContext, arrayList,this)
        recyclerNotificationSound.adapter= notificationsSoundAdapter
        btnSave.setOnClickListener {
            binding.tvSelectedSound.text = arrayList.find { it.isSelected }?.notificationTitle
            notificationViewModel.soundFile.value= arrayList.find { it.isSelected }?.notificationTitle


            if(isNotificationSoundValidate(binding.tvSelectedSound))
            {
                MyProgressDialog.show(mContext)
                notificationViewModel.notificationUpdateResponse!!.observe(
                    requireActivity()
                ) { commonResponse ->
                    MyProgressDialog.dismiss()
                    when (commonResponse.status) {
                        Resource.Status.SUCCESS -> {
                            if (commonResponse.data!!.isSuccess) {
                                Utils.showToast(mContext, commonResponse.data.message!!)
                                saveNotificationFile(binding.tvSelectedSound.text.toString())

                            } else {
                                Utils.showToast(mContext, commonResponse.data.message!!)
                            }

                        }
                        Resource.Status.LOADING -> {
                        }
                        Resource.Status.ERROR -> {
                            Utils.showToast(mContext, commonResponse.message!!)
                        }
                    }
                }

            }

            dialog!!.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog!!.dismiss()

        }

        dialog = builder.create()
        dialog!!.setCancelable(false)
        dialog!!.show()
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }
    private fun isNotificationSoundValidate(textSound:TextView) : Boolean
    {
        if(TextUtils.isEmpty(textSound.text.toString().trim()))
        {
            Utils.showToast(mContext, getString(R.string.please_enter_email))
            return false
        }
        return true
    }

    private fun saveNotificationFile(notificationTitle:String?) {
        SharedPref.writeString(AppConstants.KEY_SOUND_FILE, notificationTitle)
    }
    private fun getNotificationFile(): String? {
        return SharedPref.readString(AppConstants.KEY_SOUND_FILE)
    }



    private fun getAllFilesInAssetByExtension(
        context: Context,
        path: String?,
        extension: String?
    ): Array<String>? {
        try {
            val files = context.assets.list(path!!)
            val filesWithExtension: MutableList<String> = ArrayList()
            for (file in files!!) {
               if (file.endsWith(extension!!)) {
                    filesWithExtension.add(file)
                }
            }
            return filesWithExtension.toTypedArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun itemClickHandle(item: NotificationTune?, isChecked: Boolean?, position :Int) {
        arrayList.find {
            it.isSelected }?.isSelected=false
        if (item != null) {
            if (isChecked != null
            ) {
                item.isSelected = isChecked
                with(arrayList) {
                    remove(item)
                    add(position, item)
                }
            }
            //binding.tvSelectedSound.text = item.notificationTitle
            if(isChecked == true) play(requireContext(),item.notificationTitle)

            notificationsSoundAdapter?.notifyDataSetChanged()
        }
    }
    private fun play(context: Context, file: String) {
        try {
            val mediaPlayer = MediaPlayer()
            val afd = context.assets.openFd(file)
            mediaPlayer.setDataSource(
                afd.fileDescriptor,
                afd.startOffset,
                afd.length
            )
            afd.close()
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}