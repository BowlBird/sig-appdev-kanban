package com.github.bowlbird.kanban

enum class ListType {
    Todo,
    Doing,
    Done,
}

class AppRepository(
    private val appDataSource: AppDataSource
) {
    suspend fun createEntry(list: ListType, entry: Entry) {
        appDataSource.createEntry(list.ordinal, entry.text)
    }

    suspend fun deleteEntry(list: ListType, entry: Entry) {
        appDataSource.deleteEntry(list.ordinal, entry)
    }

    suspend fun getList(list: ListType): List<Entry> {
        return appDataSource.getListById(list.ordinal)
    }

}