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
import utils.logi
import android.content.IntentFilter
import android.widget.TextView
import project.main.model.ActionMode
import project.main.model.SendMode
import tool.getShare


class SettingContentFragment(val settingData: SettingDataItem, val position: Int) : BaseFragment<FragmentSettingContentBinding>(FragmentSettingContentBinding::inflate) {

    private val upDateDataKey by lazy { mContext.getString(R.string.setting_receiver).format(position) }

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
        mBinding.tvSettingNameShow.text = settingData.name + position

    }

    private fun initValue() {

        val setValue = mContext.getShare().getSettingById(settingData.id) //取出設定檔來設定，以避免使用者未儲存就回來這個頁面。
        logi("initValue", "設定前，本頁的settingData是=>${settingData}")
        logi("initValue", "設定前，儲存的settingData是=>${setValue}")
        if (setValue.haveSaved) {
            mBinding.edtSettingNameContent.setText(setValue.name)
            mBinding.tvSettingNameContent.text = setValue.name
            mBinding.tvSettingNameShow.text = setValue.name
        } else {//沒有儲存過的時候要顯示預設的值
            mBinding.tvSettingNameContent.text = mContext.getString(R.string.setting_file_name_default)
            mBinding.tvSettingNameShow.text = mContext.getString(R.string.setting_file_name_default)
            mBinding.edtSettingNameContent.setText("")
        }

//        mBinding.edtSettingNameContent.hint = mContext.getString(R.string.setting_name_title_hint)


//        mBinding.tvSettingNameContent.isVisible = false
    }

    private var openBooleanList = arrayListOf<Boolean>(true, true, true, true, true)

    @SuppressLint("ClickableViewAccessibility")
    private fun initEvent() {

        // Layout打開與關閉設定
        mBinding.clSettingName.setOnClickListener {
            closeAllContentLayout(0)
            logi(TAG, "此時openLayout布林值是：$openBooleanList")
            mBinding.tvSettingNameContent.isVisible = openBooleanList[0]
//            setKeyboard(true, mBinding.edtSettingNameContent)
            openBooleanList[0] = mBinding.clMain.openLayout(openBooleanList[0], mBinding.clSettingNameContent, mBinding.clSettingName)
        }


        mBinding.clScanToDirect.setOnClickListener {
            closeAllContentLayout(1)
            logi(TAG, "此時openLayout布林值是：$openBooleanList")
//            logi(TAG, "此時openLayout布林值是：$openBoolean")
            mBinding.tvScanToDirectContentShadow.isVisible =  openBooleanList[1]
            openBooleanList[1] = mBinding.clMain.openLayout(openBooleanList[1], mBinding.clScanToDirectContent, mBinding.clScanToDirect)
            judgeNeedShowHtmlEdit(settingData.goWebSiteByScan.scanMode == SendMode.ByCustom, settingData.goWebSiteByScan.sendHtml, mBinding.tvScanToDirectContentShadow, mBinding.edtScanToDirectContent)
//            judgeNeedShowHtmlEdit(true," settingData.goWebSiteByScan.sendHtml", mBinding.tvScanToDirectContentShadow, mBinding.edtScanToDirectContent)
        }

        mBinding.clAfterScanAction.setOnClickListener {
            closeAllContentLayout(2)
            logi(TAG, "此時openLayout布林值是：$openBooleanList")
            mBinding.tvAfterScanActionHtmlShadow.isVisible =  openBooleanList[2]
            openBooleanList[2] = mBinding.clMain.openLayout(openBooleanList[2], mBinding.clAfterScanActionContent, mBinding.clAfterScanAction)
            judgeNeedShowHtmlEdit(settingData.afterScanAction.actionMode == ActionMode.AnotherWeb, settingData.afterScanAction.toHtml, mBinding.tvAfterScanActionHtmlShadow, mBinding.edtAfterScanToDirect)
//            judgeNeedShowHtmlEdit(true, "settingData.afterScanAction.toHtml", mBinding.tvAfterScanActionHtmlShadow, mBinding.edtAfterScanToDirect)

        }

        mBinding.clColumnTitle.setOnClickListener {
            closeAllContentLayout(3)
            logi(TAG, "此時openLayout布林值是：$openBooleanList")
            mBinding.tvColumnEditKey.isVisible = openBooleanList[3]
            mBinding.tvColumnEditContent.isVisible = openBooleanList[3]

            openBooleanList[3] = mBinding.clMain.openLayout(openBooleanList[3], mBinding.clColumnEditContent, mBinding.clColumnTitle)

        }

        // 點擊編輯文字框要將Shadow隱藏
        mBinding.edtSettingNameContent.setOnTouchListener { _, _ ->
            mBinding.tvSettingNameContent.isVisible = false
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

        mBinding.edtColumnEditKey.setOnTouchListener { _, _ ->
            mBinding.tvColumnEditKey.isVisible = false
            mBinding.tvColumnEditContent.isVisible = false
            false
        }

        mBinding.edtColumnEditContent.setOnTouchListener { _, _ ->
            mBinding.tvColumnEditKey.isVisible = false
            mBinding.tvColumnEditContent.isVisible = false
            false
        }

        // EditText動作偵測設定
        mBinding.edtSettingNameContent.addTextChangedListener {
            settingData.name = it.toString()
        }

    }

    private fun judgeNeedShowHtmlEdit(needOpen: Boolean, html: String?, vararg needShowViews: View) {
//        logi(TAG, "收到的html是=>$html")
//        logi(TAG, "判斷結果是=>${ (needOpen && !html.isNullOrEmpty())}")
        (needShowViews.first() as? TextView)?.text = html ?: ""
        needShowViews.forEach { it.isVisible = (needOpen && !html.isNullOrEmpty()) }
    }

    /**先全部關閉再打開的實作方法*/
    private fun closeAllContentLayout(callIndex: Int) {
        //要把除了callIndex以外的boolean都重設為true。
        val nowCallIndexBoolean = openBooleanList.getOrNull(callIndex) ?: true
        openBooleanList = arrayListOf<Boolean>(true, true, true, true, true)
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

    fun initReceiver() {
        val intentFilter = IntentFilter() // 過濾器
        intentFilter.addAction(upDateDataKey) // 指定Action
        mActivity.registerReceiver(mBroadcastReceiver, intentFilter) // 註冊廣播接收器
    }

    // 初始化所有設定項，使其為關閉
    private fun initAnimation() {
        openBooleanList = arrayListOf(true, true, true, true, true)
        closeAllContentLayout(4)
    }

    override fun onResume() {
        super.onResume()
        logi("name Setting trace", "onResume")
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
        }
    }


//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment SettingContentFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            SettingContentFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}


