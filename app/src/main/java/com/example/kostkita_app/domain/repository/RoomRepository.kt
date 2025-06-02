package com.example.kostkita.domain.repository

import com.example.kostkita.domain.model.Room
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    fun getAllRooms(): Flow<List<Room>>
    suspend fun getRoomById(id: String): Room?
    fun getRoomsByStatus(status: String): Flow<List<Room>>
    suspend fun insertRoom(room: Room)
    suspend fun updateRoom(room: Room)
    suspend fun deleteRoom(room: Room)
    suspend fun syncWithRemote()
}