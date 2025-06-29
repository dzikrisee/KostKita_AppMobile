package com.example.kostkita_app.data.remote.config

object ApiConfig {
    // Environment configuration
    private const val IS_PRODUCTION = true

    // API URLs
    private const val PRODUCTION_BASE_URL = "https://kostkitaapi-production.up.railway.app/api/"
    private const val DEVELOPMENT_BASE_URL = "http://10.0.2.2:3000/api/"

    // Public API URL getter
    val BASE_URL: String = if (IS_PRODUCTION) PRODUCTION_BASE_URL else DEVELOPMENT_BASE_URL

    // API Endpoints
    object Auth {
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val FORGOT_PASSWORD = "auth/forgot-password"
        const val UPDATE_PROFILE = "auth/profile"
        const val CHANGE_PASSWORD = "auth/change-password"
    }

    object Tenants {
        const val BASE = "tenants"
        fun byId(id: String) = "tenants/$id"
    }

    object Rooms {
        const val BASE = "rooms"
        fun byId(id: String) = "rooms/$id"
    }

    object Payments {
        const val BASE = "payments"
        fun byId(id: String) = "payments/$id"
    }

    // Network timeout settings
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // Request headers
    const val CONTENT_TYPE = "application/json"
    const val ACCEPT = "application/json"

    // Demo credentials
    const val DEMO_USERNAME = "admin"
    const val DEMO_PASSWORD = "admin123"
}