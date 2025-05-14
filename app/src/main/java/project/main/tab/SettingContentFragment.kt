package project.main.tab

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.AdapterSettingColumnBinding
import com.buddha.qrcodeweb.databinding.FragmentSettingContentBinding
import com.timmymike.viewtool.clickWithTrigger
import project.main.base.BaseFragment
import project.main.base.BaseRecyclerViewDataBindingAdapter
import project.main.model.ActionMode
import project.main.model.FieldType
import project.main.model.SendMode
import project.main.model.SettingDataItem
import tool.dialog.Dialog
import tool.dialog.showConfirmDialog
import tool.dialog.showMessageDialogOnlyOKButton
import tool.getShare
import uitool.openLayout
import uitool.setTextSize
import utils.getColorByBuildVersion
import utils.setKeyboard

@SuppressLint("NotifyDataSetChanged") // 每次都要更新所有欄位中的所有值必須加上的Annotation(不然會出現黃色警告)
class SettingContentFragment : BaseFragment<FragmentSettingContentBinding>(FragmentSettingContentBinding::inflate) {
    companion object {
        const val BUNDLE_KEY_SETTING_DATA = "BUNDLE_KEY_SETTING_DATA"
        const val BUNDLE_KEY_POSITION = "BUNDLE_KEY_POSITION"
    }

