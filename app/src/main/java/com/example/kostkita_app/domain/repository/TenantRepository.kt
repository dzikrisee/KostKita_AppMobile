package com.example.kostkita_app.domain.repository

import com.example.kostkita_app.domain.model.Tenant
import kotlinx.coroutines.flow.Flow

interface TenantRepository {
    fun getAllTenants(): Flow<List<Tenant>>
    suspend fun getTenantById(id: String): Tenant?
    suspend fun insertTenant(tenant: Tenant)
    suspend fun updateTenant(tenant: Tenant)
    suspend fun deleteTenant(tenant: Tenant)
    suspend fun syncWithRemote()
}

