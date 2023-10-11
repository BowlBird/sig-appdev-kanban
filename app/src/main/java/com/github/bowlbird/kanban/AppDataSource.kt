package com.github.bowlbird.kanban

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Entry(val text: String)

class AppDataSource(applicationContext: Context) {
    private val dao = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "AppDatabase"
    ).build().entryDao()

    suspend fun getListById(id: Int): List<Entry> {
        return withContext(Dispatchers.IO) {
            dao.getEntriesInList(id)?.map { Entry(it.text) } ?: listOf()
        }
    }

    suspend fun createEntry(listId: Int, text: String) {
        withContext(Dispatchers.IO) {
            dao.createEntry(DaoEntry(listId, text))
        }
    }

    suspend fun deleteEntry(listId: Int, entry: Entry) {
        withContext(Dispatchers.IO) {
            dao.deleteEntry(DaoEntry(listId, entry.text))
        }
    }
}