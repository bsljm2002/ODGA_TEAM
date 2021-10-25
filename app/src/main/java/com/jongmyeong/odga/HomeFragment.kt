package com.jongmyeong.odga
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import net.daum.mf.map.api.MapView

import android.content.ComponentCallbacks
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.net.URISyntaxException
import java.util.jar.Manifest


class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.fragment_home, container, false)

        val webview : WebView = view.findViewById(R.id.webview)

        webview.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            //settings.setSupportMultipleWindows(true)

        }

        webview.webChromeClient = WebChromeClient()

        webview.webChromeClient = object : WebChromeClient(){

            override fun onGeolocationPermissionsShowPrompt(
                origin:String?,
                callback: GeolocationPermissions.Callback?
            )

            {
                super.onGeolocationPermissionsShowPrompt(origin, callback)
                callback?.invoke(origin, true, false)
            }


        }
        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url != null && url.startsWith("intent://")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        val existPackage =
                            activity?.packageManager?.getLaunchIntentForPackage(intent.getPackage()!!)
                        if (existPackage != null) {
                            startActivity(intent)
                        } else {
                            val marketIntent = Intent(Intent.ACTION_VIEW)
                            marketIntent.data =
                                Uri.parse("market://details?id=" + intent.getPackage()!!)
                            startActivity(marketIntent)
                        }
                        return true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else if (url != null && url.startsWith("market://")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        if (intent != null) {
                            startActivity(intent)
                        }
                        return true
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    }

                }
                view.loadUrl(url)
                return false
            }
        }

        webview.loadUrl("https://map.kakao.com/")



        return view
    }



}