package com.example.kostkita_app.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kostkita_app.presentation.screens.auth.LoginScreen
import com.example.kostkita_app.presentation.screens.auth.RegisterScreen
import com.example.kostkita_app.presentation.screens.auth.ForgotPasswordScreen
import com.example.kostkita_app.presentation.screens.home.HomeScreen
import com.example.kostkita_app.presentation.screens.profile.ProfileScreen
import com.example.kostkita_app.presentation.screens.payment.PaymentListScreen
import com.example.kostkita_app.presentation.screens.payment.PaymentFormScreen
import com.example.kostkita_app.presentation.screens.room.RoomListScreen
import com.example.kostkita_app.presentation.screens.room.RoomFormScreen
import com.example.kostkita_app.presentation.screens.tenant.TenantListScreen
import com.example.kostkita_app.presentation.screens.tenant.TenantFormScreen
import com.example.kostkita_app.presentation.navigation.KostKitaScreens

@Composable
fun KostKitaNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = KostKitaScreens.Login.route
    ) {
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

        composable(route = KostKitaScreens.Profile.route) {
            ProfileScreen(navController = navController)
        }

        // Tenant Screens
        composable(route = KostKitaScreens.TenantList.route) {
            TenantListScreen(navController = navController)
        }

        composable(route = KostKitaScreens.TenantForm.route) {
            TenantFormScreen(navController = navController)
        }

        composable(route = "${KostKitaScreens.TenantForm.route}/{tenantId}") { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getString("tenantId")
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
            val roomId = backStackEntry.arguments?.getString("roomId")
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
            val paymentId = backStackEntry.arguments?.getString("paymentId")
            PaymentFormScreen(
                navController = navController,
                paymentId = paymentId
            )
        }
    }
}