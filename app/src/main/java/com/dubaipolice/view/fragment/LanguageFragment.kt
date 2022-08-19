package com.dubaipolice.view.fragment

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.FragmentLanguageBinding
import com.dubaipolice.utils.AppConstants
import com.dubaipolice.utils.MyProgressDialog
import com.dubaipolice.utils.SharedPref
import com.dubaipolice.utils.Utils
import com.dubaipolice.view.activity.HomeEndUserActivity
import com.dubaipolice.viewmodel.ProfileViewModel
import com.dubaipolice.wrapper.Resource
import java.util.*


class LanguageFragment : Fragment(), HandleClick {

    private lateinit var profileViewModel: ProfileViewModel
    lateinit var binding: FragmentLanguageBinding
    lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_language, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = binding.root.context

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        binding.lifecycleOwner = this@LanguageFragment
        //binding.setHomeViewModel(homeViewModel)
        binding.clickHandle = this

        loadLanguage()

    }

    private fun loadLanguage() {

        if (SharedPref.readString(AppConstants.KEY_LANGUAGE).equals(AppConstants.LANGUAGE_ARABIC)) {

            binding.imgEnglish.setImageResource(R.drawable.ic_language_unselected)
            binding.imgArabic.setImageResource(R.drawable.ic_language_selected)

        } else {

            binding.imgEnglish.setImageResource(R.drawable.ic_language_selected)
            binding.imgArabic.setImageResource(R.drawable.ic_language_unselected)

        }
    }


    override fun clickHandle(v: View) {

        when (v.id) {

            R.id.back -> {

                activity?.onBackPressed()

            }

            R.id.layoutEnglish -> {

                binding.imgEnglish.setImageResource(R.drawable.ic_language_selected)
                binding.imgArabic.setImageResource(R.drawable.ic_language_unselected)

                updateLanguage(AppConstants.LANGUAGE_ENGLISH_ID)

            }

            R.id.layoutArabic -> {

                binding.imgEnglish.setImageResource(R.drawable.ic_language_unselected)
                binding.imgArabic.setImageResource(R.drawable.ic_language_selected)

                updateLanguage(AppConstants.LANGUAGE_ARABIC_ID)

            }

        }

    }

    private fun updateLanguage(selectedLanguageId: Int) {

        MyProgressDialog.show(mContext)

        profileViewModel.languageId.value = selectedLanguageId

        profileViewModel.updateLanguageResponse!!.observe(
            viewLifecycleOwner
        ) { profileResponseResource ->
            MyProgressDialog.dismiss()
            when (profileResponseResource.status) {
                Resource.Status.SUCCESS -> {
                    if (profileResponseResource.data!!.isSuccess) {

                        Utils.showToast(mContext, profileResponseResource.data.message!!)

                        if (selectedLanguageId == AppConstants.LANGUAGE_ENGLISH_ID) {
                            SharedPref.writeString(
                                AppConstants.KEY_LANGUAGE,
                                AppConstants.LANGUAGE_ENGLISH
                            )
                        } else if (selectedLanguageId == AppConstants.LANGUAGE_ARABIC_ID) {
                            SharedPref.writeString(
                                AppConstants.KEY_LANGUAGE,
                                AppConstants.LANGUAGE_ARABIC
                            )
                        }

                        SharedPref.readString(AppConstants.KEY_LANGUAGE)?.let { updateLanguage(it) }

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
        }


    }
    private fun updateLanguage(lang: String) {
        var myLocale = Locale(lang)
        val res: Resources = resources
        val dm: DisplayMetrics = res.getDisplayMetrics()
        val conf: Configuration = res.getConfiguration()
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
        val refresh = Intent(mContext, HomeEndUserActivity::class.java)
        startActivity(refresh)
        requireActivity().finish()
    }

}