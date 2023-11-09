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
import com.timmymike.viewtool.clickWithTrigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import project.main.base.BaseActivity
import project.main.base.BaseRecyclerViewDataBindingAdapter
import project.main.database.SendRecordEntity
import project.main.database.getRecordDao
import project.main.database.getSignInPersonByScan
import project.main.database.insertNewRecord
import project.main.database.searchByIdRange
import project.main.model.ActionMode
import project.main.model.SettingDataItem
import tool.dialog.Dialog
import tool.dialog.showConfirmDialog
import tool.dialog.showListDialog
import tool.dialog.showMessageDialogOnlyOKButton
import tool.getShare
import utils.concatSettingColumn
import utils.getColorByBuildVersion
import utils.intentToWebPage
import utils.sendApi
import utils.showSignInCompleteDialog
import utils.showSignInErrorDialog
import utils.toString
import java.util.Date

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

class RecordActivity : BaseActivity<ActivityRecordBinding>({ ActivityRecordBinding.inflate(it) }) {

    override var statusTextIsDark: Boolean = true

    private val nowMultipleStatus: MutableLiveData<Pair<MultipleStatus, SelectMode>> by lazy { MutableLiveData(Pair(MultipleStatus.None, SelectMode.None)) }// 多選重發的當前狀態

    var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()

        (context.getRecordDao().allData.isEmpty()).let {
            mBinding.apply {
                tvEmptyRecord.isVisible = it
                btnMultipleSelectMode.visibility = if (it) View.INVISIBLE else View.VISIBLE
            }
            if (!it)
                initObserver()
        }

        initView()

