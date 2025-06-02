package com.example.kostkita_app.domain.model

data class Payment(
    val id: String,
    val tenantId: String,
    val roomId: String,
    val bulanTahun: String,
    val jumlahBayar: Int,
    val tanggalBayar: Long,
    val statusPembayaran: String,
    val denda: Int
)