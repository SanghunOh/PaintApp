package com.example.paintapp.typeconverter

import android.graphics.Path
import android.graphics.PointF
import androidx.room.TypeConverter
import com.google.gson.Gson

class TypeConverter {
    @TypeConverter
    fun listToJson(value: List<PointF>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<PointF>::class.java).toList()

    @TypeConverter
    fun pathToJson(value: Path?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToPath(value: String) = Gson().fromJson(value, Path::class.java)

    @TypeConverter
    fun pointFToJson(value: PointF?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToPointF(value: String) = Gson().fromJson(value, PointF::class.java)

}