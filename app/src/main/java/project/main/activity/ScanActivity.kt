package project.main.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivityScanBinding
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.timmymike.viewtool.clickWithTrigger
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import project.main.base.BaseActivity
import project.main.const.PERMISSIONS_REQUEST_CODE
import project.main.const.permissionPerms
import project.main.database.getRecordDao
import project.main.database.getSignInPersonByScan
import project.main.database.insertNewRecord
import project.main.model.ActionMode
import project.main.model.SettingDataItem
import pub.devrel.easypermissions.EasyPermissions
import tool.AnimationFileName
import tool.dialog.Dialog
import tool.dialog.showMessageDialogOnlyOKButton
import tool.getShare
import tool.initialLottieByFileName
import uitool.ViewTool
import utils.concatSettingColumn
import utils.intentToWebPage
import utils.isJson
import utils.loge
import utils.sendApi
import utils.showDialogAndConfirmToSaveSetting
import utils.showSignInCompleteDialog
import utils.showSignInErrorDialog
import utils.toDataBean
import utils.toString
import java.util.Date

enum class ScanMode { //進入掃描頁的呼叫處
    SPLASH, // 無設定檔時，按下掃描
    SETTING, // 設定頁面處，呼叫掃描
    NORMAL   // 一般掃描
}

class ScanActivity : BaseActivity<ActivityScanBinding>({ ActivityScanBinding.inflate(it) }), EasyPermissions.PermissionCallbacks {

    companion object {
        const val BUNDLE_KEY_SCAN_MODE = "BUNDLE_SCAN_MODE_KEY"
        const val BUNDLE_KEY_SCAN_RESULT = "BUNDLE_SCAN_MODE_KEY"
    }

    override var statusTextIsDark: Boolean = false

    private val liveResult by lazy { MutableLiveData<String>() }

    private var scanMode: ScanMode = ScanMode.NORMAL

    private var textDialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()

        initObserver()

        initView()

