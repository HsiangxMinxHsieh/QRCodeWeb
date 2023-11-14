package tool

import android.content.Context
import com.buddha.qrcodeweb.BuildConfig
import project.main.base.BaseRecyclerViewDataBindingAdapter
import project.main.const.constantID
import project.main.const.constantName
import project.main.const.constantPassword
import project.main.database.SendRecordEntity
import project.main.model.*
import utils.toDataBean
import utils.toJson


fun Context.getShare() = BaseSharePreference(this)

/**
 *
 * Description:
 * @author Robert Chou didi31139@gmail.com
 * @date 2015/5/27 下午1:45:58
 * @version
 */
class BaseSharePreference(val context: Context) {
    private val TABLENAME = BuildConfig.APPLICATION_ID

    /**SEND_HTML_PASSWORD */
    private val KEY_HTML_PASSWORD = "KEY_USER_PASSWORD"

    /**STORE_SETTING_FILE 設定檔 */
    private val KEY_STORE_SETTING_FILE = "KEY_STORE_SETTING_FILE"

    /**NOW_USE_SETTING 現在使用的設定檔 */
    private val KEY_NOW_USE_SETTING = "KEY_NOW_USE_SETTING"

    /**KEY_KEY_DEFAULT 現在使用的欄位預設檔 */
    private val KEY_KEY_DEFAULT = "KEY_KEY_DEFAULT"

    /**KEY_ANIMATION_DURATION 現在的動畫切換秒數 */
    private val KEY_ANIMATION_DURATION = "KEY_ANIMATION_DURATION"


    /**KEY_ID 現在的設定檔ID */
    private val KEY_ID = "KEY_ID"

    private fun getString(context: Context, key: String, defValues: String, tableName: String = TABLENAME): String {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        return sharedPreferences.getString(key, defValues) ?: defValues
    }

    private fun putString(context: Context, key: String, value: String, tableName: String = TABLENAME) {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun getInt(context: Context, key: String, defValue: Int, tableName: String = TABLENAME): Int {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        return sharedPreferences.getInt(key, defValue)
    }

    private fun putInt(context: Context, key: String, value: Int, tableName: String = TABLENAME) {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    private fun getLong(context: Context, key: String, defValue: Long, tableName: String = TABLENAME): Long {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        return sharedPreferences.getLong(key, defValue)
    }

    private fun putLong(context: Context, key: String, value: Long, tableName: String = TABLENAME) {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    private fun getFloat(context: Context, key: String, defValue: Float, tableName: String = TABLENAME): Float {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        return sharedPreferences.getFloat(key, defValue)
    }

    private fun putFloat(context: Context, key: String, value: Float, tableName: String = TABLENAME) {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        val editor = sharedPreferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    private fun getBoolean(context: Context, key: String, defValue: Boolean, tableName: String = TABLENAME): Boolean {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        return sharedPreferences.getBoolean(key, defValue)
    }

    private fun putBoolean(context: Context, key: String, value: Boolean, tableName: String = TABLENAME) {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /**判斷是否是第一次進入此App的基準：是否有儲存過設定檔*/
    fun isFirstTimeStartThisApp() = getNowUseSetting() == null || getStoreSettings().isNullOrEmpty()

    /**取得動畫切換秒數  */
    fun getAnimationDuration(): Long {
        return getLong(context, KEY_ANIMATION_DURATION, 800L)
    }

    /**藉由ID取得設定檔，用於SettingFragment的onResume。*/
    fun getSettingById(id: Int): SettingDataItem? {
        return getStoreSettings().find { it.id == id }
    }

    /**藉由ID找到設定檔案名稱，如果找不到則回傳記錄檔內的設定檔名稱*/
    fun getSettingNameById(id: Int, data: SendRecordEntity) = getSettingById(id)?.name ?: data.sendSettingName


    /**設定動畫切換秒數 */
    fun setAnimationDuration(duration: Long) {
        putLong(context, KEY_ANIMATION_DURATION, duration)
    }

    /**取得送出網頁的Password  */
    fun getStorePassword(): String {
        return getString(context, KEY_HTML_PASSWORD, "")
    }

    /**設定送出網頁的Password */
    fun setPassword(password: String) {
        putString(context, KEY_HTML_PASSWORD, password)
    }

    /**取得ID欄位的欄位標題(供新增設定檔時使用) */
    fun getKeyID(): String {
        return getNowKeyDefault()?.keyID ?: constantID
    }

    /**取得密碼欄位的欄位標題(供新增設定檔時使用) */
    fun getKeyPassword(): String {
        return getNowKeyDefault()?.keyPassword ?: constantPassword
    }

    /**取得姓名欄位的欄位標題(供掃碼時判斷、紀錄檔提取時使用)  */
    fun getKeyName(): String {
        return getNowKeyDefault()?.keyName ?: constantName
    }

    /**取得現在使用的欄位預設值設定檔*/
    fun getNowKeyDefault(): KeyDefault? {
        return try {
            getString(context, KEY_KEY_DEFAULT, "").toDataBean(KeyDefault())
        } catch (e: Exception) {
            null
        }
    }

    /**設定現在使用的欄位預設值設定檔*/
    fun setNowKeyDefault(settingData: KeyDefault) {
        putString(context, KEY_KEY_DEFAULT, settingData.toJson())
    }

    /**取得現在使用的設定檔*/
    fun getNowUseSetting(): SettingDataItem? {
        return try {
            getString(context, KEY_NOW_USE_SETTING, "").toDataBean(SettingDataItem())
        } catch (e: Exception) {
            null
        }
    }

    /**設定現在使用的設定檔*/
    fun setNowUseSetting(settingData: SettingDataItem) {
        putString(context, KEY_NOW_USE_SETTING, settingData.toJson())
    }

    /**取得所有設定檔*/
    fun getStoreSettings(): SettingData = runCatching {
        (getString(context, KEY_STORE_SETTING_FILE, "").toDataBean(SettingData())) ?: SettingData()
    }.getOrElse {
        SettingData()
    }


    /**儲存所有設定檔*/
    fun savaAllSettings(list: SettingData) {
        putString(context, KEY_STORE_SETTING_FILE, list.toJson())
    }

    /**找到所有設定檔中傳入的設定檔的ID，替換後儲存。*/
    fun savaSetting(data: SettingDataItem) {
        val settings = getStoreSettings()
        settings[settings.indexOfFirst { it.id == data.id }] = data // 一定找的到
        savaAllSettings(settings)
    }

    /**取得當前的設定檔ID*/
    fun getID(): Int = getInt(context, KEY_ID, 0).apply {
        addID(this)
    }


    fun addID(id: Int) = putInt(context, KEY_ID, id + 1)

    /** 掃描到的設定檔，儲存至SharedPreference的方法。*/
    fun scanStoreSetting(isNew: Boolean, item: SettingDataItem) {
        savaAllSettings( // 將掃描到的設定儲存至SharePreference
            getStoreSettings().apply {
                var oid = 0
                this.indexOfFirst { it.name == item.name }.let { index ->
                    remove(firstOrNull { item.name == it.name }?.apply { oid = this.id })   // 用name找到settings裡面的那個設定檔，先移除再更新 // 如果找不到則移除失敗。
                    add(if (index >= 0) index else if (size == 0) 0 else this.size , item.apply {
                        id = if (isNew) // 新增
                            context.getShare().getID()
                        else // 更新
                            oid
                        haveSaved = true
                    })
                }
            }
        )
        setNowUseSetting(item)
    }
}