package me.nancex.logophile.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "language")
    val language: String = "eng",

    @ColumnInfo(name = "word")
    val word: String,

    @ColumnInfo(name = "phonetic")
    val phonetic: String? = null,

    @ColumnInfo(name = "definition")
    val definition: String? = null,

    @ColumnInfo(name = "pass_count")
    val passCount: Int = 0,

    @ColumnInfo(name = "tip_count")
    val tipCount: Int = 0,

    @ColumnInfo(name = "added_time")
    val addedTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "audio_url")
    val audioUrl: String? = null
)
