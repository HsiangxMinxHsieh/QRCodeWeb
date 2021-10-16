package project.main.activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivityRecordBinding
import com.buddha.qrcodeweb.databinding.AdapterRecordBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import project.main.base.BaseActivity
import project.main.base.BaseRecyclerViewDataBindingAdapter
import project.main.database.SendRecordEntity
import project.main.database.getRecordDao
import project.main.database.insertNewRecord
import project.main.model.SettingDataItem
import tool.dialog.*
import tool.getShare
import tool.getUrlKey
import utils.*
import java.util.*
import kotlin.collections.HashMap

// 多選重發的狀態
enum class MultipleStatus {
    None,  // 未點選
    SelectMode, // 點擊後選擇模式中
    Selecting, // 選擇要重發哪些紀錄中
    Send // 重發中
}

// 多選的模式
enum class SelectMode {
    None, // 未啟用多選模式
    P2P, // 點到點模式
    Select, // 單一多選重發
    All // 全部選擇
}

class RecordActivity() : BaseActivity<ActivityRecordBinding>({ ActivityRecordBinding.inflate(it) }) {

    override var statusTextIsDark: Boolean = true

    private val nowMultipleStatus: MutableLiveData<Pair<MultipleStatus, SelectMode>> by lazy { MutableLiveData(Pair(MultipleStatus.None, SelectMode.None)) }// 多選重發的當前狀態

    var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()

        if (context.getRecordDao().allData.isEmpty()) {
            mBinding.apply {
                tvEmptyRecord.isVisible = true
                btnMultipleSelectMode.isVisible = true //開發中，暫時皆為true。
            }
        } else {
            mBinding.apply {
                tvEmptyRecord.isVisible = false
                mBinding.btnMultipleSelectMode.isVisible = true
            }
            initObserver()
        }
        initObserver() // 開發中

        initView()

