package workwork.test.revaerdaddtest

// important library for Google adMob

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import workwork.test.revaerdaddtest.databinding.ActivityMainBinding
import java.util.concurrent.atomic.AtomicBoolean

const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
const val COUNTER_TIME = 10L
const val GAME_OVER_REWARD = 1
const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleMobileAdsConsentManager: GoogleAdManager
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var coinCount: Int = 0
    private var isLoading = false
    private var rewardedAd: RewardedAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion())

        googleMobileAdsConsentManager = GoogleAdManager(this)

        googleMobileAdsConsentManager?.gatherConsent { error ->
            if (error != null) {
                // Consent not obtained in current session.
                Log.d("TAG33333", "${error.errorCode}: ${error.message}")
            }

            if (googleMobileAdsConsentManager?.canRequestAds == true) {
                initializeMobileAdsSdk()
            }

            if (googleMobileAdsConsentManager?.isPrivacyOptionsRequired == true) {
                // Regenerate the options menu to include a privacy setting.
                invalidateOptionsMenu()
            }
        }

        binding.showVideoButton.setOnClickListener {
            showRewardedVideo()
        }

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager?.canRequestAds == true) {
            initializeMobileAdsSdk()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)
        val moreMenu = menu?.findItem(R.id.action_more)
        moreMenu?.isVisible = googleMobileAdsConsentManager.isPrivacyOptionsRequired
        return super.onCreateOptionsMenu(menu)
    }
    private fun showRewardedVideo() {
//        binding.showVideoButton.visibility = View.INVISIBLE
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("TAG", "Ad was dismissed.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null
                        if (googleMobileAdsConsentManager?.canRequestAds == true) {
                            loadRewardedAd()
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d("TAG", "Ad failed to show.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d("TAG", "Ad showed fullscreen content.")
                        // Called when ad is dismissed.
                    }
                }

            rewardedAd?.show(
                this,
                OnUserEarnedRewardListener { rewardItem ->
                    // Handle the reward.
                    val rewardAmount = rewardItem.amount
                    val rewardType = rewardItem.type
                    addAndropoints()
                    Log.d("TAG", "User earned the reward.")
                }
            )
        }
    }

    private fun addAndropoints(){
        coinCount++
        binding.coinCountText.text = coinCount.toString()
    }


    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this){}
        // Load an ad.
        loadRewardedAd()
    }

    private fun loadRewardedAd() {
        if (rewardedAd == null) {
            isLoading = true
            var adRequest = AdRequest.Builder().build()

            RewardedAd.load(
                this,
                AD_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d("TAG", adError.message)
                        isLoading = false
                        rewardedAd = null
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d("TAG", "Ad was loaded.")
                        rewardedAd = ad
                        isLoading = false
                    }
                }
            )
        }
    }
}
