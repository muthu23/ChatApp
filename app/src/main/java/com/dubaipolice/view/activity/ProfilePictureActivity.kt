package com.dubaipolice.view.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityProfilePictureBinding
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.model.ProfileResponse
import com.dubaipolice.utils.*
import com.dubaipolice.viewmodel.ProfileViewModel
import com.dubaipolice.wrapper.Resource
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File
import java.io.FileInputStream

class ProfilePictureActivity : AppCompatActivity(), HandleClick {

    lateinit var profileViewModel: ProfileViewModel
    lateinit var binding: ActivityProfilePictureBinding

    lateinit  var mContext: Context

    var dialog: AlertDialog?= null

    var profileImage: File?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this@ProfilePictureActivity, R.layout.activity_profile_picture)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        binding.lifecycleOwner= this@ProfilePictureActivity
        //binding.loginViewModel= loginViewModel
        binding.clickHandle= this

        mContext = this

    }

    override fun clickHandle(v: View) {

        when(v.id) {

            R.id.back -> {

                onBackPressed()

            }

            R.id.layoutUploadPhoto -> {

                chooseGalleryOrCameraPopup()

            }

            R.id.tvSkip -> {

                startActivity(Intent(mContext, HomeEndUserActivity::class.java))
                finish()

            }

            R.id.tvContinue -> {

                uploadProfilePic()

            }

        }

    }

    /**
     * This function is used to show the popup for selecting camera or gallery
     */
    private fun chooseGalleryOrCameraPopup() {


        val builder = AlertDialog.Builder(mContext)
        val customLayout: View = layoutInflater.inflate(R.layout.popup_choose_camera_gallery, null)
        builder.setView(customLayout)
        val layoutCamera: LinearLayout
        val layoutGallery: LinearLayout
        layoutCamera = customLayout.findViewById(R.id.layoutCamera)
        layoutGallery = customLayout.findViewById(R.id.layoutGallery)

        layoutCamera.setOnClickListener {
            dialog!!.dismiss()

            ImagePicker.with(this)
                .cropSquare()
                .cameraOnly()
                .compress(1024)         //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)  //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }

        }

        layoutGallery.setOnClickListener {
            dialog!!.dismiss()

            ImagePicker.with(this)
                .cropSquare()
                .galleryOnly()
                .compress(1024)         //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)  //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        dialog = builder.create()
        dialog!!.setCancelable(true)
        dialog!!.show()

        /*Set the background of the dialog's root view to transparent, because Android
        puts your dialog layout within a root view that hides the corners
        in your custom layout and show the layout rounded cornors as created.*/
        dialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val fileUri = data?.data!!

                //mProfileUri = fileUri
                Log.e("URI: ", fileUri.toString())
                Log.e("PATH: ", fileUri.path.toString())
                //binding.profileImage.setImageURI(fileUri)

                profileImage= File(fileUri.path)

                Glide.with(mContext).load(profileImage).placeholder(
                    AppCompatResources.getDrawable(mContext, R.drawable.ic_profile)).into(binding.profileImage)

            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Utils.showToast(mContext, ImagePicker.getError(data))
            } else {
                Log.e("Cancelled", "Task Cancelled")
            }
        }

    private fun uploadProfilePic()
    {

        if(profileImage != null)
        {
            MyProgressDialog.show(mContext);

            profileViewModel.filePath.value= profileImage

            profileViewModel.updateProfilePicResponse!!.observe(
                this@ProfilePictureActivity,
                Observer<Resource<ProfileResponse?>> { profileResponseResource ->
                    MyProgressDialog.dismiss()
                    when (profileResponseResource.status) {
                        Resource.Status.SUCCESS -> {
                            if (profileResponseResource.data!!.isSuccess) {

                                SharedPref.writeString(AppConstants.KEY_PROFILE_PHOTO, profileResponseResource.data?.data?.userDetails?.profileImage)

                                startActivity(Intent(mContext, HomeEndUserActivity::class.java))
                                finish()

                            } else {
                                Utils.showToast(mContext, profileResponseResource.data.message!!)
                            }
                        }
                        Resource.Status.LOADING -> {
                        }
                        Resource.Status.ERROR -> {
                            Utils.showToast(mContext, profileResponseResource.message!!)
                        }
                    }
                })
        }
        else
        {
            Utils.showToast(mContext, getString(R.string.photo_upload_validation))
        }

    }

}