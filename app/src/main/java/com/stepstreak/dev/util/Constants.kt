package com.stepstreak.dev.util

import java.util.Locale
import com.stepstreak.dev.BuildConfig

const val BASE_URL = BuildConfig.BASE_URL

val locale: String = Locale.getDefault().language
val HOME_URL = "$BASE_URL?locale=$locale"

const val SIGN_IN_URL = "$BASE_URL/signin"
