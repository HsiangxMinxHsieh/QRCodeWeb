package project.main.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

fun Context.getBase() = AppDatabase.getDatabase(this)

fun Context.getRecordDao() = this.getBase().sendRecordDao

@Database(
    version = 1,
    exportSchema = false,
    entities = [
        ErrorRecordEntity::class, SendRecordEntity::class
    ]
)

abstract class AppDatabase : RoomDatabase() {

    abstract val errorRecordDao: ErrorRecordDao
    abstract val sendRecordDao: SendRecordDao

    companion object {

        private var sInstance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (sInstance == null) {
                synchronized(AppDatabase::class) {
                    sInstance = Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "room.db"
                        )
                        .addMigrations(MIGRATION_1_2)
                        .fallbackToDestructiveMigration()
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                //第一次建立DB
                                super.onCreate(db)
                            }

                            override fun onOpen(db: SupportSQLiteDatabase) {
                                //DB被開啟
                                super.onOpen(db)
                            }
                        })
                        .allowMainThreadQueries()//TODO 讓DB在主執行續執行 不建議這樣做
                        .build()
                }
            }
            return sInstance!!
        }

        /**DB 版本號升級 */
        internal val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //DB 版本號升級
            }
        }

        /**DB 版本號升級 */
        internal val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //DB 版本號升級
//                val map = mutableMapOf<String, String>()
//                map["record_id"] = "INTEGER"
//                map["user_id"] = "INTEGER"
//                map["department_id"] = "INTEGER"
//                map["started_work_at"] = "INTEGER"
//                map["finished_work_at"] = "INTEGER"
//                map["started_work_formate"] = "TEXT"
//                map["finished_work_formate"] = "TEXT"
//                map["date"] = "TEXT"
//                val col = mutableListOf<String>()
//                val type = mutableListOf<String>()
//                for (entry in map) {
//                    col.add(entry.key)
//                    type.add(entry.value)
//                }
//                createTable(
//                    database,
//                    "UserRecord",
//                    col.toTypedArray(),
//                    type.toTypedArray(),
//                    arrayOf("record_id")
//                )
            }
        }

        /** 新增欄位 */
        private fun addColumn(
            database: SupportSQLiteDatabase,
            table: String,
            col: String,
            type: String = "TEXT"
        ) {
            database.execSQL("ALTER TABLE $table ADD COLUMN $col $type;")
        }

        /** 刪除欄位 */
        protected fun deleteColumn(database: SupportSQLiteDatabase, table: String, col: String) {
            database.execSQL("ALTER TABLE $table DROP COLUMN $col;")
        }

        /**
         * 新增 Table
         * 產生sql字串如下
         * CREATE TABLE IF NOT EXISTS ShoeMinuteRecord (
         *     date INTEGER NOT NULL DEFAULT 0,
         *     mac TEXT NOT NULL,
         *     steps TEXT NOT NULL,
         *     updateTime INTEGER NOT NULL DEFAULT 0,
         *     apiUpdateTime INTEGER NOT NULL DEFAULT 0,
         *     PRIMARY KEY (date, mac)
         * )
         * */
        private fun createTable(
            database: SupportSQLiteDatabase,
            table: String,
            col: Array<String>,
            type: Array<String>,
            pk: Array<String>
        ) {
            var sql = ""
            sql += "CREATE TABLE IF NOT EXISTS $table ("
            for (i in col.indices) {
                sql += when {
                    type[i] == "INTEGER" -> "${col[i]} INTEGER NOT NULL DEFAULT 0"
                    type[i] == "REAL" -> "${col[i]} REAL NOT NULL DEFAULT 0"
                    type[i] == "TEXT" -> "${col[i]} TEXT NOT NULL"
                    else -> ""
                }
                if (i < col.size - 1) {
                    sql += ","
                }
            }
            if (pk.isNotEmpty()) {
                sql += ",PRIMARY KEY ("
                for (i in pk.indices) {
                    sql += pk[i]
                    if (i < pk.size - 1) {
                        sql += ","
                    }
                }
                sql += ")"
            }
            sql += ")"
//            loge("TAG", "sql = $sql")
            database.execSQL(sql)
        }
    }
}
