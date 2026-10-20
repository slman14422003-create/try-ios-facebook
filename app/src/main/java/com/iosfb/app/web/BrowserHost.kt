package com.iosfb.app.web

import android.net.Uri
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient

/** Things a tab's WebView needs from the Activity. */
interface BrowserHost {
    fun openExternal(url: String)
    fun openIntent(uri: Uri)
    fun chooseFiles(params: WebChromeClient.FileChooserParams, callback: ValueCallback<Array<Uri>>): Boolean
    fun showCustomView(view: View, callback: WebChromeClient.CustomViewCallback)
    fun hideCustomView()
    fun download(url: String, userAgent: String?, contentDisposition: String?, mimeType: String?)
}
