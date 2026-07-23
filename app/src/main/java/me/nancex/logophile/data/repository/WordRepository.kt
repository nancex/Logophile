package me.nancex.logophile.data.repository

import com.google.gson.Gson
import me.nancex.logophile.data.local.WordDao
import me.nancex.logophile.data.local.WordEntry
import me.nancex.logophile.data.remote.IcibaMean
import me.nancex.logophile.data.remote.NetworkClient
import kotlinx.coroutines.flow.Flow

class WordRepository(private val wordDao: WordDao) {

    private val gson = Gson()

    val allWords: Flow<List<WordEntry>> = wordDao.getAllWordsFlow()

    fun searchWords(query: String): Flow<List<WordEntry>> {
        return if (query.isBlank()) {
            wordDao.getAllWordsFlow()
        } else {
            wordDao.searchWords(query)
        }
    }

    fun searchWordsSorted(query: String, alphaOrder: Boolean): Flow<List<WordEntry>> {
        return if (query.isBlank()) {
            wordDao.getAllWordsFlow()
        } else if (alphaOrder) {
            wordDao.searchWordsAlpha(query)
        } else {
            wordDao.searchWordsDate(query)
        }
    }

    suspend fun getWordById(id: Int): WordEntry? = wordDao.getById(id)

    suspend fun findByWordAndLanguage(word: String, language: String): WordEntry? =
        wordDao.findByWordAndLanguage(word, language)

    suspend fun insertWord(word: WordEntry): Long = wordDao.insert(word)

    suspend fun insertWords(words: List<WordEntry>) = wordDao.insertAll(words)

    suspend fun deleteWord(word: WordEntry) = wordDao.delete(word)

    suspend fun deleteWordById(id: Int) = wordDao.deleteById(id)

    suspend fun incrementPassCount(id: Int) = wordDao.incrementPassCount(id)

    suspend fun incrementTipCount(id: Int) = wordDao.incrementTipCount(id)

    suspend fun getWordCount(): Int = wordDao.getWordCount()

    suspend fun getAllWordsList(): List<WordEntry> = wordDao.getAllWords()

    suspend fun fetchWordDefinition(word: String): Triple<String?, String?, String?>? {
        return try {
            val icibaDeferred = NetworkClient.icibaApi.getWordSuggest(word = word)
            val dictDeferred = NetworkClient.dictionaryApi.getWordEntry(word)

            val icibaResponse = icibaDeferred
            val dictResponse = dictDeferred

            var definitionJson: String? = null
            var paraphrase: String? = null

            if (icibaResponse.status == 1 && !icibaResponse.message.isNullOrEmpty()) {
                val msg = icibaResponse.message[0]
                val means = msg.means
                if (!means.isNullOrEmpty()) {
                    definitionJson = gson.toJson(means)
                    paraphrase = means.joinToString("；") { mean ->
                        "${mean.part ?: ""} ${mean.means?.joinToString("，") ?: ""}"
                    }
                }
                if (paraphrase.isNullOrEmpty() && !msg.paraphrase.isNullOrEmpty()) {
                    paraphrase = msg.paraphrase
                }
            }

            var phonetic: String? = null
            var audioUrl: String? = null

            if (dictResponse.isNotEmpty()) {
                phonetic = dictResponse[0].phonetic
                val phonetics = dictResponse[0].phonetics
                if (!phonetics.isNullOrEmpty()) {
                    for (p in phonetics) {
                        if (!p.audio.isNullOrEmpty()) {
                            audioUrl = p.audio
                            break
                        }
                    }
                }
                if (phonetic.isNullOrEmpty() && !phonetics.isNullOrEmpty()) {
                    for (p in phonetics) {
                        if (!p.text.isNullOrEmpty()) {
                            phonetic = p.text
                            break
                        }
                    }
                }
            }

            Triple(phonetic, definitionJson, audioUrl)
        } catch (e: Exception) {
            null
        }
    }

    fun parseDefinitionToDisplayText(definitionJson: String?): List<Pair<String, String>> {
        if (definitionJson.isNullOrEmpty()) return emptyList()

        return try {
            val means = gson.fromJson(definitionJson, Array<IcibaMean>::class.java)
            means?.mapNotNull { mean ->
                val part = mean.part ?: return@mapNotNull null
                val text = mean.means?.joinToString("；") ?: return@mapNotNull null
                part to text
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
