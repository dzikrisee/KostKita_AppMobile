package com.example.kostkita.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey
    val id: String,
    val nomorKamar: String,
    val tipeKamar: String,
    val hargaBulanan: Int,
    val fasilitas: String,
    val statusKamar: String,
    val lantai: Int
)