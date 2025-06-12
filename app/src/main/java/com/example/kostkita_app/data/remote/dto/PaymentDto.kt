package com.example.kostkita_app.data.remote.dto

data class PaymentDto(
    val id: String,
    val tenant_id: String,
    val room_id: String,
    val bulan_tahun: String,
    val jumlah_bayar: Int,
    val tanggal_bayar: Long,
    val status_pembayaran: String,
    val denda: Int
)