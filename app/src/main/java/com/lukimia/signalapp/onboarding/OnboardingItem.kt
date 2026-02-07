package com.lukimia.signalapp.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class OnboardingItem(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val description: Int
)
