package project.main.tab

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.FragmentSettingContentBinding
import project.main.base.BaseFragment
import project.main.model.SettingDataItem
import uitool.openLayout
import android.content.IntentFilter
import android.widget.TextView
import com.buddha.qrcodeweb.databinding.AdapterSettingColumnBinding
import project.main.base.BaseRecyclerViewDataBindingAdapter
import project.main.model.ActionMode
import project.main.model.SendMode
import tool.dialog.TextDialog
import tool.dialog.showConfirmDialg
import tool.dialog.showMessageDialogOnlyOKButton
import tool.getShare
import uitool.setTextSize
import utils.logi

@SuppressLint("NotifyDataSetChanged") // 每次都要更新所有欄位中的所有值必須加上的Annotation(不然會出現黃色警告)
class SettingContentFragment(val settingData: SettingDataItem, val position: Int) : BaseFragment<FragmentSettingContentBinding>(FragmentSettingContentBinding::inflate) {

    private val upDateDataKey by lazy { mContext.getString(R.string.setting_receiver).format(position) }

    private var textDialog: TextDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()

        initView()

        initEvent()

        initReceiver()
    }

    private val mBroadcastReceiver by lazy { UpdateDataReceiver() }

    private fun unRegisterReceiver() {
        mActivity.unregisterReceiver(mBroadcastReceiver) // 註銷廣播接收器
    }


    private fun initData() {

    }

    private fun initView() {


        // 開發中  暫時隱藏
        mBinding.clScanToDirect.isVisible = false

        mBinding.clAfterScanAction.isVisible = false

        val edtTextSize =14
        mBinding.edtColumnEditName.setTextSize(edtTextSize)
        mBinding.edtColumnEditKey.setTextSize(edtTextSize)
        mBinding.edtColumnEditContent.setTextSize(edtTextSize)

        mBinding.tvSettingNameShow.text = settingData.name

        mBinding.rvColumn.adapter = SettingColumnAdapter(mContext).apply {
            addItem(settingData.fields)
            clickListener = object : SettingColumnAdapter.ClickListener {
                override fun click(index: Int, data: SettingDataItem.SettingField) {
                    openColumnEditLayout(CallMode.Edit, data)
                }

                override fun delete(index: Int) {
                    if (textDialog == null) {
                        val field = settingData.fields[index]
                        textDialog = mContext.showConfirmDialg(mContext.getString(R.string.dialog_notice_title), mContext.getString(R.string.setting_delete_confirm).format(field.columnKey, "欄位"), {
                            settingData.fields.removeAt(index)
                            mBinding.rvColumn.adapter?.notifyDataSetChanged()
                            textDialog = null
                        }, {
                            textDialog = null
                        })
                    }
                }
            }
        }
    }

