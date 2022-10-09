package project.main.api

import android.content.Context
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import utils.logi

fun getURLResponse(url: String, TAG: String = "getURLResponse"): Response<String>? {
    val cell = ApiConnect.getService().getURLResponse(url)
    logi(TAG, "開始呼叫API，請求 getURLResponse 方法,送出字串是=>$url")

    val response = cell.execute()
    logi(TAG, "getURLResponse 取得的資料是===>${response ?: "null"}")
    logi(TAG, "getURLResponse 取到的內容長度是=>${response.body()?.length}")
    return when {
//        response.body()?.length ?: 160000 > 150000 -> Response.error(418, "Response Error".toResponseBody(null)) //若有錯誤則會10萬起跳
        response.isSuccessful -> { // 新增導向錯誤的判斷
            null
        }
        else -> {
            response
        }
    }

}