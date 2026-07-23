package me.nancex.logophile

import android.app.Application
import me.nancex.logophile.data.local.AppDatabase
import me.nancex.logophile.data.repository.WordRepository

class LogophileApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: WordRepository by lazy { WordRepository(database.wordDao()) }
}
