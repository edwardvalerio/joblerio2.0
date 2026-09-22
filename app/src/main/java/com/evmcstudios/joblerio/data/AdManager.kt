package com.evmcstudios.joblerio.data

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.evmcstudios.joblerio.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

object AdManager {

    private const val BANNER_AD_UNIT_ID = "ca-app-pub-9284077315374106/9814108892"
    private const val NATIVE_AD_UNIT_ID = "ca-app-pub-9284077315374106/2049325633"

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            com.google.android.gms.ads.MobileAds.initialize(context) { status ->
                Log.d("AdManager", "MobileAds initialized: $status")
                isInitialized = true
            }
        } catch (e: Exception) {
            Log.e("AdManager", "MobileAds init failed: ${e.message}")
        }
    }

    @Composable
    fun BannerAd(modifier: Modifier = Modifier) {
        if (LocalInspectionMode.current) return

        val context = LocalContext.current
        val adView = remember {
            AdView(context).apply {
                adUnitId = BANNER_AD_UNIT_ID
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 360))
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d("AdManager", "Banner ad loaded")
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e("AdManager", "Banner ad failed: ${error.message}")
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            adView.loadAd(AdRequest.Builder().build())
            onDispose {
                adView.destroy()
            }
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(width = 0.5.dp, color = Color(0xFFE0E0E0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ADVERTISEMENT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF999999),
                    letterSpacing = 1.sp
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { adView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }
        }
    }

    @Composable
    fun NativeAdCard(modifier: Modifier = Modifier) {
        if (LocalInspectionMode.current) return

        val context = LocalContext.current
        val isLoaded = remember { mutableStateOf(false) }
        val nativeAdRef = remember { mutableStateOf<NativeAd?>(null) }

        DisposableEffect(Unit) {
            val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
                .forNativeAd { ad ->
                    nativeAdRef.value = ad
                    isLoaded.value = true
                    Log.d("AdManager", "Native ad loaded: ${ad.headline}")
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e("AdManager", "Native ad failed: ${error.message}")
                        isLoaded.value = false
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT)
                        .build()
                )
                .build()

            adLoader.loadAd(AdRequest.Builder().build())

            onDispose {
                nativeAdRef.value?.destroy()
            }
        }

        if (isLoaded.value) {
            val ad = nativeAdRef.value
            if (ad != null) {
                AndroidView(
                    factory = { ctx ->
                        val view = LayoutInflater.from(ctx).inflate(R.layout.ad_native_job, null)
                        populateNativeAdView(view, ad)
                        view
                    },
                    modifier = modifier.fillMaxWidth()
                )
            }
        }
    }

    private fun populateNativeAdView(view: View, nativeAd: NativeAd) {
        val nativeAdView = view as NativeAdView

        nativeAdView.headlineView = view.findViewById(R.id.ad_headline)
        nativeAdView.bodyView = view.findViewById(R.id.ad_body)
        nativeAdView.callToActionView = view.findViewById(R.id.ad_call_to_action)
        nativeAdView.advertiserView = view.findViewById(R.id.ad_advertiser)
        nativeAdView.iconView = view.findViewById(R.id.ad_app_icon)
        nativeAdView.mediaView = view.findViewById(R.id.ad_media)

        (nativeAdView.headlineView as? TextView)?.text = nativeAd.headline
        (nativeAdView.bodyView as? TextView)?.text = nativeAd.body
        (nativeAdView.callToActionView as? Button)?.text = nativeAd.callToAction
        (nativeAdView.advertiserView as? TextView)?.text = nativeAd.advertiser

        nativeAd.icon?.let { icon ->
            (nativeAdView.iconView as? ImageView)?.setImageDrawable(icon.drawable)
        }

        nativeAdView.setNativeAd(nativeAd)
    }
}
