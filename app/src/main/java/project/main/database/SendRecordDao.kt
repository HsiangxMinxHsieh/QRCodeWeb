package project.main.database

import androidx.lifecycle.LiveData
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

    @Query("SELECT * FROM SendRecordEntity")
    fun liveData(): LiveData<List<SendRecordEntity>>

    @get:Query("SELECT * FROM SendRecordEntity ORDER BY send_time DESC")
    val liveData: LiveData<List<SendRecordEntity>>

    @Query("SELECT * FROM SendRecordEntity WHERE send_id = :sendId ")
    fun searchByPkId(sendId: Long): SendRecordEntity

    @Query("SELECT * FROM SendRecordEntity WHERE send_id >= :idMin AND send_id <= :idMax ")
    fun searchByIdRange(idMin: Long, idMax: Long): List<SendRecordEntity>

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
fun SendRecordDao.insertNewRecord(signInTime: Long, scanContent: String, sendContent: String, settingDataItem: SettingDataItem) {
    this.insert(SendRecordEntity(sendTime = signInTime, scanContent = scanContent, sendContent = sendContent, sendSettingName = settingDataItem.name, sendSettingId = settingDataItem.id))
}

fun SendRecordDao.searchByIdRange(idRange: LongRange) = this.searchByIdRange(idRange.first.coerceAtMost(idRange.last), idRange.first.coerceAtLeast(idRange.last))