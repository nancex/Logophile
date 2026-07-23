package me.nancex.logophile.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntry>)

    @Delete
    suspend fun delete(word: WordEntry)

    @Query("DELETE FROM words WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM words ORDER BY added_time DESC")
    fun getAllWordsFlow(): Flow<List<WordEntry>>

    @Query("SELECT * FROM words ORDER BY added_time DESC")
    suspend fun getAllWords(): List<WordEntry>

    @Query("SELECT * FROM words WHERE word LIKE :query || '%' ORDER BY added_time DESC")
    fun searchWords(query: String): Flow<List<WordEntry>>

    @Query("SELECT * FROM words WHERE word LIKE :query || '%' ORDER BY word COLLATE NOCASE ASC")
    fun searchWordsAlpha(query: String): Flow<List<WordEntry>>

    @Query("SELECT * FROM words WHERE word LIKE :query || '%' ORDER BY added_time DESC")
    fun searchWordsDate(query: String): Flow<List<WordEntry>>

    @Query("SELECT * FROM words WHERE word = :word AND language = :language LIMIT 1")
    suspend fun findByWordAndLanguage(word: String, language: String): WordEntry?

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): WordEntry?

    @Query("UPDATE words SET pass_count = pass_count + 1 WHERE id = :id")
    suspend fun incrementPassCount(id: Int)

    @Query("UPDATE words SET tip_count = tip_count + 1 WHERE id = :id")
    suspend fun incrementTipCount(id: Int)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int
}
