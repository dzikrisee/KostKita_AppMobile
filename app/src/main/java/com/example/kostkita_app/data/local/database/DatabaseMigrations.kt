package com.example.kostkita.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Centralized database migrations untuk KostKita
 * Setiap migration menjelaskan perubahan yang dilakukan
 */
object DatabaseMigrations {

    /**
     * Migration 1 -> 2
     * Changes:
     * - Menambahkan index untuk improve performance
     * - Memperbaiki foreign key constraints
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            try {
                // Tambahkan index untuk improve query performance
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_tenants_roomId 
                    ON tenants (roomId)
                """.trimIndent())

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_payments_tenantId 
                    ON payments (tenantId)
                """.trimIndent())

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_payments_roomId 
                    ON payments (roomId)
                """.trimIndent())

                // Tambahkan index untuk search functionality
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_tenants_nama 
                    ON tenants (nama)
                """.trimIndent())

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_rooms_nomorKamar 
                    ON rooms (nomorKamar)
                """.trimIndent())

                // Log migration success
                android.util.Log.d("DatabaseMigration", "Migration 1->2 completed successfully")

            } catch (e: Exception) {
                android.util.Log.e("DatabaseMigration", "Migration 1->2 failed", e)
                throw e
            }
        }
    }

    /**
     * Migration 2 -> 3
     * Changes:
     * - Contoh untuk future updates
     * - Bisa menambah kolom, tabel baru, dll
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            try {
                // Contoh: Menambah kolom avatar untuk tenant
                database.execSQL("""
                    ALTER TABLE tenants 
                    ADD COLUMN avatar_url TEXT DEFAULT NULL
                """.trimIndent())

                // Contoh: Menambah kolom notes untuk payments
                database.execSQL("""
                    ALTER TABLE payments 
                    ADD COLUMN notes TEXT DEFAULT NULL
                """.trimIndent())

                // Contoh: Create new table untuk settings
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS app_settings (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        key TEXT NOT NULL,
                        value TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_app_settings_key 
                    ON app_settings (key)
                """.trimIndent())

                android.util.Log.d("DatabaseMigration", "Migration 2->3 completed successfully")

            } catch (e: Exception) {
                android.util.Log.e("DatabaseMigration", "Migration 2->3 failed", e)
                throw e
            }
        }
    }

    /**
     * Migration 3 -> 4
     * Template untuk future migrations
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            try {
                // Future migration logic here

                android.util.Log.d("DatabaseMigration", "Migration 3->4 completed successfully")

            } catch (e: Exception) {
                android.util.Log.e("DatabaseMigration", "Migration 3->4 failed", e)
                throw e
            }
        }
    }

    /**
     * Get all available migrations
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4
        )
    }

    /**
     * Utility function untuk check database integrity
     */
    fun checkDatabaseIntegrity(database: SupportSQLiteDatabase): Boolean {
        return try {
            val cursor = database.query("PRAGMA integrity_check")
            cursor.use {
                if (it.moveToFirst()) {
                    val result = it.getString(0)
                    result == "ok"
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DatabaseMigration", "Integrity check failed", e)
            false
        }
    }

    /**
     * Get database version
     */
    fun getDatabaseVersion(database: SupportSQLiteDatabase): Int {
        return try {
            val cursor = database.query("PRAGMA user_version")
            cursor.use {
                if (it.moveToFirst()) {
                    it.getInt(0)
                } else {
                    0
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DatabaseMigration", "Failed to get database version", e)
            0
        }
    }
}