package project.main.activity

import android.os.Bundle
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivityRecordBinding
import project.main.base.BaseActivity


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