package tool.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.DialogTextBinding
import com.timmymike.viewtool.clickWithTrigger
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import uitool.getRectangleBg
import uitool.getScreenHeightPixels
import uitool.setTextSize

//fun showMessageDialogOnlyOKButton(context: Context, message: String, lambda: () -> Unit = {}): TextDialog {
//    return TextDialog(context).apply {
//        title = message
//        dialogBinding.btnLift.visibility = View.GONE
//        dialogBinding.btnRight.text = context.getString(R.string.dialog_ok)
//        dialogBinding.btnRight.clickWithTrigger {
//            lambda()
//            dialog.dismiss()
//        }
//        show()
//    }
//}

fun Context.showMessageDialogOnlyOKButton(title: String, message: String, lambda: () -> Unit = {}): TextDialog {
    return TextDialog(this).apply {
        this.title = title
        this.message = message
        MainScope().launch {
            dialogBinding.btnLift.visibility = View.GONE
            dialogBinding.btnRight.text = context.getString(R.string.dialog_ok)
            dialogBinding.btnRight.clickWithTrigger {
                lambda()
                dialog.dismiss()
            }
            show()
        }
    }
}

fun Context.showConfirmDialog(
    title: String, message: String, confirmAction: () -> Unit = {}, cancelAction: () -> Unit = {},
    confirmBtnStr: String = this.getString(R.string.dialog_ok),
    cancelBtnStr: String = this.getString(R.string.dialog_cancel)
): TextDialog {
    return TextDialog(this).apply {
        this.title = title
        this.message = message
        dialogBinding.btnLift.text = cancelBtnStr
        dialogBinding.btnLift.clickWithTrigger {
            cancelAction.invoke()
            dialog.dismiss()
        }
        dialogBinding.btnRight.text = confirmBtnStr
        dialogBinding.btnRight.clickWithTrigger {
            confirmAction()
            dialog.dismiss()
        }
        show()
    }
}

class TextDialog(val context: Context) : Dialog {
    var title = ""
    var message = ""

    val dialog by lazy { MaterialDialog(context) }
    val dialogBinding by lazy { DataBindingUtil.inflate<DialogTextBinding>(LayoutInflater.from(context), R.layout.dialog_text, null, false) }
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
            tvMessage.text = message
            tvMessage.setTextSize(16)
//            tvMessage.movementMethod = ScrollingMovementMethod.getInstance()
            if (message.length > 200) {
                val layoutParams = scrollView.layoutParams ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
                layoutParams.height = (getScreenHeightPixels(context) * 0.5).toInt()
                scrollView.layoutParams = layoutParams
            }
            if (message == "") {
                tvMessage.visibility = View.GONE
            }
            //按鈕設定參數：
//            val corner = context.resources.getDimensionPixelSize(R.dimen.dialog_button_corner)   //圓角弧度
            val btnTextSize = context.resources.getDimensionPixelSize(R.dimen.btn_text_size) //按鍵文字大小
//            val strokeWidth = context.resources.getDimensionPixelSize(R.dimen.btn_stroke_width) //按鈕邊界寬度

            btnRight.setTextSize(btnTextSize)
//            btnRight.background = null
//            btnRight.setDefaultBackgroundAndTouchListener(R.color.white, R.color.green, R.color.green, R.color.white, R.color.green, R.color.green)

//            if (btnLift.visibility == View.VISIBLE) {
            btnLift.setTextSize(btnTextSize)
//            btnLift.background = null

//
//            btnLift.setPressedBackground(
//                BackgroundDrawable.getRectangleBg(context, corner, corner, corner, corner, R.color.card_back_white, R.color.theme_gray, strokeWidth),
//                BackgroundDrawable.getRectangleBg(context, corner, corner, corner, corner, R.color.theme_gray, R.color.theme_gray, strokeWidth)
//            )

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

    fun dismiss() = dialog.dismiss()
}