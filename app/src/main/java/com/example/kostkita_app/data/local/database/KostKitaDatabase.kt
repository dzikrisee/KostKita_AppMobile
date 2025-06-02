package com.example.kostkita_app.data.local.database

import android.annotation.SuppressLint
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kostkita_app.data.local.dao.PaymentDao
import com.example.kostkita_app.data.local.dao.RoomDao
import com.example.kostkita_app.data.local.dao.TenantDao
import com.example.kostkita_app.data.local.entity.PaymentEntity
import com.example.kostkita_app.data.local.entity.RoomEntity
import com.example.kostkita_app.data.local.entity.TenantEntity

@Database(
    entities = [TenantEntity::class, RoomEntity::class, PaymentEntity::class],
    version = 2,
    exportSchema = false
)
abstract class KostKitaDatabase : RoomDatabase() {
    abstract fun tenantDao(): TenantDao
    abstract fun roomDao(): RoomDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        /**
         * Migration 1→2: Menambahkan kolom roomId ke tabel tenants
         * Database versi 1 tidak punya kolom roomId, jadi kita tambahkan
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    android.util.Log.d("DatabaseMigration", "Starting migration 1->2")

                    // Cek apakah kolom roomId sudah ada
                    val hasRoomId = checkColumnExists(database, "tenants", "roomId")
                    android.util.Log.d("DatabaseMigration", "Column roomId exists: $hasRoomId")

                    if (!hasRoomId) {
                        // Tambahkan kolom roomId ke tabel tenants
                        database.execSQL("""
                            ALTER TABLE tenants 
                            ADD COLUMN roomId TEXT DEFAULT NULL
                        """.trimIndent())

                        android.util.Log.d("DatabaseMigration", "Added roomId column to tenants table")
                    }

                    android.util.Log.d("DatabaseMigration", "Migration 1->2 completed successfully")

                } catch (e: Exception) {
                    android.util.Log.e("DatabaseMigration", "Migration 1->2 failed", e)
                    throw e
                }
            }
        }

        /**
         * Helper function untuk cek apakah kolom ada di tabel
         */
        @SuppressLint("Range")
        private fun checkColumnExists(
            database: SupportSQLiteDatabase,
            tableName: String,
            columnName: String
        ): Boolean {
            return try {
                val cursor = database.query("PRAGMA table_info($tableName)")
                cursor.use {
                    while (it.moveToNext()) {
                        val colName = it.getString(it.getColumnIndex("name"))
                        if (colName == columnName) {
                            return true
                        }
                    }
                }
                false
            } catch (e: Exception) {
                android.util.Log.e("DatabaseMigration", "Failed to check column $columnName in table $tableName", e)
                false
            }
        }

        /**
         * Database callback untuk logging dan setup
         */
        private val databaseCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                android.util.Log.d("KostKitaDatabase", "Database created successfully")

                // Enable foreign keys
                db.execSQL("PRAGMA foreign_keys=ON")
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                android.util.Log.d("KostKitaDatabase", "Database opened")

                // Enable foreign keys
                db.execSQL("PRAGMA foreign_keys=ON")
            }
        }

        /**
         * Helper function untuk membuat database instance
         */
        fun buildDatabase(context: android.content.Context): KostKitaDatabase {
            return Room.databaseBuilder(
                context,
                KostKitaDatabase::class.java,
                "kostkita_database"
            )
                .addMigrations(MIGRATION_1_2)
                .addCallback(databaseCallback)
                .fallbackToDestructiveMigration() // Fallback jika migration tetap gagal
                .build()
        }

        /**
         * Helper function untuk membuat in-memory database (untuk testing)
         */
        fun buildInMemoryDatabase(context: android.content.Context): KostKitaDatabase {
            return Room.inMemoryDatabaseBuilder(
                context,
                KostKitaDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}