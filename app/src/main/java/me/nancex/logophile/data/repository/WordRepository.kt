package me.nancex.logophile.data.repository

import android.content.Context
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.gson.Gson
import me.nancex.logophile.data.local.AppDatabase
import me.nancex.logophile.data.local.WordDao
import me.nancex.logophile.data.local.WordEntry
import me.nancex.logophile.data.remote.IcibaMean
import me.nancex.logophile.data.remote.NetworkClient
import kotlinx.coroutines.flow.Flow

class WordRepository(private val wordDao: WordDao) {

    companion object {
        private const val TAG = "WordRepository"
    }

    private val gson = Gson()

    val allWords: Flow<List<WordEntry>> = wordDao.getAllWordsFlow()

    suspend fun getWordById(id: Int): WordEntry? = wordDao.getById(id)
    suspend fun findByWordAndLanguage(word: String, language: String): WordEntry? =
        wordDao.findByWordAndLanguage(word, language)
    suspend fun insertWord(word: WordEntry): Long = wordDao.insert(word)
    suspend fun insertWords(words: List<WordEntry>) = wordDao.insertAll(words)
    suspend fun deleteWord(word: WordEntry) = wordDao.delete(word)
    suspend fun deleteWordById(id: Int) = wordDao.deleteById(id)
    suspend fun incrementPassCount(id: Int) = wordDao.incrementPassCount(id)
    suspend fun incrementTipCount(id: Int) = wordDao.incrementTipCount(id)
    suspend fun resetAllCounts() = wordDao.resetAllCounts()
    suspend fun getWordCount(): Int = wordDao.getWordCount()
    suspend fun getAllWordsList(): List<WordEntry> = wordDao.getAllWords()

    suspend fun checkpointBeforeExport(context: Context) {
        Log.d(TAG, "checkpointBeforeExport: starting WAL checkpoint")
        val db = AppDatabase.getDatabase(context)
        val supportDb = db.openHelper.writableDatabase
        val cursor = supportDb.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(TRUNCATE)"))
        if (cursor.moveToFirst()) {
            Log.d(TAG, "checkpointBeforeExport: result busys=${cursor.getInt(0)} log=${cursor.getInt(1)} ckpt=${cursor.getInt(2)}")
        }
        cursor.close()
        Log.d(TAG, "checkpointBeforeExport: completed")
    }

    suspend fun importFromFile(context: Context, filePath: String): Int {
        Log.d(TAG, "importFromFile: opening external DB at $filePath")
        val file = java.io.File(filePath)
        Log.d(TAG, "importFromFile: file exists=${file.exists()}, size=${file.length()} bytes")
        val externalDb = AppDatabase.openExternalDatabase(context, filePath)
        var importedCount = 0
        try {
            val externalWords = externalDb.wordDao().getAllWords()
            Log.d(TAG, "importFromFile: found ${externalWords.size} words in external DB")
            externalWords.forEachIndexed { i, w ->
                Log.d(TAG, "importFromFile: [$i] id=${w.id} word='${w.word}' lang=${w.language} addedTime=${w.addedTime}")
            }
            for (externalWord in externalWords) {
                val existing = wordDao.findByWordAndLanguage(externalWord.word, externalWord.language)
                if (existing == null) {
                    wordDao.insert(externalWord)
                    importedCount++
                    Log.d(TAG, "importFromFile: INSERTED '${externalWord.word}'")
                } else if (externalWord.addedTime > existing.addedTime) {
                    wordDao.deleteById(existing.id)
                    wordDao.insert(externalWord.copy(id = 0))
                    importedCount++
                    Log.d(TAG, "importFromFile: REPLACED '${externalWord.word}' (newer)")
                } else {
                    Log.d(TAG, "importFromFile: SKIPPED '${externalWord.word}' (existing newer)")
                }
            }
        } finally {
            externalDb.close()
        }
        Log.d(TAG, "importFromFile: done, total imported=$importedCount")
        return importedCount
    }

    suspend fun fetchWordDefinition(word: String): Triple<String?, String?, String?>? {
        return try {
            val icibaResponse = NetworkClient.icibaApi.getWordSuggest(word = word)
            val dictResponse = NetworkClient.dictionaryApi.getWordEntry(word)
            var definitionJson: String? = null
            if (icibaResponse.status == 1 && !icibaResponse.message.isNullOrEmpty()) {
                val msg = icibaResponse.message[0]
                if (!msg.means.isNullOrEmpty()) {
                    definitionJson = gson.toJson(msg.means)
                }
            }
            var phonetic: String? = null
            var audioUrl: String? = null
            if (dictResponse.isNotEmpty()) {
                phonetic = dictResponse[0].phonetic
                dictResponse[0].phonetics?.forEach { p ->
                    if (!p.audio.isNullOrEmpty() && audioUrl == null) audioUrl = p.audio
                    if (!p.text.isNullOrEmpty() && phonetic == null) phonetic = p.text
                }
            }
            Triple(phonetic, definitionJson, audioUrl)
        } catch (e: Exception) {
            Log.e(TAG, "fetchWordDefinition: failed for '$word': ${e.message}", e)
            null
        }
    }

    fun parseDefinitionToDisplayText(definitionJson: String?): List<Pair<String, String>> {
        if (definitionJson.isNullOrEmpty()) return emptyList()
        return try {
            val means = gson.fromJson(definitionJson, Array<IcibaMean>::class.java)
            means?.mapNotNull { mean ->
                val part = mean.part ?: return@mapNotNull null
                val text = mean.means?.joinToString("\uff1b") ?: return@mapNotNull null
                part to text
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}