        initEvent()

    }

    private var nowSetting: SettingDataItem? = null

    private fun setSettingFabText() { // 如果有儲存的設定值才要設定fab按鍵內容(要顯示當前的設定檔名稱)。

        if (!context.getShare().isFirstTimeStartThisApp()) { //不是第一次進入才要顯示設定檔名稱

            mBinding.fabSetting.icon = null
            mBinding.fabSetting.text = nowSetting?.name ?: ""

            ConstraintSet().apply { // 動態設定ConstraintLayout相依關係：
                clone(mBinding.clMain)
                setMargin(R.id.fab_setting, ConstraintSet.TOP, ViewTool.DpToPx(context, 16f))
                connect(R.id.fab_record, ConstraintSet.BOTTOM, R.id.fab_setting, ConstraintSet.TOP, ViewTool.DpToPx(context, 16f))
                constrainWidth(R.id.fab_setting, ConstraintSet.WRAP_CONTENT)
                applyTo(mBinding.clMain)
            }
        }
    }

    private var signInResult = ""

    private fun initObserver() {
        liveResult.observe(activity) { scanContent ->
            when (scanMode) {
                ScanMode.NORMAL -> signInAction(scanContent)// 一般掃描
                ScanMode.SETTING -> processSettingPageScan(scanContent) // 設定頁呼叫
                ScanMode.SPLASH -> splashSettingToScan(scanContent)// Splash頁呼叫
            }
        }
    }

    private fun splashSettingToScan(scanContent: String) {

        judgeIsSettingAndConfirmToSave(scanContent,
            dataProcessAction = { item ->
                activity.showDialogAndConfirmToSaveSetting(item ?: return@judgeIsSettingAndConfirmToSave, context.getShare().getStoreSettings()) { itemCallBack ->
                    resumeScreenAnimation()
                    itemCallBack?.let { update ->
                        // 更新當前設定檔
                        nowSetting = update

                        // 結束SplashActivity，避免返回的時候返回Splash
                        SplashActivity.splashActivity?.finish()

                        // 更新掃描模式
                        scanMode = ScanMode.NORMAL

                        // 重新更新本頁動畫
                        initView()

                        // 設定浮動按鈕文字
                        setSettingFabText()

                    } ?: finish() // 使用者取消，則返回Splash頁。
                    return@showDialogAndConfirmToSaveSetting true
                }
            }, resumeAction = {
                resumeScreenAnimation()
            })
    }

    private fun processSettingPageScan(scanContent: String) {
        judgeIsSettingAndConfirmToSave(scanContent,
            dataProcessAction = { item ->
                val intent = Intent().apply {
                    putExtra(BUNDLE_KEY_SCAN_RESULT, item)
                }
                setResult(RESULT_OK, intent)
                finish()
            }, resumeAction = { resumeScreenAnimation() })
    }

    private fun signInAction(scanContent: String) {
        // 正常掃碼，先判斷是否是設定檔
        judgeIsSettingAndConfirmToSave(scanContent, dataProcessAction = { item ->

            activity.showDialogAndConfirmToSaveSetting(item ?: return@judgeIsSettingAndConfirmToSave, context.getShare().getStoreSettings()) { itemCallBack ->
                resumeScreenAnimation()
                itemCallBack?.let { update ->
                    nowSetting = update
                    mBinding.fabSetting.text = update.name

                }
                return@showDialogAndConfirmToSaveSetting true
            }

        }) ?: return

        // 到這裡應該只要處理Google表單的網址，不是的話就不處理。
        if (!scanContent.startsWith("https://docs.google.com/forms/")) {
            resumeScreenAnimation()
            return
        }

        if (nowSetting?.afterScanAction?.illegal() == true || nowSetting?.goWebSiteByScan?.illegal() == true) {
            loge(TAG, "nowSetting?.afterScanAction?.illegal()=>${nowSetting?.afterScanAction?.illegal()},,,nowSetting?.goWebSiteByScan?.illegal() =>${nowSetting?.goWebSiteByScan?.illegal()}")
            if (textDialog == null) {
                textDialog = activity.showSignInErrorDialog {
                    resumeScreenAnimation()
                    textDialog = null
                }
            }
            return
        }

        // 處理掃描到的簽到QRCode：
        val getScanSignInPersonName = scanContent.getSignInPersonByScan(context)

        if (getScanSignInPersonName == "null") {
            loge(TAG, "getScanSignInPersonName=>${getScanSignInPersonName}")
            if (textDialog == null) {
                textDialog = activity.showSignInErrorDialog {
                    resumeScreenAnimation()
                    textDialog = null
                }
            }
            return
        }
        val signInTime = Date().time
        signInResult = "${signInTime.toString("yyyy/MM/dd HH:mm:ss")}\n${getScanSignInPersonName}簽到完成。"

        val sendRequest = scanContent.concatSettingColumn(context)

        if (nowSetting?.afterScanAction?.actionMode == ActionMode.OpenBrowser.value) {  // 導向至網頁
            activity.getRecordDao().insertNewRecord(signInTime, scanContent, sendRequest, nowSetting ?: return)
            activity.intentToWebPage(sendRequest)
        } else {
            // 應用程式內打API
            MainScope().launch {
                if (activity.sendApi(sendRequest)) {
                    // 顯示簽到結果視窗。
                    if (nowSetting?.afterScanAction?.actionMode == ActionMode.StayApp.value) {
                        if (textDialog == null && signInResult.isNotEmpty()) {
                            textDialog = activity.showSignInCompleteDialog(signInResult) {
                                signInResult = ""
                                resumeScreenAnimation()
                                textDialog = null
                                activity.getRecordDao().insertNewRecord(signInTime, scanContent, sendRequest, nowSetting ?: return@showSignInCompleteDialog)
                            }
                        }
                    } else { // 導向至設定的網頁
                        activity.getRecordDao().insertNewRecord(signInTime, scanContent, sendRequest, nowSetting ?: return@launch)
                        activity.intentToWebPage(nowSetting?.afterScanAction?.toHtml)
                    }
                } else {
                    if (textDialog == null) {
                        textDialog = activity.showSignInErrorDialog {
                            resumeScreenAnimation()
                            textDialog = null
                        }
                    }
                }
            }
        }
    }

    private fun judgeIsSettingAndConfirmToSave(
        scanContent: String, dataProcessAction: (item: SettingDataItem?) -> Unit = {}, resumeAction: () -> Unit = {}
    ): Boolean? {

        if (scanContent.startsWith("QRCodeSignIn※")) { // 掃描到設定檔
            scanContent.split("※").getOrNull(1)?.replace("&", "")?.let {
                if (!it.isJson())
                    return@let null

                it.toDataBean(SettingDataItem())?.let { item ->
                    dataProcessAction.invoke(item)
                }
                return null
            } ?: resumeScreenAnimation() // 辨識失敗，無條件回復相機
        } else {//不是設定檔，不能直接call resumeScreenAnimation()方法(不能直接回復相機，因為有可能是是簽到的時候掃到簽到，要接下去處理。)
            resumeAction.invoke()
        }
        return true
    }

    private fun initData() {
        scanMode = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (intent?.extras?.getSerializable(BUNDLE_KEY_SCAN_MODE, ScanMode::class.java)) ?: ScanMode.NORMAL // 沒接到值是Normal(實際上都有傳)
        } else {
            (intent?.extras?.getSerializable(BUNDLE_KEY_SCAN_MODE) ?: ScanMode.NORMAL)
        }) as ScanMode

    }

    private fun initView() = mBinding.run {
        lvScanQrcodeMotion.initialLottieByFileName(context, if (scanMode == ScanMode.NORMAL) AnimationFileName.SIGN_IN_SCAN_MOTION else AnimationFileName.SETTING_SCAN_MOTION, true)
        (scanMode == ScanMode.NORMAL).let {
            fabRecord.isVisible = it
            fabSetting.isVisible = it
            tvTipScanSetting.isVisible = !it
        }
    }


    private fun initEvent() {
        mBinding.zxingQrcodeScanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                pauseScrennAnimation() // 暫停播放動畫

                liveResult.postValue(result.text)
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {

            }
        })

        mBinding.fabRecord.clickWithTrigger {
            clickToRecordPage()
        }

        mBinding.fabSetting.clickWithTrigger {
            clickToSettingPage()
        }
    }


    private fun clickToRecordPage() {
        val intent = Intent(activity, RecordActivity::class.java)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

    }

    private fun clickToSettingPage() {
        val intent = Intent(activity, SettingSelectActivity::class.java)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onResume() {
        super.onResume()

        requestPermissions() //若沒有請求權限會是一片黑屏 // 無論之前是否有權限都要再次請求權限，因為要開相機(實測過後發現)

        nowSetting = context.getShare().getNowUseSetting()
//        logi(TAG, "onResume取到的設定檔內容是=>${nowSetting}")

        resumeScreenAnimation()

        setSettingFabText()
    }

    override fun onPause() {
        super.onPause()
        pauseScrennAnimation()
    }

    override fun finish() {
        super.finish()
        if (scanMode == ScanMode.NORMAL)
            activity.overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up)
        else
            activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
    }

    private fun pauseScrennAnimation() {
        mBinding.zxingQrcodeScanner.pause()
        mBinding.lvScanQrcodeMotion.pauseAnimation()
    }

    private fun resumeScreenAnimation() {
        mBinding.zxingQrcodeScanner.resume()
        mBinding.lvScanQrcodeMotion.resumeAnimation()
    }

    private fun requestPermissions() {
        EasyPermissions.requestPermissions(this, activity.getString(R.string.permission_request), PERMISSIONS_REQUEST_CODE, *permissionPerms)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        //權限被拒
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (textDialog == null) {
                textDialog = activity.showMessageDialogOnlyOKButton(context.getString(R.string.dialog_notice_title), context.getString(R.string.permission_request)) {
                    textDialog = null
                    //連續拒絕，導向設定頁設定權限。
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        //權限允許
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            mBinding.zxingQrcodeScanner.resume()
        }
    }

}


