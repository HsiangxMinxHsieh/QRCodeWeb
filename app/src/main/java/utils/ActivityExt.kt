package utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.net.toUri
import com.buddha.qrcodeweb.R
import kotlinx.coroutines.*
import project.main.api.getURLResponse
import project.main.model.SettingData
import project.main.model.SettingDataItem
import tool.dialog.Dialog
import tool.dialog.ProgressDialog
import tool.dialog.showConfirmDialog
import tool.dialog.showMessageDialogOnlyOKButton
import tool.getShare
import kotlin.coroutines.CoroutineContext

/**顯示送出中等待Dialog、並回傳送出結果。*/
suspend fun Activity.sendApi(sendRequest: String, waitingText: String = this.getString(R.string.dialog_progress_default_title), beforeSendAction: () -> Unit = {}, afterSendAction: () -> Unit = {}): Boolean {
    val activity = this
    val progressDialog = ProgressDialog(activity).apply { title = waitingText }

    val result: Deferred<Boolean> = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Job()
    }.async(Dispatchers.IO) {
        MainScope().launch { // 顯示進度框
            beforeSendAction.invoke() // 暫停播放動畫
            progressDialog.show()
        }
        try {
            logi("Send", "組合後的發送內容是=>$sendRequest")
            val response = getURLResponse(sendRequest)
//            logi("Send", "取得的錯誤內容是=>${response?.errorBody()?.string()}")
            return@async response?.errorBody()?.string() == null // 等於null代表有成功
        } catch (e: Exception) {
//            e.printStackTrace()
            return@async false
        } finally {
            MainScope().launch {
                afterSendAction.invoke()
                progressDialog.dismiss() // 關閉進度框
            }
        }
    }

    return result.await()
}

fun Activity.setKeyboard(open: Boolean, editFocus: EditText? = null) {
    if (open) { // 關閉中，要打開
        if (editFocus?.requestFocus() == true) {
            val imm = (this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager) ?: return
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    } else { //打開中，要關閉
        if (this.currentFocus != null) {
            ((this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)).hideSoftInputFromWindow(this.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
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

fun Activity.intentToWebPage(url: String?) {
    Intent().let {
        it.action = Intent.ACTION_VIEW
        it.data = url?.toUri()
        this.startActivity(it)
    }
}

fun Activity.maxSettingSize() = this.resources.getInteger(R.integer.setting_size_max_size)
fun Activity.maxSettingNameSize() = this.resources.getInteger(R.integer.edit_text_max_size)

/**
 * 兩種情況無法新增失敗：
 * 1.有設定檔沒有被儲存過(只有有設定名字才可以儲存)
 * 2.當前設定檔已有20個，就不能再新增(已設計為無論掃描頁掃描到新增、設定頁面按下新增、設定頁面掃描頁面掃描到新增都無法再新增。)。
 * */
fun Activity.couldBeAdd(newItem: SettingDataItem, settings: SettingData, confirmAction: (item: SettingDataItem?) -> Unit = {}): Boolean {

    if (settings.any { !it.haveSaved }) { // 如果有false(未儲存者)要return。
        this.showMessageDialogOnlyOKButton(this.getString(R.string.dialog_notice_title), this.getString(R.string.setting_cant_add_by_not_saved_current))
        return false
    }

    if (newItem.name.isEmpty()) { // 設定檔的名稱不可為空
        this.showMessageDialogOnlyOKButton(this.getString(R.string.dialog_notice_title), this.getString(R.string.setting_cant_add_by_empty_name)) {
            confirmAction.invoke(null)
        }
        return false
    }

    if (newItem.name.length > this.maxSettingNameSize()) { // 設定檔的名稱不可大於設定的數字
        this.showMessageDialogOnlyOKButton(this.getString(R.string.dialog_notice_title), this.getString(R.string.setting_cant_add_by_over_max_name).format(this.maxSettingNameSize())) {
            confirmAction.invoke(null)
        }
        return false
    }

//    if (settings.isNew(newItem) && settings.size >= this.maxSettingSize()) { // 設定檔的數目不能大於設定的數字
//        this.showMessageDialogOnlyOKButton(this.getString(R.string.dialog_notice_title), this.getString(R.string.setting_cant_add_by_over_max_size).format(this.maxSettingSize())) {
//            confirmAction.invoke(null)
//        }
//        return false
//    }
    confirmAction.invoke(newItem)
    return true
}

/** 統一在此方法這裡判斷是否有新增成功
 * confirmAction比較複雜一點，因為它是用來給外部呼叫，但又要回Call判斷這裡能不能儲存的方法
 * */
fun Activity.showDialogAndConfirmToSaveSetting(newItem: SettingDataItem, settings: SettingData, confirmAction: (item: SettingDataItem?) -> Boolean): Dialog {

    settings.isNew(newItem).let { isNew ->
        val message = this.getString(R.string.setting_scan_action).format(
            if (isNew)// 新增
                getString(R.string.setting_scan_action_new)
            else // 更新
                getString(R.string.setting_scan_action_update),
            newItem.name
        )

        return showConfirmDialog(this.getString(R.string.dialog_notice_title), message,
            confirmAction = {
                couldBeAdd(newItem, settings) {
                    it?.let { store ->
                        this.getShare().scanStoreSetting(isNew, store) // 一律會對儲存的資料作處理(因沒有辦法驗證設定頁的資料當前是否沒有儲存)
                    }
                    confirmAction.invoke(it)
                }.run {

                }
            },
            cancelAction = {
                confirmAction.invoke(null) //執行但不更新值
            })
    }
}
