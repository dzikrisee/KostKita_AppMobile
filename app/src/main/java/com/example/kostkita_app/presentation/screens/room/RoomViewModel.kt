package com.example.kostkita.presentation.screens.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kostkita.domain.model.Room
import com.example.kostkita.domain.model.Tenant
import com.example.kostkita.domain.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadRooms()
    }

    private fun loadRooms() {
        viewModelScope.launch {
            roomRepository.getAllRooms().collect {
                _rooms.value = it
            }
        }
    }

    fun addRoom(
        nomorKamar: String,
        tipeKamar: String,
        hargaBulanan: Int,
        fasilitas: String,
        statusKamar: String,
        lantai: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val room = Room(
                id = UUID.randomUUID().toString(),
                nomorKamar = nomorKamar,
                tipeKamar = tipeKamar,
                hargaBulanan = hargaBulanan,
                fasilitas = fasilitas,
                statusKamar = statusKamar,
                lantai = lantai
            )
            roomRepository.insertRoom(room)
            _isLoading.value = false
        }
    }

    fun updateRoom(room: Room) {
        viewModelScope.launch {
            _isLoading.value = true
            roomRepository.updateRoom(room)
            _isLoading.value = false
        }
    }

    fun deleteRoom(room: Room) {
        viewModelScope.launch {
            _isLoading.value = true
            roomRepository.deleteRoom(room)
            _isLoading.value = false
        }
    }

    fun syncWithRemote() {
        viewModelScope.launch {
            _isLoading.value = true
            roomRepository.syncWithRemote()
            _isLoading.value = false
        }
    }
}