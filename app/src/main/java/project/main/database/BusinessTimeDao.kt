package project.main.database

import androidx.room.*
//import tools.getDayOfWeek
//import tools.onTheHoureToTodayMillSecond
import java.util.*

@Dao
interface BusinessTimeDao {
    @get:Query("SELECT * FROM BusinessTimeEntity")
    val allData: List<BusinessTimeEntity>

    @get:Query("SELECT count(*) FROM BusinessTimeEntity")
    val allSize: Long

    @Query("SELECT * FROM BusinessTimeEntity WHERE location_id = :locationId ")
    fun searchByPkId(locationId: Long): List<BusinessTimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: BusinessTimeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<BusinessTimeEntity>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: BusinessTimeEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(list: List<BusinessTimeEntity>)

    @Delete
    fun delete(entity: BusinessTimeEntity)

    @Delete
    fun delete(list: List<BusinessTimeEntity>)

    @Query("DELETE FROM BusinessTimeEntity")
    fun deleteAll()

    @Query("DELETE FROM BusinessTimeEntity WHERE location_id = :locationId")
    fun deleteByPkId(locationId: Long)
}

/**判斷現在是否在營業中，就兩個狀態，營業中與非營業中*/
//fun List<BusinessTimeEntity>.isNowOpen(): Boolean {
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