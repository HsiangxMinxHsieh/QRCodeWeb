package tool

import android.content.Context
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

object AnimationFileName {

    /** Progress等待對話框的動畫 */
    val LOADING_DIALOG = "loading_motion.json"

    /** BarCode掃描動畫 */
    val SCAN_MOTION = "scan_barcode_motion_2.json"

}


/**初始化Lottie動畫*/
fun LottieAnimationView.initialLottieByFileName(context: Context, fileName: String, needRepeat: Boolean = false) {
    this.animation = null
    try {
        this.setComposition( //因為新的方法很爛，不能保證每次setAnimation後的composition不會是null，所以使用舊(被註釋棄用)方法。
            LottieComposition.Factory.fromFileSync(context, fileName)!!
        )
    } catch (e: Exception) {
//        loge("", "$fileName 解析時發生錯誤！錯誤訊息：${e.message ?: "無錯誤"}")
    }
    if (needRepeat) {
        this.repeatCount = LottieDrawable.INFINITE
        this.repeatMode = LottieDrawable.RESTART
    }
    this.progress = 0f
    this.playAnimation()
}
