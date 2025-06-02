package com.example.kostkita.data.remote.dto

data class RoomDto(
    val id: String,
    val nomor_kamar: String,
    val tipe_kamar: String,
    val harga_bulanan: Int,
    val fasilitas: String,
    val status_kamar: String,
    val lantai: Int
)