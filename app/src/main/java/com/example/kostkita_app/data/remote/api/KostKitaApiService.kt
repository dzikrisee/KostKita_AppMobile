package com.example.kostkita.data.remote.api

import com.example.kostkita.data.remote.dto.*
import retrofit2.http.*

interface KostKitaApiService {
    // Tenant endpoints
    @GET("tenants")
    suspend fun getAllTenants(): List<TenantDto>

    @GET("tenants/{id}")
    suspend fun getTenantById(@Path("id") id: String): TenantDto

    @POST("tenants")
    suspend fun createTenant(@Body tenant: TenantDto): TenantDto

    @PUT("tenants/{id}")
    suspend fun updateTenant(@Path("id") id: String, @Body tenant: TenantDto): TenantDto

    @DELETE("tenants/{id}")
    suspend fun deleteTenant(@Path("id") id: String)

    // Room endpoints
    @GET("rooms")
    suspend fun getAllRooms(): List<RoomDto>

    @GET("rooms/{id}")
    suspend fun getRoomById(@Path("id") id: String): RoomDto

    @POST("rooms")
    suspend fun createRoom(@Body room: RoomDto): RoomDto

    @PUT("rooms/{id}")
    suspend fun updateRoom(@Path("id") id: String, @Body room: RoomDto): RoomDto

    @DELETE("rooms/{id}")
    suspend fun deleteRoom(@Path("id") id: String)

    // Payment endpoints
    @GET("payments")
    suspend fun getAllPayments(): List<PaymentDto>

    @GET("payments/{id}")
    suspend fun getPaymentById(@Path("id") id: String): PaymentDto

    @POST("payments")
    suspend fun createPayment(@Body payment: PaymentDto): PaymentDto

    @PUT("payments/{id}")
    suspend fun updatePayment(@Path("id") id: String, @Body payment: PaymentDto): PaymentDto

    @DELETE("payments/{id}")
    suspend fun deletePayment(@Path("id") id: String)
}