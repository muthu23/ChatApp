package com.dubaipolice.view.activity

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.dubaipolice.databinding.ActivityOnlineDocumentViewerBinding
import com.dubaipolice.utils.AppConstants


class OnlineDocumentViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnlineDocumentViewerBinding
    private var url = ""
    private val gDriveURL = "https://docs.google.com/gview?embedded=true&url="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineDocumentViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
    }

    private fun getExtras() {
        val extras = intent.extras
        extras.let {
            url = extras?.getString(AppConstants.IntentConstants.HLS_LINK_DATA)!!
            loadWebView(gDriveURL+url)
        }
    }

    private fun loadWebView(url: String) {
        binding.pdfRendererView.webViewClient = MyBrowser()
        binding.pdfRendererView.settings.loadsImagesAutomatically = true
        binding.pdfRendererView.settings.builtInZoomControls = true
        binding.pdfRendererView.settings.displayZoomControls = false
        binding.pdfRendererView.settings.javaScriptEnabled =true
        binding.pdfRendererView.settings.loadWithOverviewMode = true
        binding.pdfRendererView.settings.useWideViewPort = true
        binding.pdfRendererView.settings.userAgentString =
            binding.pdfRendererView.settings.userAgentString.replace("; wv", "")
        binding.pdfRendererView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        binding.pdfRendererView.loadUrl(url)
    }

    private class MyBrowser : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
        override fun onLoadResource(view: WebView, url: String) {}
        override fun onPageFinished(view: WebView, url: String) {
            if (view.contentHeight == 0) {
                view.reload()
            }
        }
    }
}