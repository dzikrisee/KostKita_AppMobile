package com.example.kostkita_app.data.mapper

import com.example.kostkita_app.data.local.entity.RoomEntity
import com.example.kostkita_app.data.remote.dto.RoomDto
import com.example.kostkita_app.domain.model.Room

fun RoomEntity.toDomain(): Room {
    return Room(
        id = id,
        nomorKamar = nomorKamar,
        tipeKamar = tipeKamar,
        hargaBulanan = hargaBulanan,
        fasilitas = fasilitas,
        statusKamar = statusKamar,
        lantai = lantai
    )
}

fun Room.toEntity(): RoomEntity {
    return RoomEntity(
        id = id,
        nomorKamar = nomorKamar,
        tipeKamar = tipeKamar,
        hargaBulanan = hargaBulanan,
        fasilitas = fasilitas,
        statusKamar = statusKamar,
        lantai = lantai
    )
}

fun RoomDto.toDomain(): Room {
    return Room(
        id = id,
        nomorKamar = nomor_kamar,
        tipeKamar = tipe_kamar,
        hargaBulanan = harga_bulanan,
        fasilitas = fasilitas,
        statusKamar = status_kamar,
        lantai = lantai
    )
}

fun Room.toDto(): RoomDto {
    return RoomDto(
        id = id,
        nomor_kamar = nomorKamar,
        tipe_kamar = tipeKamar,
        harga_bulanan = hargaBulanan,
        fasilitas = fasilitas,
        status_kamar = statusKamar,
        lantai = lantai
    )
}