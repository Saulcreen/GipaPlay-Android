package com.gipaplay.android

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private val homeUrl = "https://gipa-play.vercel.app"
    private lateinit var webView: WebView
    private lateinit var root: FrameLayout
    private lateinit var audio: AudioManager
    private var customView: View? = null
    private var customCallback: WebChromeClient.CustomViewCallback? = null
    private var oldUi = View.SYSTEM_UI_FLAG_VISIBLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audio = getSystemService(AUDIO_SERVICE) as AudioManager
        root = FrameLayout(this).apply { setBackgroundColor(android.graphics.Color.BLACK) }
        webView = WebView(this)
        root.addView(webView, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        configure()
        if (savedInstanceState == null) webView.loadUrl(homeUrl)
        else webView.restoreState(savedInstanceState)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configure() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = true
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            allowFileAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        // Solo ofrece las dos acciones solicitadas al JavaScript de GipaPlay.
        webView.addJavascriptInterface(AndroidControls(), "AndroidControls")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                val host = uri.host ?: return false
                if (host == "gipa-play.vercel.app") return false
                runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                return true
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (view == null || customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customCallback = callback
                root.removeView(webView)
                root.addView(view, FrameLayout.LayoutParams(-1, -1))
                @Suppress("DEPRECATION")
                oldUi = window.decorView.systemUiVisibility
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            override fun onHideCustomView() = hideCustomView()
        }
    }

    private inner class AndroidControls {
        @JavascriptInterface
        fun setVolume(percent: Int) {
            runOnUiThread {
                val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val level = (percent.coerceIn(0, 100) * max / 100f).roundToInt()
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0)
            }
        }

        @JavascriptInterface
        fun setBrightness(percent: Int) {
            runOnUiThread {
                val params = window.attributes
                params.screenBrightness = percent.coerceIn(1, 100) / 100f
                window.attributes = params
            }
        }
    }

    private fun hideCustomView() {
        val view = customView ?: return
        root.removeView(view)
        customView = null
        root.addView(webView, FrameLayout.LayoutParams(-1, -1))
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = oldUi
        val callback = customCallback
        customCallback = null
        callback?.onCustomViewHidden()
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        when {
            customView != null -> hideCustomView()
            webView.canGoBack() -> webView.goBack()
            else -> super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }
}
