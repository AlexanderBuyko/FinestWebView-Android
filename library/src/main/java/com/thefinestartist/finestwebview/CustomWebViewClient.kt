package com.thefinestartist.finestwebview

import android.webkit.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class CustomWebViewClient(
    private val token: String
): WebViewClient() {
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse {
        val requestString = request.url.toString()
        val httpClient = OkHttpClient()
        val httpRequest: Request = Request.Builder()
            .url(requestString)
            .addHeader("Authorization", token) //add headers
            .build()
        val response: Response = httpClient.newCall(httpRequest).execute()
        return WebResourceResponse(
            getMimeType(requestString), // set content-type
            response.header("content-encoding", "utf-8"),
            response.body?.byteStream()
        )
    }

    //get mime type by url
    private fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension: String = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension == "js") {
            return "text/javascript"
        } else if (extension == "woff") {
            return "application/font-woff"
        } else if (extension == "woff2") {
            return "application/font-woff2"
        } else if (extension == "ttf") {
            return "application/x-font-ttf"
        } else if (extension == "eot") {
            return "application/vnd.ms-fontobject"
        } else if (extension == "svg") {
            return "image/svg+xml"
        }
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return type
    }
}