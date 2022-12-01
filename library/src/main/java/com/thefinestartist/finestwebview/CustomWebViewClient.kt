package com.thefinestartist.finestwebview

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.wait
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import kotlin.math.max

class CustomWebViewClient(
    private val token: String
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return true
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse {
        val callable = Callable {
            val requestString = request.url.toString()
            val httpClient = OkHttpClient()
            val httpRequest: Request = Request.Builder()
                .url(requestString)
                .addHeader("Authorization", token) //add headers
                .build()
            val response = httpClient.newCall(httpRequest).execute()
            Log.d("TEST HEADERS", response.headers.toString())
            Log.d("TEST IS_SUCCESSFUL", response.isSuccessful.toString())
            okHttpResponseToWebResourceResponse(response)
        }
        val threadsCount = max(64, Runtime.getRuntime().availableProcessors())
        val executorService = Executors.newFixedThreadPool(threadsCount)
        val future = executorService.submit(callable)
        val result = future.get()
        Log.d("TEST FLAG", "Task is done")
        return result
    }

    /**
     * Convert OkHttp [Response] into a [WebResourceResponse]
     *
     * @param resp The OkHttp [Response]
     * @return The [WebResourceResponse]
     */
    private fun okHttpResponseToWebResourceResponse(resp: Response): WebResourceResponse {
        val contentTypeValue = resp.header("Content-Type")
        val inputStream = copyInputStream(resp.body!!.byteStream())
        return if (contentTypeValue != null) {
            if (contentTypeValue.indexOf("charset=") > 0) {
                val contentTypeAndEncoding = contentTypeValue.split("; ").toTypedArray()
                val contentType = contentTypeAndEncoding[0]
                val charset = contentTypeAndEncoding[1].split("=").toTypedArray()[1]
                WebResourceResponse(contentType, charset, inputStream)
            } else {
                WebResourceResponse(contentTypeValue, null, inputStream)
            }
        } else {
            WebResourceResponse("application/octet-stream", null, inputStream)
        }
    }

    private fun copyInputStream(byteStream: InputStream): InputStream {
        return ByteArrayInputStream(byteStream.readBytes())
    }
}