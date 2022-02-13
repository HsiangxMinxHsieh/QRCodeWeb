package utils

import android.content.Context
import android.os.Build
import android.provider.Settings.System.DATE_FORMAT
import android.util.Log
import com.buddha.qrcodeweb.BuildConfig
import com.buddha.qrcodeweb.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import project.main.model.SendMode
import tool.getShare
import tool.getUrlKey
import java.io.*
import java.text.DecimalFormat
import java.text.Format
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


/**導向網址+設定檔欄位結合的功用方法*/
fun String.concatSettingColumn(context: Context): String {
    val settingDataItem = context.getShare().getNowUseSetting()!!
//    logi("concatSettingColumn", "settingDataItem.field內容是=>${settingDataItem.fields}")
    val fieldsStr = "&" + settingDataItem.fields.filterNot { it.columnValue.isNullOrEmpty() }.map { field -> "${field.columnKey}=${field.columnValue}" }.toString().replace(", ", "&").replace("[", "").replace("]", "")
    return if (settingDataItem.goWebSiteByScan.scanMode == SendMode.ByScan.value)
        this + fieldsStr
    else { // 這裡要把設定檔中的field重新組合，依照ID、Name、Password的欄位重組

        settingDataItem.let {
            return it.goWebSiteByScan.sendHtml + "?" +
                    "&" + it.getFieldsKeyByName(context.getString(R.string.setting_id_title_default)) + "=" + this.getUrlKey(1) +
                    "&" + it.getFieldsKeyByName(context.getString(R.string.setting_name_title_default)) + "=" + this.getUrlKey(2) + fieldsStr
        }
    }

}

fun Double.format(format: String = "#.#"): String {
    return DecimalFormat(format).format(this)
}


fun getRaw(context: Context, id: Int): String {
    val stream: InputStream = context.resources.openRawResource(id)
    return read(stream)
}

fun read(stream: InputStream?): String {
    return read(stream, "utf-8")
}

fun read(`is`: InputStream?, encode: String?): String {
    if (`is` != null) {
        try {
            val reader = BufferedReader(InputStreamReader(`is`, encode))
            val sb = java.lang.StringBuilder()
            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                sb.append(
                    """
                            $line
                            
                            """.trimIndent()
                )
            }
            `is`.close()
            return sb.toString()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return ""
}

fun Date.todaylbl(): Int {
    val sdf: Format = SimpleDateFormat(DATE_FORMAT)
    return sdf.format(this).toInt()
}

fun Date.tomorrowlbl(): Int {
    val calendar = GregorianCalendar()
    calendar.time = this
    calendar.add(Calendar.DATE, 1)
    val sdf: Format = SimpleDateFormat(DATE_FORMAT)
    return sdf.format(calendar.time).toInt()
}

fun Date.thisHrlbl(): Int {
    val sdf: Format = SimpleDateFormat("hh")
    return sdf.format(this).toInt()
}

fun Date.thisMinOfThisHrlbl(): Int {
    val sdf: Format = SimpleDateFormat("mm")
    return sdf.format(this).toInt()
}

fun Collection<Any>.logAllData(TAG: String = "logAllData") {
    this.forEach {
        logi(TAG, it.toString())
    }

}


fun logi(tag: String, log: Any) {

    if (BuildConfig.DEBUG_MODE) Log.i(tag, log.toString())
    if (BuildConfig.LOG2FILE) {
        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now().toString()
        } else {
            "TIME"
        }
//        appendLog("$current : $tag : $log ")
    }
}


fun loge(tag: String, log: Any, tr: Throwable? = null) {

    if (BuildConfig.DEBUG_MODE && tr != null) Log.e(tag, log.toString(), tr)
    else if (BuildConfig.DEBUG_MODE) Log.e(tag, log.toString(), tr)

    if (BuildConfig.LOG2FILE) {
        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now().toString()
        } else {
            "TIME"
        }
//        appendLog("$current : $tag : $log ")
    }
}

//fun appendLog(text: String) {
//    val directory = App.instance.ctx().externalCacheDir
//    val logFile = File(directory, "loge.file")
////    Log.e("AAAA", " path is $logFile")
//    if (!logFile.exists()) {
//        try {
//            logFile.createNewFile()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//    }
//    try {
//        //BufferedWriter for performance, true to set append to file flag
//        val buf = BufferedWriter(FileWriter(logFile, true))
//        buf.append(text)
//        buf.newLine()
//        buf.close()
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
//
//}

//private fun delay(h: Handler, sec: Float, lambda: () -> Unit) {
//    h.postDelayed({ lambda() }, (sec * 1000).toLong())
//}

fun parseScanRecord(scanRecord: ByteArray): Map<Int, ByteArray> {
    val dict = mutableMapOf<Int, ByteArray>()
    val rawData: ByteArray?
    var index = 0
    while (index < scanRecord.size) {
        val length = scanRecord[index++].toInt()
        //if no record
        if (length == 0) break
        //type
        val type = scanRecord[index].toInt()
        //if not valid type
//        print("UTILS", "[MANUFACTURE] type is $type")
//        print("UTILS", "[MANUFACTURE] length is $length")
        if (type == 0) break
        dict[type] = Arrays.copyOfRange(scanRecord, index + 1, index + length)
        //next
        index += length
    }

    return dict
}

