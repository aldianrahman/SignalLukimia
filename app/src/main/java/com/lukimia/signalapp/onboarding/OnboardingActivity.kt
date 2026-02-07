package com.lukimia.signalapp.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.lukimia.signalapp.MainActivity
import com.lukimia.signalapp.R
import com.lukimia.signalapp.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter
    private val indicators = mutableListOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Full screen immersive
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        setupOnboardingItems()
        setupIndicators()
        setupListeners()
    }

    private fun setupOnboardingItems() {
        val items = listOf(
            OnboardingItem(
                icon = R.drawable.ic_briefcase,
                title = R.string.onboarding_title_1,
                description = R.string.onboarding_desc_1
            ),
            OnboardingItem(
                icon = R.drawable.ic_chat_bubble,
                title = R.string.onboarding_title_2,
                description = R.string.onboarding_desc_2
            ),
            OnboardingItem(
                icon = R.drawable.ic_document_sign,
                title = R.string.onboarding_title_3,
                description = R.string.onboarding_desc_3
            )
        )

        onboardingAdapter = OnboardingAdapter(items)
        binding.viewPager.adapter = onboardingAdapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
            }
        })
    }

    private fun setupIndicators() {
        val count = onboardingAdapter.itemCount
        binding.indicatorContainer.removeAllViews()
        indicators.clear()

        for (i in 0 until count) {
            val dot = ImageView(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(6, 0, 6, 0)
            dot.layoutParams = params
            indicators.add(dot)
            binding.indicatorContainer.addView(dot)
        }

        updateIndicators(0)
    }

    private fun updateIndicators(position: Int) {
        for (i in indicators.indices) {
            if (i == position) {
                indicators[i].setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.bg_dot_active)
                )
            } else {
                indicators[i].setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.bg_dot_inactive)
                )
            }
        }
    }

    private fun setupListeners() {
        binding.btnGetStarted.setOnClickListener {
            navigateToMain()
        }

        binding.tvSkip.setOnClickListener {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