        initEvent()

    }

    private fun initData() {
//        repeat(10) { // 測試資料填入
//            activity.getRecordDao().insertNewRecord(Date().time, "123", "456", activity.getShare().getNowUseSetting() ?: return)
//        }
    }

    private fun initObserver() {

        context.getRecordDao().liveData().observe(activity, Observer {
            logi(TAG, "收到更新通知！新的size是=>${it.size}")
            adapter.addItem(it.sortedByDescending { it.sendTime }.toMutableList())
            logi(TAG, "最後一個是=>${it.getOrNull(it.size - 1)}")
//            adapter.notifyItemInserted(it.size - 1)
            adapter.notifyDataSetChanged()

            mBinding.rvRecord.postDelayed({ mBinding.rvRecord.smoothScrollToPosition(0) }, 20L)
        })

        nowMultipleStatus.observe(activity, {
            logi(TAG, "觀察到的內容是=>${it}")
            when (it.first) {
                MultipleStatus.None -> {

                    mBinding.clControl.isVisible = true
                    mBinding.btnResend.isVisible = false
                    mBinding.btnMultipleSelectMode.background = ContextCompat.getDrawable(context, R.drawable.ic_baseline_multiple_select_mode)
                }
                MultipleStatus.SelectMode -> {
                    mBinding.btnMultipleSelectMode.background = ContextCompat.getDrawable(context, R.drawable.ic_baseline_multiple_selecting_mode)
                }
                MultipleStatus.Selecting -> {
                    mBinding.btnMultipleSelectMode.background = ContextCompat.getDrawable(
                        context, when (it.second) {
                            SelectMode.P2P -> R.drawable.ic_baseline_multiple_p2p
                            SelectMode.Select -> R.drawable.ic_baseline_multiple_single
                            SelectMode.All -> R.drawable.ic_baseline_multiple_all
                            SelectMode.None -> 0 // 原則上不可能到這裡
                        }
                    )
                    mBinding.btnResend.isVisible = true
                }
                MultipleStatus.Send -> {
                    mBinding.btnMultipleSelectMode.background = null
                    mBinding.clControl.visibility = View.INVISIBLE
                }
            }
        })
    }

    private val adapter by lazy {
        RecordAdapter(context).apply {
            listener = infoClass
        }
    }
    private val selectClass: SelectClass by lazy { SelectClass() }


    inner class SelectClass : RecordAdapter.SelectListener {

        val selectMap: HashMap<Int, String> by lazy { HashMap() }
        var nowSelectIndex = -1

        fun init() = this.apply {
            selectMap.clear()
            nowSelectIndex = -1
        }

        override fun choose(isSelect: Boolean, index: Int, scanString: String) {
            if (isSelect) {
                selectMap[index] = scanString
            } else {
                selectMap.remove(index)
            }
            logi(TAG, "此時的 selectMap 是=>${selectMap.toJson()}")
        }

    }

    private val infoClass: InfoListener by lazy { InfoListener() }

    inner class InfoListener : RecordAdapter.InfoListener {

        override fun resend(scanString: String) {
            logi(TAG, "點擊到重新送出！掃描到的內容是=>$scanString")
            if (dialog == null) {
                dialog = showConfirmDialog(context.getString(R.string.dialog_notice_title),
                    context.getString(R.string.record_resend_confirm).format(context.getShare().getNowUseSetting()?.name, scanString.getUrlKey(context.getShare().getKeyName())), {
                        resendCallApi(scanString, context.getShare().getNowUseSetting())
                        dialog = null
                    }, {
                        dialog = null
                    })
            }
        }

        override fun showInfo(data: SendRecordEntity) {
            logi(TAG, "點擊到顯示內容！要顯示的內容是=>${data.toJson()}")
            if (dialog == null) {
                dialog = showMessageDialogOnlyOKButton(
                    context.getString(R.string.record_info_dialog_title).format(data.getSignInPerson(context.getShare().getKeyName()), data.sendTime.toString("HH:mm:ss")), data.toFullInfo(
                        context.getString(R.string.record_time),
                        context.getString(R.string.record_scan_content),
                        context.getString(R.string.record_send_content),
                        context.getString(R.string.record_send_setting),
                        context.getShare().getSettingNameById(data.sendSettingId, data)
                    )
                ) {
                    dialog = null
                }
            }
        }
    }


    private fun initView() {
        // 開發中功能暫時隱藏
        mBinding.btnDelete.isVisible = false

        mBinding.btnResend.isVisible = false

        mBinding.rvRecord.adapter = adapter
    }

    private var signInResult = ""

    private val empty: (Throwable?) -> Unit = {} // 用於不讓使用者經檢查後才可返回的方法判斷變數(用於判斷是在何處呼叫saveData方法)

    private fun resendCallApi(scanString: String, nowUseSetting: SettingDataItem?, afterCallAction: (Throwable) -> Unit = empty) {
        // Call API
        val sendRequest = scanString.concatSettingColumn(nowUseSetting)
        val signInTime = Date().time
        signInResult = "${signInTime.toString("yyyy/MM/dd HH:mm:ss")}\n${scanString.getUrlKey(context.getShare().getKeyName())}簽到完成。"
        MainScope().launch {
            if (activity.sendApi(sendRequest)) {

                // 顯示簽到結果視窗。
                if (afterCallAction == empty) {
                    if (dialog == null) {
                        dialog = activity.showSignInCompleteDialog(signInResult) {
                            signInResult = ""
                            dialog = null
                            activity.getRecordDao().insertNewRecord(signInTime, scanString, sendRequest, activity.getShare().getNowUseSetting() ?: return@showSignInCompleteDialog)
                        }
                    }
                } else {
                    activity.getRecordDao().insertNewRecord(signInTime, scanString, sendRequest, activity.getShare().getNowUseSetting() ?: return@launch)
                    afterCallAction.invoke(Throwable())
                }
            } else {
                if (dialog == null) {
                    dialog = activity.showSignInErrorDialog {
                        setNowStatusToNoneOrSelecting(MultipleStatus.None)
                        dialog = null
                    }
                }
            }
        }
    }

    private fun initEvent() {
        mBinding.btnBack.setOnClickListener {
            activity.onBackPressed()
        }

        mBinding.btnMultipleSelectMode.setOnClickListener {
            chooseMultipleSelectMode()
        }

        mBinding.btnResend.setOnClickListener {
            multipleResend()
        }

    }

    private fun chooseMultipleSelectMode() {
        if (dialog == null && nowMultipleStatus.value?.first == MultipleStatus.None) {
            nowMultipleStatus.postValue(Pair(MultipleStatus.SelectMode, SelectMode.None))
            val list = mutableListOf<String>().apply {
                addAll(context.resources.getStringArray(R.array.multiple_resend_mode))
            }
            dialog = activity.showListDialog(context.getString(R.string.record_multiple_resend_dialog_title), list,
                selectAction = { selectIndex, selectData ->
                    logi(TAG, "選到的position是=>$selectIndex,,,選到的文字是=>$selectData")
                    nowMultipleStatus.postValue(
                        Pair(
                            MultipleStatus.Selecting,
                            when (selectIndex) {
                                0 -> SelectMode.P2P
                                1 -> SelectMode.Select
                                2 -> SelectMode.All
                                else -> SelectMode.None
                            }
                        )
                    )

                    setNowStatusToNoneOrSelecting(MultipleStatus.Selecting)
                    dialog = null
                },
                cancelAction = {
                    nowMultipleStatus.postValue(Pair(MultipleStatus.SelectMode, SelectMode.None))
                    dialog = null
                })
        }
    }

    private fun multipleResend() {
        if (selectClass.selectMap.isEmpty()) {
            if (dialog == null) {
                dialog = context.showConfirmDialog(context.getString(R.string.dialog_notice_title), context.getString(R.string.record_multiple_resend_no_select),
                    confirmAction = { // 確定重選
                        dialog = null
                    }, cancelAction = { // 取消不送
                        setNowStatusToNoneOrSelecting(MultipleStatus.None)
                        dialog = null
                    },
                    confirmBtnStr = context.getString(R.string.record_multiple_resend_no_select_confirm),
                    cancelBtnStr = context.getString(R.string.record_multiple_resend_no_select_cancel)
                )
            }
            return
        }
        logi(TAG, "多重重送紀錄點 001")
        //這裡應該要彈出一個對話框供使用者確認

        nowMultipleStatus.postValue(Pair(MultipleStatus.Send, SelectMode.None))
        logi(TAG, "多重重送紀錄點 002")
        recursiveResend(selectClass.selectMap)

    }

    private fun recursiveResend(map: HashMap<Int, String>) {
        logi(TAG, "收到的map是=>${map.toJson()}")
        if (map.isEmpty()) { // 全部送完
            setNowStatusToNoneOrSelecting(MultipleStatus.None)
            logi(TAG, "收到的map是=>${map.toJson()}")
            return
        }
        val nowKey = map.keys.last()
        val nowSend = map[nowKey] ?: return
        //每次取第一個來重送
        resendCallApi(nowSend, context.getShare().getNowUseSetting()) {
            recursiveResend(map.apply { remove(nowKey) })
        }


    }

    private fun setNowStatusToNoneOrSelecting(status: MultipleStatus) {
        (status == MultipleStatus.None).let { isNone ->
            if(isNone)
                nowMultipleStatus.postValue(Pair(MultipleStatus.None, SelectMode.None))

            adapter.apply {
                listener = if (isNone) infoClass else selectClass.init()
                clearSelectMap()
                notifyDataSetChanged()
            }
        }
    }

    override fun finish() {
        super.finish()
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    class RecordAdapter(val context: Context) : BaseRecyclerViewDataBindingAdapter<SendRecordEntity>(context, R.layout.adapter_record) {
        val selectBackGroundColor: Int by lazy { context.getColor(R.color.gray) }

        interface Listener

        interface InfoListener : Listener {
            fun resend(scanString: String)
            fun showInfo(data: SendRecordEntity)
        }

        interface SelectListener : Listener {
            fun choose(isSelect: Boolean, index: Int, scanString: String)
        }

        var listener: Listener? = null

        override fun initViewHolder(viewHolder: ViewHolder) {
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, data: SendRecordEntity) {
            val adapterBinding = viewHolder.binding as AdapterRecordBinding

            adapterBinding.apply {
                tvRecordTime.text = data.sendTime.toString("yyyy/MM/dd HH:mm:ss")
                tvRecordScanContent.text = data.getSignInPerson(context.getShare().getKeyName())
                tvRecordSendContent.text = data.sendContent
                tvRecordSendSetting.text = context.getShare().getSettingNameById(data.sendSettingId, data)
                ((listener as? InfoListener) != null).apply {
                    ivRecordResend.isVisible = this
                    ivRecordResend.setOnClickListener {
                        (listener as RecordActivity.InfoListener).resend(data.scanContent)
                    }
                }
                root.setBackgroundColor(if (isSelectMap[position] != true) Color.TRANSPARENT else selectBackGroundColor)
            }
        }

        fun clearSelectMap() {
            isSelectMap.clear()
        }

        private val isSelectMap by lazy { HashMap<Int, Boolean>() }

        override fun onItemClick(view: View, position: Int, data: SendRecordEntity): Boolean {
            (listener as? InfoListener)?.apply {
                showInfo(data)
                return true
            }

            logi(TAG, "點擊前，isSelectMap判斷是=>${isSelectMap[position] != true}")
            (isSelectMap[position] != true).apply {
                (listener as? SelectListener)?.choose(this, position, data.scanContent)
                isSelectMap[position] = this
                view.setBackgroundColor(if (this) selectBackGroundColor else Color.TRANSPARENT)
            }

            logi(TAG, "點擊完成後，isSelectMap是=>${isSelectMap[position]}")

            return false
        }

        override fun onItemLongClick(view: View, position: Int, data: SendRecordEntity): Boolean {

            return false
        }


    }

}



