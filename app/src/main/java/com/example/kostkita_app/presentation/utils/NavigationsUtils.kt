// Create this file: presentation/utils/NavigationUtils.kt
package com.example.kostkita_app.presentation.utils

import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object NavigationUtils {

    fun safeNavigate(
        navController: NavController,
        route: String,
        scope: CoroutineScope,
        onError: (suspend () -> Unit)? = null
    ) {
        scope.launch {
            try {
                navController.navigate(route)
            } catch (e: Exception) {
                onError?.invoke() ?: run {
                    // Fallback navigation
                    try {
                        navController.popBackStack()
                    } catch (ex: Exception) {
                        // Last resort - do nothing
                    }
                }
            }
        }
    }

    fun safeNavigateUp(
        navController: NavController,
        scope: CoroutineScope,
        fallbackRoute: String? = null,
        onError: (suspend () -> Unit)? = null
    ) {
        scope.launch {
            try {
                navController.navigateUp()
            } catch (e: Exception) {
                try {
                    navController.popBackStack()
                } catch (ex: Exception) {
                    fallbackRoute?.let { route ->
                        try {
                            navController.navigate(route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } catch (finalEx: Exception) {
                            onError?.invoke()
                        }
                    } ?: onError?.invoke()
                }
            }
        }
    }
}