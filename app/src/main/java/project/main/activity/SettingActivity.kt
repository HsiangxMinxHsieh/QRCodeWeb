package project.main.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TableLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivitySettingBinding
import com.google.android.material.tabs.TabLayout
import com.timmymike.viewtool.clickWithTrigger
import project.main.base.BaseActivity
import project.main.model.SettingData
import project.main.model.SettingDataItem
import project.main.tab.SettingContentFragment
import tool.dialog.showConfirmDialog
import tool.dialog.showMessageDialogOnlyOKButton
import tool.getShare
import uitool.setMarginByDpUnit
import uitool.setTextSize
import uitool.setViewSize
import utils.couldBeAdd
import utils.getColorByBuildVersion
import utils.logAllData
import utils.logi
import utils.toJson


class SettingActivity : BaseActivity<ActivitySettingBinding>({ ActivitySettingBinding.inflate(it) }) {

    companion object {
        const val SETTING_TYPE_KEY = "SETTING_TYPE_KEY"
    }

    override var statusTextIsDark: Boolean = true

    private val type: SettingType by lazy {
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.extras?.getSerializable(SETTING_TYPE_KEY, SettingType::class.java)
        } else {
            intent?.extras?.getSerializable(SETTING_TYPE_KEY)
        }) as SettingType
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()

        initData()

        initEvent()

    }


    private fun initData() {

        if (type == SettingType.Add) {
            addSetting(getDefaultSetting(context.getShare().getID()))
        }
    }

    private fun initView() {

        initTab()

        initPagers()


    }

    private val pagerAdapter: PagerAdapter get() = PagerAdapter(activity, settings)

    private val settings: SettingData by lazy { context.getShare().getStoreSettings() }
    private var nowTabIndex = 0

    private fun initTab() {
        mBinding.tlSettingTitle.isVisible = false
        //初始化TabLayout

//        val storeSetting = context.getShare().getNowUseSetting() // 取出之前儲存的，滑到那個位置。

//        logi("initTab", "storeSetting 是=>${storeSetting?.name}")

        nowTabIndex = if (type.settingId == -1) { // 無資料傳入，初始在第一頁
            0
        } else {
            //有設定檔，找到這筆設定檔的位置
            settings.indexOf(settings.find { it.id == type.settingId })
        }
//        logi("initTab", "nowTabIndex 是=>${nowTabIndex}")
        //判斷當前設定檔數量與在上面新增tab。
        for (index in settings.indices) {
            mBinding.tlSettingTitle.addTab(mBinding.tlSettingTitle.newTab().setCustomView(getTabViewByText(settings.getOrNull(index) ?: break, (index == nowTabIndex))))
        }

//        mBinding.tlSettingTitle.addTab(mBinding.tlSettingTitle.newTab().setCustomView(ImageView(context).apply { //無論如何都要在最尾端加一個+號
//            Glide.with(context).load(R.drawable.ic_baseline_add).into(this)
//            clickWithTrigger {  }
//        }))

        delayScrollToPosition(nowTabIndex)
    }

    private fun delayScrollToPosition(index: Int) {
//        logi("initTab", "nowTabIndex 是=>${index}")
//        logi("initTab", "mBinding.clMain 是=>${mBinding.clMain}")
//        logi("initTab", "mBinding.clMain.handler 是=>${mBinding.clMain.handler}")
        Handler(Looper.getMainLooper()).postDelayed({
            mBinding.tlSettingTitle.getTabAt(index)?.select()
            mBinding.vpContent.setCurrentItem(index, false)
        }, 20L)
    }

    private fun initPagers() {
        mBinding.vpContent.adapter = pagerAdapter // 初始指定Fragment內容
        mBinding.vpContent.isUserInputEnabled = false
    }

    private fun getDefaultSetting(id: Int) = SettingDataItem.getDefaultSetting(id, context)

    /**取得TabLayout中的View*/
    private fun getTabViewByText(s: SettingDataItem, isFirstTab: Boolean = false): View {
        val tabIndicator: View = LayoutInflater.from(context).inflate(R.layout.tablayout_item, mBinding.tlSettingTitle, false)
        (tabIndicator.findViewById(R.id.tv_tab_text) as TextView).apply {
            text = s.name
            setTextSize(11)
            setMarginByDpUnit(0, 0, 0, 10)

            val color = if (isFirstTab) //第一個Tab要設定不一樣的顏色
                context.getColorByBuildVersion(R.color.theme_green)
            else
                context.getColorByBuildVersion(R.color.light_green)
            setTextColor(color)
        }
        (tabIndicator.findViewById(R.id.tv_id) as TextView).apply {
            text = s.id.toString()
        }

        tabIndicator.setViewSize((widthPixel * 0.22).toInt(), TableLayout.LayoutParams.MATCH_PARENT)
//        logi("getViewByText", "設定完成，即將回傳tabIndicator是=>$tabIndicator")
        return tabIndicator
    }

    fun View.findText(id: Int) = this.findViewById<TextView>(id)

    /**設定上方Tab的內容或背景顏色。*/
    private fun setTabText(setting: SettingDataItem?) {
        if (setting == null)
            return
//        logi("setTabText", "childCount =>${mBinding.tlSettingTitle.childCount}")
//        logi("setTabText", "tabCount =>${mBinding.tlSettingTitle.tabCount}")

        val tab = mBinding.tlSettingTitle.getTabAt(settings.indexOf(setting))?.view ?: return
//        logi("setTabText", "找到的tab是=>$tab")
        tab.findText(R.id.tv_tab_text).apply {
            this.text = setting.name
        }

    }

    private fun initEvent() {
        settings.map { it.id }.logAllData()
        //上方頁籤捲動事件設定
        mBinding.tlSettingTitle.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                //設定文字為深色
                tab?.customView?.findViewById<TextView>(R.id.tv_tab_text).apply {
                    this?.setTextColor(context.getColorByBuildVersion(R.color.theme_green))
                }
                val index = tab?.position ?: 0
                nowTabIndex = index

                //設定捲動頁面
                mBinding.vpContent.setCurrentItem(index, true)

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //設定文字為淺色
                tab?.customView?.findViewById<TextView>(R.id.tv_tab_text).apply {
                    this?.setTextColor(context.getColorByBuildVersion(R.color.light_green))
                }

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        //下方頁面捲動設定(上面頁籤要可以跟著動)
        mBinding.vpContent.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mBinding.tlSettingTitle.getTabAt(position)?.select()
            }
        })

        // 刪除按鈕事件
        mBinding.btnDelete.clickWithTrigger {
            deleteSetting(nowTabIndex)
        }

        // 儲存按鈕事件
        mBinding.btnSave.clickWithTrigger {
            saveData(nowTabIndex)
        }

        mBinding.btnBack.clickWithTrigger {
            activity.onBackPressed()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        judgeCouldBack { super.onBackPressed() }
    }

    /**2023/11/14嘉伸講師提出，不自動儲存，因此若直接返回要判斷是否取消變更。
     *
     * 有變更，沒儲存的情況下，才要提示「是否取消變更？」的對話框
     * */
    private fun judgeCouldBack(action: () -> Unit) {


        if (type == SettingType.Add) { // 嘉伸講師要求，新增的時候，可以不判斷直接返回。
            action.invoke()
            return
        }

        val newSaveData = settings.getOrNull(nowTabIndex) ?: run {// 刪除設定檔的時候，沒有找到這個內容時，會導致錯誤。
            action.invoke()
            return
        }

        val oldSaveData = context.getShare().getStoreSettings().let { it.getOrNull(it.indexOf(it.find { fd -> fd.id == type.settingId })) }
        if (newSaveData.toJson() != oldSaveData?.toJson()) { // 有變更，沒儲存的情況下，才要提示「是否取消變更？」的對話框
            context.showConfirmDialog(context.getString(R.string.dialog_notice_title), context.getString(R.string.setting_name_title_edit_cancel), {
                action.invoke()
            }, confirmBtnStr = context.getString(R.string.dialog_edit_cancel_confirm), cancelBtnStr = context.getString(R.string.dialog_edit_cancel))
            return
        } else { //新舊資料不同
            action.invoke()
        }
    }

    private fun addSetting(item: SettingDataItem) {

        couldBeAdd(item, settings) { have ->
            have?.let {
                // 新增資料至本頁
                settings.add(item)

                // 新增tab
                mBinding.tlSettingTitle.addTab(mBinding.tlSettingTitle.newTab().setCustomView(getTabViewByText(item)), settings.size - 1)

                // 新增頁面內容
                mBinding.vpContent.adapter = pagerAdapter //新增後更新Fragment內容(重新指定)

                // 滑動到最後一個
                delayScrollToPosition(settings.size - 1)
            }
        }
    }


    private fun deleteSetting(index: Int) {
        if (settings.size == 1) {
            context.showMessageDialogOnlyOKButton(context.getString(R.string.dialog_notice_title), context.getString(R.string.setting_cant_delete_by_size_is_1))
            return
        }

        val deleteSettingName = if (settings[index].name.isEmpty()) context.getString(R.string.setting_file_name_default) else settings[index].name

        context.showConfirmDialog(context.getString(R.string.dialog_notice_title), context.getString(R.string.setting_delete_confirm).format(deleteSettingName, "設定檔"), {
            // 刪除第index筆資料
            settings.removeAt(index)
            // 變更設定流程，刪除後要回到上一頁。

            // 刪除tab
//            mBinding.tlSettingTitle.removeTabAt(index)
//            // 更新頁面內容 // 變更設定流程，不更新畫面後直接結束此頁面。
//            mBinding.vpContent.adapter = pagerAdapter //刪除後更新Fragment內容 (重新指定)

            // 如果刪除的是正在使用的設定檔，則要將這個設定檔設定成上一個內容。
            if (type.settingId == context.getShare().getNowUseSetting()?.id)
                context.getShare().setNowUseSetting(settings[(index - 1).takeIf { it > 0 } ?: 0]) // 如果是刪除第一個的話，一樣要設定成第一個。7

            finish() // 變更設定流程，刪除後要回到上一頁。
            // 滑動到上一個
//            delayScrollToPosition(index - 1)
            // 因測試的時候發現，若「刪除此設定檔再重新掃描，會出現是否更新的訊息，使用者必然會confuse，因此此處幫使用者儲存一次。」
            context.getShare().savaAllSettings(settings)

        }, {
        })


    }

    //將 index的資料存到sharedPreference內。
    private fun saveData(index: Int) {

        val saveData = settings.getOrNull(nowTabIndex) ?: return
//        logi("saveData", "saveData時，saveData 是=>$saveData")
//        logi("saveData", "saveData時，settings.name 是=>${settings.map { it.name }}")
        if (settings.any { it.name.isEmpty() }) {
            context.showMessageDialogOnlyOKButton(context.getString(R.string.dialog_notice_title), context.getString(R.string.setting_name_title_hint))
            return
        }
        settings.checkHaveRepeatName()
            .takeIf { it != null }
            ?.let { sameName ->
                context.showMessageDialogOnlyOKButton(
                    context.getString(R.string.dialog_notice_title),
                    context.getString(R.string.setting_cant_save_by_same_name).format(
                        context.getString(R.string.setting_save_action),
                        sameName
                    )
                )
                return
            }

        saveData.haveSaved = true
        logi("saveData", "saveData時，saveData 是=>${saveData.toJson()}")
        context.getShare().savaAllSettings(settings.apply
        { this[index].haveSaved = true })
        context.getShare().setNowUseSetting(saveData)
        setTabText(saveData)
        sendBroadcastToUpdateFragment(index)
    }

    private fun sendBroadcastToUpdateFragment(index: Int) {
        val intent = Intent()
        intent.action = context.getString(R.string.setting_receiver).format(index) // 設置廣播的Action
        sendBroadcast(intent) // 發送廣播
    }

    //檢查每個檔案名稱是否有跟其他名稱重複
    private fun SettingData.checkHaveRepeatName(): String? {
        val checkList = (this.clone() as SettingData).apply { sortBy { it.name } }
        checkList.forEachIndexed { i, _ -> if (i + 1 < checkList.size && checkList[i].name == checkList[i + 1].name) return checkList[i].name }

        return null
    }

    override fun finish() {
        super.finish()
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private class PagerAdapter(activity: BaseActivity<ActivitySettingBinding>, val setting: SettingData) : FragmentStateAdapter(activity) {

        private val NUM_PAGES = setting.size //至少顯示一個未命名的設定檔

        override fun getItemCount(): Int {
            return NUM_PAGES
        }

        override fun createFragment(position: Int): Fragment {
            return SettingContentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(SettingContentFragment.BUNDLE_KEY_SETTING_DATA, setting.getOrNull(position))
                    putInt(SettingContentFragment.BUNDLE_KEY_POSITION, position)
                }
            }
        }
    }
}