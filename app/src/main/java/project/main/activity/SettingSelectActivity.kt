package project.main.activity

import android.os.Bundle
import com.buddha.qrcodeweb.databinding.ActivitySettingSelectBinding
import project.main.base.BaseActivity

class SettingSelectActivity : BaseActivity<ActivitySettingSelectBinding>({ ActivitySettingSelectBinding.inflate(it) }) {

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
    }

    private fun initEvent() {
    }


}