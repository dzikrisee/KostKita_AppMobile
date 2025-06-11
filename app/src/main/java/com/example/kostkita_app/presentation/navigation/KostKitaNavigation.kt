package com.example.kostkita_app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kostkita_app.R
import com.example.kostkita_app.presentation.screens.auth.LoginScreen
import com.example.kostkita_app.presentation.screens.auth.RegisterScreen
import com.example.kostkita_app.presentation.screens.auth.ForgotPasswordScreen
import com.example.kostkita_app.presentation.screens.home.HomeScreen
import com.example.kostkita_app.presentation.screens.profile.ProfileScreen
import com.example.kostkita_app.presentation.screens.profile.ProfileViewModel
import com.example.kostkita_app.presentation.screens.profile.EditProfileScreen
import com.example.kostkita_app.presentation.screens.profile.ChangePasswordScreen
import com.example.kostkita_app.presentation.screens.payment.PaymentListScreen
import com.example.kostkita_app.presentation.screens.payment.PaymentFormScreen
import com.example.kostkita_app.presentation.screens.room.RoomListScreen
import com.example.kostkita_app.presentation.screens.room.RoomFormScreen
import com.example.kostkita_app.presentation.screens.tenant.TenantListScreen
import com.example.kostkita_app.presentation.screens.tenant.TenantFormScreen
import com.example.kostkita_app.presentation.screens.splash.SplashScreen
import com.example.kostkita_app.presentation.screens.splash.SplashViewModel
import com.example.kostkita_app.presentation.screens.splash.SplashState

@Composable
fun KostKitaNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = KostKitaScreens.Splash.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        }
    ) {
        // Splash Screen
        composable(
            route = KostKitaScreens.Splash.route,
            enterTransition = { EnterTransition.None },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
            }
        ) {
            val viewModel: SplashViewModel = hiltViewModel()
            val splashState by viewModel.splashState.collectAsState()

            LaunchedEffect(splashState) {
                when (splashState) {
                    is SplashState.Authenticated -> {
                        navController.navigate(KostKitaScreens.Home.route) {
                            popUpTo(KostKitaScreens.Splash.route) { inclusive = true }
                        }
                    }
                    is SplashState.Unauthenticated -> {
                        navController.navigate(KostKitaScreens.Login.route) {
                            popUpTo(KostKitaScreens.Splash.route) { inclusive = true }
                        }
                    }
                    else -> { /* Loading state */ }
                }
            }

            SplashScreen(
                logoResId = R.drawable.kostkita_splash,
                onSplashComplete = {
                    // Navigation handled by LaunchedEffect above
                }
            )
        }

        // Auth Screens
        composable(route = KostKitaScreens.Login.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(KostKitaScreens.Home.route) {
                        popUpTo(KostKitaScreens.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = KostKitaScreens.Register.route) {
            RegisterScreen(
                navController = navController,
                onRegisterSuccess = {
                    navController.navigate(KostKitaScreens.Home.route) {
                        popUpTo(KostKitaScreens.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = KostKitaScreens.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        // Main Screens
        composable(route = KostKitaScreens.Home.route) {
            HomeScreen(navController = navController)
        }

        // Profile Screens with proper refresh handling
        composable(route = KostKitaScreens.Profile.route) { backStackEntry ->
            val viewModel: ProfileViewModel = hiltViewModel()

            // Listen for profile update result
            val savedStateHandle = backStackEntry.savedStateHandle
            val profileUpdatedFlow = savedStateHandle.getStateFlow("profile_updated", false)
            val profileUpdated by profileUpdatedFlow.collectAsState()

            LaunchedEffect(profileUpdated) {
                if (profileUpdated) {
                    viewModel.forceRefreshAfterUpdate()
                    savedStateHandle["profile_updated"] = false
                }
            }

            ProfileScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(route = KostKitaScreens.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }

        composable(route = KostKitaScreens.ChangePassword.route) {
            ChangePasswordScreen(navController = navController)
        }

        // Tenant Screens
        composable(route = KostKitaScreens.TenantList.route) {
            TenantListScreen(navController = navController)
        }

        composable(route = KostKitaScreens.TenantForm.route) {
            TenantFormScreen(navController = navController)
        }

        composable(route = "${KostKitaScreens.TenantForm.route}/{tenantId}") { backStackEntry ->
            val tenantId: String? = backStackEntry.arguments?.getString("tenantId")
            TenantFormScreen(
                navController = navController,
                tenantId = tenantId
            )
        }

        // Room Screens
        composable(route = KostKitaScreens.RoomList.route) {
            RoomListScreen(navController = navController)
        }

        composable(route = KostKitaScreens.RoomForm.route) {
            RoomFormScreen(navController = navController)
        }

        composable(route = "${KostKitaScreens.RoomForm.route}/{roomId}") { backStackEntry ->
            val roomId: String? = backStackEntry.arguments?.getString("roomId")
            RoomFormScreen(
                navController = navController,
                roomId = roomId
            )
        }

        // Payment Screens
        composable(route = KostKitaScreens.PaymentList.route) {
            PaymentListScreen(navController = navController)
        }

        composable(route = KostKitaScreens.PaymentForm.route) {
            PaymentFormScreen(navController = navController)
        }

        composable(route = "${KostKitaScreens.PaymentForm.route}/{paymentId}") { backStackEntry ->
            val paymentId: String? = backStackEntry.arguments?.getString("paymentId")
            PaymentFormScreen(
                navController = navController,
                paymentId = paymentId
            )
        }
    }
}