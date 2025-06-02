package com.example.kostkita_app.data.repository

import com.example.kostkita_app.data.local.dao.TenantDao
import com.example.kostkita_app.data.mapper.*
import com.example.kostkita_app.data.remote.api.KostKitaApiService
import com.example.kostkita_app.domain.model.Tenant
import com.example.kostkita_app.domain.repository.TenantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TenantRepositoryImpl @Inject constructor(
    private val tenantDao: TenantDao,
    private val apiService: KostKitaApiService
) : TenantRepository {

    override fun getAllTenants(): Flow<List<Tenant>> {
        return tenantDao.getAllTenants().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTenantById(id: String): Tenant? {
        return tenantDao.getTenantById(id)?.toDomain()
    }

    override suspend fun insertTenant(tenant: Tenant) {
        tenantDao.insertTenant(tenant.toEntity())
        try {
            apiService.createTenant(tenant.toDto())
        } catch (e: Exception) {
            // Handle offline mode
        }
    }

    override suspend fun updateTenant(tenant: Tenant) {
        tenantDao.updateTenant(tenant.toEntity())
        try {
            apiService.updateTenant(tenant.id, tenant.toDto())
        } catch (e: Exception) {
            // Handle offline mode
        }
    }

    override suspend fun deleteTenant(tenant: Tenant) {
        tenantDao.deleteTenant(tenant.toEntity())
        try {
            apiService.deleteTenant(tenant.id)
        } catch (e: Exception) {
            // Handle offline mode
        }
    }

    override suspend fun syncWithRemote() {
        try {
            val remoteTenants = apiService.getAllTenants()
            tenantDao.deleteAllTenants()
            remoteTenants.forEach { dto ->
                tenantDao.insertTenant(dto.toDomain().toEntity())
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}
