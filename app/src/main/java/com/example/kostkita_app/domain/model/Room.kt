package com.example.kostkita.domain.model

data class Room(
    val id: String,
    val nomorKamar: String,
    val tipeKamar: String,
    val hargaBulanan: Int,
    val fasilitas: String,
    val statusKamar: String,
    val lantai: Int,
    // Additional field for tenant info
    val currentTenant: Tenant? = null
)