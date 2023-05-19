package com.example.paintapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.paintapp.typeconverter.TypeConverter

@Database(entities = [PaintCanvas::class, CanvasPath::class, ModelAnswer::class], version = 1)
@TypeConverters(TypeConverter::class)
abstract class PaintDatabase : RoomDatabase() {
    abstract fun PaintCanvasDao(): PaintCanvasDao
    abstract fun CanvasPathDao(): CanvasPathDao
    abstract fun ModelAnswerDao(): ModelAnswerDao

    companion object {
        @Volatile
        private var INSTANCE: PaintDatabase? = null

        fun getDatabase(
            context: Context,
//            scope: CoroutineScope
        ): PaintDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PaintDatabase::class.java,
                    "paint_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}