//    private fun setColumeEditLayoutValue(field: SettingDataItem.SettingField) {
//        mBinding.tvColumnEditName.isVisible = false
//        mBinding.tvColumnEditContent.isVisible = false
//        mBinding.tvColumnEditKey.isVisible = false
//
//        mBinding.tvColumnEditName.text = if (judgeColumnIsEmpty(field.fieldName)) mContext.getString(R.string.setting_adapter_column_title) else field.fieldName
//        mBinding.tvColumnEditKey.text = if (judgeColumnIsEmpty(field.columnKey)) mContext.getString(R.string.setting_adapter_column_key) else field.columnKey
//        mBinding.tvColumnEditContent.text = if (judgeColumnIsEmpty(field.columnValue)) mContext.getString(R.string.setting_adapter_column_value) else field.columnValue
//
//        mBinding.edtColumnEditName.setText(if (judgeColumnIsEmpty(field.fieldName)) "" else field.fieldName)
//        mBinding.edtColumnEditKey.setText(if (judgeColumnIsEmpty(field.columnKey)) "" else field.columnKey)
//        mBinding.edtColumnEditContent.setText(if (judgeColumnIsEmpty(field.columnValue)) "" else field.columnValue)
//    }


    private fun initValue() {

        val setValue = mContext.getShare().getSettingById(settingData.id) // 取出設定檔來設定
        val showName = if (setValue?.haveSaved == true) setValue.name else mContext.getString(R.string.setting_file_name_default)
        mBinding.edtSettingNameContent.setText(if (setValue?.haveSaved == true) showName else "")
        mBinding.tvSettingNameShadow.text = showName
        mBinding.tvSettingNameShow.text = showName


    }

    private val openBooleanList by lazy { resetBooleanList(arrayListOf()) }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEvent() {

        // Layout打開與關閉設定
        mBinding.clSettingName.setOnClickListener {
            closeAllContentLayout(0)
//            logi(TAG, "此時openLayout布林值是：$openBooleanList")
            mBinding.tvSettingNameShadow.isVisible = openBooleanList[0]
//            setKeyboard(true, mBinding.edtSettingNameContent)
            openBooleanList[0] = mBinding.clMain.openLayout(openBooleanList[0], mBinding.clSettingNameContent, mBinding.clSettingName)
        }


        mBinding.clScanToDirect.setOnClickListener {
            closeAllContentLayout(1)
//            logi(TAG, "此時openLayout布林值是：$openBooleanList")
            mBinding.tvScanToDirectContentShadow.isVisible = openBooleanList[1]
            openBooleanList[1] = mBinding.clMain.openLayout(openBooleanList[1], mBinding.clScanToDirectContent, mBinding.clScanToDirect)
            judgeNeedShowHtmlEdit(settingData.goWebSiteByScan.scanMode == SendMode.ByCustom, settingData.goWebSiteByScan.sendHtml, mBinding.tvScanToDirectContentShadow, mBinding.edtScanToDirectContent)
//            judgeNeedShowHtmlEdit(true," settingData.goWebSiteByScan.sendHtml", mBinding.tvScanToDirectContentShadow, mBinding.edtScanToDirectContent)
        }

        mBinding.clAfterScanAction.setOnClickListener {
            closeAllContentLayout(2)
//            logi(TAG, "此時openLayout布林值是：$openBooleanList")
            mBinding.tvAfterScanActionHtmlShadow.isVisible = openBooleanList[2]
            openBooleanList[2] = mBinding.clMain.openLayout(openBooleanList[2], mBinding.clAfterScanActionContent, mBinding.clAfterScanAction)
            judgeNeedShowHtmlEdit(settingData.afterScanAction.actionMode == ActionMode.AnotherWeb, settingData.afterScanAction.toHtml, mBinding.tvAfterScanActionHtmlShadow, mBinding.edtAfterScanToDirect)
//            judgeNeedShowHtmlEdit(true, "settingData.afterScanAction.toHtml", mBinding.tvAfterScanActionHtmlShadow, mBinding.edtAfterScanToDirect)

        }

        mBinding.clColumnTitle.setOnClickListener {
            openColumnEditLayout(callFrom = CallMode.New)
        }

        // 點擊編輯文字框要將Shadow隱藏
        mBinding.edtSettingNameContent.setOnTouchListener { _, _ ->
            mBinding.tvSettingNameShadow.isVisible = false
            false
        }

        mBinding.edtScanToDirectContent.setOnTouchListener { _, _ ->
            mBinding.tvScanToDirectContentShadow.isVisible = false
            false
        }

        mBinding.edtAfterScanToDirect.setOnTouchListener { _, _ ->
            mBinding.tvAfterScanActionHtmlShadow.isVisible = false
            false
        }

        mBinding.edtColumnEditName.setOnTouchListener { _, _ ->
            mBinding.tvColumnEditNameShadow.isVisible = false
            mBinding.tvColumnEditKeyShadow.isVisible = false
            mBinding.tvColumnEditContentShadow.isVisible = false
            false
        }

        mBinding.edtColumnEditKey.setOnTouchListener { _, _ ->
            mBinding.tvColumnEditNameShadow.isVisible = false
            mBinding.tvColumnEditKeyShadow.isVisible = false
            mBinding.tvColumnEditContentShadow.isVisible = false
            false
        }

        mBinding.edtColumnEditContent.setOnTouchListener { _, _ ->
            mBinding.tvColumnEditNameShadow.isVisible = false
            mBinding.tvColumnEditKeyShadow.isVisible = false
            mBinding.tvColumnEditContentShadow.isVisible = false
            false
        }

        // 內容修改設定
        mBinding.edtSettingNameContent.addTextChangedListener {
            settingData.name = it.toString()
        }


        mBinding.ivColumnCheck.setOnClickListener {
            if (saveFieldToData(CallMode.Confirm)) { //儲存成功才要收回編輯Layout與捲動頁面
                openColumnEditLayout(callFrom = CallMode.Confirm)

            }
        }

    }

    /** 按下欄位編輯的綠色勾勾要執行的方法：  */

    private fun saveFieldToData(callFrom: CallMode): Boolean {
        if (mBinding.edtColumnEditKey.text.toString().isEmpty() && callFrom == CallMode.Confirm) { //
            if (textDialog == null) {
                textDialog = mContext.showMessageDialogOnlyOKButton(mContext.getString(R.string.dialog_notice_title), mContext.getString(R.string.setting_adapter_key_can_not_be_empty)) {
                    textDialog = null
                }
            }
            return false
        }

        if (mBinding.edtColumnEditKey.text.toString().isEmpty()) { //如果是空的，不儲存。
            return false
        }

        val editField = SettingDataItem.SettingField(mBinding.edtColumnEditName.text.toString(), mBinding.edtColumnEditKey.text.toString(), mBinding.edtColumnEditContent.text.toString())

        //找到當前的Fields裡面是否有editField，如果沒有就要新增，如果有就要更新舊的值
        if (settingData.fields.none { it.columnKey == editField.columnKey }) { // 找不到，是新增
            settingData.fields.add(editField)
            mBinding.scContent.smoothScrollTo(0, mBinding.clMain.measuredHeight) // 新增的時候才要捲到最下面
        } else { //有找到，是編輯
            val editIndex = settingData.fields.indexOfFirst { it.columnKey == editField.columnKey }
            settingData.fields[editIndex] = editField
        }
        mBinding.rvColumn.adapter?.notifyDataSetChanged() // 更新畫面

        return true
    }

    enum class CallMode {
        Edit,
        New,
        Confirm,
        OutSave
    }

    /** 編輯或新增都會打開欄位編輯器 */
    private fun openColumnEditLayout(callFrom: CallMode, field: SettingDataItem.SettingField? = null): Boolean {

        if (callFrom == CallMode.Confirm) openBooleanList[3] = false

        val nowIsOpenEditLayoutAndClickEdit = !openBooleanList[3] && callFrom == CallMode.Edit
        logi(TAG, "openColumnEditLayout時，open是=>${openBooleanList[3]},callFrom =>$callFrom")
        logi(TAG, "openColumnEditLayout時，nowIsOpenEditLayoutAndClickEdit是=>${nowIsOpenEditLayoutAndClickEdit}")

//        mBinding.tvColumnEditNameShadow.isVisible = if (nowIsOpenEditLayoutAndClickEdit) false else openBooleanList[3]  //是打開的情況下按下編輯就絕對不再顯示Shadow文字。
//        mBinding.tvColumnEditContentShadow.isVisible = if (nowIsOpenEditLayoutAndClickEdit) false else openBooleanList[3]
//        mBinding.tvColumnEditKeyShadow.isVisible = if (nowIsOpenEditLayoutAndClickEdit) false else openBooleanList[3]
        mBinding.tvColumnEditNameShadow.isVisible = false // 不知道怎麼回事，editText在某次調整以後就會正常顯示(之前滑出後都沒有辦法直接顯示hint)，所以shadow部分暫時註解。
        mBinding.tvColumnEditContentShadow.isVisible = false
        mBinding.tvColumnEditKeyShadow.isVisible = false

        mBinding.tvColumnEditNameShadow.text = if (judgeColumnIsEmpty(field?.fieldName)) mContext.getString(R.string.setting_adapter_column_title) else field?.fieldName
        mBinding.tvColumnEditKeyShadow.text = if (judgeColumnIsEmpty(field?.columnKey)) mContext.getString(R.string.setting_adapter_column_key) else field?.columnKey
        mBinding.tvColumnEditContentShadow.text = if (judgeColumnIsEmpty(field?.columnValue)) mContext.getString(R.string.setting_adapter_column_value) else field?.columnValue

        mBinding.edtColumnEditName.setText(if (judgeColumnIsEmpty(field?.fieldName)) "" else field?.fieldName)
        mBinding.edtColumnEditKey.setText(if (judgeColumnIsEmpty(field?.columnKey)) "" else field?.columnKey)
        mBinding.edtColumnEditContent.setText(if (judgeColumnIsEmpty(field?.columnValue)) "" else field?.columnValue)

        if (!nowIsOpenEditLayoutAndClickEdit) {// 打開的情況按下編輯不控制EditLayout。
            logi(TAG, "openColumnEditLayout時，即將執行開關動作，此時 nowIsOpenEditLayoutAndClickEdit是=>$nowIsOpenEditLayoutAndClickEdit")
            closeAllContentLayout(3)
            openBooleanList[3] = mBinding.clMain.openLayout(openBooleanList[3], mBinding.clColumnEditContent, mBinding.clColumnTitle)
        }


        return openBooleanList[3]
    }

    private fun judgeColumnIsEmpty(judgeContent: String?) = judgeContent.isNullOrEmpty()

    private fun judgeNeedShowHtmlEdit(needOpen: Boolean, html: String?, vararg needShowViews: View) {
        (needShowViews.first() as? TextView)?.text = html ?: ""
        needShowViews.forEach { it.isVisible = (needOpen && !html.isNullOrEmpty()) }
    }

    /** 先全部關閉再打開的實作方法 */
    private fun closeAllContentLayout(callIndex: Int) {
        //要把除了callIndex以外的boolean都重設為true。
        val nowCallIndexBoolean = openBooleanList.getOrNull(callIndex) ?: true

        resetBooleanList(openBooleanList)
        openBooleanList[callIndex] = nowCallIndexBoolean
        mBinding.clMain.openLayout(false, mBinding.clSettingNameContent, mBinding.clSettingName)
        mBinding.clMain.openLayout(false, mBinding.clScanToDirectContent, mBinding.clScanToDirect)
        mBinding.clMain.openLayout(false, mBinding.clAfterScanActionContent, mBinding.clAfterScanAction)
        mBinding.clMain.openLayout(false, mBinding.clColumnEditContent, mBinding.clColumnTitle)
    }

