package com.dubaipolice.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.dubaipolice.R
import com.dubaipolice.callback.HandleClick
import com.dubaipolice.databinding.FragmentHelpBinding
import com.dubaipolice.view.activity.HomeEndUserActivity
import com.dubaipolice.view.activity.NotificationActivity


class HelpFragment : Fragment(), HandleClick {

    //lateinit var homeViewModel: HomeViewModel? = null
    lateinit var binding: FragmentHelpBinding
    lateinit var mContext: Context


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_help, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = binding.root.context

        //homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        binding.lifecycleOwner = this@HelpFragment
        //binding.setHomeViewModel(homeViewModel)
        binding.clickHandle = this

    }

    override fun clickHandle(v: View) {
        when(v.id) {

            R.id.imgMenu -> {

                val activity: HomeEndUserActivity? = activity as HomeEndUserActivity?
                activity?.openDrawer()

            }


            R.id.imgNotification -> {

                startActivity(Intent(mContext, NotificationActivity::class.java))

            }

        }
    }
}