package com.example.kostkita.domain.repository

import com.example.kostkita.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun getAllPayments(): Flow<List<Payment>>
    suspend fun getPaymentById(id: String): Payment?
    fun getPaymentsByTenantId(tenantId: String): Flow<List<Payment>>
    fun getPaymentsByRoomId(roomId: String): Flow<List<Payment>>
    suspend fun insertPayment(payment: Payment)
    suspend fun updatePayment(payment: Payment)
    suspend fun deletePayment(payment: Payment)
    suspend fun syncWithRemote()
}