package com.example.kostkita.data.repository

import com.example.kostkita.data.local.dao.PaymentDao
import com.example.kostkita.data.mapper.*
import com.example.kostkita.data.remote.api.KostKitaApiService
import com.example.kostkita.domain.model.Payment
import com.example.kostkita.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val paymentDao: PaymentDao,
    private val apiService: KostKitaApiService
) : PaymentRepository {

    override fun getAllPayments(): Flow<List<Payment>> {
        return paymentDao.getAllPayments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPaymentById(id: String): Payment? {
        return paymentDao.getPaymentById(id)?.toDomain()
    }

    override fun getPaymentsByTenantId(tenantId: String): Flow<List<Payment>> {
        return paymentDao.getPaymentsByTenantId(tenantId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPaymentsByRoomId(roomId: String): Flow<List<Payment>> {
        return paymentDao.getPaymentsByRoomId(roomId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertPayment(payment: Payment) {
        paymentDao.insertPayment(payment.toEntity())
        try {
            apiService.createPayment(payment.toDto())
        } catch (e: Exception) {
            // Handle offline mode
        }
    }

    override suspend fun updatePayment(payment: Payment) {
        paymentDao.updatePayment(payment.toEntity())
        try {
            apiService.updatePayment(payment.id, payment.toDto())
        } catch (e: Exception) {
            // Handle offline mode
        }
    }

    override suspend fun deletePayment(payment: Payment) {
        paymentDao.deletePayment(payment.toEntity())
        try {
            apiService.deletePayment(payment.id)
        } catch (e: Exception) {
            // Handle offline mode
        }
    }

    override suspend fun syncWithRemote() {
        try {
            val remotePayments = apiService.getAllPayments()
            paymentDao.deleteAllPayments()
            remotePayments.forEach { dto ->
                paymentDao.insertPayment(dto.toDomain().toEntity())
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}