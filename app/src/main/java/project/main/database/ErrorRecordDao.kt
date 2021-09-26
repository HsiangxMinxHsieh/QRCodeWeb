package project.main.database

import androidx.room.*

@Dao
interface ErrorRecordDao {
    @get:Query("SELECT * FROM ErrorRecord ORDER BY updateTime DESC")
    val allData: List<ErrorRecordEntity>

    @get:Query("SELECT count(*) FROM ErrorRecord")
    val allSize: Long

    @Query("SELECT * FROM ErrorRecord WHERE pk_id = :pk_id ORDER BY updateTime DESC")
    fun searchByPkId(pk_id: Long): ErrorRecordEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ErrorRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<ErrorRecordEntity>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entity: ErrorRecordEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(list: List<ErrorRecordEntity>)

    @Delete
    fun delete(entity: ErrorRecordEntity)

    @Delete
    fun delete(list: List<ErrorRecordEntity>)

    @Query("DELETE FROM ErrorRecord")
    fun deleteAll()

    @Query("DELETE FROM ErrorRecord WHERE pk_id = :pk_id")
    fun deleteByPkId(pk_id: Long)
}