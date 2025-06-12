package com.example.kostkita_app.presentation.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostkita_app.domain.model.Payment
import com.example.kostkita_app.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadPayments()
    }

    private fun loadPayments() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            paymentRepository.getAllPayments()
                .catch { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
                .collect { paymentList ->
                    _payments.value = paymentList.sortedByDescending { it.tanggalBayar }
                    _isLoading.value = false
                }
        }
    }

    fun addPayment(
        tenantId: String,
        roomId: String,
        bulanTahun: String,
        jumlahBayar: Int,
        statusPembayaran: String,
        denda: Int
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val payment = Payment(
                    id = UUID.randomUUID().toString(),
                    tenantId = tenantId,
                    roomId = roomId,
                    bulanTahun = bulanTahun,
                    jumlahBayar = jumlahBayar,
                    tanggalBayar = System.currentTimeMillis(),
                    statusPembayaran = statusPembayaran,
                    denda = denda
                )

                paymentRepository.insertPayment(payment)
            } catch (exception: Exception) {
                _error.value = exception.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePayment(payment: Payment) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                paymentRepository.updatePayment(payment)
            } catch (exception: Exception) {
                _error.value = exception.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                paymentRepository.deletePayment(payment)
            } catch (exception: Exception) {
                _error.value = exception.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun syncWithRemote() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                paymentRepository.syncWithRemote()
            } catch (exception: Exception) {
                _error.value = exception.message ?: "Gagal melakukan sinkronisasi"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}