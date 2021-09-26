package project.main.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tool.constantName
import tool.getUrlKey
import utils.toDate
import utils.toString


@Entity(tableName = "SendRecordEntity")
data class SendRecordEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "send_id")
    var sendId: Long = 0L,
    @ColumnInfo(name = "send_time")
    var sendTime: Long = 0L,
    @ColumnInfo(name = "scan_content")
    var scanContent: String = "",         // 掃描到的內容
    @ColumnInfo(name = "send_content")
    var sendContent: String = "",         // 送出的內容
    @ColumnInfo(name = "send_setting")
    var sendSettingName: String = "",      // 送出時使用的設定檔(不使用ID是因為不知道使用者是否會把該設定檔刪除或改名)
    @ColumnInfo(name = "send_setting_id")
    var sendSettingId: Int = 0             // 送出時使用的設定檔ID(邏輯是先找ID，如果找不到這個ID再使用儲存的名稱顯示)
) {
    /**取得完整資訊用於顯示*/
    fun toFullInfo(sendTimeKey: String, scanContentKey: String, sendContentKey: String, sendSettingKey: String, sendSettingName: String) = "${sendTimeKey}:${sendTime.toString("yyyy/MM/dd HH:mm:ss")}\n" +
            "${scanContentKey}:$scanContent\n" +
            "${sendContentKey}:$sendContent\n" +
            "${sendSettingKey}:$sendSettingName"

    /**取得這筆資料的簽到人員名稱*/
    fun getSignInPerson() = this.scanContent.getUrlKey(constantName)
}