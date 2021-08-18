package tool

import android.content.Context
import com.buddha.qrcodeweb.BuildConfig
import project.main.model.SettingData
import project.main.model.SettingDataItem
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

    /**STORE_SETTING_FILE 設定檔*/
    private val KEY_STORE_SETTING_FILE = "KEY_STORE_SETTING_FILE"


    /**NOW_USE_SETTING 現在使用的設定檔*/
    private val KEY_NOW_USE_SETTING = "KEY_NOW_USE_SETTING"

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
    fun isFirstTimeStartThisApp()=  getNowUseSetting() == null || getStoreSettings().isNullOrEmpty()

    /**取得送出網頁的Password  */
    fun getStorePassword(): String {
        return getString(context, KEY_HTML_PASSWORD, "")
    }

    /**設定送出網頁的Password */
    fun setPassword(password: String) {
        putString(context, KEY_HTML_PASSWORD, password)
    }

    /**取得現在使用的設定檔*/
    fun getNowUseSetting(): SettingDataItem? {
        return try {
            getString(context, KEY_NOW_USE_SETTING, "").toDataBean(SettingDataItem())
        } catch (e: Exception) {
            null
        }
    }

    /**取得現在使用的設定檔*/
    fun setNowUseSetting(settingData: SettingDataItem) {
        putString(context, KEY_NOW_USE_SETTING, settingData.toJson())
    }

    /**取得所有設定檔*/
    fun getStoreSettings(): SettingData? {
        return try {
            getString(context, KEY_STORE_SETTING_FILE, "").toDataBean(SettingData())
        } catch (e: Exception) {
            null
        }
    }

    /**儲存所有設定檔*/
    fun savaAllSettings(list: SettingData) {
        putString(context, KEY_STORE_SETTING_FILE, list.toJson())
    }

}