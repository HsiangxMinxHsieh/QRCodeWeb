package tool.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.AdapterDialogListBinding
import com.buddha.qrcodeweb.databinding.DialogListBinding
import com.timmymike.viewtool.clickWithTrigger
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import project.main.base.BaseRecyclerViewDataBindingAdapter
import uitool.getRectangleBg
import uitool.*
import utils.getColorByBuildVersion

fun Context.showListDialog(title: String, list: MutableList<String>, selectAction:(selectIndex:Int,selectData:String)->Unit, cancelAction: () -> Unit = {}): ListDialog {

    return ListDialog(this).apply {
        this.title = title
        this.list = list
        MainScope().launch {
            dialogBinding.btn.text = context.getString(R.string.dialog_cancel)
            dialogBinding.btn.clickWithTrigger {
                cancelAction()
                dialog.dismiss()
            }
            listener = object:ListDialog.Listener{
                override fun onItemClick(position: Int, data: String): Boolean {
                    selectAction.invoke(position,data)
                    dialog.dismiss()
                    return true
                }
            }
            show()
        }
    }
}


class ListDialog(val context: Context) :Dialog{
    var title = ""
    var list = mutableListOf<String>()

    val dialog by lazy { MaterialDialog(context) }
    val dialogBinding by lazy { DataBindingUtil.inflate<DialogListBinding>(LayoutInflater.from(context), R.layout.dialog_list, null, false) }
    override fun show() {
        if (dialog.isShowing) {
            return
        }
        //
        dialogBinding.apply {
            val backgroundCorner = context.resources.getDimensionPixelSize(R.dimen.dialog_background_corner)
            root.background = getRectangleBg(context, backgroundCorner, backgroundCorner, backgroundCorner, backgroundCorner, R.color.dialog_bg, 0, 0)
            tvTitle.text = title
            tvTitle.setTextSize(16)
            if (title == "") {
                tvTitle.visibility = View.GONE
            }
            //
            val layoutManager = object : LinearLayoutManager(context, VERTICAL, false) {
                override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) = try {
                    super.onLayoutChildren(recycler, state)
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }
            recyclerView.layoutManager = layoutManager

            if (list.size > 10) {
                val layoutParams = recyclerView.layoutParams ?: RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
                layoutParams.height = (getScreenHeightPixels(context) * 0.5).toInt()
                recyclerView.layoutParams = layoutParams
            }

            val divider = DividerItemDecoration(context, layoutManager.orientation)
            divider.setDrawable(ColorDrawable(context.getColorByBuildVersion(R.color.dialog_divider)))
            recyclerView.addItemDecoration(divider)
            adapter.addItem(list)
            recyclerView.adapter = adapter
            //
            //按鈕設定參數：
//            val corner = context.resources.getDimensionPixelSize(R.dimen.dialog_button_corner)    //圓角弧度
            val btnTextSize = context.resources.getDimensionPixelSize(R.dimen.btn_text_size) //按鍵文字大小
//            val strokeWidth = context.resources.getDimensionPixelSize(R.dimen.btn_stroke_width) //按鈕邊界寬度

            btn.setTextSize(btnTextSize)
//            btnLift.background = null
//            btn.setPressedBackground(
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

    var listener: Listener? = null

    interface Listener {
        fun onItemClick(position: Int, data: String): Boolean
    }

    private val adapter by lazy { ListAdapter(context) }

    private inner class ListAdapter(val context: Context) : BaseRecyclerViewDataBindingAdapter<String>(context, R.layout.adapter_dialog_list) {
        override fun initViewHolder(viewHolder: ViewHolder) {
            val binding = viewHolder.binding as AdapterDialogListBinding
            binding.tvName.setTextSize(14)
            binding.tvName.setPressedTextColor(R.color.dialog_txt_message, R.color.txt_gray)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, data: String) {
            val binding = viewHolder.binding as AdapterDialogListBinding
            binding.tvName.text = data
        }

        override fun onItemClick(view: View, position: Int, data: String): Boolean {
            return listener?.onItemClick(position, data) ?: false
        }

        override fun onItemLongClick(view: View, position: Int, data: String): Boolean {
            return false
        }

    }
}