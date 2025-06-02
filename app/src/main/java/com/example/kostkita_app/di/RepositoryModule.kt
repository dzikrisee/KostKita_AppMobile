package com.example.kostkita.di

import com.example.kostkita.data.repository.AuthRepositoryImpl
import com.example.kostkita.data.repository.PaymentRepositoryImpl
import com.example.kostkita.data.repository.RoomRepositoryImpl
import com.example.kostkita.data.repository.TenantRepositoryImpl
import com.example.kostkita.domain.repository.AuthRepository
import com.example.kostkita.domain.repository.PaymentRepository
import com.example.kostkita.domain.repository.RoomRepository
import com.example.kostkita.domain.repository.TenantRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTenantRepository(
        tenantRepositoryImpl: TenantRepositoryImpl
    ): TenantRepository

    @Binds
    @Singleton
    abstract fun bindRoomRepository(
        roomRepositoryImpl: RoomRepositoryImpl
    ): RoomRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        paymentRepositoryImpl: PaymentRepositoryImpl
    ): PaymentRepository
}