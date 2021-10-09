package tool.dialog

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.DialogKeyCheckBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import project.main.model.KeyDefault
import tool.getShare
import uitool.getRectangleBg
import uitool.setTextSize
import utils.logi

fun Activity.showKeyDefaultCheckDialog(data: KeyDefault = KeyDefault(), finishAction: () -> Unit = {}) {
    val context = this
    KeyDefaultCheckDialog(context, data).apply {
        this.title = context.getString(R.string.splash_key_check_title)
        MainScope().launch {
            dialogBinding.edtNameLayout.isErrorEnabled = true
            dialogBinding.btnLift.visibility = View.GONE
            dialogBinding.btnRight.text = context.getString(R.string.dialog_ok)
            dialogBinding.btnRight.setOnClickListener {
                if (dialogBinding.edtName.text.toString().isEmpty()) {
                    dialogBinding.edtNameLayout.error = context.getString(R.string.splash_column_could_not_be_empty)
                    return@setOnClickListener
                }

                context.getShare().setNowKeyDefault(data.apply {
                    keyName = dialogBinding.edtName.text.toString()
                    keyPassword = dialogBinding.edtPassword.text.toString()
                    settingStatus = data.settingStatus + 1
                })

                finishAction.invoke()
                dialog.dismiss()
            }
            show()
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

            logi("KeyDefaultCheckDialog", "要設定的內容是=>${data.keyName}")

            edtName.setText(data.keyName)
            edtPassword.setText(data.keyPassword)

            edtName.setTextSize(14)
            edtPassword.setTextSize(14)

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