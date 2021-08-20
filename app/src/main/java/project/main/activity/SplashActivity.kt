package project.main.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivitySplashBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import project.main.activity.const.PERMISSIONS_REQUEST_CODE
import project.main.activity.const.permissionPerms
import project.main.base.BaseActivity
import pub.devrel.easypermissions.EasyPermissions
import tool.*
import tool.dialog.showMessageDialogOnlyOKButton
import uitool.setTextSize
import utils.DateTool
import utils.goToNextPageFinishThisPage


class SplashActivity : BaseActivity<ActivitySplashBinding>({ ActivitySplashBinding.inflate(it) }), EasyPermissions.PermissionCallbacks {

    private val activity = this
    private val context: Context = this

    private val loadingPercent by lazy { context.resources.getStringArray(R.array.loading_percent).asList().map { it.toDouble() } }

    //總計載入秒數
    private val totalLoadingSec by lazy { (5..7).random().toLong().times(DateTool.oneSec) }

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

        initView()

        initLoading()

    }

    private fun initView() {
        mBinding.lvLoadingProgress.initialLottieByFileName(context, AnimationFileName.SPLASH_LOADING, startAfterLoading = false)
        mBinding.tvLoadingStatus.setTextSize(16)
    }



    private fun initLoading() {
        loadingByStep(loadingTextArray, loadingPercent, totalLoadingSec)
    }

    /**遞迴方法，每次只載入第一個，完成後傳下一個 loadingTextArray
     * 依照設定的總秒數，分階段載入不同文字與設定的載入比例*/
    private fun loadingByStep(loadingTextArray: List<String>, loadingPercent: List<Double>, totalLoadingSec: Long, nowExeCuteIndex: Int = 0) {
        if (activity.isFinishing)
            return

        if (loadingTextArray.isEmpty()) {
            toNextActivity()
            return
        }

        mBinding.tvLoadingStatus.text = loadingTextArray[0]

        if (mBinding.tvLoadingStatus.text.contains(context.getString(R.string.splash_permission_key_word)) && !checkPermission()) {
            requestPermissions()
        } else {
            val totalPercent = loadingPercent.take(nowExeCuteIndex + 1).sum().toFloat()
            val nowPercent = loadingPercent[nowExeCuteIndex]
            val delayTime = if (nowPercent < 1.0) (totalLoadingSec * nowPercent).toLong() else nowPercent.toLong() * DateTool.oneSec // 若大於等於1的話直接等該秒數
            mBinding.lvLoadingProgress.setProgressValue(mBinding.lvLoadingProgress.progress, totalPercent, delayTime)
            MainScope().launch {
                delay(delayTime)
                subList = loadingTextArray.subList(1, loadingTextArray.size)
                nextIndex++
                loadingByStep(subList, loadingPercent, totalLoadingSec, nextIndex)
            }
        }
    }

    private fun toNextActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        activity.goToNextPageFinishThisPage(intent)
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun checkPermission(): Boolean {
//        val perms = arrayOf(Manifest.permission.CAMERA)
        return EasyPermissions.hasPermissions(this, *permissionPerms)
    }

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
            showMessageDialogOnlyOKButton(context, context.getString(R.string.dialog_notice_title), context.getString(R.string.permission_request)) {
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
            loadingByStep(subList, loadingPercent, totalLoadingSec, nextIndex)
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