    private val settingData by lazy {
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(BUNDLE_KEY_SETTING_DATA, SettingDataItem::class.java)
        } else {
            arguments?.getSerializable(BUNDLE_KEY_SETTING_DATA)
        }) as SettingDataItem
    }

    private val position by lazy { arguments?.getInt(BUNDLE_KEY_POSITION) }

    private val upDateDataKey by lazy { mContext.getString(R.string.setting_receiver).format(position) }

    private var textDialog: Dialog? = null

    private var storeStatus: Int = FieldType.AddColumn.value // 現在的欄位編輯狀態，決定儲存的內容與顯示的Type。

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()

        initView()

        initEvent()

        initReceiver()
    }

    private val mBroadcastReceiver by lazy { UpdateDataReceiver() }


    private fun initData() {

    }

    private fun initView() {

        val edtTextSize = 14
        mBinding.edtColumnEditName.setTextSize(edtTextSize)
        mBinding.edtColumnEditKey.setTextSize(edtTextSize)
        mBinding.edtColumnEditContent.setTextSize(edtTextSize)

        mBinding.tvSettingNameShow.text = settingData.name

        mBinding.rvColumn.adapter = SettingColumnAdapter(mContext).apply {
            addItem(settingData.fields)
            clickListener = object : SettingColumnAdapter.ClickListener {
                override fun click(index: Int, data: SettingDataItem.SettingField) {
                    storeStatus = data.fieldType
                    openColumnEditLayout(data)
                }

                override fun delete(index: Int) {
                    if (textDialog == null) {
                        val field = settingData.fields[index]
                        textDialog = mContext.showConfirmDialog(mContext.getString(R.string.dialog_notice_title), mContext.getString(R.string.setting_delete_confirm).format(field.columnKey, "欄位"), {
                            settingData.fields.removeAt(index)
                            mBinding.rvColumn.adapter?.notifyDataSetChanged()
                            textDialog = null
                        }, {
                            textDialog = null
                        })
                    }
                }

                override fun couldNotDelete(fieldName: String) {
                    if (textDialog == null) {
                        textDialog = mActivity.showMessageDialogOnlyOKButton(mContext.getString(R.string.dialog_notice_title), mContext.getString(R.string.setting_could_not_delete).format(fieldName)) {
                            textDialog = null
                        }
                    }
                }
            }
        }
    }

    private fun setScreenValue() {
//        logi(TAG,"setScreenValue 時的 Setting是=>$settingData")
        val setValue = mContext.getShare().getSettingById(settingData.id) // 取出設定檔來設定
        val showName = if (setValue?.haveSaved == true) setValue.name else mContext.getString(R.string.setting_file_name_default)
        mBinding.edtSettingNameContent.setText(if (setValue?.haveSaved == true) showName else "")
        mBinding.tvSettingNameShadow.text = showName
        mBinding.tvSettingNameShow.text = showName

        mBinding.setScanModeTextValue(settingData.goWebSiteByScan.scanMode == SendMode.ByScan.value) // false是自定義，true是掃到什麼傳什麼

        mBinding.setAfterScanAction(settingData.afterScanAction)

    }

    private fun FragmentSettingContentBinding.setScanModeTextValue(isChecked: Boolean) {
        this.apply {
            swDirect.isChecked = isChecked
            val result = if (isChecked) mContext.getString(R.string.setting_file_send_mode_by_scan) else
                mContext.getString(R.string.setting_file_send_mode_by_custom)
            tvScanToDirectShow.text = result
            tvScanToDirectHtml.isVisible = !isChecked
            tvScanToDirectHtml.text = settingData.goWebSiteByScan.sendHtml
            tvScanToDirectContentShow.text = result
            edtScanToDirectContent.isVisible = !isChecked
            edtScanToDirectContent.setText(settingData.goWebSiteByScan.sendHtml)
            tvScanToDirectContentShadow.text = settingData.goWebSiteByScan.sendHtml
            tvScanToDirectContentShadow.isVisible = !isChecked
        }
    }

    private fun FragmentSettingContentBinding.setAfterScanAction(action: SettingDataItem.AfterScanAction) {
        this.apply {
            tvAfterScanActionShow.text = when (action.actionMode) {
                ActionMode.StayApp.value -> {
                    setAllChoiceTextToGrayExclusiveIndex(0)
                    mContext.getString(R.string.setting_file_scan_after_action_1)
                }

                ActionMode.OpenBrowser.value -> {
                    setAllChoiceTextToGrayExclusiveIndex(1)
                    mContext.getString(R.string.setting_file_scan_after_action_2)
                }

                ActionMode.AnotherWeb.value -> {
                    setAllChoiceTextToGrayExclusiveIndex(2)
                    mContext.getString(R.string.setting_file_scan_after_action_3)
                }

                else -> "Not Appear"
            }
            (action.actionMode == ActionMode.AnotherWeb.value).apply {
                tvAfterScanActionHtml.isVisible = this
                tvAfterScanActionHtml.text = action.toHtml
                edtAfterScanToDirect.setText(action.toHtml)
            }

        }
    }

    private fun FragmentSettingContentBinding.setAllChoiceTextToGrayExclusiveIndex(index: Int) {
        val textViewArray = arrayOf(tvAfterScanActionShow1, tvAfterScanActionShow2, tvAfterScanActionShow3)
        this.apply {
            textViewArray.forEach { it.setTextColor(mContext.getColorByBuildVersion(R.color.dark_gray)) }
            textViewArray[index].setTextColor(mContext.getColorByBuildVersion(R.color.theme_blue))
        }
    }

    //控制當前應該只有一個編輯框打開的布林陣列，
    // 第0個->設定檔名稱
    // 第1個->導向網址
    // 第2個->掃碼完成後動作
    // 第3個->欄位設定
    // 第4個->預設空值
    private val openBooleanList by lazy { resetBooleanList(arrayListOf()) }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEvent() {

        // Layout打開與關閉設定
        mBinding.clSettingName.clickWithTrigger {
            closeAllContentLayout(0)
//            logi(TAG, "此時openLayout布林值是：$openBooleanList")
            mBinding.tvSettingNameShadow.isVisible = openBooleanList[0]
//            mActivity.setKeyboard(true, mBinding.edtSettingNameContent)
            openBooleanList[0] = mBinding.clMain.openLayout(openBooleanList[0], mBinding.clSettingNameContent, mBinding.clSettingName)
        }

        mBinding.clScanToDirect.clickWithTrigger {
            closeAllContentLayout(1)
//            logi(TAG, "此時openLayout布林值是：$openBooleanList")
            mBinding.tvScanToDirectContentShadow.isVisible = openBooleanList[1]
            openBooleanList[1] = mBinding.clMain.openLayout(openBooleanList[1], mBinding.clScanToDirectContent, mBinding.clScanToDirect)
            judgeNeedShowHtmlEdit(settingData.goWebSiteByScan.scanMode == SendMode.ByCustom.value, settingData.goWebSiteByScan.sendHtml, mBinding.tvScanToDirectContentShadow, mBinding.edtScanToDirectContent)
//            judgeNeedShowHtmlEdit(true," settingData.goWebSiteByScan.sendHtml", mBinding.tvScanToDirectContentShadow, mBinding.edtScanToDirectContent)
        }

        mBinding.clAfterScanAction.clickWithTrigger {
            closeAllContentLayout(2)
//            logi(TAG, "此時openLayout布林值是：$openBooleanList")
            mBinding.tvAfterScanActionHtmlShadow.isVisible = openBooleanList[2]
            openBooleanList[2] = mBinding.clMain.openLayout(openBooleanList[2], mBinding.clAfterScanActionContent, mBinding.clAfterScanAction)
            judgeNeedShowHtmlEdit(settingData.afterScanAction.actionMode == ActionMode.AnotherWeb.value, settingData.afterScanAction.toHtml, mBinding.tvAfterScanActionHtmlShadow, mBinding.edtAfterScanToDirect)
//            judgeNeedShowHtmlEdit(true, "settingData.afterScanAction.toHtml", mBinding.tvAfterScanActionHtmlShadow, mBinding.edtAfterScanToDirect)
        }

        mBinding.clColumnTitle.clickWithTrigger {
            storeStatus = FieldType.AddColumn.value
            openColumnEditLayout()
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

//        mBinding.edtColumnEditName.setOnTouchListener { _, _ ->
//            mBinding.tvColumnEditNameShadow.isVisible = false
//            mBinding.tvColumnEditKeyShadow.isVisible = false
//            mBinding.tvColumnEditContentShadow.isVisible = false
//            false
//        }
//
//        mBinding.edtColumnEditKey.setOnTouchListener { _, _ ->
//            mBinding.tvColumnEditNameShadow.isVisible = false
//            mBinding.tvColumnEditKeyShadow.isVisible = false
//            mBinding.tvColumnEditContentShadow.isVisible = false
//            false
//        }
//
//        mBinding.edtColumnEditContent.setOnTouchListener { _, _ ->
//            mBinding.tvColumnEditNameShadow.isVisible = false
//            mBinding.tvColumnEditKeyShadow.isVisible = false
//            mBinding.tvColumnEditContentShadow.isVisible = false
//            false
//        }

        // 內容修改設定 //因為儲存時要更新到Tab，所以如果儲存時再指定值會來不及。
        mBinding.edtSettingNameContent.addTextChangedListener {
            settingData.name = it.toString()
        }

        mBinding.edtColumnEditKey.addTextChangedListener {
            delayToGetFieldToData()
        }

        mBinding.edtColumnEditName.addTextChangedListener {
            delayToGetFieldToData()
        }

        mBinding.edtColumnEditContent.addTextChangedListener {
            delayToGetFieldToData()
        }

        mBinding.swDirect.setOnCheckedChangeListener { _, isChecked ->
            storeDataToSettingItem()
            mBinding.apply {
                tvScanToDirectContentShow.text = if (isChecked) mContext.getString(R.string.setting_file_send_mode_by_scan) else
                    mContext.getString(R.string.setting_file_send_mode_by_custom)
                edtScanToDirectContent.isVisible = !isChecked //自定義(為false時)才要顯示
                tvScanToDirectContentShadow.isVisible = false
            }
        }

        mBinding.edtScanToDirectContent.addTextChangedListener {
            storeDataToSettingItem()
        }

        mBinding.tvAfterScanActionShow1.clickWithTrigger {
            mBinding.setAllChoiceTextToGrayExclusiveIndex(0)
            mBinding.tvAfterScanActionHtmlShadow.isVisible = false
            mBinding.edtAfterScanToDirect.isVisible = false
            settingData.afterScanAction.actionMode = ActionMode.StayApp.value
        }

        mBinding.tvAfterScanActionShow2.clickWithTrigger {
            mBinding.setAllChoiceTextToGrayExclusiveIndex(1)
            mBinding.tvAfterScanActionHtmlShadow.isVisible = false
            mBinding.edtAfterScanToDirect.isVisible = false
            settingData.afterScanAction.actionMode = ActionMode.OpenBrowser.value
        }

        mBinding.tvAfterScanActionShow3.clickWithTrigger {
            mBinding.setAllChoiceTextToGrayExclusiveIndex(2)
            mBinding.tvAfterScanActionHtmlShadow.isVisible = false
            mBinding.edtAfterScanToDirect.isVisible = true
            settingData.afterScanAction.actionMode = ActionMode.AnotherWeb.value
        }

        mBinding.edtAfterScanToDirect.addTextChangedListener {
            storeDataToSettingItem()
        }


        mBinding.ivColumnCheck.clickWithTrigger {
            if (getFieldToData()) { //儲存成功才要收回編輯Layout與捲動頁面
                openColumnEditLayout()
            }
        }
    }

    private fun delayToGetFieldToData() {

        if (storeStatus != FieldType.CanNotBeDelete.value && nowEdtNameIsSettingId()) {
            // 若欄位名稱是保留字的話，不做設定為資料的處理
            // 只是，這樣的處理方式，在「使用者新增欄位名稱為保留值」，且其他欄位有新增內容時，會沒有「是否取消變更？」的效果。
            return
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                if ((requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).isActive) {
                    getFieldToData()
                }
            }, 500L)
        }
    }


    /** 按下欄位編輯的綠色勾勾要執行的方法：
     *  成功儲存回傳true，失敗回傳false。
     * */
    private fun getFieldToData(): Boolean {

        if (mBinding.edtColumnEditKey.text.toString().isEmpty() &&
            mBinding.edtColumnEditName.text.toString().isEmpty() &&
            mBinding.edtColumnEditContent.text.toString().isEmpty()
        )
            return false  //全部空值，不儲存。(通常發生在取消新增或直接儲存的情況。)


        if (mBinding.edtColumnEditContent.text.toString().isEmpty() && mBinding.edtColumnEditKey.text.toString().isEmpty() && storeStatus == FieldType.KeyColumn.value) { // 不能只有欄位索引是空的。
            showFieldCouldNotEmptyDialog()
            return false
        }
        val editField = when (storeStatus) {//KeyColumn
            FieldType.KeyColumn.value -> {
                SettingDataItem.SettingField(FieldType.KeyColumn.value, mBinding.tvColumnEditNameShadow.text.toString(), mBinding.edtColumnEditContent.text.toString(), null)
            }

            FieldType.CanNotBeDelete.value -> {
                SettingDataItem.SettingField(FieldType.CanNotBeDelete.value, mBinding.tvColumnEditNameShadow.text.toString(), mBinding.edtColumnEditKey.text.toString(), mBinding.edtColumnEditContent.text.toString())
            }

            FieldType.AddColumn.value -> {
                SettingDataItem.SettingField(FieldType.AddColumn.value, mBinding.edtColumnEditName.text.toString(), mBinding.edtColumnEditKey.text.toString(), mBinding.edtColumnEditContent.text.toString())
            }

            else -> null
        }

        //找到當前的Fields裡面是否有editField，如果沒有就要新增，如果有就要更新舊的值
        if (settingData.fields.none { it.columnKey == editField?.columnKey || it.fieldName == editField?.fieldName }) { // 找不到，是新增，或代表只要名稱相同或欄位相同都是編輯。
            settingData.fields.add(editField ?: return false)
            mBinding.scContent.smoothScrollTo(0, mBinding.clMain.measuredHeight) // 新增的時候才要捲到最下面
        } else { //有找到，是編輯
            val editIndex = settingData.fields.indexOfFirst { it.columnKey == editField?.columnKey || it.fieldName == editField?.fieldName }
            settingData.fields[editIndex] = editField ?: return false
        }
        mBinding.rvColumn.adapter?.notifyDataSetChanged() // 更新畫面
//        logi(TAG, "儲存完畢的fields是=>${settingData.fields}")
        return true
    }

    private fun showFieldCouldNotEmptyDialog() {
        if (textDialog == null) {
            textDialog = mContext.showMessageDialogOnlyOKButton(mContext.getString(R.string.dialog_notice_title), mContext.getString(R.string.setting_adapter_key_can_not_be_empty)) {
                textDialog = null
            }
        }
    }

    /** 編輯或新增都會打開欄位編輯器 */
    private fun openColumnEditLayout(field: SettingDataItem.SettingField? = null): Boolean {

//        if (callFrom == CallMode.Confirm) openBooleanList[3] = false

        val nowIsOpenEditLayoutAndClickEdit = !openBooleanList[3]
//        logi(TAG, "openColumnEditLayout時，open是=>${openBooleanList[3]},callFrom =>$callFrom")
//        logi(TAG, "openColumnEditLayout時，nowIsOpenEditLayoutAndClickEdit是=>${nowIsOpenEditLayoutAndClickEdit}")
        mBinding.apply {

//        mBinding.tvColumnEditNameShadow.isVisible = if (nowIsOpenEditLayoutAndClickEdit) false else openBooleanList[3]  //是打開的情況下按下編輯就絕對不再顯示Shadow文字。
//        mBinding.tvColumnEditContentShadow.isVisible = if (nowIsOpenEditLayoutAndClickEdit) false else openBooleanList[3]
//        mBinding.tvColumnEditKeyShadow.isVisible = if (nowIsOpenEditLayoutAndClickEdit) false else openBooleanList[3]

            tvColumnEditContentShadow.isVisible = false // 不知道怎麼回事，editText在某次調整以後就會正常顯示(之前滑出後都沒有辦法直接顯示hint)，所以shadow部分暫時關閉與註解。

            // ColumnEditName =>欄位名稱
            // ColumnEditKey =>欄位索引
            // ColumnEditContent =>欄位值

//            logi("openColumnEditLayout", "storeStatus 是=>${storeStatus}")

            tvColumnEditNameShadow.isVisible = storeStatus == FieldType.CanNotBeDelete.value
            tvColumnEditNameShadow.text = if (judgeColumnIsEmpty(field?.fieldName)) mContext.getString(R.string.setting_adapter_column_title) else field?.fieldName

            tvColumnEditKeyShadow.isVisible = storeStatus == FieldType.KeyColumn.value // 只有是掃碼時才要顯示，因此直接指定值。
            tvColumnEditKeyShadow.text = field?.fieldName
            edtColumnEditName.isVisible = storeStatus == FieldType.AddColumn.value
            edtColumnEditName.setText(field?.fieldName ?: "")
            edtColumnEditKey.isVisible = storeStatus != FieldType.KeyColumn.value
            edtColumnEditKey.setText(field?.columnKey ?: "")

            edtColumnEditContent.setText(if (storeStatus == FieldType.KeyColumn.value) field?.columnKey else field?.columnValue ?: "")
            //無論如何都要顯示value的編輯欄位。
            edtColumnEditContent.isVisible = true
        }
        if (!nowIsOpenEditLayoutAndClickEdit) { // 打開的情況按下編輯不控制EditLayout。
//            logi(TAG, "openColumnEditLayout時，即將執行開關動作，此時 nowIsOpenEditLayoutAndClickEdit是=>$nowIsOpenEditLayoutAndClickEdit")
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


    private fun initReceiver() {
        val intentFilter = IntentFilter() // 過濾器
        intentFilter.addAction(upDateDataKey) // 指定Action
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mActivity.registerReceiver(mBroadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            mActivity.registerReceiver(mBroadcastReceiver, intentFilter)
        }

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
        setScreenValue() // 返回時更新值
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

    private fun unRegisterReceiver() {
        mActivity.unregisterReceiver(mBroadcastReceiver) // 註銷廣播接收器
    }


    fun storeDataToSettingItem() {

        settingData.apply {
            // 設定檔名稱
            mBinding.edtSettingNameContent.text.toString().let {
                if (it.isEmpty())
                    return@let
                name = it
            }

            // 掃碼網址內容
            goWebSiteByScan = SettingDataItem.GoWebSiteByScan().apply {
                scanMode = if (mBinding.swDirect.isChecked) SendMode.ByScan.value else SendMode.ByCustom.value
                sendHtml = mBinding.edtScanToDirectContent.text.toString()
            }

            // 掃碼後動作(導到網頁)
            afterScanAction.toHtml = mBinding.edtAfterScanToDirect.text.toString()

        }
    }

    inner class UpdateDataReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //以下判斷使用者想要透過新增的方法來修改預設欄位值
            if (storeStatus == FieldType.AddColumn.value && nowEdtNameIsSettingId()) {
                if (textDialog == null) {
                    textDialog = mContext.showMessageDialogOnlyOKButton(mContext.getString(R.string.dialog_notice_title), mContext.getString(R.string.setting_illegal_operation_can_not_modify_scan_var)) {
                        textDialog = null
                    }
                }
                return
            }
            //將畫面內容設定值儲存至Data後再儲存進SharedPreference
            storeDataToSettingItem()
            // 按下儲存時要做綠色勾勾做的事
            getFieldToData()

            mContext.getShare().savaSetting(settingData)
            mContext.getShare().setNowUseSetting(settingData)
            openColumnEditLayout()
            closeAllContentLayout(4) // 無論是否儲存成功都要收回編輯Layout與捲動頁面
            setScreenValue() // 收到SaveData發的BroadCast時，儲存完畢要更新值
            mActivity.setKeyboard(false)
        }
    }

    private fun nowEdtNameIsSettingId(): Boolean {
        val settingIds = setOf(
            mContext.getString(R.string.setting_id_title_default),
            mContext.getString(R.string.setting_name_title_default),
            mContext.getString(R.string.setting_password_title_default)
        )

        return settingIds.any { it == mBinding.edtColumnEditName.text.toString() }
    }

    class SettingColumnAdapter(val context: Context) : BaseRecyclerViewDataBindingAdapter<SettingDataItem.SettingField>(context, R.layout.adapter_setting_column) {

        interface ClickListener {
            fun click(index: Int, data: SettingDataItem.SettingField)
            fun delete(index: Int)
            fun couldNotDelete(fieldName: String)
        }

        var clickListener: ClickListener? = null

        override fun initViewHolder(viewHolder: ViewHolder) {

        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, data: SettingDataItem.SettingField) {
            val adapterBinding = viewHolder.binding as AdapterSettingColumnBinding
            val layouts = arrayOf(adapterBinding.icKey, adapterBinding.icValue)
            layouts.forEach { it.root.isVisible = false } // 老方法，把所有Layout關起來再打開要開的那個。

            adapterBinding.apply {
                if (data.fieldType != FieldType.CanNotBeDelete.value) { // 不是密碼的欄位都要是藍底白字
                    root.setBackgroundColor(context.getColorByBuildVersion(R.color.theme_blue))
                    setTextViewsToColor(context.getColorByBuildVersion(R.color.white), icKey.tvSettingColumnName, icKey.tvSettingColumnKey, icKey.tvSettingColumnKey, icValue.tvSettingColumnName, icValue.tvSettingColumnKey, icValue.tvSettingColumnValue)
                } else {
                    root.setBackgroundColor(context.getColorByBuildVersion(R.color.transparent))
                    setTextViewsToColor(context.getColorByBuildVersion(R.color.theme_blue), icKey.tvSettingColumnName, icKey.tvSettingColumnKey, icValue.tvSettingColumnName, icValue.tvSettingColumnKey, icValue.tvSettingColumnValue)
                }

                if (data.fieldType == FieldType.KeyColumn.value) {
                    icKey.apply {
                        tvSettingColumnName.text = data.fieldName
                        tvSettingColumnKey.text = data.columnKey
                    }
                    layouts.getOrNull(0)?.root?.isVisible = true
                } else {
                    icValue.apply {
                        tvSettingColumnName.text = if (data.fieldName.isEmpty()) context.getString(R.string.setting_adapter_column_title_default) else data.fieldName
                        tvSettingColumnKey.text = data.columnKey
                        tvSettingColumnValue.text = if (data.columnValue.isNullOrEmpty()) "未設定值" else data.columnValue
                    }
                    layouts.getOrNull(1)?.root?.isVisible = true

                }
            }
        }

        private fun setTextViewsToColor(color: Int, vararg tvs: TextView) {
            tvs.forEach { it.setTextColor(color) }
        }

        override fun onItemClick(view: View, position: Int, data: SettingDataItem.SettingField): Boolean {
//            logi(TAG, "data=>${data}點擊到了！")
            clickListener?.click(position, data)
            return false
        }

        override fun onItemLongClick(view: View, position: Int, data: SettingDataItem.SettingField): Boolean {
            if (data.fieldType == FieldType.AddColumn.value) { // 只有由使用者新增的的才可以刪除。
                clickListener?.delete(position)
            } else {
                clickListener?.couldNotDelete(data.fieldName)
            }
            return false
        }
    }
}




