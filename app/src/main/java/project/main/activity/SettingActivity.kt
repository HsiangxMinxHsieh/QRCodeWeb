package project.main.activity

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import tool.getShare
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivitySettingBinding
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import project.main.base.BaseActivity
import project.main.model.SettingData
import project.main.model.SettingDataItem
import project.main.tab.SettingContentFragment
import tool.dialog.TextDialog
import tool.dialog.showMessageDialogOnlyOKButton
import uitool.setMarginByDpUnit
import uitool.setTextSize
import uitool.setViewSize
import utils.logi


class SettingActivity : BaseActivity<ActivitySettingBinding>({ ActivitySettingBinding.inflate(it) }) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()

        initObserver()

        initView()

        initEvent()

    }

    private fun initData() {

        if (context.getShare().isFirstTimeStartThisApp()) {
            logi("initData", "偵測到是第一次進入！無設定檔，即將新增一筆預設的")
            settings.add(getDefaultSetting())
        }


    }

    private fun initObserver() {

    }

    private fun initView() {

        initTab()

        initPagers()

    }

    private val pagerAdapter: PagerAdapter get() = PagerAdapter(activity, settings)


    private val settings: SettingData by lazy { context.getShare().getStoreSettings() }
    private var nowTabIndex = 0
    private var textDialog: TextDialog? = null
    private fun initTab() {
        //初始化TabLayout

        val storeSetting = context.getShare().getNowUseSetting() // 取出之前儲存的，滑到那個位置。

        logi("initTab", "storeSetting 是=>${storeSetting?.name}")

        nowTabIndex = if (storeSetting == null) { // 無資料傳入，初始在第一頁
            0
        } else {
            //有設定檔，找到這筆設定檔的位置
            settings.indexOf(settings.find { it.name == storeSetting.name })
        }
        logi("initTab", "nowTabIndex 是=>${nowTabIndex}")
        //判斷當前設定檔數量與在上面新增tab。
        for (index in settings.indices) {
            mBinding.tlSettingTitle.addTab(mBinding.tlSettingTitle.newTab().setCustomView(getTabViewByText(settings.getOrNull(index)?.name ?: "", (index == nowTabIndex))))
        }

        mBinding.tlSettingTitle.addTab(mBinding.tlSettingTitle.newTab().setCustomView(ImageView(context).apply { //無論如何都要在最尾端加一個+號
            Glide.with(context).load(R.drawable.ic_baseline_add).into(this)
            setOnClickListener { addSetting(settings.size) }
        }))

        delayScrollToPosition(nowTabIndex)
        setTabView(storeSetting)//填入頁面內容

    }

    private fun delayScrollToPosition(index: Int) {
        logi("initTab", "nowTabIndex 是=>${index}")
        logi("initTab", "mBinding.clMain 是=>${mBinding.clMain}")
        logi("initTab", "mBinding.clMain.handler 是=>${mBinding.clMain.handler}")
        Handler(Looper.getMainLooper()).postDelayed({
            mBinding.tlSettingTitle.getTabAt(index)?.select()
            mBinding.vpContent.setCurrentItem(index, false)
        }, 20L)
    }

    private fun initPagers() {
        mBinding.vpContent.adapter = pagerAdapter

    }

    private fun getDefaultSetting() =
        SettingDataItem(name = (context.getString(R.string.setting_file_name_default) + (0..100).random().toString())).apply {
            fields.add(SettingDataItem.SettingField(fieldName = context.getString(R.string.password_title_default), columnKey = "entry.1199127502"))
        }

    /**取得TabLayout中的View*/
    private fun getTabViewByText(s: String?, isFirstTab: Boolean = false): View {
        val tabIndicator: View = LayoutInflater.from(context).inflate(R.layout.tablayout_item, mBinding.tlSettingTitle, false)
        val tvTabText = tabIndicator.findViewById(R.id.tv_tab_text) as TextView
        tvTabText.text = s
        tvTabText.setTextSize(12)
        tvTabText.setMarginByDpUnit(0, 0, 0, 10)
        val color = if (isFirstTab) //第一個Tab要設定不一樣的顏色
            context.resources.getColor(R.color.theme_green, null)
        else
            context.resources.getColor(R.color.light_green, null)
        tvTabText.setTextColor(color)
        tabIndicator.setViewSize((widthPixel * 0.22).toInt(), TableLayout.LayoutParams.MATCH_PARENT)
        logi("getViewByText", "設定完成，即將回傳tabIndicator是=>$tabIndicator")
        return tabIndicator
    }

    /**設定本頁的設定檔內容*/
    private fun setTabView(setting: SettingDataItem?) {
        if (setting == null)
            return


    }

    private fun initEvent() {
        //上方頁籤捲動事件設定
        mBinding.tlSettingTitle.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                //設定文字為深色
                tab?.customView?.findViewById<TextView>(R.id.tv_tab_text).apply {
                    this?.setTextColor(context.getColor(R.color.theme_green))
                }
                val index = tab?.position ?: 0
                nowTabIndex = index
                //設定下方頁面內容
                setTabView(settings.getOrNull(index))

                //設定捲動頁面
                mBinding.vpContent.setCurrentItem(index, true)

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //設定文字為淺色
                tab?.customView?.findViewById<TextView>(R.id.tv_tab_text).apply {
                    this?.setTextColor(context.getColor(R.color.light_green))
                }

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        //下方頁面捲動設定(上面頁籤藥可以跟著動)
        mBinding.vpContent.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mBinding.tlSettingTitle.getTabAt(position)?.select()
            }
        })

        // 按鈕事件
        mBinding.btnDelete.setOnClickListener {
            deleteSetting(nowTabIndex)
        }

        // 按鈕事件
        mBinding.btnSave.setOnClickListener {
            saveData(nowTabIndex)
        }
    }


    private fun addSetting(index: Int) {

        if (settings.any { !it.haveSaved }) { // 如果有false要return。

            if (textDialog == null) {
                textDialog = showMessageDialogOnlyOKButton(context, context.getString(R.string.dialog_notice_title), context.getString(R.string.setting_cant_add_by_not_saved_current)) {
                    textDialog = null
                }
            }
            return
        }
        // 新增預設資料
        settings.add(getDefaultSetting())
        // 新增tab
        mBinding.tlSettingTitle.addTab(mBinding.tlSettingTitle.newTab().setCustomView(getTabViewByText(settings.getOrNull(index)?.name ?: "", (index == nowTabIndex))), index)

        // 新增頁面內容
        mBinding.vpContent.adapter = pagerAdapter

        // 滑動到最後一個
        delayScrollToPosition(index)
    }

    private fun deleteSetting(index: Int) {
        if (settings.size == 1) {
            if (textDialog == null) {
                textDialog = showMessageDialogOnlyOKButton(context, context.getString(R.string.dialog_notice_title), context.getString(R.string.setting_cant_delete_by_size_is_1)) {
                    textDialog = null
                }
            }
            return
        }

        // 刪除第index筆資料
        settings.removeAt(index)
        // 刪除tab
        mBinding.tlSettingTitle.removeTabAt(index)
        // 更新頁面內容
        mBinding.vpContent.adapter = pagerAdapter//
        // 滑動到上一個
        delayScrollToPosition(index - 1)
    }

    private val empty: (Throwable?) -> Unit = {}

    //將 index的資料存到sharedPreference內。
    private fun saveData(index: Int, action: (Throwable) -> Unit = empty) {

        val saveData = settings.getOrNull(nowTabIndex) ?: return
        val sameName = settings.checkHaveRepeatName()
        if (sameName != null) {
            if (textDialog == null) {
                textDialog = showMessageDialogOnlyOKButton(
                    context,
                    context.getString(R.string.dialog_notice_title),
                    context.getString(R.string.setting_cant_save_by_same_name).format(
                        (if (action == empty)
                            context.getString(R.string.setting_save_action)
                        else
                            context.getString((R.string.setting_leave_action))),
                        sameName
                    )
                ) {
                    textDialog = null
                }
            }
            return
        }
        logi("saveData", "saveData時，index是=>$index")
        context.getShare().setNowUseSetting(saveData)
        context.getShare().savaAllSettings(settings.apply { this[index].haveSaved = true })

        action.invoke(Throwable())
    }

    //檢查每個檔案名稱是否有跟其他名稱重複
    private fun SettingData.checkHaveRepeatName(): String? {
        val checkList = (this.clone() as SettingData).apply { sortBy { it.name } }
        checkList.forEachIndexed { i, _ -> if (i + 1 < checkList.size && checkList[i].name == checkList[i + 1].name) return checkList[i].name }

        return null
    }

    override fun finish() {

        logi("finish", "即將儲存的設定檔案是=>${settings.getOrNull(nowTabIndex)?.name}")
        saveData(nowTabIndex) { super.finish() }
//        activity.setResult(Activity.RESULT_OK, Intent().apply { putExtra(KEY_SETTING_RESULT, settings.getOrNull(nowTabIndex)?.name) })
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private inner class PagerAdapter(activity: BaseActivity<ActivitySettingBinding>, val setting: SettingData) : FragmentStateAdapter(activity) {

        private val NUM_PAGES = setting.size //至少顯示一個未命名的設定檔

        override fun getItemCount(): Int {
            return NUM_PAGES
        }

        override fun createFragment(position: Int): Fragment {
            return SettingContentFragment(settingData = setting.getOrNull(position) ?: getDefaultSetting(), position)
        }
    }
}