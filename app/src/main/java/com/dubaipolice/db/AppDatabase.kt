package com.dubaipolice.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dubaipolice.db.dao.GroupInfoTableDao
import com.dubaipolice.db.dao.MemberInfoTableDao
import com.dubaipolice.db.dao.MessageInfoTableDao
import com.dubaipolice.db.model.GroupInfoTable
import com.dubaipolice.db.model.MemberInfoTable
import com.dubaipolice.db.model.MessageInfoTable

@Database(entities = [MessageInfoTable::class, GroupInfoTable::class, MemberInfoTable::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageInfoTableDao(): MessageInfoTableDao?
    abstract fun groupInfoTableDao(): GroupInfoTableDao?
    abstract fun memberInfoTableDao(): MemberInfoTableDao?

    companion object {
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "dubai-police-db"
        fun getAppDatabase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, DB_NAME
                ) // allow queries on the main thread.
                    // Don't do this on a real app! See PersistenceBasicSample for an example.
                    .allowMainThreadQueries()
                    .build()
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}