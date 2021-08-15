package tool.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.DialogProgressBinding
import tool.AnimationFileName
import uitool.getRectangleBg
import uitool.setTextSize

class ProgressDialog(val context: Context) : Dialog {
    var title = context.getString(R.string.dialog_progress_default_title)

    val dialog by lazy { MaterialDialog(context) }
    val binding by lazy { DataBindingUtil.inflate<DialogProgressBinding>(LayoutInflater.from(context), R.layout.dialog_progress, null, false) }

    fun isShowing(): Boolean = dialog.isShowing
    fun dismiss() = dialog.dismiss()

    override fun show(){
        if (dialog.isShowing){
            return
        }
        binding.progressBar.setAnimation(AnimationFileName.LOADING_DIALOG)
        //
//        dialogBinding.progressBar.useHardwareAcceleration(true)//啟動動畫的硬件加速
        //
        binding.apply {
            val backgroundCorner = context.resources.getDimensionPixelSize(R.dimen.dialog_background_corner)
            root.background = getRectangleBg(context, backgroundCorner, backgroundCorner, backgroundCorner, backgroundCorner, R.color.dialog_bg, 0, 0)
            tvTitle.text = title
            tvTitle.setTextSize(16)
            if (title == ""){
                tvTitle.visibility = View.GONE
            }
        }
        binding.progressBar.playAnimation()
        //
        dialog.apply {
            setContentView(binding.root)
            setCancelable(false)
            window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            show()
        }
        binding.progressBar.playAnimation()
    }
}