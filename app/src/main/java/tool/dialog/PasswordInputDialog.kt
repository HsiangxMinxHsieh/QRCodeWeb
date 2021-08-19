package tool.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.DialogPasswordInputBinding
import com.buddha.qrcodeweb.databinding.DialogProgressBinding
import uitool.getRectangleBg
import uitool.setTextSize


class PasswordInputDialog(val context: Context) : Dialog {
    var title = ""
    var editText = ""
    var hintText = ""
    var limitTextSize = 30
    val dialog by lazy { MaterialDialog(context) }
    val dialogBinding by lazy { DataBindingUtil.inflate<DialogPasswordInputBinding>(LayoutInflater.from(context), R.layout.dialog_password_input, null, false) }
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

            if (limitTextSize == 0)
                tvTextSizeLimit.visibility = View.GONE
            else {
                tvTextSizeLimit.text = "(0/$limitTextSize)"
                tvTextSizeLimit.visibility = View.VISIBLE
            }

            edtText.setText(editText)
            edtText.hint = hintText

            if (editText == "" && hintText == "") {
                edtText.visibility = View.GONE
            }
            edtText.setTextSize(14)
//            tvMessage.movementMethod = ScrollingMovementMethod.getInstance()

            //按鈕設定參數：
//            val corner = context.resources.getDimensionPixelSize(R.dimen.dialog_button_corner)    //圓角弧度
            val btnTextSize = context.resources.getDimensionPixelSize(R.dimen.btn_text_size) //按鍵文字大小
//            val strokeWidth = context.resources.getDimensionPixelSize(R.dimen.btn_stroke_width) //按鈕邊界寬度

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