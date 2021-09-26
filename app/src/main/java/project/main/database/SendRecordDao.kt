package project.main.database

import androidx.room.*
import project.main.model.SettingDataItem
import java.util.*

//import tools.getDayOfWeek
//import tools.onTheHoureToTodayMillSecond

@Dao
interface SendRecordDao {
    @get:Query("SELECT * FROM SendRecordEntity")
    val allData: List<SendRecordEntity>

    @get:Query("SELECT count(*) FROM SendRecordEntity")
    val allSize: Long

    @Query("SELECT * FROM SendRecordEntity WHERE send_id = :sendId ")
    fun searchByPkId(sendId: Long): List<SendRecordEntity>

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

    @Query("DELETE FROM SendRecordEntity WHERE send_id = :sendId")
    fun deleteByPkId(sendId: Long)
}

//
fun SendRecordDao.insertNewRecord(scanContent: String, sendContent: String, settingDataItem: SettingDataItem) {
    this.insert(SendRecordEntity(sendTime = Date().time, scanContent = scanContent, sendContent = sendContent, sendSettingName = settingDataItem.name, sendSettingId = settingDataItem.id))
}

