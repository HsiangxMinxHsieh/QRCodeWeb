package project.main.api

import android.content.Context
import okhttp3.ResponseBody
import retrofit2.Response
import utils.logi

fun Context.getURLResponse(url: String, TAG: String = "getURLResponse"): Response<String>? {
    val cell = ApiConnect.getService(this).getURLResponse(url)
    logi(TAG, "開始呼叫API，請求 getURLResponse 方法")

    val response = cell.execute()
    logi(TAG, "getURLResponse 送出的資料是===>${response ?: "null"}")

    return if (response.isSuccessful) {
        logi(TAG,"getURLResponse 取到的結果是=>${response.body()}")
        null
    } else{
        response
    }

}