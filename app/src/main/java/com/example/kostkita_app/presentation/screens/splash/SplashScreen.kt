package com.example.kostkita_app.presentation.screens.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    logoResId: Int,
    onSplashComplete: () -> Unit
) {
    var showSplash by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Langsung tampilkan splash tanpa delay (fix zoom bug)
        showSplash = true

        // Tunggu 3 detik
        delay(7000)

        // Exit splash dengan slide left
        showSplash = false

        // Tunggu animasi selesai baru callback
        delay(500)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Splash Image dengan animasi - NO ZOOM BUG
        AnimatedVisibility(
            visible = showSplash,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight }, // Dari bawah
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth }, // Ke kiri
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            Image(
                painter = painterResource(id = logoResId),
                contentDescription = "KostKita Splash",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}