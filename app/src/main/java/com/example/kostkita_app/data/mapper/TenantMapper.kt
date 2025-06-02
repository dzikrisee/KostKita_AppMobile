package com.example.kostkita_app.data.mapper

import com.example.kostkita_app.data.local.entity.TenantEntity
import com.example.kostkita_app.data.remote.dto.TenantDto
import com.example.kostkita_app.domain.model.Tenant

fun TenantEntity.toDomain(): Tenant {
    return Tenant(
        id = id,
        nama = nama,
        email = email,
        phone = phone,
        pekerjaan = pekerjaan,
        emergencyContact = emergencyContact,
        tanggalMasuk = tanggalMasuk,
        roomId = roomId
    )
}

fun Tenant.toEntity(): TenantEntity {
    return TenantEntity(
        id = id,
        nama = nama,
        email = email,
        phone = phone,
        pekerjaan = pekerjaan,
        emergencyContact = emergencyContact,
        tanggalMasuk = tanggalMasuk,
        roomId = roomId
    )
}

fun TenantDto.toDomain(): Tenant {
    return Tenant(
        id = id,
        nama = nama,
        email = email,
        phone = phone,
        pekerjaan = pekerjaan,
        emergencyContact = emergency_contact,
        tanggalMasuk = tanggal_masuk,
        roomId = room_id,
        nomorKamar = nomor_kamar,
        tipeKamar = tipe_kamar,
        hargaBulanan = harga_bulanan
    )
}

fun Tenant.toDto(): TenantDto {
    return TenantDto(
        id = id,
        nama = nama,
        email = email,
        phone = phone,
        pekerjaan = pekerjaan,
        emergency_contact = emergencyContact,
        tanggal_masuk = tanggalMasuk,
        room_id = roomId,
        nomor_kamar = nomorKamar,
        tipe_kamar = tipeKamar,
        harga_bulanan = hargaBulanan
    )
}