//    private fun setKeyboard(open: Boolean, editFocus: EditText) {
//        if (open) { // 關閉中，要打開
//            if (editFocus.requestFocus()) {
//                val imm = (mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager) ?: return
//                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
//            }
//        } else { //打開中，要關閉
//            if (mActivity.currentFocus != null) {
//                ((mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)).hideSoftInputFromWindow(mActivity.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
//            }
//        }
//    }

    private fun initReceiver() {
        val intentFilter = IntentFilter() // 過濾器
        intentFilter.addAction(upDateDataKey) // 指定Action
        mActivity.registerReceiver(mBroadcastReceiver, intentFilter) // 註冊廣播接收器
    }

    // 初始化所有設定項，使其為關閉
    private fun initAnimation() {
        resetBooleanList(openBooleanList)
        closeAllContentLayout(4)
    }

    private fun resetBooleanList(arrayList: ArrayList<Boolean>): ArrayList<Boolean> {
        arrayList.clear()
        (0 until 5).forEach { _ -> arrayList.add(true) }
        return arrayList
    }

    override fun onResume() {
        super.onResume()
//        logi("name Setting trace", "onResume")
        initValue() // 返回時更新值
    }

    override fun onPause() {
        super.onPause()
//        setKeyboard(false, mBinding.edtSettingNameContent)
        initAnimation()
    }

    override fun onDetach() {
        super.onDetach()
        unRegisterReceiver()
    }

    inner class UpdateDataReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            initValue() // 收到SaveData發的BroadCast時要更新值

            // 按下儲存時要做綠色勾勾做的事
            saveFieldToData(CallMode.OutSave) // 無論是否儲存成功都要收回編輯Layout與捲動頁面
            openColumnEditLayout(callFrom = CallMode.Confirm)
            closeAllContentLayout(4)
            mContext.getShare().savaSetting(settingData)
            mContext.getShare().setNowUseSetting(settingData)


        }
    }

    class SettingColumnAdapter(val context: Context) : BaseRecyclerViewDataBindingAdapter<SettingDataItem.SettingField>(context, R.layout.adapter_setting_column) {

        interface ClickListener {
            fun click(index: Int, data: SettingDataItem.SettingField)
            fun delete(index: Int)
        }

        var clickListener: ClickListener? = null

        override fun initViewHolder(viewHolder: ViewHolder) {

        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, data: SettingDataItem.SettingField) {
            val adapterBinding = viewHolder.binding as AdapterSettingColumnBinding
            adapterBinding.tvSettingColumnName.text = data.fieldName
            adapterBinding.tvSettingColumnKey.text = data.columnKey
            adapterBinding.tvSettingColumnValue.text = if (data.columnValue.isEmpty()) "未設定值" else data.columnValue
            adapterBinding.tvSettingColumnValue.setTextColor(if (data.columnValue.isEmpty()) context.getColor(R.color.gray) else context.getColor(R.color.theme_blue))

        }

        override fun onItemClick(view: View, position: Int, data: SettingDataItem.SettingField): Boolean {
//            logi(TAG, "data=>${data}點擊到了！")
            clickListener?.click(position, data)
            return false
        }

        override fun onItemLongClick(view: View, position: Int, data: SettingDataItem.SettingField): Boolean {
            clickListener?.delete(position)
            return false
        }
    }
}


