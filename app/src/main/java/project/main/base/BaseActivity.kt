package project.main.base

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding


abstract class BaseActivity<B : ViewBinding>(val bindingFactory: (LayoutInflater) -> B) : AppCompatActivity() {
    open val TAG = javaClass.simpleName
    lateinit var mBinding: B

    open val heightPixel by lazy { this.resources.displayMetrics.heightPixels }
    open val widthPixel by lazy { this.resources.displayMetrics.widthPixels }

    open val activity by lazy { this }
    open val context: Context by lazy { this }

    abstract var statusTextIsDark: Boolean

    fun setStatusBarText() {
        if (statusTextIsDark && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } // 讓狀態列文字是深色
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //隱藏標題列
        supportActionBar?.hide()
        mBinding = bindingFactory(layoutInflater)
//        hideSystemUI(mBinding.root)
        setContentView(mBinding.root)

        // 沉浸式布局設定
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT // 全透明狀態列背景

        setStatusBarText()
    }

    fun toast(message: String? = "") {
        Toast.makeText(context, message ?: "null", Toast.LENGTH_SHORT).show()
    }

}