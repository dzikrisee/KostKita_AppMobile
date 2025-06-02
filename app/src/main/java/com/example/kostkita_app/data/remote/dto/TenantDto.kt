package com.example.kostkita.data.remote.dto

data class TenantDto(
    val id: String,
    val nama: String,
    val email: String,
    val phone: String,
    val pekerjaan: String,
    val emergency_contact: String,
    val tanggal_masuk: Long,
    val room_id: String? = null,
    // Additional fields from JOIN
    val nomor_kamar: String? = null,
    val tipe_kamar: String? = null,
    val harga_bulanan: Int? = null
)