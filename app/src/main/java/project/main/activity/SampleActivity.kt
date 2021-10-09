package project.main.activity

import android.os.Bundle
import com.buddha.qrcodeweb.databinding.ActivitySampleBinding
import project.main.base.BaseActivity


class SampleActivity : BaseActivity<ActivitySampleBinding>({ ActivitySampleBinding.inflate(it) }) {

    override var statusTextIsDark: Boolean = true

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
        mBinding.tvTitle.text = activity.toString()
    }

    private fun initEvent() {
    }


}