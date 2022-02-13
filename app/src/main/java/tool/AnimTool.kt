package tool

import android.content.Context
import android.view.animation.Animation
import android.view.animation.Transformation
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieDrawable

object AnimationFileName {

    /** Progress等待對話框的動畫 */
    val LOADING_DIALOG = "loading_motion.json"

    /** 簽到QRCode掃描動畫 */
    val SIGN_IN_SCAN_MOTION = "scan_barcode_motion_4.json"

    /** 設定檔QRCode掃描動畫 */
    val SETTING_SCAN_MOTION = "scan_barcode_motion_3.json"

    /** SPLASH頁面的進度條 */
    val SPLASH_LOADING = "loading_progress.json"

}


/**初始化Lottie動畫*/
fun LottieAnimationView.initialLottieByFileName(context: Context, fileName: String, needRepeat: Boolean = false, startAfterLoading: Boolean = true) {
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
    if (startAfterLoading)
        this.playAnimation()
}



/**
 * setProgressValue，動畫設定進度條
 * @author 蝦米
 * @Date 2020/05/15
 * @param startProgress : 開始值傳入(傳入0則從頭開始)
 * @param toValueProcess : 0.65或0.53一類Float數字
 * @param animationTime :動畫開始與結束時間
 * */
fun LottieAnimationView.setProgressValue(startProgress: Float, toValueProcess: Float, animationTime: Long = 1500L) {
    // 由於完成後要顯示完成的動畫，因此，必須要在資源檔內設定完成秒數(轉完一圈的秒數)


    val trueSetValue = if (toValueProcess > 1f) 1f else toValueProcess
    // 取得現在進度(等一下動畫計算使用)，為現在進度，為連續切換頁面時使用

//        logi("setProgressValue", "傳入值===>$toValueProcess")
//        logi("setProgressValue", "完成值===>$finishProgress")
//        logi("setProgressValue", "現在全進度值===>${pgBar.progress}")
//        logi("setProgressValue", "目前進度值===>$nowPg")
//        logi("setProgressValue", "設定值===>$trueSetValue")
//        logi("setProgressValue", "實際設定值===>${trueSetValue.toFloat()}")
//        logi("setProgressValue","比率值===>$finishRadius")

    val anim = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            val value = startProgress + (trueSetValue - startProgress) * interpolatedTime
            progress = value
        }
    }
    //設定動畫間隔(為預設動畫時間)
    anim.duration = animationTime

    this.startAnimation(anim)

}