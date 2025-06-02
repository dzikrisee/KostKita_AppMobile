package com.example.kostkita.presentation.screens.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostkita.domain.model.Tenant
import com.example.kostkita.domain.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TenantViewModel @Inject constructor(
    private val tenantRepository: TenantRepository
) : ViewModel() {

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTenants()
    }

    private fun loadTenants() {
        viewModelScope.launch {
            tenantRepository.getAllTenants().collect {
                _tenants.value = it
            }
        }
    }

    fun addTenant(
        nama: String,
        email: String,
        phone: String,
        pekerjaan: String,
        emergencyContact: String,
        roomId: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val tenant = Tenant(
                id = UUID.randomUUID().toString(),
                nama = nama,
                email = email,
                phone = phone,
                pekerjaan = pekerjaan,
                emergencyContact = emergencyContact,
                tanggalMasuk = System.currentTimeMillis(),
                roomId = roomId
            )
            tenantRepository.insertTenant(tenant)
            _isLoading.value = false
        }
    }

    fun updateTenant(tenant: Tenant) {
        viewModelScope.launch {
            _isLoading.value = true
            tenantRepository.updateTenant(tenant)
            _isLoading.value = false
        }
    }

    fun deleteTenant(tenant: Tenant) {
        viewModelScope.launch {
            _isLoading.value = true
            tenantRepository.deleteTenant(tenant)
            _isLoading.value = false
        }
    }

    fun syncWithRemote() {
        viewModelScope.launch {
            _isLoading.value = true
            tenantRepository.syncWithRemote()
            _isLoading.value = false
        }
    }
}