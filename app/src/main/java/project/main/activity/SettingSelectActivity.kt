package project.main.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.recyclerview.widget.LinearSmoothScroller
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
import com.timmymike.viewtool.setRippleBackgroundById
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

    private val settingSelectAdapter: SettingSelectAdapter by lazy {
        SettingSelectAdapter(this@SettingSelectActivity)
    }

    private fun initRecyclerView() = mBinding.rvSettings.run {

        adapter = settingSelectAdapter.apply {

            clickListener = object : SettingSelectAdapter.ClickListener {
                override fun edit(item: SettingDataItem) { // 點擊編輯要到編輯頁
                    toSettingActivity(type = SettingType.Edit.apply { settingId = item.id })
                }

                override fun select(item: SettingDataItem) { // 選擇後要返回掃描頁
                    saveData(item)
                    finish()
                }

                override fun resort(item: SettingDataItem) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        this@run.layoutManager?.startSmoothScroll(SlowLinearSmoothScroller(context).apply {
                            targetPosition = 0
                        })
                    }, 300L)
                }

            }

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


    //將 選擇到的資料存到sharedPreference內。
    private fun saveData(item: SettingDataItem) {
        context.getShare().setNowUseSetting(item)
    }

    class ScanActivityResultContract : ActivityResultContract<ScanMode, SettingDataItem?>() {
        override fun createIntent(context: Context, input: ScanMode): Intent {
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
            val settings = context.getShare().getStoreSettings()
            activity.showDialogAndConfirmToSaveSetting(item, settings) { itemCallBack ->

                // 變更頁面內容
                itemCallBack?.let { update ->
                    settings.indexOf(settings.firstOrNull { it.name == update.name }).let { findIndex -> // 要先找到名稱來確認是否有更新，不然可能會造成「同名稱不同ID」的錯誤。
                        if (findIndex < 0) { // 找不到要新增
//                            mBinding.tlSettingTitle.addTab(mBinding.tlSettingTitle.newTab().setCustomView(getTabViewByText(it)), settings.size) // 掃描後確定要新增tab
                            settings.add(it)

                        } else { // 找得到要更新
                            settings[findIndex] = update
                        }

                        // 更新畫面

                        settingSelectAdapter.addItem(settings)

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
        activity.overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up)
    }

    override fun onResume() {
        super.onResume()
        // 返回畫面的時候，必須更新一次列表，以確保每次顯示的時候都是最新的。
        settingSelectAdapter.addItem(context.getShare().getStoreSettings())

    }

    override fun finish() {
        super.finish()
        val settingData: SettingData = SettingData()
        settingData.addAll(settingSelectAdapter.list)

        context.getShare().savaAllSettings(settingData)
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}


class SettingSelectAdapter(val context: Context) : BaseRecyclerViewDataBindingAdapter<SettingDataItem>(context, R.layout.adapter_setting_select) {

    interface ClickListener {
        fun edit(item: SettingDataItem)

        fun select(item: SettingDataItem)

        fun resort(item: SettingDataItem)
    }

    var clickListener: ClickListener? = null

    override fun initViewHolder(viewHolder: ViewHolder) {
    }

    private fun TextView.setTextStatus(status: Status) = this.run {
        // 設定文字顯示效果
        this.setTextSize(20)
        10.dpToPx.let {
            setPadding(it, it, it, it)
        }
        setClickBgState(
            getRoundBg(context, 10, if (status == Status.Normal) R.color.theme_blue else R.color.theme_green, R.color.black, if (status == Status.Normal) 2 else 5)
        )
        setClickTextColorStateById(R.color.white)
        setRippleBackgroundById(if (status == Status.Normal) R.color.white else R.color.black)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, data: SettingDataItem) {
        (viewHolder.binding as AdapterSettingSelectBinding).run {

            tvSettingName.text = data.name
            // 設定釘選動作
            ivPin.run {

                isSelected = data.sortIndex < 0
                rotation = if (isSelected) -45f else 0f

                setColorFilter(getResourceColor(if (isSelected) R.color.orange else R.color.gray))

                click {

                    // 點擊效果
                    it?.run {
                        isSelected = isSelected.not()
                        animRotate(if (isSelected) 0f else -45f, if (isSelected) -45f else 0f)
                        animColor(getResourceColor(if (isSelected) R.color.gray else R.color.orange), getResourceColor(if (isSelected) R.color.orange else R.color.gray))
                    }

                    // 本來有選中，改為沒選中的要改成0。
                    if (!isSelected) {
                        data.sortIndex = 0
                        notifyItemMoved(list.indexOf(data), reorderDataList().indexOf(data))
                    } else { // 本來沒選中，後來有選中，外部要跳轉順序
                        clickListener?.resort(data)
                        notifyItemMoved(list.indexOf(data), 0)
                        data.sortIndex = list.map { it.sortIndex }.min() - 1 // 找到比當前最小的，再-1。
                    }

                    reorderDataList()
                }
            }


            // 設定文字效果
            tvSettingName.setTextStatus(status = if (data.id == context.getShare().getNowUseSetting()?.id) Status.Choice else Status.Normal)

            // 設定文字選擇動作
            tvSettingName.clickWithTrigger {
                clickListener?.select(data)
            }

            // 設定編輯按鈕點擊動作
            ivEdit.clickWithTrigger {
                clickListener?.edit(data)
            }

        }
    }

    private fun reorderDataList(): List<SettingDataItem> {
        list.sortWith(compareBy<SettingDataItem> {
            if (it.sortIndex < 0) it.sortIndex else 0
        }.thenBy { it.id })

        return list
    }

    override fun onItemClick(view: View, position: Int, data: SettingDataItem): Boolean {
        return true
    }

    override fun onItemLongClick(view: View, position: Int, data: SettingDataItem): Boolean {
        return true
    }
}

/**為了自定義滑動速度，而使用的捲動器*/
class SlowLinearSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    companion object {
        private const val MILLISECONDS_PER_INCH = 200f // 調整這個值來設置滾動速度
    }

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
    }
}

/**這筆設定檔是否正在被使用*/
enum class Status {
    Normal,
    Choice
}

/**設定狀態類別(新增or編輯)2*/
enum class SettingType(var settingId: Int) {
    Add(-1),
    Edit(0),
}
