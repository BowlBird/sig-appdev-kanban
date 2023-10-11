package com.github.bowlbird.kanban

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase


@Entity(primaryKeys= [ "list_id", "text" ] )
data class DaoEntry(
    @ColumnInfo(name = "list_id") val listId: Int,
    val text: String,
)

@Dao
interface EntryDao {
    @Query("SELECT * FROM DaoEntry WHERE list_id = :listId")
    fun getEntriesInList(listId: Int): List<DaoEntry>?

    @Insert
    fun createEntry(entry: DaoEntry)

    @Delete
    fun deleteEntry(entry: DaoEntry)
}

@Database(entities = [DaoEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
}
