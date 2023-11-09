package project.main.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivitySettingSelectBinding
import com.buddha.qrcodeweb.databinding.AdapterSettingSelectBinding
import com.timmymike.viewtool.animColor
import com.timmymike.viewtool.animRotate
import com.timmymike.viewtool.click
import com.timmymike.viewtool.clickWithTrigger
import com.timmymike.viewtool.dpToPx
import com.timmymike.viewtool.getResourceColor
import com.timmymike.viewtool.setClickBgState
import com.timmymike.viewtool.setClickTextColorStateById
import com.timmymike.viewtool.setTextSize
import project.main.base.BaseActivity
import project.main.base.BaseRecyclerViewDataBindingAdapter
import project.main.model.SettingData
import project.main.model.SettingDataItem
import tool.getShare
import uitool.getRoundBg
import utils.showDialogAndConfirmToSaveSetting

class SettingSelectActivity : BaseActivity<ActivitySettingSelectBinding>({ ActivitySettingSelectBinding.inflate(it) }) {

    override var statusTextIsDark: Boolean = true

    private val settings: SettingData by lazy { context.getShare().getStoreSettings() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()

        initObserver()

        initView()

        initEvent()

    }

    private fun initData() {

    }

    private fun initObserver() {

    }

    private fun initView() {
        initRecyclerView()
    }

    private fun initRecyclerView() = mBinding.rvSettings.run {
        adapter = SettingSelectAdapter(this@SettingSelectActivity).apply {
            addItem(settings)
        }

    }

    private fun initEvent() {
        mBinding.btnAdd.clickWithTrigger {
            toSettingActivity(type = SettingType.Add)
        }

        mBinding.btnBack.clickWithTrigger {
            activity.onBackPressed()
        }

        mBinding.btnScanToGet.clickWithTrigger {
            toScanActivity()
        }
    }

    private fun toSettingActivity(type: SettingType) {
        val intent = Intent(activity, SettingActivity::class.java).apply {
            putExtra(SettingActivity.SETTING_TYPE_KEY, type)
        }
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    class ScanActivityResultContract : ActivityResultContract<ScanMode, SettingDataItem>() {
        override fun createIntent(context: Context, input: ScanMode?): Intent {
            return Intent(context, ScanActivity::class.java).apply {
                putExtra(ScanActivity.BUNDLE_KEY_SCAN_MODE, input)
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): SettingDataItem? {
            return if (resultCode == Activity.RESULT_OK)
                intent?.getSerializableExtra(ScanActivity.BUNDLE_KEY_SCAN_RESULT) as? SettingDataItem
            else
                null
        }
    }


    private val scanActivityLauncher = registerForActivityResult(ScanActivityResultContract()) { item ->
        item?.let {
            activity.showDialogAndConfirmToSaveSetting(item, settings) { itemCallBack ->
                // 變更頁面內容
                itemCallBack?.let { update ->
                    settings.indexOf(settings.firstOrNull { it.name == update.name }).let { findIndex -> // 要先找到名稱來確認是否有更新，不然可能會造成「同名稱不同ID」的錯誤。
                        if (findIndex < 0) { // 找不到要新增
//                            mBinding.tlSettingTitle.addTab(mBinding.tlSettingTitle.newTab().setCustomView(getTabViewByText(it)), settings.size) // 掃描後確定要新增tab
                            settings.add(it)
                        } else // 找得到要更新
                            settings[findIndex] = update

//                        mBinding.vpContent.adapter = pagerAdapter //掃描後更新Fragment內容(重新指定)
//                        delayScrollToPosition(settings.indexOf(update)) // 滑動到找到的index
                    }
                }
                return@showDialogAndConfirmToSaveSetting true
            }

        } ?: kotlin.run {
            Toast.makeText(context, context.getString(R.string.setting_scan_action_no_content), Toast.LENGTH_SHORT).show()
        }

    }

    private fun toScanActivity() {

        scanActivityLauncher.launch(ScanMode.SETTING)
//        activity.startActivity(Intent(activity, ScanActivity::class.java).apply {
//            putExtra(ScanActivity.BUNDLE_SCAN_MODE_KEY, ScanMode.SETTING)
//        })
        activity.overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up)
    }


    override fun finish() {
        super.finish()
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}


class SettingSelectAdapter(val context: Context) : BaseRecyclerViewDataBindingAdapter<SettingDataItem>(context, R.layout.adapter_setting_select) {

    override fun initViewHolder(viewHolder: ViewHolder) {
        (viewHolder.binding as? AdapterSettingSelectBinding)?.run {
            tvSettingName.run {
                this.setTextSize(20)
                10.dpToPx.let {
                    setPadding(it, it, it, it)
                }
                setClickBgState(
                    getRoundBg(context, 10, R.color.theme_blue, R.color.black, 2)
                )
                setClickTextColorStateById(R.color.white)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, data: SettingDataItem) {
        (viewHolder.binding as AdapterSettingSelectBinding).run {
            tvSettingName.text = data.name
            // 設定釘選動作
            ivPin.click {
                it?.run {
                    isSelected = isSelected.not()
                    it.animRotate(if (isSelected) 0f else -45f, if (isSelected) -45f else 0f)
                    it.animColor(getResourceColor(if (isSelected) R.color.gray else R.color.orange), getResourceColor(if (isSelected) R.color.orange else R.color.gray))
                }
            }

        }
    }

    override fun onItemClick(view: View, position: Int, data: SettingDataItem): Boolean {
        return true
    }

    override fun onItemLongClick(view: View, position: Int, data: SettingDataItem): Boolean {
        return true
    }
}


enum class SettingType(var settingName: String) {
    Add("NoneSettingName"),
    Edit(""),
}
