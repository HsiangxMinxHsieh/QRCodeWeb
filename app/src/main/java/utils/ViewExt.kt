package utils

import android.view.View
import kotlinx.coroutines.*

private val empty: (View?) -> Unit = {} // 是否是空方法判斷

private var clickEvent: (View?) -> Unit = empty // 指定為各類事件(使用clickWithTrigger，在500毫秒內不可能有兩種事件)

//蝦米註：有跳頁或提示對話框動作才要使用，避免跳頁跳兩次或對話框出現兩個。
fun <T : View> T.clickWithTrigger(block: (View?) -> Unit) {
    setOnClickListener {
        if (clickEvent === empty) {
            clickEvent = block.apply {
                invoke(it)
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            delay(500L)
            clickEvent = empty
        }
    }
}

//蝦米註：同一頁面的事件(就和原本的clickWithTrigger一膜一樣，只是比較短XD)
fun <T : View> T.click(block: (T?) -> Unit) = clickWithTrigger {
    block.invoke(this)
}