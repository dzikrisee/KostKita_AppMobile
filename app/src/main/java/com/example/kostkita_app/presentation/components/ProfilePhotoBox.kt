package com.example.kostkita_app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kostkita_app.presentation.screens.profile.ProfileViewModel
import java.io.File

@Composable
fun ProfilePhotoBox(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val currentUser by profileViewModel.user.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State untuk force refresh image
    var imageKey by remember { mutableIntStateOf(0) }

    // Listen untuk lifecycle events dan refresh user data
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Refresh user data setiap kali screen resume (misal balik dari EditProfile)
                    profileViewModel.refreshUserData()
                    imageKey++ // Force refresh image
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Trigger refresh ketika profilePhoto berubah
    LaunchedEffect(currentUser?.profilePhoto) {
        if (currentUser?.profilePhoto != null) {
            imageKey++ // Force refresh image ketika path berubah
        }
    }

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, secondaryColor)
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val hasValidPhoto = remember(currentUser?.profilePhoto, imageKey) {
            currentUser?.profilePhoto?.let { photoPath ->
                photoPath.isNotEmpty() && File(photoPath).exists()
            } ?: false
        }

        if (hasValidPhoto && currentUser?.profilePhoto != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(currentUser!!.profilePhoto))
                    .memoryCacheKey("profile_photo_$imageKey") // Cache key yang berubah untuk force refresh
                    .diskCacheKey("profile_photo_${currentUser!!.profilePhoto}_$imageKey")
                    .build(),
                contentDescription = "Foto Profil",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onSuccess = {
                    android.util.Log.d("ProfilePhotoBox", "✅ Image loaded successfully: ${currentUser!!.profilePhoto}")
                },
                onError = { error ->
                    android.util.Log.e("ProfilePhotoBox", "❌ Failed to load image: ${currentUser!!.profilePhoto}, Error: ${error.result.throwable}")
                }
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}