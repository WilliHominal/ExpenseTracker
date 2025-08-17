package com.warh.data.db

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Converters {
    private val dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter @JvmStatic
    fun fromLdt(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it, dtf) }

    @TypeConverter @JvmStatic
    fun toLdt(value: LocalDateTime?): String? = value?.format(dtf)
}