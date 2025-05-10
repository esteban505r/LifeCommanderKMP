package com.esteban.ruano.core_ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.esteban.ruano.core_ui.R


val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val lexendFont = GoogleFont("Lexend")

val lexendFontFamily = FontFamily(
    Font(googleFont = lexendFont, fontProvider = provider)
)