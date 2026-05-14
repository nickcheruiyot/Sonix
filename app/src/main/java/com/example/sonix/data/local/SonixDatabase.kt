package com.example.sonix.data.local
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sonix.data.local.dao.SongDao
import com.example.sonix.data.local.entity.SongEntity

@Database(
    entities = [SongEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SonixDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: SonixDatabase? = null

        fun getInstance(context: Context): SonixDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    SonixDatabase::class.java,
                    "sonix_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}