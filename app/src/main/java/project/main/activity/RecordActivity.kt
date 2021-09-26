package project.main.activity

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.databinding.DataBindingUtil
import com.buddha.qrcodeweb.databinding.ActivityMainBinding
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import project.main.api.getURLResponse
import pub.devrel.easypermissions.EasyPermissions
import tool.AnimationFileName
import tool.dialog.*
import tool.getShare
import tool.getUrlKey
import tool.initialLottieByFileName
import utils.toString
import java.util.*
import androidx.constraintlayout.widget.ConstraintSet
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivityRecordBinding
import com.buddha.qrcodeweb.databinding.ActivitySampleBinding
import project.main.base.BaseActivity
import uitool.ViewTool


class RecordActivity : BaseActivity<ActivityRecordBinding>({ ActivityRecordBinding.inflate(it) }) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()

        initObserver()

        initView()

        initEvent()

    }

    private fun initData() {

    }

    private fun initObserver() {

    }

    private fun initView() {
//        mBinding.tvTitle.text = activity.toString()
    }

    private fun initEvent() {
        mBinding.btnBack.setOnClickListener {
            activity.onBackPressed()
        }

    }

    override fun finish() {
        super.finish()
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}