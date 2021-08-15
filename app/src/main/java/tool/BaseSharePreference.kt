package tool

import android.content.Context


fun Context.getShare() = BaseSharePreference(this)

/**
 *
 * Description:
 * @author Robert Chou didi31139@gmail.com
 * @date 2015/5/27 下午1:45:58
 * @version
 */
class BaseSharePreference(val context: Context) {
    private val TABLENAME = "shear"

    /**SEND_HTML_PASSWORD */
    private val KEY_HTML_PASSWORD = "KEY_USER_PASSWORD"

    fun getString(context: Context, key: String, defValues: String, tableName: String = TABLENAME): String {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        return sharedPreferences.getString(key, defValues) ?: defValues
    }

    fun putString(context: Context, key: String, value: String, tableName: String = TABLENAME) {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getInt(context: Context, key: String, defValue: Int, tableName: String = TABLENAME): Int {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        return sharedPreferences.getInt(key, defValue)
    }

    fun putInt(context: Context, key: String, value: Int, tableName: String = TABLENAME) {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getLong(context: Context, key: String, defValue: Long, tableName: String = TABLENAME): Long {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        return sharedPreferences.getLong(key, defValue)
    }

    fun putLong(context: Context, key: String, value: Long, tableName: String = TABLENAME) {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getFloat(context: Context, key: String, defValue: Float, tableName: String = TABLENAME): Float {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        return sharedPreferences.getFloat(key, defValue)
    }

    fun putFloat(context: Context, key: String, value: Float, tableName: String = TABLENAME) {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        val editor = sharedPreferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun getBoolean(context: Context, key: String, defValue: Boolean, tableName: String = TABLENAME): Boolean {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        return sharedPreferences.getBoolean(key, defValue)
    }

    fun putBoolean(context: Context, key: String, value: Boolean, tableName: String = TABLENAME) {
        val sharedPreferences = context.getSharedPreferences(tableName, 0)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /**取得送出網頁的Password  */
    fun getStorePassword(): String {
        return getString(context, KEY_HTML_PASSWORD, "")
    }

    /**設定送出網頁的Password */
    fun setPassword(password: String) {
        putString(context, KEY_HTML_PASSWORD, password)
    }
//

}