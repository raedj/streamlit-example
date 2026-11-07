package com.polyglot.academy.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.polyglot.academy.data.db.PolyglotDatabase
import java.util.Properties

/** JVM driver used by the desktop verification harness and tests. Creates or migrates the schema via user_version. */
fun createJvmDriver(url: String = JdbcSqliteDriver.IN_MEMORY): SqlDriver =
    JdbcSqliteDriver(url, Properties().apply { put("foreign_keys", "true") }, PolyglotDatabase.Schema)
