package com.travel.app.presentation.components.auth

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

class CaptchaInterface(private val onVerified: (String) -> Unit) {
    @JavascriptInterface
    fun onCaptchaVerified(token: String) {
        onVerified(token)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReCaptchaDialog(
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(450.dp), // Altezza ottimale della scheda per mostrare titolo, CAPTCHA e pulsante annulla
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Verifica di Sicurezza",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                )

                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = false // Disabilita viewport desktop fittizio
                            settings.loadWithOverviewMode = false

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                }
                            }

                            addJavascriptInterface(
                                CaptchaInterface { token ->
                                    post {
                                        onSuccess(token)
                                    }
                                },
                                "AndroidInterface"
                            )

                            // Carica un HTML standard ottimizzato per mobile: semplice, senza flexbox verticali o scale
                            val html = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
                                    <script type="text/javascript">
                                        function onCaptchaSuccess(token) {
                                            if (window.AndroidInterface) {
                                                window.AndroidInterface.onCaptchaVerified(token);
                                            }
                                        }
                                    </script>
                                </head>
                                <body style="margin: 0; padding: 20px 0; background-color: #ffffff; text-align: center;">
                                    <div style="display: inline-block; text-align: left;">
                                        <div class="g-recaptcha" 
                                             data-sitekey="6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI" 
                                             data-callback="onCaptchaSuccess">
                                        </div>
                                    </div>
                                </body>
                                </html>
                            """.trimIndent()

                            loadDataWithBaseURL("http://localhost", html, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp) // Altezza dedicata al WebView all'interno della scheda
                )

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("Annulla")
                }
            }
        }
    }
}
