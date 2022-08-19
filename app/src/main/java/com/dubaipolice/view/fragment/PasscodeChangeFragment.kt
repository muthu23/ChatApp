package com.dubaipolice.view.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.FragmentPasscodeChangeBinding
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.activity.HomeEndUserActivity
import com.dubaipolice.viewmodel.PasscodeSetupViewModel
import com.dubaipolice.wrapper.Resource

class PasscodeChangeFragment : Fragment(), HandleClick {

    private lateinit var passcodeSetupViewModel: PasscodeSetupViewModel
    lateinit var binding: FragmentPasscodeChangeBinding
    lateinit var mContext: Context

    var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_passcode_change, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = binding.root.context

        passcodeSetupViewModel = ViewModelProvider(this)[PasscodeSetupViewModel::class.java]
        binding.lifecycleOwner = this@PasscodeChangeFragment
        //binding.setHomeViewModel(homeViewModel)
        binding.clickHandle = this

    }

    private fun isPasscodeValidate(): Boolean {
        if (TextUtils.isEmpty(binding.etOldPasscode.text.toString().trim())) {
            Utils.showToast(mContext, getString(R.string.please_enter_old_passcode))
            return false
        }
        if (TextUtils.isEmpty(binding.etNewPasscode.text.toString().trim())) {
            Utils.showToast(mContext, getString(R.string.please_enter_new_passcode))
            return false
        }
        if (binding.etNewPasscode.text.toString() != binding.etConfirmPasscode.text.toString()
        ) {
            Utils.showToast(mContext, getString(R.string.passcode_not_matched))
            return false
        }
        return true
    }

    override fun clickHandle(v: View) {
        when (v.id) {

            R.id.back -> {

                activity?.onBackPressed()

            }

            R.id.btnCancel -> {

                activity?.onBackPressed()

            }

            R.id.btnSaveChanges -> {

                if (isPasscodeValidate()) {

                    MyProgressDialog.show(mContext)

                    passcodeSetupViewModel.oldPasscode.value = binding.etOldPasscode.text.toString()
                    passcodeSetupViewModel.newPasscode.value = binding.etNewPasscode.text.toString()

                    passcodeSetupViewModel.passcodeResponse!!.observe(
                        viewLifecycleOwner
                    ) { commonResponseResource ->
                        MyProgressDialog.dismiss()
                        when (commonResponseResource.status) {
                            Resource.Status.SUCCESS -> {
                                if (commonResponseResource.data!!.isSuccess) {

                                    popupCongratulations()

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
                    }

                }

            }

        }
    }

    private fun popupCongratulations() {

        val builder = AlertDialog.Builder(mContext)
        val customLayout: View =
            layoutInflater.inflate(R.layout.popup_passcode_change_success, null)
        builder.setView(customLayout)
        val btnGoToHome: Button = customLayout.findViewById(R.id.btnGoToHome)
        val imgCongratulations: ImageView = customLayout.findViewById(R.id.imgCongratulations)

        Glide.with(mContext).load(
            AppCompatResources.getDrawable(
                mContext,
                R.drawable.ic_congratulations2
            )
        ).into(imgCongratulations)

        btnGoToHome.setOnClickListener {
            dialog!!.dismiss()

            val i = Intent(mContext, HomeEndUserActivity::class.java)
            i.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            mContext.startActivity(i)

        }

        dialog = builder.create()
        dialog!!.setCancelable(false)
        dialog!!.show()

        /*Set the background of the dialog's root view to transparent, because Android
        puts your dialog layout within a root view that hides the corners
        in your custom layout and show the layout rounded corners as created.*/
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


    }

}