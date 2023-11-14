package project.main.model

import android.content.Context
import com.buddha.qrcodeweb.R
import com.google.gson.annotations.SerializedName
import tool.getShare
import java.io.Serializable


/***
 *
 *  ※※※ 範例Json如下：
[{
"AfterScanAction": {
"ActionMode": 1,
"ToHtml": "https://"
},
"SettingField": [
{
"ColumnName": "entry.1823314342",
"ColumnValue": "1234",
"fieldName": "檢核密碼",
"fieldType": 2
},
{
"ColumnName": "entry.1486848017",
"ColumnValue": "",
"fieldName": "檢核ID",
"fieldType": 1
},
{
"ColumnName": "entry.186778128",
"ColumnValue": "",
"fieldName": "檢核名稱",
"fieldType": 1
}
],
"GoWebSiteByScan": {
"ScanMode": 1,
"SendHtml": "https://"
},
"haveSaved": true,
"id": 0,
"SettingName": "預設設定檔",
"themeColor": 0
}]

 * ※※※掃描到，要處理的設定檔範例如下：
QRCodeSighIn#{
"AfterScanAction": {
"ActionMode": 1,
"ToHtml": "https://"
},
"SettingField": [
{
"ColumnName": "entry.1823314342",
"ColumnValue": "1234",
"fieldName": "檢核密碼",
"fieldType": 2
},
{
"ColumnName": "entry.1486848017",
"ColumnValue": "",
"fieldName": "檢核ID",
"fieldType": 1
},
{
"ColumnName": "entry.186778128",
"ColumnValue": "",
"fieldName": "檢核名稱",
"fieldType": 1
}
],
"GoWebSiteByScan": {
"ScanMode": 1,
"SendHtml": "https://"
},
"haveSaved": true,
"id": 0,
"SettingName": "預設設定檔",
"themeColor": 0
}
 */
class SettingData() : ArrayList<SettingDataItem>() {
    constructor(list: List<SettingDataItem>) : this()

    fun isNew(item: SettingDataItem) = this.none { it.name == item.name }
}

enum class SendMode(val value: Int) {
    ByScan(1),         // 1.依照掃碼掃到什麼送什麼
    ByCustom(2)        // 2.輸入自定義的網址組合設定欄位並送出
}

enum class ActionMode(val value: Int) {
    StayApp(1),        //1.組完的字串當網址打出去，不離開App。
    OpenBrowser(2),    //2.打開瀏覽器做後續動作
    AnotherWeb(3)      //3.打出去以後想看結果頁(多一個輸入框)
}

enum class FieldType(val value: Int) {
    KeyColumn(1),      //1.掃碼時會填入的欄位，不能刪除，沒有value值(為null)。
    CanNotBeDelete(2), //2.可由使用者自行編輯的欄位，但不可刪除，有value值。
    AddColumn(3)       //3.由使用者自行新增欄位，可刪除，有value值。
}


data class SettingDataItem(

    @SerializedName("id")
    var id: Int = 0, // 此設定檔的ID，會儲存在sharedPreference，只會加不會減

    @SerializedName("themeColor")
    var sortIndex: Int = 0, // 此設定檔的主題顏色

    @SerializedName("haveSaved")
    var haveSaved: Boolean = false, // 是否儲存過(用於不能讓使用者連續新增設定檔)(一定要為true才能新增下一個)

    @SerializedName("SettingName")
    var name: String = "", // 設定檔案名稱

    @SerializedName("GoWebSiteByScan")
    var goWebSiteByScan: GoWebSiteByScan = GoWebSiteByScan(),  // 是否依照QRcode掃到的網址去導向(否的時候能提供文字框輸入)
    // 依照掃碼掃到什麼送什麼              // sendByScan
    // 輸入自定義的網址組合設定欄位並送出    // sendByCustom

    @SerializedName("AfterScanAction")
    val afterScanAction: AfterScanAction = AfterScanAction(),  // 掃碼完成後的動作，有以下三種情境：
    //1.組完的字串當網址打出去，不離開App。  // stayApp
    //2.打開瀏覽器做後續動作               // openBrowser
    //3.打出去以後想看結果頁(多一個輸入框)   // anotherWeb
    @SerializedName("SettingField")
    val fields: ArrayList<SettingField> = arrayListOf() //設定值 (多個)
) : Serializable {

    fun getFieldsKeyByName(name: String) = this.fields.firstOrNull { it.fieldName == name }?.columnKey
//if (id == 0) else
    /**依照ID取得預設的設定檔*/
    companion object {
        fun getFirstDefaultSetting(context: Context) = SettingDataItem(id = 0, name = context.getString(R.string.splash_setting_default_name), haveSaved = true).apply {
            fields.add(
                SettingField(
                    fieldType = FieldType.CanNotBeDelete.value,
                    fieldName = context.getString(R.string.setting_password_title_default),
                    columnKey = context.getShare().getKeyPassword(),
                    columnValue = context.getString(R.string.splash_setting_default_password)
                )
            )
            addIDandNameKeyItem(context)
        }

        fun getDefaultSetting(id: Int, context: Context) = SettingDataItem(id = id, name = (context.getString(R.string.setting_file_name_default))).apply {
            fields.add(SettingField(fieldType = FieldType.CanNotBeDelete.value, fieldName = context.getString(R.string.setting_password_title_default), columnKey = context.getShare().getKeyPassword()))
            addIDandNameKeyItem(context)
        }

        private fun SettingDataItem.addIDandNameKeyItem(context: Context) {
            fields.add(SettingField(fieldType = FieldType.KeyColumn.value, fieldName = context.getString(R.string.setting_id_title_default), columnKey = context.getShare().getKeyID(), columnValue = null))
            fields.add(SettingField(fieldType = FieldType.KeyColumn.value, fieldName = context.getString(R.string.setting_name_title_default), columnKey = context.getShare().getKeyName(), columnValue = null))
        }
    }

    data class GoWebSiteByScan(
        @SerializedName("ScanMode")
        var scanMode: Int = SendMode.ByScan.value,
        @SerializedName("SendHtml")
        var sendHtml: String = "https://"
    ) : Serializable{
        // 判斷是否非法 // 只有當動作模式是openBrowser && toHtml 網址錯誤才會回傳true
        fun illegal() = scanMode == SendMode.ByCustom.value && !sendHtml.startsWith("https://docs.google.com/forms")
    }

    data class AfterScanAction(
        @SerializedName("ActionMode")
        var actionMode: Int = ActionMode.StayApp.value,
        @SerializedName("ToHtml")
        var toHtml: String = "https://"
    ) : Serializable {
        // 判斷是否非法 // 只有當動作模式是 AnotherWeb && toHtml 網址錯誤才會回傳true
        fun illegal() = actionMode == ActionMode.AnotherWeb.value && !toHtml.startsWith("https://")
    }

    data class SettingField(
        @SerializedName("fieldType")
        val fieldType: Int = FieldType.AddColumn.value,
        @SerializedName("fieldName")
        val fieldName: String = "",
        @SerializedName("ColumnName")
        val columnKey: String = "",
        @SerializedName("ColumnValue")
        var columnValue: String? = ""
    ) : Serializable
}