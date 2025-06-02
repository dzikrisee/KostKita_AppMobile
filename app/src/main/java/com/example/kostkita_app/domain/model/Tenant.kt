package com.example.kostkita.domain.model

data class Tenant(
    val id: String,
    val nama: String,
    val email: String,
    val phone: String,
    val pekerjaan: String,
    val emergencyContact: String,
    val tanggalMasuk: Long,
    val roomId: String? = null,
    // Additional fields from JOIN query
    val nomorKamar: String? = null,
    val tipeKamar: String? = null,
    val hargaBulanan: Int? = null

)