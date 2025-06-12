package com.example.kostkita_app.data.repository

import com.example.kostkita_app.data.local.dao.RoomDao
import com.example.kostkita_app.data.mapper.*
import com.example.kostkita_app.data.remote.api.KostKitaApiService
import com.example.kostkita_app.domain.model.Room
import com.example.kostkita_app.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val roomDao: RoomDao,
    private val apiService: KostKitaApiService
) : RoomRepository {

    override fun getAllRooms(): Flow<List<Room>> {
        return roomDao.getAllRooms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRoomById(id: String): Room? {
        return roomDao.getRoomById(id)?.toDomain()
    }

    override fun getRoomsByStatus(status: String): Flow<List<Room>> {
        return roomDao.getRoomsByStatus(status).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertRoom(room: Room) {
        roomDao.insertRoom(room.toEntity())
        try {
            apiService.createRoom(room.toDto())
        } catch (e: Exception) {
            // Handle offline mode
        }
    }

    override suspend fun updateRoom(room: Room) {
        roomDao.updateRoom(room.toEntity())
        try {
            apiService.updateRoom(room.id, room.toDto())
        } catch (e: Exception) {
            // Handle offline mode
        }
    }

    override suspend fun deleteRoom(room: Room) {
        roomDao.deleteRoom(room.toEntity())
        try {
            apiService.deleteRoom(room.id)
        } catch (e: Exception) {
            // Handle offline mode
        }
    }

    override suspend fun syncWithRemote() {
        try {
            val remoteRooms = apiService.getAllRooms()
            roomDao.deleteAllRooms()
            remoteRooms.forEach { dto ->
                roomDao.insertRoom(dto.toDomain().toEntity())
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}