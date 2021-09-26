package project.main.database

import androidx.room.*
//import tools.getDayOfWeek
//import tools.onTheHoureToTodayMillSecond

@Dao
interface SendRecordDao {
    @get:Query("SELECT * FROM SendRecordEntity")
    val allData: List<SendRecordEntity>

    @get:Query("SELECT count(*) FROM SendRecordEntity")
    val allSize: Long

    @Query("SELECT * FROM SendRecordEntity WHERE location_id = :locationId ")
    fun searchByPkId(locationId: Long): List<SendRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: SendRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<SendRecordEntity>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: SendRecordEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(list: List<SendRecordEntity>)

    @Delete
    fun delete(entity: SendRecordEntity)

    @Delete
    fun delete(list: List<SendRecordEntity>)

    @Query("DELETE FROM SendRecordEntity")
    fun deleteAll()

    @Query("DELETE FROM SendRecordEntity WHERE location_id = :locationId")
    fun deleteByPkId(locationId: Long)
}

/**判斷現在是否在營業中，就兩個狀態，營業中與非營業中*/
//fun List<SendRecordEntity>.isNowOpen(): Boolean {
//
//    val today = Date().getDayOfWeek()
//    // 先判斷今天的日期是否在List中
//    if (this.none { it.week == today }) {
//        return false
//    }
//
//    // 有再檢查是否是在時間中
//    val subListInDay = this.filter { it.week == today }
//    var isOpen = false
//    for (entity in subListInDay) {
//        val open = entity.open.onTheHoureToTodayMillSecond()
//        val close = entity.close.onTheHoureToTodayMillSecond()
//        if (Date().time in open until close) {
//            isOpen = true
//            break
//        }
//    }
//
//    return isOpen
//}