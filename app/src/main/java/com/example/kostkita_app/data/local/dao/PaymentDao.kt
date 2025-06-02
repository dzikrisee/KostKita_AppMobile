package com.example.kostkita.data.local.dao

import androidx.room.*
import com.example.kostkita.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE tenantId = :tenantId")
    fun getPaymentsByTenantId(tenantId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE roomId = :roomId")
    fun getPaymentsByRoomId(roomId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()
}
