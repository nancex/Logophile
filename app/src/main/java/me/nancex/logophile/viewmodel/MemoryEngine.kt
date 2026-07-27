package me.nancex.logophile.viewmodel

import android.util.Log
import me.nancex.logophile.data.local.WordEntry
import kotlin.math.min

class MemoryEngine {

    companion object {
        private const val TAG = "MemoryEngine"
    }

    private val tipQueue = mutableListOf<WordEntry>()
    val queueSize: Int get() = tipQueue.size

    fun enqueueIfTipShown(word: WordEntry, tipWasShown: Boolean) {
        if (tipWasShown && tipQueue.none { it.id == word.id }) {
            tipQueue.add(word)
            Log.d(TAG, "enqueue: + '${word.word}' (size=$queueSize)")
        }
    }

    fun removeWord(wordId: Int) {
        tipQueue.removeAll { it.id == wordId }
    }

    fun clearQueue() {
        tipQueue.clear()
    }

    fun selectNext(words: List<WordEntry>): WordEntry {
        val queueSize = tipQueue.size
        val p = min(queueSize / 10.0, 1.0)

        if (queueSize > 0 && Math.random() < p) {
            return pickFromQueue()
        }
        return pickByDelta(words)
    }

    private fun pickFromQueue(): WordEntry {
        val roll = Math.random()
        val index = when {
            queueSize == 1 -> 0
            queueSize == 2 -> if (roll < 0.75) 0 else 1
            roll < 0.6 -> 0
            roll < 0.8 -> 1
            else -> 2
        }
        val picked = tipQueue.removeAt(index.coerceAtMost(tipQueue.lastIndex))
        Log.d(TAG, "select: QUEUE (p idx=$index) → '${picked.word}'")
        return picked
    }

    private fun pickByDelta(words: List<WordEntry>): WordEntry {
        val queueIds = tipQueue.map { it.id }.toSet()
        val candidates = words.filter { it.id !in queueIds }
        val pool = candidates.ifEmpty { words }

        val minDelta = pool.minOf { it.passCount - it.tipCount }
        val bestPool = pool.filter { it.passCount - it.tipCount == minDelta }
        val picked = bestPool.random()

        Log.d(TAG, "select: DELTA (delta=$minDelta, pool=${bestPool.size}/${pool.size}) → '${picked.word}'")
        return picked
    }
}