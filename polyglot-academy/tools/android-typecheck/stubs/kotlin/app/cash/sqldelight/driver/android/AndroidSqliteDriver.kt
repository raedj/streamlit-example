// SIGNATURE STUB for type-checking only (tools/android-typecheck). Mirrors the public API of the real library; never shipped.
package app.cash.sqldelight.driver.android
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlSchema
abstract class AndroidSqliteDriverBase
class AndroidSqliteDriver(schema: SqlSchema<QueryResult.Value<Unit>>, context: Context, name: String? = null, callback: Callback = Callback(schema)) :
    app.cash.sqldelight.db.SqlDriver by (TODO() as app.cash.sqldelight.db.SqlDriver) {
    open class Callback(schema: SqlSchema<QueryResult.Value<Unit>>) { open fun onOpen(db: SupportSQLiteDatabase) {} }
}
