package com.example.kostkita_app.presentation.navigation

sealed class KostKitaScreens(val route: String) {
    object Splash : KostKitaScreens("splash")
    object Login : KostKitaScreens("login")
    object Register : KostKitaScreens("register")
    object ForgotPassword : KostKitaScreens("forgot_password")
    object Home : KostKitaScreens("home")
    object Profile : KostKitaScreens("profile")
    object EditProfile : KostKitaScreens("edit_profile")
    object ChangePassword : KostKitaScreens("change_password")
    object TenantList : KostKitaScreens("tenant_list")
    object TenantForm : KostKitaScreens("tenant_form")
    object RoomList : KostKitaScreens("room_list")
    object RoomForm : KostKitaScreens("room_form")
    object PaymentList : KostKitaScreens("payment_list")
    object PaymentForm : KostKitaScreens("payment_form")
}