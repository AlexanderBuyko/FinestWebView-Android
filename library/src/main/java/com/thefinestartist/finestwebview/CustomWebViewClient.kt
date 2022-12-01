package com.thefinestartist.finestwebview

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class CustomWebViewClient(
    private val token: String
): WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return true
    }

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
        val response = httpClient.newCall(httpRequest).execute()
        Log.d("TEST HEADERS", response.headers.toString())
        Log.d("TEST BODY", response.body!!.byteStream().toString())
        return okHttpResponseToWebResourceResponse(response);
    }

    /**
     * Convert OkHttp [Response] into a [WebResourceResponse]
     *
     * @param resp The OkHttp [Response]
     * @return The [WebResourceResponse]
     */
    private fun okHttpResponseToWebResourceResponse(resp: Response): WebResourceResponse {
        val contentTypeValue = resp.header("Content-Type")
        return if (contentTypeValue != null) {
            if (contentTypeValue.indexOf("charset=") > 0) {
                val contentTypeAndEncoding = contentTypeValue.split("; ").toTypedArray()
                val contentType = contentTypeAndEncoding[0]
                val charset = contentTypeAndEncoding[1].split("=").toTypedArray()[1]
                WebResourceResponse(contentType, charset, resp.body!!.byteStream())
            } else {
                Log.d("TEST CASE", "PASSED with mocked mime type")
                WebResourceResponse("application/msword", null, resp.body!!.byteStream())
            }
        } else {
            WebResourceResponse("application/octet-stream", null, resp.body!!.byteStream())
        }
    }
}