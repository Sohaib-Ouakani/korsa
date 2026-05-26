package com.example.corsa.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle( //use this for int the app bar, a composable text for that already exist in composables.AppBarText
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        fontSize = 28.sp,
        letterSpacing = 2.sp,
    ),
    displayLarge = TextStyle( //use this for big bold text in the center of the screen like in the home page "READY TO MOVE" text
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        fontSize = 60.sp,
        lineHeight = 58.sp,
    ),
    displayMedium = TextStyle( //use this for big bold text in the center of the screen like in the home page "READY TO MOVE" text
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        fontSize = 42.sp,
        lineHeight = 58.sp,
    ),
    displaySmall = TextStyle( //use this for big bold text in the center of the screen like in the home page "READY TO MOVE" text
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        fontSize = 15.sp,
        lineHeight = 58.sp,
    ),
    labelLarge = TextStyle( //automatically applied to Buttons like composables
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 1.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 1.sp,
    )
)