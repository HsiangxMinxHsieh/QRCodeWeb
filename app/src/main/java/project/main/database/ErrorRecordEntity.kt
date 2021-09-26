package project.main.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ErrorRecord")
data class ErrorRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pk_id")
    var pk_id: Long,
    @ColumnInfo(name = "updateTime")
    var updateTime: Long = 0,
    @ColumnInfo(name = "versionName")
    var versionName: String? = null,
    @ColumnInfo(name = "versionCode")
    var versionCode: String? = null,
    @ColumnInfo(name = "message")
    var message: String? = null
){
    constructor() : this(0)
}