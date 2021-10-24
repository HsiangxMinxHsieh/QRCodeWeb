package project.main.database

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.buddha.qrcodeweb.R
import project.main.const.constantName
import project.main.model.SettingDataItem
import tool.getShare
import tool.getUrlKey
import utils.logi
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

    /**取得這筆資料的簽到人員名稱  三段式尋找，1.這筆簽到記錄的設定檔中的KEY 2.儲存的KEY 3.預設的KEY 都取不到則為null*/
    fun getSignInPerson(context: Context): String {
        return this.scanContent.getSignInPersonByScan(context, this.sendSettingId)
    }
}

fun String.getSignInPersonByScan(context: Context, settingId: Int = context.getShare().getNowUseSetting()?.id ?: 0) =
    this.getUrlKey(context.getShare().getSettingById(settingId)?.fields?.filter { it.fieldName == context.getString(R.string.setting_name_title_default) }?.getOrNull(0)?.columnKey ?: "null")
        ?: this.getUrlKey(context.getShare().getKeyName())
        ?: this.getUrlKey(constantName)
        ?: this.findSignInPersonByScanInAllSetting(context)
        ?: "null"

/**藉由掃描到的字串，使用所有設定檔中的名稱Key來使用，以確定是否是null。*/
fun String.findSignInPersonByScanInAllSetting(context: Context): String? {
    context.getShare().getStoreSettings().forEach {
        val result = this.getUrlKey(it.fields.find { it.fieldName == context.getString(R.string.setting_name_title_default) }?.columnKey ?: "null")
        if (result != null)
            return result
    }
    return null
}