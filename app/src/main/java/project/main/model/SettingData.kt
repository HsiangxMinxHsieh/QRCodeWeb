package project.main.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable


/***
 * 範例Json如下：
[{"SettingName":"",
"GoWebSiteByScan":{"ScanMode":1,"SendHtml":null},
"AfterScanAction":{"ActionMode":1,"ToHtml":null},
"SettingField":[{
"fieldName":"name",
"ColumnName":"entry.1199127502",
"ColumnValue":"510502"
}]

}]
 */
class SettingData : ArrayList<SettingDataItem>()

enum class SendMode {
    ByScan,         // 依照掃碼掃到什麼送什麼
    ByCustom        // 輸入自定義的網址組合設定欄位並送出
}

enum class ActionMode {
    StayApp,        //1.組完的字串當網址打出去，不離開App。
    OpenBrowser,    //2.打開瀏覽器做後續動作
    AnotherWeb      //3.打出去以後想看結果頁(多一個輸入框)
}

data class SettingDataItem(

    @SerializedName("themeColor")
    var themeColor: Int = 0, // 此設定檔的主題顏色

    @SerializedName("haveSaved")
    var haveSaved: Boolean = false, // 是否儲存過(用於不能讓使用者連續新增設定檔)(一定要為true才能新增下一個)

    @SerializedName("SettingName")
    var name: String = "", // 設定檔案名稱
    @SerializedName("GoWebSiteByScan")
    val goWebSiteByScan: GoWebSiteByScan = GoWebSiteByScan(),  // 是否依照QRcode掃到的網址去導向(否的時候能提供文字框輸入)
    // 依照掃碼掃到什麼送什麼              // sendByScan
    // 輸入自定義的網址組合設定欄位並送出    // sendByCustom

    @SerializedName("AfterScanAction")
    val afterScanAction: AfterScanAction = AfterScanAction(),  // 掃碼完成後的動作，有以下三種情境：
    //1.組完的字串當網址打出去，不離開App。  // stayApp
    //2.打開瀏覽器做後續動作               // openBrowser
    //3.打出去以後想看結果頁(多一個輸入框)   // anotherWeb
    @SerializedName("SettingField")
    val fields: ArrayList<SettingField> = arrayListOf() //設定值 (多個)
):Serializable {

    data class GoWebSiteByScan(
        @SerializedName("ScanMode")
        val scanMode: SendMode = SendMode.ByScan,
        @SerializedName("SendHtml")
        val sendHtml: String? = null
    )

    data class AfterScanAction(
        @SerializedName("ActionMode")
        val actionMode: ActionMode = ActionMode.StayApp,
        @SerializedName("ToHtml")
        val toHtml: String? = null
    )

    data class SettingField(
        @SerializedName("fieldName")
        val fieldName: String = "",
        @SerializedName("ColumnName")
        val columnKey: String = "",
        @SerializedName("ColumnValue")
        val columnValue: String = ""
    )
}