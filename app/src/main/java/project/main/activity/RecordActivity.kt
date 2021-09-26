package project.main.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivityRecordBinding
import com.buddha.qrcodeweb.databinding.AdapterRecordBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import project.main.activity.const.constantName
import project.main.base.BaseActivity
import project.main.base.BaseRecyclerViewDataBindingAdapter
import project.main.database.SendRecordEntity
import project.main.database.getRecordDao
import project.main.database.insertNewRecord
import project.main.model.SettingDataItem
import tool.dialog.TextDialog
import tool.dialog.showConfirmDialg
import tool.dialog.showMessageDialogOnlyOKButton
import tool.getShare
import tool.getUrlKey
import utils.*
import java.util.*

class RecordActivity : BaseActivity<ActivityRecordBinding>({ ActivityRecordBinding.inflate(it) }) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 讓狀態列文字是深色

        initData()
        if( context.getRecordDao().allData.isEmpty()){
            mBinding.tvEmptyRecord.isVisible = true
        }else{
            mBinding.tvEmptyRecord.isVisible = false
            initObserver()

        }

        initView()

        initEvent()

    }

    private fun initData() {

    }

    private fun initObserver() {
        context.getRecordDao().liveData().observe(activity, Observer {
            logi(TAG, "收到更新通知！新的size是=>${it.size}")
            adapter.addItem(it.sortedByDescending { it.sendTime })
            logi(TAG, "最後一個是=>${it.getOrNull(it.size - 1)}")
//            adapter.notifyItemInserted(it.size - 1)
            adapter.notifyDataSetChanged()

            mBinding.rvRecord.postDelayed({ mBinding.rvRecord.smoothScrollToPosition(0) }, 20L)
        })
    }

    private val adapter by lazy {
        RecordAdapter(context).apply {
//            addItem(context.getRecordDao().allData.sortedByDescending { it.sendTime }) // 初始化LiveData時候會重新載入
            listener = object : RecordAdapter.ClickListener {
                override fun resend(scanString: String) {
                    logi(TAG, "點擊到重新送出！掃描到的內容是=>$scanString")
                    if (textDialog == null) {
                        textDialog = showConfirmDialg(context.getString(R.string.dialog_notice_title),
                            context.getString(R.string.record_resend_confirm).format(context.getShare().getNowUseSetting()?.name, scanString.getUrlKey(constantName)), {
                                resendCallApi(scanString, context.getShare().getNowUseSetting())
                                textDialog = null
                            }, {
                                textDialog = null
                            })
                    }
                }

                override fun showInfo(data: SendRecordEntity) {
                    logi(TAG, "點擊到顯示內容！要顯示的內容是=>${data.toJson()}")
                    if (textDialog == null) {
                        textDialog = showMessageDialogOnlyOKButton(
                            context.getString(R.string.record_info_dialog_title).format(data.getSignInPerson(), data.sendTime.toString("HH:mm:ss")), data.toFullInfo(
                                context.getString(R.string.record_time),
                                context.getString(R.string.record_scan_content),
                                context.getString(R.string.record_send_content),
                                context.getString(R.string.record_send_setting),
                                context.getShare().getSettingNameById(data.sendSettingId, data)
                            )
                        ) {
                            textDialog = null
                        }
                    }
                }
            }
        }
    }
    var textDialog: TextDialog? = null
    private fun initView() {
        // 開發中功能暫時隱藏
        mBinding.btnDelete.isVisible = false

        mBinding.btnResend.isVisible = false

        mBinding.rvRecord.adapter = adapter
    }

    private var signInResult = ""
    private fun resendCallApi(scanString: String, nowUseSetting: SettingDataItem?) {
        //打API
        val sendRequest = scanString.concatSettingColumn(nowUseSetting)
        val signInTime = Date().time
        signInResult = "${signInTime.toString("yyyy/MM/dd HH:mm:ss")}\n${scanString.getUrlKey(constantName)}簽到完成。"
        MainScope().launch {
            if (activity.sendApi(sendRequest)) {
                // 關閉進度框、顯示簽到結果視窗。
                if (textDialog == null) {
                    textDialog = activity.showSignInCompleteDialog(signInResult) {
                        signInResult = ""
                        textDialog = null
                        activity.getRecordDao().insertNewRecord(signInTime, scanString, sendRequest, activity.getShare().getNowUseSetting() ?: return@showSignInCompleteDialog)
                    }
                }
            } else {
                if (textDialog == null) {
                    textDialog = activity.showSignInErrorDialog {
                        textDialog = null
                    }
                }
            }

        }

    }

    private fun initEvent() {
        mBinding.btnBack.setOnClickListener {
            activity.onBackPressed()
        }

    }

    override fun finish() {
        super.finish()
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    class RecordAdapter(val context: Context) : BaseRecyclerViewDataBindingAdapter<SendRecordEntity>(context, R.layout.adapter_record) {

        interface ClickListener {
            fun resend(scanString: String)
            fun showInfo(data: SendRecordEntity)
        }

        var listener: ClickListener? = null

        override fun initViewHolder(viewHolder: ViewHolder) {
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, data: SendRecordEntity) {
            val adapterBinding = viewHolder.binding as AdapterRecordBinding
            adapterBinding.apply {
                tvRecordTime.text = data.sendTime.toString("yyyy/MM/dd HH:mm:ss")
                tvRecordScanContent.text = data.getSignInPerson()
                tvRecordSendContent.text = data.sendContent
                tvRecordSendSetting.text = context.getShare().getSettingNameById(data.sendSettingId, data)
                ivRecordResend.setOnClickListener {
                    listener?.resend(data.scanContent)
                }
            }

        }

        override fun onItemClick(view: View, position: Int, data: SendRecordEntity): Boolean {
            listener?.showInfo(data)

            return false
        }

        override fun onItemLongClick(view: View, position: Int, data: SendRecordEntity): Boolean {
            return false
        }


    }

}



