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
            "application/octet-stream", // set content-type
            response.header("content-encoding", "utf-8"),
            response.body?.byteStream()
        )
    }

    //get mime type by url
    private fun getMimeType(url: String?): String? {
        return when (val extension: String = MimeTypeMap.getFileExtensionFromUrl(url)) {
            "js" -> "text/javascript"
            "woff" -> "application/font-woff"
            "woff2" -> "application/font-woff2"
            "ttf" -> "application/x-font-ttf"
            "eot" -> "application/vnd.ms-fontobject"
            "svg" -> "image/svg+xml"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
    }
}