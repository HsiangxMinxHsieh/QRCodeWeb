package utils

import android.app.Activity
import android.content.Intent
import com.buddha.qrcodeweb.R
import kotlinx.coroutines.*
import project.main.api.getURLResponse
import tool.dialog.ProgressDialog
import tool.dialog.showMessageDialogOnlyOKButton
import tool.getShare
import kotlin.coroutines.CoroutineContext

/**顯示送出中等待Dialog、並回傳送出結果。*/
suspend fun Activity.sendApi(sendRequest: String, beforeSendAction: () -> Unit = {}, afterSendAction: () -> Unit = {}): Boolean {
    val activity = this
    val progressDialog = ProgressDialog(activity)

    val result: Deferred<Boolean> = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Job()
    }.async(Dispatchers.IO) {
        MainScope().launch { // 顯示進度框
            beforeSendAction.invoke() // 暫停播放動畫
            progressDialog.show()
        }
        try {
            //                val columnValues = context.getShare().getNowUseSetting()?.fields?.map { field -> "${field.columnKey}=${field.columnValue}" }.toString().replace(", ", "&").replace("[", "").replace("]", "")
            logi("Send", "組合後的發送內容是=>$sendRequest")
            val response = getURLResponse(sendRequest)
//            logi("Send", "取得的錯誤內容是=>${response?.errorBody()?.string()}")
            return@async response == null // 等於null代表有成功
        } catch (e: Exception) {
//            e.printStackTrace()
            return@async false
        } finally {
            MainScope().launch {
                progressDialog.dismiss()
            }
        }
    }

    return result.await()
}

fun Activity.showSignInCompleteDialog(signInResult: String, okButtonClickAction: () -> Unit = {}) = this.showMessageDialogOnlyOKButton(this.getString(R.string.dialog_sign_in_success_title), signInResult) {
    okButtonClickAction.invoke()
}

fun Activity.showSignInErrorDialog(afterShowErrorAction: () -> Unit = {}) = this.showMessageDialogOnlyOKButton(this.getString(R.string.dialog_notice_title), this.getString(R.string.dialog_error_message)) {
    afterShowErrorAction.invoke()
}

fun Activity.goToNextPageFinishThisPage(intent: Intent) {
    this.startActivity(intent)
    this.finish()
}
