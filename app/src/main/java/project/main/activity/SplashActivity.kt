package project.main.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.buddha.qrcodeweb.BuildConfig
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivitySplashBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import project.main.const.PERMISSIONS_REQUEST_CODE
import project.main.const.permissionPerms
import project.main.base.BaseActivity
import pub.devrel.easypermissions.EasyPermissions
import tool.*
import tool.dialog.showKeyDefaultCheckDialog
import tool.dialog.showMessageDialogOnlyOKButton
import uitool.setTextSize
import utils.DateTool
import utils.goToNextPageFinishThisPage
import utils.logi


class SplashActivity : BaseActivity<ActivitySplashBinding>({ ActivitySplashBinding.inflate(it) }), EasyPermissions.PermissionCallbacks {
    companion object {
        var splashActivity: SplashActivity? = null
    }

    override var statusTextIsDark: Boolean = false

    private val loadingPercent by lazy {
        if (context.getShare().isFirstTimeStartThisApp())
            context.resources.getStringArray(R.array.init_loading_percent).asList().map { it.toDouble() }
        else
            context.resources.getStringArray(R.array.loading_percent).asList().map { it.toDouble() }

    }

    //總計載入秒數
    private val totalLoadingSec by lazy { (2..4).random().toLong().times(DateTool.oneSec) }

    private var subList: List<String> = mutableListOf()

    private var nextIndex = 0

    //如果在未取得權限的情況下onResume，要判斷是否權限取得成功。
    private var isGettingPermission = false

    private val loadingTextArray by lazy { // 載入狀態文字陣列
        if (context.getShare().isFirstTimeStartThisApp())
            context.resources.getStringArray(R.array.initial_status).asList()
        else
            context.resources.getStringArray(R.array.loading_status).asList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        splashActivity = this

        initView()

        initLoading()

        initEvent()

    }

    private fun initEvent() {
    }

    private fun initView() {
        mBinding.lvLoadingProgress.initialLottieByFileName(context, AnimationFileName.SPLASH_LOADING, startAfterLoading = false)
        mBinding.tvLoadingStatus.setTextSize(16)
        mBinding.tvBuildVersion.setTextSize(14)

        mBinding.tvBuildVersion.text = context.getString(R.string.splash_version).format(BuildConfig.VERSION_NAME)
    }


    private fun initLoading() {
//        logi(TAG, "totalLoadingSec是=>${totalLoadingSec}")
        loadingByStep(loadingTextArray, loadingPercent, totalLoadingSec)
    }
    //測試中 待刪除

    var job: Job? = null

    /**遞迴方法，依照index執行內容，完成後傳下一個 loadingTextArray
     * 依照設定的總秒數，分階段載入不同文字與設定的載入比例*/
    private fun loadingByStep(loadingTextArray: List<String>, loadingPercent: List<Double>, totalLoadingSec: Long, nowExeCuteIndex: Int = 0) {
        if (activity.isFinishing)
            return
        val nowShowText = loadingTextArray.getOrNull(nowExeCuteIndex)
//        logi(TAG, "要顯示的文字是=>$nowShowText,文字列表是=>$loadingTextArray")
        if (nowShowText == null) {
            toNextActivity()
            return
        }

        mBinding.tvLoadingStatus.text = nowShowText

        if (nowShowText.contains(context.getString(R.string.splash_permission_key_word)) && !checkPermission()) {
            requestPermissions() //
        } else if (nowShowText.contains(context.getString(R.string.splash_key_key_word)) && checkKeyDefaultValue()) {
            setCheckValue()
        } else {
            val totalPercent = loadingPercent.take(nowExeCuteIndex + 1).sum().toFloat() // 前 nowExeCuteIndex + 1 的總和
            val nowPercent = loadingPercent[nowExeCuteIndex]
            val delayTime = if (nowExeCuteIndex != loadingPercent.lastIndex) (totalLoadingSec * nowPercent).toLong() else (nowPercent * DateTool.oneSec).toLong() // 最後一個計算方法不一樣
//            logi(TAG, "延遲時間是=>$delayTime,index是=>$nowExeCuteIndex,nowPercent是=>$nowPercent")
//            logi(TAG, "顯示百分比是=>$nowPercent,總百分比要設定的是=>${totalPercent},loadingPercent是=>${loadingPercent}")
            mBinding.lvLoadingProgress.setProgressValue(mBinding.lvLoadingProgress.progress, totalPercent, delayTime)
            job = MainScope().launch {
                delay(delayTime)
                nextIndex = nowExeCuteIndex + 1
                loadingByStep(loadingTextArray, loadingPercent, totalLoadingSec, nextIndex)
            }
        }
    }

    private fun toNextActivity() {
        activity.goToNextPageFinishThisPage(Intent(activity, ScanActivity::class.java).apply {
            putExtra(ScanActivity.BUNDLE_KEY_SCAN_MODE, ScanMode.NORMAL)
        })
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun checkKeyDefaultValue(): Boolean =
        context.getShare().getNowKeyDefault()?.settingStatus ?: 0 == 0


    private fun setCheckValue() {
        activity.showKeyDefaultCheckDialog {
            loadingByStep(loadingTextArray, loadingPercent, totalLoadingSec, nextIndex)
        }
    }

    private fun checkPermission() =
//        val perms = arrayOf(Manifest.permission.CAMERA)
        EasyPermissions.hasPermissions(this, *permissionPerms)


    private fun requestPermissions() {
        EasyPermissions.requestPermissions(this, activity.getString(R.string.permission_request), PERMISSIONS_REQUEST_CODE, *permissionPerms)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @SuppressLint("RestrictedApi")
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        //權限被拒
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            activity.showMessageDialogOnlyOKButton(context.getString(R.string.dialog_notice_title), context.getString(R.string.permission_request)) {
                //連續拒絕，導向設定頁設定權限。
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                isGettingPermission = true
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        //權限允許
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            loadingByStep(loadingTextArray, loadingPercent, totalLoadingSec, nextIndex)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isGettingPermission) {
            requestPermissions()
            isGettingPermission = false //避免重複進入
        }
    }
}