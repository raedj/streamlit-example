package com.polyglot.academy.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.polyglot.academy.data.db.PolyglotDatabase

const val DATABASE_NAME = "polyglot.db"

/** Android SQLite driver with foreign keys on; SQLDelight runs migrations from user_version. */
fun createAndroidDriver(context: Context): SqlDriver = AndroidSqliteDriver(
    schema = PolyglotDatabase.Schema,
    context = context,
    name = DATABASE_NAME,
    callback = object : AndroidSqliteDriver.Callback(PolyglotDatabase.Schema) {
        override fun onOpen(db: SupportSQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
    },
)
