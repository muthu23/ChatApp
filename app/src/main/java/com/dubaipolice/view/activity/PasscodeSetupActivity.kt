package com.dubaipolice.view.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.ActivityPasscodeSetupBinding
import com.dubaipolice.model.CommonResponse
import com.dubaipolice.utils.*
import com.dubaipolice.viewmodel.PasscodeSetupViewModel
import com.dubaipolice.wrapper.Resource

class PasscodeSetupActivity : AppCompatActivity(), HandleClick {

    lateinit var passcodeSetupViewModel: PasscodeSetupViewModel
    lateinit var binding: ActivityPasscodeSetupBinding

    lateinit  var mContext: Context

    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this@PasscodeSetupActivity, R.layout.activity_passcode_setup)
        passcodeSetupViewModel = ViewModelProvider(this)[PasscodeSetupViewModel::class.java]

        binding.lifecycleOwner= this@PasscodeSetupActivity
        //binding.loginViewModel= loginViewModel
        binding.clickHandle= this

        mContext = this

        //ThemeUtility.setGradientThemeColor(binding.btnContinue, resources.getDimension(R.dimen._50sdp))

    }

    private fun isPinValidate() : Boolean
    {
        val pin: String = binding.otpView.otp!!

        if (pin.length < 6) {
            Utils.showToast(mContext, getString(R.string.please_enter_6_digits_pin))
            return false
        }
        return true
    }

    private fun isConfirmPinValidate() : Boolean
    {
        val pin: String = binding.otpView.otp!!
        val pinConfirm: String = binding.otpViewConfirm.otp!!

        if (pin != pinConfirm) {
            Utils.showToast(mContext, getString(R.string.pin_does_not_match))
            return false
        }
        return true
    }

    override fun clickHandle(v: View) {
        when(v.id) {
            R.id.back -> {

                onBackPressed()

            }

            R.id.btnContinue -> {

                if(isPinValidate())
                {
                    if (page == 1) {
                        //Utils.hideKeyboard(mContext)
                        page = 2
                        binding.layoutSetPin.visibility = View.GONE
                        binding.layoutConfirmPin.visibility = View.VISIBLE
                        binding.otpViewConfirm.requestFocus()
                    }
                }

            }

            R.id.btnSubmit -> {

                if(isConfirmPinValidate())
                {

                    MyProgressDialog.show(mContext);

                    passcodeSetupViewModel.oldPasscode.value= binding.otpView.otp!!
                    passcodeSetupViewModel.newPasscode.value= binding.otpView.otp!!

                    passcodeSetupViewModel.passcodeResponse!!.observe(
                        this@PasscodeSetupActivity,
                        Observer<Resource<CommonResponse?>> { commonResponseResource ->
                            MyProgressDialog.dismiss()
                            when (commonResponseResource.status) {
                                Resource.Status.SUCCESS -> {
                                    if (commonResponseResource.data!!.isSuccess) {

                                        Utils.showToast(mContext, commonResponseResource.data.message!!)
                                        SharedPref.writeBoolean(AppConstants.KEY_IS_PASSCODE_GENERATED, true)
                                        startActivity(Intent(mContext, ProfilePictureActivity::class.java))
                                        finish()

                                    } else {
                                        Utils.showToast(mContext, commonResponseResource.data.message!!)
                                    }
                                }
                                Resource.Status.LOADING -> {
                                }
                                Resource.Status.ERROR -> {
                                    Utils.showToast(mContext, commonResponseResource.message!!)
                                }
                            }
                        })

                }

            }

        }
    }

    override fun onBackPressed() {
        if (page == 2) {
            page = 1
            binding.layoutSetPin.visibility= View.VISIBLE
            binding.layoutConfirmPin.visibility= View.GONE
        } else {
            super.onBackPressed()
        }
    }

}