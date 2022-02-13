package utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

import org.json.JSONObject



private val TAG = "GsonExt"


inline fun <reified T> String.toDataBean(param: T) = Gson().fromJson<T>(this, object : TypeToken<T>() {}.type)

fun <T> String?.toDataBean(classOfT: Class<T>?): T? {

    return if (this.isJson()) Gson().fromJson(this, classOfT)
    else null
}

fun <T : Any> T.toJson(): String {
    return GsonBuilder().disableHtmlEscaping().create().toJson(this) ?: ""
}

fun String?.isJson(): Boolean {
    if (isNullOrEmpty()) {
        loge(TAG, "json is null or empty:: ${this.toString()}")
        return false
    }
    var jsonObject: JSONObject? = null
    try {
        jsonObject = JSONObject(this)
    } catch (e: Exception) {
        loge(TAG, "response json is:: $this \r\n ${e.message}")
        e.printStackTrace()
        loge(TAG, "check fail, this string is not JSON format")
    }
    return jsonObject != null
}