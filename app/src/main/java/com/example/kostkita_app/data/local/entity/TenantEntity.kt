package com.example.kostkita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tenants")
data class TenantEntity(
    @PrimaryKey
    val id: String,
    val nama: String,
    val email: String,
    val phone: String,
    val pekerjaan: String,
    val emergencyContact: String,
    val tanggalMasuk: Long,
    val roomId: String? = null
)