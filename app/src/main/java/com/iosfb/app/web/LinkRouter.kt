package com.iosfb.app.web

import android.net.Uri

object LinkRouter {
    private val internalDomains = listOf("facebook.com", "fb.com", "fbcdn.net", "fbsbx.com")

    fun isInternal(uri: Uri): Boolean {
        val host = uri.host?.lowercase() ?: return false
        return internalDomains.any { host == it || host.endsWith(".$it") }
    }

    /** Facebook wraps outbound links as l.facebook.com/l.php?u=<target>. */
    fun unwrap(uri: Uri): Uri? {
        val host = uri.host?.lowercase() ?: return null
        val isRedirector = host == "l.facebook.com" || host == "lm.facebook.com"
        if (!isRedirector || uri.path?.startsWith("/l.php") != true) return null
        return uri.getQueryParameter("u")?.let { Uri.parse(it) }
    }
}
