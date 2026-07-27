package me.nancex.logophile.viewmodel

import android.util.Log
import me.nancex.logophile.data.local.WordEntry
import kotlin.math.min

class MemoryEngine {

    companion object {
        private const val TAG = "MemoryEngine"

        // Probability: P = min(queueSize / QUEUE_DIVISOR, 1.0)
        private const val QUEUE_DIVISOR = 10.0

        // Distribution for picking from queue (first / second / third)
        private const val QUEUE_PICK_FIRST = 0.6
        private const val QUEUE_PICK_SECOND = 0.8  // cumulative: 0.6 + 0.2

        // Distribution when queue has only 2 items (first / second)
        private const val QUEUE_TWO_FIRST = 0.75
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

    fun getQueueIds(): List<Int> = tipQueue.map { it.id }

    fun selectNext(words: List<WordEntry>): WordEntry {
        val qSize = tipQueue.size
        val p = min(qSize / QUEUE_DIVISOR, 1.0)

        if (qSize > 0 && Math.random() < p) {
            return pickFromQueue()
        }
        return pickByDelta(words)
    }

    private fun pickFromQueue(): WordEntry {
        val roll = Math.random()
        val index = when {
            queueSize == 1 -> 0
            queueSize == 2 -> if (roll < QUEUE_TWO_FIRST) 0 else 1
            roll < QUEUE_PICK_FIRST -> 0
            roll < QUEUE_PICK_SECOND -> 1
            else -> 2
        }
        val picked = tipQueue.removeAt(index.coerceAtMost(tipQueue.lastIndex))
        Log.d(TAG, "select: QUEUE (idx=$index) -> '${picked.word}'")
        return picked
    }

    private fun pickByDelta(words: List<WordEntry>): WordEntry {
        val queueIds = tipQueue.map { it.id }.toSet()
        val candidates = words.filter { it.id !in queueIds }
        val pool = candidates.ifEmpty { words }

        val minDelta = pool.minOf { it.passCount - it.tipCount }
        val bestPool = pool.filter { it.passCount - it.tipCount == minDelta }
        val picked = bestPool.random()

        Log.d(TAG, "select: DELTA (delta=$minDelta, pool=${bestPool.size}/${pool.size}) -> '${picked.word}'")
        return picked
    }
}