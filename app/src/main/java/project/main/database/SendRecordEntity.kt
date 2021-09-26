package project.main.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "SendRecordEntity")
data class SendRecordEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "raw_id")
    var rawId: Long = 0L,
    @ColumnInfo(name = "location_id")
    var locationId: Long = 0L,
    @ColumnInfo(name = "week")
    var week: Int = -1,
    @ColumnInfo(name = "open")
    var open: String = "",          //09:30
    @ColumnInfo(name = "close")
    var close: String = ""          //17:30
) {
    //供非營利時間使用的建構子。
    constructor(week: Int) : this(0L, 0L, week, "non-business days", "non-business days")

}