        initEvent()

    }

    private fun initData() {

        reAssociateDataSettingId()

    }

    /** 嘗試重新關聯簽到記錄與設定檔ID與設定檔名稱 */
    private fun reAssociateDataSettingId() {
        CoroutineScope(Dispatchers.Default).launch {
            context.getRecordDao().allData.forEach {
                if (context.getShare().getSettingById(it.sendSettingId) == null) { // 如果找不到這筆設定檔才要重新找關聯性
                    val beforeId = it.sendSettingId
                    it.sendSettingId = context.getShare().getStoreSettings().filter { set -> set.name == it.sendSettingName }.getOrNull(0)?.id ?: it.sendSettingId
                    if (beforeId != it.sendSettingId) { //不一樣才要存起來
                        context.getRecordDao().update(it)
                    }
                } else if (it.sendSettingName != context.getShare().getSettingById(it.sendSettingId)?.name) { // 有這筆設定檔，要檢查儲存的 settingName 是否和設定檔中的名稱相同
                    context.getRecordDao().update(it.apply {
                        this.sendSettingName = context.getShare().getSettingById(it.sendSettingId)?.name ?: "impossible appear。"
                    })
                }
            }
        }
    }

    private fun initObserver() {

        context.getRecordDao().liveData().observe(activity, Observer {
//            logi(TAG, "收到更新通知！新的size是=>${it.size}")
            adapter.addItem(it.sortedByDescending { it.sendTime }.toMutableList())
//            logi(TAG, "最後一個是=>${it.getOrNull(it.size - 1)}")
//            adapter.notifyItemInserted(it.size - 1)
            adapter.notifyDataSetChanged()

            mBinding.rvRecord.postDelayed({ mBinding.rvRecord.smoothScrollToPosition(0) }, 20L)
        })

        nowMultipleStatus.observe(activity) {
//            logi(TAG, "觀察到的內容是=>${it}")
            when (it.first) {
                MultipleStatus.None -> {
                    adapter.clearSelectMap()
                    mBinding.apply {
                        clControl.isVisible = true
                        btnDelete.isVisible = false
                        btnResend.isVisible = false
                        btnMultipleSelectMode.background = ContextCompat.getDrawable(context, R.drawable.ic_baseline_multiple_select_mode)
                    }
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
                    if (it.second == SelectMode.P2P)
                        adapter.selectMode = SelectMode.P2P

                    if (it.second == SelectMode.All)
                        adapter.fullSelectMap()

                    mBinding.apply {
                        btnResend.isVisible = true
                        btnDelete.isVisible = true
                    }
                }

                MultipleStatus.Send -> {
                    adapter.clearSelectMap()
                    mBinding.btnMultipleSelectMode.background = null
                    mBinding.clControl.visibility = View.INVISIBLE
                }
            }
        }
    }

    private val adapter by lazy {
        RecordAdapter(context).apply {
            listener = infoClass
        }
    }
    private val selectClass: SelectClass by lazy { SelectClass() }


    class SelectClass : RecordAdapter.SelectListener {

        val selectMap: HashMap<Long, String> by lazy { HashMap() }
//        var nowSelectIndex = -1

        fun init() = this.apply {
            selectMap.clear()
//            nowSelectIndex = -1
        }

        override fun choose(isSelect: Boolean, id: Long, scanString: String) {
//            logi("紀錄到紀錄選擇除錯", "id是=>$id，select是=>$isSelect")
            if (isSelect) {
                selectMap[id] = scanString
            } else {
                selectMap.remove(id)
            }
//            logi(TAG, "此時的 selectMap 是=>${selectMap.toJson()}")
        }

    }

    private val infoClass: InfoListener by lazy { InfoListener() }

    private val nowSetting: SettingDataItem? by lazy { context.getShare().getNowUseSetting() }

    inner class InfoListener : RecordAdapter.InfoListener {

        override fun resend(scanString: String) {
//            logi(TAG, "點擊到重新送出！掃描到的內容是=>$scanString")
            if (dialog == null) {
                dialog = showConfirmDialog(context.getString(R.string.dialog_notice_title),
                    context.getString(R.string.record_resend_confirm).format(
                        nowSetting?.name,
                        scanString.getSignInPersonByScan(context)
                    ), {
                        resendCallApi(scanString)
                        dialog = null
                    }, {
                        dialog = null
                    })
            }
        }

        override fun showInfo(data: SendRecordEntity) {
//            logi(TAG, "點擊到顯示內容！要顯示的內容是=>${data.toJson()}")
            if (dialog == null) {
                dialog = showMessageDialogOnlyOKButton(
                    context.getString(R.string.record_info_dialog_title).format(data.getSignInPerson(context), data.sendTime.toString("HH:mm:ss")), data.toFullInfo(
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

        mBinding.apply {
            btnDelete.isVisible = false // 需要多重選擇的功能初始化時先隱藏。

            btnResend.isVisible = false // 需要多重選擇的功能初始化時先隱藏。

            rvRecord.adapter = adapter
        }
    }

    private var signInResult = ""

    private val empty: (Throwable?) -> Unit = {} // 是否是空方法判斷

    private fun resendCallApi(scanString: String, nowThNum: Pair<Int, Int> = Pair(0, 0), afterCallAction: (Throwable) -> Unit = empty) {
        // Call API
        val sendRequest = scanString.concatSettingColumn(context)
        val signInTime = Date().time
        signInResult = "${signInTime.toString("yyyy/MM/dd HH:mm:ss")}\n${scanString.getSignInPersonByScan(context)}簽到完成。"
        MainScope().launch {
            if (activity.sendApi(
                    sendRequest, waitingText =
                    if (afterCallAction == empty) {
                        context.getString(R.string.record_resend_progress_text).format(scanString.getSignInPersonByScan(context))
                    } else {
                        context.getString(R.string.record_multiple_resend_progress_text).format(scanString.getSignInPersonByScan(context), nowThNum.first, nowThNum.second)
                    }
                )
            ) {
                // 顯示簽到結果視窗。
                if (afterCallAction == empty) { // 單一重送
                    if (nowSetting?.afterScanAction?.actionMode == ActionMode.OpenBrowser.value) { //進入該網頁開瀏覽器。
                        activity.intentToWebPage(sendRequest)
                        activity.getRecordDao().insertNewRecord(signInTime, scanString, sendRequest, nowSetting ?: return@launch)
                    } else {
                        if (nowSetting?.afterScanAction?.actionMode == ActionMode.StayApp.value) {
                            if (dialog == null) {
                                dialog = activity.showSignInCompleteDialog(signInResult) {
                                    signInResult = ""
                                    dialog = null
                                    activity.getRecordDao().insertNewRecord(signInTime, scanString, sendRequest, nowSetting ?: return@showSignInCompleteDialog)
                                }
                            }
                        } else { // 導向至設定的網頁
                            activity.intentToWebPage(nowSetting?.afterScanAction?.toHtml)
                        }
                    }
                } else {
                    activity.getRecordDao().insertNewRecord(signInTime, scanString, sendRequest, nowSetting ?: return@launch)
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
        mBinding.btnBack.clickWithTrigger {
            activity.onBackPressed()
        }

        mBinding.btnMultipleSelectMode.clickWithTrigger {
            chooseMultipleSelectMode()
        }

        mBinding.btnResend.clickWithTrigger {
            multipleResend()
        }
        mBinding.btnDelete.clickWithTrigger {
            multipleDelete()
        }
    }

    private fun multipleDelete() {
        if (checkNowHasSelecToOperate()) return

        if (dialog == null) {
            activity.showConfirmDialog(context.getString(R.string.dialog_notice_title), context.getString(R.string.record_delete_confirm).format(selectClass.selectMap.size),
                confirmAction = {
                    selectClass.selectMap.keys.forEach {
                        context.getRecordDao().deleteByPkId(it)
                    }
                    setNowStatusToNoneOrSelecting(MultipleStatus.None)
                    dialog = null

                },
                cancelAction = {
                    setNowStatusToNoneOrSelecting(MultipleStatus.None)
                    dialog = null
                })
        }
    }

    private fun checkNowHasSelecToOperate(): Boolean {
        if (selectClass.selectMap.isEmpty()) {
            if (dialog == null) {
                dialog = context.showConfirmDialog(context.getString(R.string.dialog_notice_title), context.getString(R.string.record_multiple_resend_no_select),
                    confirmAction = { // 確定重選
                        dialog = null
                    }, cancelAction = { // 取消操作
                        setNowStatusToNoneOrSelecting(MultipleStatus.None)
                        dialog = null
                    },
                    confirmBtnStr = context.getString(R.string.record_multiple_resend_no_select_confirm),
                    cancelBtnStr = context.getString(R.string.record_multiple_resend_no_select_cancel)
                )
            }
            return true
        }
        return false
    }

    private fun chooseMultipleSelectMode() {
        if (nowMultipleStatus.value?.first == MultipleStatus.None) {
            if (dialog == null) {
                nowMultipleStatus.postValue(Pair(MultipleStatus.SelectMode, SelectMode.None))
                val list = mutableListOf<String>().apply {
                    addAll(context.resources.getStringArray(R.array.multiple_resend_mode))
                }

                dialog = activity.showListDialog(context.getString(R.string.record_multiple_resend_dialog_title), list,
                    selectAction = { selectIndex, _ ->
//                        logi(TAG, "選到的position是=>$selectIndex,,,選到的文字是=>$selectData")
                        setNowStatusToNoneOrSelecting(MultipleStatus.Selecting)
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

                        dialog = null
                    },
                    cancelAction = {
                        setNowStatusToNoneOrSelecting(MultipleStatus.None)
                        dialog = null
                    })
            }
        }
        nowMultipleStatus.postValue(Pair(MultipleStatus.None, SelectMode.None)) // 避免因使用者按太快導致的問題。
    }

    private fun multipleResend() {
        if (checkNowHasSelecToOperate()) return

        if (dialog == null) {
            activity.showConfirmDialog(context.getString(R.string.dialog_notice_title),
                context.getString(R.string.record_multiple_resend_dialog_check_message)
                    .format(nowSetting?.name, selectClass.selectMap.size),
                {
                    nowMultipleStatus.postValue(Pair(MultipleStatus.Send, SelectMode.None))
                    recursiveResend(selectClass.selectMap, selectClass.selectMap.size)
                    dialog = null
                },
                {
                    setNowStatusToNoneOrSelecting(MultipleStatus.None)
                    dialog = null
                })
        }

    }

    private fun recursiveResend(map: HashMap<Long, String>, oriSize: Int) {
        if (map.isEmpty()) { // 全部送完

            if (nowSetting?.afterScanAction?.actionMode == ActionMode.AnotherWeb.value) {
                activity.intentToWebPage(nowSetting?.afterScanAction?.toHtml)
                setNowStatusToNoneOrSelecting(MultipleStatus.None)
            } else {
                dialog = activity.showMessageDialogOnlyOKButton(
                    context.getString(R.string.record_multiple_resend_dialog_success_title),
                    context.getString(R.string.record_multiple_resend_dialog_success_message).format(oriSize, nowSetting?.name)
                ) {
                    setNowStatusToNoneOrSelecting(MultipleStatus.None)
                    dialog = null
                }
            }
            return
        }
        val nowKey = map.keys.first()
        val nowSend = map[nowKey] ?: return
        //每次取第一個來重送
        resendCallApi(nowSend, Pair((oriSize - map.size) + 1, oriSize)) {
            recursiveResend(map.apply { remove(nowKey) }, oriSize)
        }

    }

    private fun setNowStatusToNoneOrSelecting(status: MultipleStatus) {
        (status == MultipleStatus.None).let { isNone ->
            if (isNone)
                nowMultipleStatus.postValue(Pair(MultipleStatus.None, SelectMode.None))

            adapter.apply {
                listener = if (isNone) infoClass else selectClass.init()
                clearSelectMap()
            }
        }
    }

    override fun finish() {
        super.finish()
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    class RecordAdapter(val context: Context) : BaseRecyclerViewDataBindingAdapter<SendRecordEntity>(context, R.layout.adapter_record) {
        private val selectBackGroundColor: Int by lazy { context.getColorByBuildVersion(R.color.gray) }

        var selectMode: SelectMode = SelectMode.None // 這兩個變數都是P2P要使用的
        var selectId: Long = -1L
//        var beforeSelect: Int = -2

        interface Listener

        interface InfoListener : Listener {
            fun resend(scanString: String)
            fun showInfo(data: SendRecordEntity)
        }

        interface SelectListener : Listener {
            fun choose(isSelect: Boolean, id: Long, scanString: String)
        }

        private val isSelectMap: HashMap<Long, Boolean> by lazy { HashMap() }
        private val rangeList: MutableList<LongRange> by lazy { mutableListOf() }

        var listener: Listener? = null

        override fun initViewHolder(viewHolder: ViewHolder) {
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, data: SendRecordEntity) {
            val adapterBinding = viewHolder.binding as AdapterRecordBinding

            adapterBinding.apply {
                tvRecordTime.text = data.sendTime.toString("yyyy/MM/dd HH:mm:ss")
                tvRecordScanContent.text = data.getSignInPerson(context)
                tvRecordSendContent.text = data.sendContent
                tvRecordSendSetting.text = context.getShare().getSettingNameById(data.sendSettingId, data)

                ((listener as? InfoListener) != null).apply {
                    ivRecordResend.isVisible = this
                    ivRecordResend.clickWithTrigger {
                        (listener as RecordActivity.InfoListener).resend(data.scanContent)
                    }
                }
                root.setBackgroundColor(if (isSelectMap[data.sendId] != true) Color.TRANSPARENT else selectBackGroundColor)
            }
        }

        fun clearSelectMap() {
            isSelectMap.clear()
            rangeList.clear()
            selectMode = SelectMode.None
            selectId = -1
            updateContent()
        }

        fun fullSelectMap() {
            CoroutineScope(Dispatchers.Default).launch {
                context.getRecordDao().allData.forEach {
                    it.toSetSelectData(true)
                }
                MainScope().launch {
                    updateContent()
                }
            }
        }

        private fun updateContent() {
            notifyDataSetChanged()
        }


        override fun onItemClick(view: View, position: Int, data: SendRecordEntity): Boolean {
            (listener as? InfoListener)?.apply {
                showInfo(data)
                return true
            }

            if (selectMode == SelectMode.P2P) { // 是P2P的話才要以下操作：
//                logi("紀錄到紀錄選擇除錯", "selectId 是=>$selectId")
//                logi("紀錄到紀錄選擇除錯", "選到的ID是=>${data.sendId},移除判斷是=>${rangeList.any { data.sendId in it }},rangeList 是=>${rangeList.toJson()}")

                if (rangeList.any { data.sendId in it }) { // 第二次選到之前選到過的內容，要在之前選過的選項中，要把那個選項找到，並取消選取。
                    //找到之前選過的，取消
                    val thisRange = rangeList.findMaxRange(data.sendId)
//                    logi("紀錄到紀錄選擇除錯", "即將第一次移除：$thisRange")
//                    logi("紀錄到紀錄選擇除錯", "第一次移除大小=>${context.getRecordDao().searchByIdRange(thisRange).size}")
                    context.getRecordDao().searchByIdRange(thisRange).forEach {
                        it.toSetSelectData(false)
                    }
//                    logi("紀錄到紀錄選擇除錯", "第一次移除後，isSelectMap 是=>${isSelectMap.toJson()}")
                    rangeList.findHaveSmallerRangeToRemove(thisRange)
                    updateContent()
                } else if (selectId == -1L) { // 第一次選取
//                    logi("紀錄到紀錄選擇除錯", "即將第一次選擇，此時的data.sendId是=>${data.sendId}")
                    selectId = data.sendId
                    data.toSetSelectData(true)
                    view.setBackgroundColor(selectBackGroundColor)
                } else if (selectId != -1L && rangeList.none { data.sendId in it }) { // 第二次選到selectIndex以外的內容
                    //要把data.sendId到selectIndex全部選取，放到rangeMap、isSelectMap裡面。

                    rangeList.add(data.sendId.coerceAtMost(selectId)..data.sendId.coerceAtLeast(selectId))
//                    logi("紀錄到紀錄選擇除錯", "即將第二次選擇，DaoSearch結果大小是=>${context.getRecordDao().searchByIdRange(data.sendId..selectId).size}")
                    context.getRecordDao().searchByIdRange(data.sendId..selectId).forEach {
                        isSelectMap[it.sendId] = true
                        it.toSetSelectData(true)
                    }
                    updateContent()
                    selectId = -1L // 選完以後要歸0選擇
//                    beforeSelect = -1
                }
                return false
            }

            (isSelectMap[data.sendId] != true).apply {
                data.toSetSelectData(this)
                view.setBackgroundColor(if (this) selectBackGroundColor else Color.TRANSPARENT)
            }
            return false
        }

        private fun SendRecordEntity.toSetSelectData(isSelect: Boolean) {
            isSelectMap[this.sendId] = isSelect
            (listener as? SelectListener)?.choose(isSelect, this.sendId, this.scanContent)
        }

        override fun onItemLongClick(view: View, position: Int, data: SendRecordEntity): Boolean {
            return false
        }

        /** 移除這個Range，與找到範圍比thisRange更小的Range去移除。*/
        private fun MutableList<LongRange>.findHaveSmallerRangeToRemove(thisRange: LongRange) {
            this.remove(thisRange)
            this.map {
                if (it.first in thisRange && it.last in thisRange) {
//                    logi("紀錄到紀錄選擇除錯","找到較小Range了！是=>$it")
                    return@map it
                }
                return@map null
            }.filterNotNull().forEach { this.remove(it) }
        }

        /**找到給定的數字中，範圍最大的Range*/
        private fun List<LongRange>.findMaxRange(sendId: Long): LongRange {
            return this.sortedBy { it.last - it.first }.last { sendId in it }
        }


    }

}





