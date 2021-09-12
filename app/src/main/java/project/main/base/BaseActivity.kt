package project.main.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import uitool.hideSystemUI


abstract class BaseActivity<B : ViewBinding>(val bindingFactory: (LayoutInflater) -> B) : AppCompatActivity() {
    lateinit var mBinding: B

    open val heightPixel by lazy { this.resources.displayMetrics.heightPixels }
    open val widthPixel by lazy { this.resources.displayMetrics.widthPixels }

    open val activity by lazy { this }
    open val context: Context by lazy { this }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //隱藏標題列
        supportActionBar?.hide()
        mBinding = bindingFactory(layoutInflater)
        hideSystemUI(mBinding.root)
        setContentView(mBinding.root)
    }



}