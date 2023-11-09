package tool.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.DialogKeyCheckBinding
import com.timmymike.viewtool.clickWithTrigger
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import project.main.activity.ScanActivity
import project.main.activity.ScanMode
import project.main.model.KeyDefault
import project.main.model.SettingDataItem
import tool.getShare
import uitool.getRectangleBg
import uitool.setTextSize
import utils.logi
import utils.toJson

fun Activity.showKeyDefaultCheckDialog(data: KeyDefault = KeyDefault(), finishAction: () -> Unit = {}) {
    val context = this
    KeyDefaultCheckDialog(context, data).apply {
        this.title = context.getString(R.string.splash_key_check_title)
        MainScope().launch {
            dialogBinding.run {
                edtNameLayout.isErrorEnabled = true
                btnLift.visibility = View.GONE
                btnScanToGet.clickWithTrigger { // 進入掃描設定檔頁面
                    startActivity(Intent(context, ScanActivity::class.java).apply {
                        putExtra(ScanActivity.BUNDLE_KEY_SCAN_MODE, ScanMode.SPLASH)
                    })
                    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up)

                }
                btnRight.text = context.getString(R.string.dialog_ok)
                btnRight.clickWithTrigger {

                    if (edtId.text.toString().isEmpty()) {
                        edtIdLayout.error = context.getString(R.string.splash_column_could_not_be_empty)
                        return@clickWithTrigger
                    }

                    if (edtName.text.toString().isEmpty()) {
                        edtNameLayout.error = context.getString(R.string.splash_column_could_not_be_empty)
                        return@clickWithTrigger
                    }

                    if (edtPassword.text.toString().isEmpty()) {
                        edtPasswordLayout.error = context.getString(R.string.splash_column_could_not_be_empty)
                        return@clickWithTrigger
                    }

                    context.getShare().setNowKeyDefault(data.apply {
                        keyID = edtId.text.toString()
                        keyName = edtName.text.toString()
                        keyPassword = edtPassword.text.toString()
                        settingStatus = data.settingStatus + 1
                    })

                    //嘉伸講師指示，第一次按下確定後，自動產生一個預設設定檔供使用者直接使用。
                    val initSetting = SettingDataItem.getFirstDefaultSetting(context)
                    context.apply {
                        getShare().apply {
                            getID()
                            savaAllSettings(getShare().getStoreSettings().also {
                                it.add(initSetting)
                            })
                            setNowUseSetting(initSetting)
                        }
                    }
                    finishAction.invoke()
                    dialog.dismiss()
                }
                show()
            }
        }
    }
}


class KeyDefaultCheckDialog(val context: Context, val data: KeyDefault) : Dialog {
    var title = ""
    val dialog by lazy { MaterialDialog(context) }
    val dialogBinding by lazy { DataBindingUtil.inflate<DialogKeyCheckBinding>(LayoutInflater.from(context), R.layout.dialog_key_check, null, false) }
    override fun show() {
        if (dialog.isShowing) {
            return
        }
        //
        dialogBinding.apply {
            val backgroundCorner = context.resources.getDimensionPixelSize(R.dimen.dialog_background_corner)
            root.background = getRectangleBg(context, backgroundCorner, backgroundCorner, backgroundCorner, backgroundCorner, R.color.dialog_bg, 0, 0)
            tvTitle.text = title
            tvTitle.setTextSize(20)

            if (title == "") {
                tvTitle.visibility = View.GONE
            }

//            logi("KeyDefaultCheckDialog", "要設定的內容是=>${data.keyName}")
            edtId.setText(data.keyID)
            edtName.setText(data.keyName)
            edtPassword.setText(data.keyPassword)

            edtId.setTextSize(16)
            edtName.setTextSize(16)
            edtPassword.setTextSize(16)

            //按鈕設定參數：
            val btnTextSize = context.resources.getDimensionPixelSize(R.dimen.btn_text_size) //按鍵文字大小

            btnRight.setTextSize(btnTextSize)
            btnLift.setTextSize(btnTextSize)

        }
        //
        dialog.apply {
            setContentView(dialogBinding.root)
            setCancelable(false)
            window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
    }
}