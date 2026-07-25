package com.debtfreein.app.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.debtfreein.app.data.network.UpstoxExecutionService
import com.debtfreein.app.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun UpstoxAuthScreen(
    viewModel: FinanceViewModel,
    authUrl: String = "",
    onAuthComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val targetUrl = authUrl.ifBlank { UpstoxExecutionService.getUpstoxAuthUrl(context) }

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.setSupportMultipleWindows(false)
            settings.javaScriptCanOpenWindowsAutomatically = false
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    val currentUrl = url ?: ""
                    if (currentUrl.startsWith("https://127.0.0.1") || currentUrl.contains("code=") || currentUrl.contains("error=")) {
                        if (currentUrl.contains("code=")) {
                            view?.stopLoading()
                            val code = Uri.parse(currentUrl).getQueryParameter("code")
                            if (!code.isNullOrEmpty()) {
                                viewModel.handleUpstoxAuthCode(context, code)
                                UpstoxExecutionService.handleAuthCode(context, code)
                                onAuthComplete()
                            }
                        } else if (currentUrl.contains("error=")) {
                            view?.stopLoading()
                            val errorMsg = Uri.parse(currentUrl).getQueryParameter("error_description")
                                ?: Uri.parse(currentUrl).getQueryParameter("error")
                                ?: "Unknown error"
                            Log.e("UpstoxAuth", "Authentication failed: $errorMsg")
                            Toast.makeText(context, "Auth Failed", Toast.LENGTH_LONG).show()
                            onBack()
                        }
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: ""
                    if (url.startsWith("https://127.0.0.1") || url.contains("code=") || url.contains("error=")) {
                        if (url.contains("code=")) {
                            view?.stopLoading()
                            val code = request?.url?.getQueryParameter("code") ?: Uri.parse(url).getQueryParameter("code")
                            if (!code.isNullOrEmpty()) {
                                viewModel.handleUpstoxAuthCode(context, code)
                                UpstoxExecutionService.handleAuthCode(context, code)
                                onAuthComplete()
                            }
                            return true
                        } else if (url.contains("error=")) {
                            view?.stopLoading()
                            val errorMsg = request?.url?.getQueryParameter("error_description")
                                ?: Uri.parse(url).getQueryParameter("error_description")
                                ?: request?.url?.getQueryParameter("error")
                                ?: Uri.parse(url).getQueryParameter("error")
                                ?: "Unknown error"
                            Log.e("UpstoxAuth", "Authentication failed: $errorMsg")
                            Toast.makeText(context, "Auth Failed", Toast.LENGTH_LONG).show()
                            onBack()
                            return true
                        }
                    }
                    return false
                }
            }
            clearCache(true)
            clearHistory()
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            loadUrl(targetUrl)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Upstox Authorization",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        AndroidView(
            factory = { webView },
            update = { },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

