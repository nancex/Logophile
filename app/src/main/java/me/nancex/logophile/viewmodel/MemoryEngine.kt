package me.nancex.logophile.viewmodel

import android.util.Log
import me.nancex.logophile.data.local.WordEntry
import kotlin.math.min

class MemoryEngine {

    companion object {
        private const val TAG = "MemoryEngine"

        // P = min(queueSize / QUEUE_DIVISOR, 1.0)
        private const val QUEUE_DIVISOR = 10.0

        // Queue pick distribution (first / second / third)
        private const val QUEUE_PICK_FIRST = 0.6
        private const val QUEUE_PICK_SECOND = 0.8

        // Distribution when queue has only 2 items
        private const val QUEUE_TWO_FIRST = 0.75

        // P2 = min((delta - 1) * REJECTION_BASE, REJECTION_MAX)
        private const val REJECTION_BASE = 0.25
        private const val REJECTION_MAX = 0.75

        // Recent-queue size for deduplication
        private const val RECENT_QUEUE_SIZE = 5

        // Safety limit for dedup retries
        private const val MAX_DEDUP_ATTEMPTS = 50
    }

    private val tipQueue = mutableListOf<WordEntry>()
    private val recentQueue = mutableListOf<WordEntry>()

    val queueSize: Int get() = tipQueue.size

    fun enqueueIfTipShown(word: WordEntry, tipWasShown: Boolean) {
        if (tipWasShown && tipQueue.none { it.id == word.id }) {
            tipQueue.add(word)
            Log.d(TAG, "enqueue: + '${word.word}' (size=$queueSize)")
        }
    }

    fun removeWord(wordId: Int) {
        tipQueue.removeAll { it.id == wordId }
        recentQueue.removeAll { it.id == wordId }
    }

    fun clearQueue() {
        tipQueue.clear()
        recentQueue.clear()
    }

    fun getQueueIds(): List<Int> = tipQueue.map { it.id }

    // ── Main algorithm ─────────────────────────────────────────────

    fun selectNext(words: List<WordEntry>): WordEntry {
        val qSize = tipQueue.size
        val p = min(qSize / QUEUE_DIVISOR, 1.0)

        if (qSize > 0 && Math.random() < p) {
            return pickFromQueue()
        }

        val queueIds = tipQueue.map { it.id }.toSet()
        val nonQueue = words.filter { it.id !in queueIds }
        val pool = nonQueue.ifEmpty { words }

        return pickFromNonQueue(pool)
    }

    // ── Step 3: pick from tip-queue ────────────────────────────────

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

    // ── Steps 4-6: pick from non-queue pool ────────────────────────

    private fun pickFromNonQueue(pool: List<WordEntry>): WordEntry {
        // Precompute zero-delta words once (O(n), not repeated in the loop)
        val zeroDelta = pool.filter { (it.passCount - it.tipCount) == 0 }

        var attempts = 0
        while (attempts < MAX_DEDUP_ATTEMPTS) {
            attempts++
            val candidate = if (zeroDelta.isNotEmpty()) zeroDelta.random() else pickByRejection(pool)

            if (recentQueue.none { it.id == candidate.id }) {
                recentQueue.add(candidate)
                if (recentQueue.size > RECENT_QUEUE_SIZE) {
                    recentQueue.removeAt(0)
                }
                Log.d(TAG, "select: NON-QUEUE (attempts=$attempts) -> '${candidate.word}' (recent=${recentQueue.map { it.word }})")
                return candidate
            }
            Log.d(TAG, "select: skip '${candidate.word}' (already in recent queue)")
        }

        // Safety fallback: all words are in recentQueue → pick any
        val fallback = pool.random()
        Log.w(TAG, "select: FALLBACK after $attempts attempts -> '${fallback.word}'")
        return fallback
    }

    // ── Step 5: rejection sampling by delta ────────────────────────

    private fun pickByRejection(pool: List<WordEntry>): WordEntry {
        while (true) {
            val word = pool.random()
            val delta = word.passCount - word.tipCount
            if (delta <= 1) return word
            val p2 = min((delta - 1) * REJECTION_BASE, REJECTION_MAX)
            if (Math.random() >= p2) return word
        }
    }
}
