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
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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


        mBinding.tvSettingNameContent.isVisible = false
    }

    var openBoolean = true

    @SuppressLint("ClickableViewAccessibility")
    private fun initEvent() {

        mBinding.clSettingName.setOnClickListener {
            closeAllContentLayout()
            mBinding.tvSettingNameContent.isVisible = openBoolean
            setKeyboard(openBoolean, mBinding.edtSettingNameContent)
            openBoolean = mBinding.clMain.openLayout(openBoolean, mBinding.clSettingNameContent, mBinding.clSettingName)
        }

        mBinding.edtSettingNameContent.setOnTouchListener { _, _ ->
            mBinding.tvSettingNameContent.isVisible = false
            false
        }

        mBinding.edtSettingNameContent.addTextChangedListener {
            settingData.name = it.toString()
        }

        mBinding.clScanToDirect.setOnClickListener {
            closeAllContentLayout()
//            logi(TAG, "此時openLayout布林值是：$openBoolean")
            openBoolean = mBinding.clMain.openLayout(openBoolean, mBinding.clScanToDirectContent, mBinding.clScanToDirect)
        }

        mBinding.clAfterScanAction.setOnClickListener {
            closeAllContentLayout()
            openBoolean = mBinding.clMain.openLayout(openBoolean, mBinding.clAfterScanActionContent, mBinding.clAfterScanAction)

        }

    }

    /**先全部關閉再打開的實作方法*/
    private fun closeAllContentLayout() {
        mBinding.clMain.openLayout(false, mBinding.clSettingNameContent, mBinding.clSettingName)
        mBinding.clMain.openLayout(false, mBinding.clScanToDirectContent, mBinding.clScanToDirect)
        mBinding.clMain.openLayout(false, mBinding.clAfterScanActionContent, mBinding.clAfterScanAction)

    }


    private fun setKeyboard(open: Boolean, editFocus: EditText) {
        if (open) { // 關閉中，要打開
            if (editFocus.requestFocus()) {
                val imm = (mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager) ?: return
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
            }
        } else { //打開中，要關閉
            if (mActivity.currentFocus != null) {
                ((mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)).hideSoftInputFromWindow(mActivity.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    fun initReceiver() {
        val intentFilter = IntentFilter() // 過濾器
        intentFilter.addAction(upDateDataKey) // 指定Action
        mActivity.registerReceiver(mBroadcastReceiver, intentFilter) // 註冊廣播接收器
    }

    // 初始化所有設定項，使其為關閉
    private fun initAnimation() {
        openBoolean = true
        mBinding.clMain.openLayout(false, mBinding.clSettingNameContent, mBinding.clSettingName)

    }

    override fun onResume() {
        super.onResume()
        logi("name Setting trace", "onResume")
        initValue() // 返回時更新值
    }

    override fun onPause() {
        super.onPause()
        setKeyboard(false, mBinding.edtSettingNameContent)
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


