package com.iosfb.app

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.iosfb.app.ui.FacebookApp
import com.iosfb.app.ui.IosFbTheme
import com.iosfb.app.web.BrowserHost
import com.iosfb.app.web.FbTab
import com.iosfb.app.web.TabHost

class MainActivity : ComponentActivity(), BrowserHost {

    private lateinit var tabs: TabHost

    @Volatile
    private var ready = false

    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    private val fileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            fileCallback?.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data),
            )
            fileCallback = null
        }

    private val fullscreenBack = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = hideCustomView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        splash.setKeepOnScreenCondition { !ready }
        Handler(Looper.getMainLooper()).postDelayed({ ready = true }, 1500)

        tabs = TabHost(this, this)
        tabs.state(FbTab.HOME).apply {
            onFirstContent = { ready = true }
            ensureLoaded()
        }

        setContent { IosFbTheme { FacebookApp(tabs) } }
    }

    override fun onPause() {
        tabs.onActivityPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        tabs.onActivityResume()
    }

    override fun onDestroy() {
        tabs.destroy()
        super.onDestroy()
    }

    // ------------------------------------------------------------ BrowserHost

    override fun openExternal(url: String) {
        try {
            CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(this, Uri.parse(url))
        } catch (e: ActivityNotFoundException) {
            runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        }
    }

    override fun openIntent(uri: Uri) {
        if (uri.scheme in setOf("mailto", "tel", "sms", "geo")) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (_: ActivityNotFoundException) {
            }
        }
        // fb://, intent:// etc. are ignored on purpose: they try to bounce you to the official app.
    }

    override fun chooseFiles(
        params: WebChromeClient.FileChooserParams,
        callback: ValueCallback<Array<Uri>>,
    ): Boolean {
        fileCallback?.onReceiveValue(null)
        fileCallback = callback
        try {
            fileLauncher.launch(params.createIntent())
        } catch (e: ActivityNotFoundException) {
            fileCallback = null
            callback.onReceiveValue(null)
        }
        return true
    }

    override fun showCustomView(view: View, callback: WebChromeClient.CustomViewCallback) {
        if (customView != null) {
            callback.onCustomViewHidden()
            return
        }
        customView = view
        customViewCallback = callback
        view.setBackgroundColor(Color.BLACK)
        (window.decorView as ViewGroup).addView(
            view,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        // Registered last so it wins over the Compose back handlers while fullscreen.
        onBackPressedDispatcher.addCallback(this, fullscreenBack)
    }

    override fun hideCustomView() {
        val view = customView ?: return
        (view.parent as? ViewGroup)?.removeView(view)
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
        fullscreenBack.remove()
    }

    override fun download(url: String, userAgent: String?, contentDisposition: String?, mimeType: String?) {
        if (!url.startsWith("http")) {
            Toast.makeText(this, "This file can't be downloaded", Toast.LENGTH_SHORT).show()
            return
        }
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            mimeType?.let { setMimeType(it) }
            userAgent?.let { addRequestHeader("User-Agent", it) }
            CookieManager.getInstance().getCookie(url)?.let { addRequestHeader("Cookie", it) }
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimeType),
            )
        }
        (getSystemService(DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
        Toast.makeText(this, "Downloading…", Toast.LENGTH_SHORT).show()
    }
}
