package com.example.kostkita.di

import android.content.Context
import com.example.kostkita.data.local.dao.PaymentDao
import com.example.kostkita.data.local.dao.RoomDao
import com.example.kostkita.data.local.dao.TenantDao
import com.example.kostkita.data.local.database.KostKitaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKostKitaDatabase(@ApplicationContext context: Context): KostKitaDatabase {
        return KostKitaDatabase.buildDatabase(context)
    }

    @Provides
    fun provideTenantDao(database: KostKitaDatabase): TenantDao {
        return database.tenantDao()
    }

    @Provides
    fun provideRoomDao(database: KostKitaDatabase): RoomDao {
        return database.roomDao()
    }

    @Provides
    fun providePaymentDao(database: KostKitaDatabase): PaymentDao {
        return database.paymentDao()
    }
}