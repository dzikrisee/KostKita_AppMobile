package com.example.kostkita_app.di

import com.example.kostkita_app.data.repository.AuthRepositoryImpl
import com.example.kostkita_app.data.repository.PaymentRepositoryImpl
import com.example.kostkita_app.data.repository.RoomRepositoryImpl
import com.example.kostkita_app.data.repository.TenantRepositoryImpl
import com.example.kostkita_app.domain.repository.AuthRepository
import com.example.kostkita_app.domain.repository.PaymentRepository
import com.example.kostkita_app.domain.repository.RoomRepository
import com.example.kostkita_app.domain.repository.TenantRepository
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