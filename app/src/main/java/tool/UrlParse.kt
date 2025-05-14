package tool

import android.net.Uri
import java.net.URL
import java.net.URLDecoder

/**
 * 类：UrlParse
 * 作者： qxc
 * 日期：2017/5/22.
 */


/**
 * 取得解析后的URL参数
 * @author 蝦米
 * @param key 要取的key
 * @return String 結果
 */
fun String.getUrlKey(key: String) = getUrlParams(this).getOrElse(key) { null }

fun String.getUrlKey(index: Int): String? {
    var count = 0
    val urlMap = getUrlParams(this)
    urlMap.keys.forEach {
        if (count == index)
            return urlMap[it]
        count++
    }
    return null
}

/**
 * 獲得解析後的URL參數
 * @param url url对象
 * @return URL参数map集合
 */
fun getUrlParams(url: String?): Map<String, String> {
    val query_pairs: MutableMap<String, String> = LinkedHashMap()
    val mUrl = stringToURL(url) ?: return query_pairs
    try {
        var query = mUrl.query ?: return query_pairs
        //判断是否包含url=,如果是url=后面的内容不用解析
        if (query.contains("url=")) {
            val index = query.indexOf("url=")
            val urlValue = query.substring(index + 4)
            query_pairs["url"] = URLDecoder.decode(urlValue, "UTF-8")
            query = query.substring(0, index)
        }
        //除url之外的参数进行解析
        if (query.length > 0) {
            val pairs = query.split("&".toRegex()).toTypedArray()
            for (pair in pairs) {
                val idx = pair.indexOf("=")
                //如果等号存在且不在字符串两端，取出key、value
                if (idx > 0 && idx < pair.length - 1) {
                    val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                    val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                    query_pairs[key] = value
                }
            }
        }
    } catch (ex: Exception) {
//            ExceptionProxy.catchException(ex);
    }
    return query_pairs
}

/**
 * 获得Url参数字符串
 * @param url url地址
 * @return 参数字符串
 */
fun getUrlParamStr(url: String?): String {
    val mUrl = stringToURL(url) ?: return ""
    try {
        return mUrl.query
    } catch (ex: Exception) {
//            ExceptionProxy.catchException(ex);
    }
    return ""
}

/**
 * 获得url的协议+域+路径（即url路径问号左侧的内容）
 * @param url url地址
 * @return url的协议+域+路径
 */
fun getUrlHostAndPath(url: String): String {
    return if (url.contains("?")) {
        url.substring(0, url.indexOf("?"))
    } else url
}

/**
 * 获得Uri参数值
 * @param uri uri
 * @param paramKey 参数名称
 * @return 参数值
 */
fun getUriParam(uri: Uri?, paramKey: String?): String {
    if (uri == null || paramKey == null || paramKey.length == 0) {
        return ""
    }
    var paramValue = uri.getQueryParameter(paramKey)
    if (paramValue == null) {
        paramValue = ""
    }
    return paramValue
}

/**
 * 获得Uri参数值
 * @param uri uri
 * @param paramKey 参数名称
 * @return 参数值
 */
fun getIntUriParam(uri: Uri?, paramKey: String?): Int {
    if (uri == null || paramKey == null || paramKey.length == 0) {
        return 0
    }
    try {
        val paramValue = uri.getQueryParameter(paramKey)
        return if (paramValue == null || paramValue.length == 0) {
            0
        } else paramValue.toInt()
    } catch (ex: Exception) {
//            ExceptionProxy.catchException(ex);
    }
    return 0
}

/**
 * 字符串转为URL对象
 * @param url url字符串
 * @return url对象
 */
private fun stringToURL(url: String?): URL? {
    return if (url == null || url.length == 0 || !url.contains("://")) {
        null
    } else try {
        val sbUrl = StringBuilder("http")
        sbUrl.append(url.substring(url.indexOf("://")))
        URL(sbUrl.toString())
    } catch (ex: Exception) {
//            ExceptionProxy.catchException(ex);
        null
    }
}
//}