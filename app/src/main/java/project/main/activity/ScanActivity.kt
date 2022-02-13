package project.main.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import tool.dialog.*
import java.util.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivityScanBinding
import project.main.const.PERMISSIONS_REQUEST_CODE
import project.main.const.permissionPerms
import project.main.base.BaseActivity
import project.main.database.getRecordDao
import project.main.database.getSignInPersonByScan
import project.main.database.insertNewRecord
import project.main.model.ActionMode
import project.main.model.SettingDataItem
import tool.*
import uitool.ViewTool
import utils.*

enum class ScanMode { //進入掃描頁的呼叫處
    DEFAULT, // 無設定檔時，按下掃描
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
//    private val livePassword by lazy { MutableLiveData<String>() }

    private val scanMode: ScanMode by lazy { (intent?.extras?.getSerializable(BUNDLE_KEY_SCAN_MODE) as? ScanMode) ?: ScanMode.NORMAL } // 不傳值是Normal

    private var textDialog: Dialog? = null
//    var inputDialog: Dialog? = null

//    private lateinit var forSettingResult: ActivityResultLauncher<Intent>


    //如果在未取得權限的情況下onResume，要判斷是否權限取得成功。

    //    lateinit var mBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        mBinding = DataBindingUtil.setContentView(activity, R.layout.activity_main)

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
        liveResult.observe(activity, Observer { scanContent ->
            if (scanMode == ScanMode.NORMAL) // 一般掃描
                signInAction(scanContent)
            else { // 設定頁呼叫
                processSettingPageScan(scanContent)
            }

        })
    }

    private fun processSettingPageScan(scanContent: String) {
        if (scanContent.startsWith("QRCodeSignIn")) { // 掃描到設定檔
            scanContent.split("※").getOrNull(1)?.let {
                if (!it.isJson())
                    return@let null

                it.toDataBean(SettingDataItem())?.let { item ->
                    val intent = Intent().apply {
                        putExtra(BUNDLE_KEY_SCAN_RESULT, item)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
            } ?: resumeScreenAnimation()
        } else //不是設定檔，直接回復掃描動畫。
            resumeScreenAnimation()
    }

    // 和SettingActivity不一樣的地方是，Scan比的是儲存空間內的檔案，Setting比的是當前頁面的檔案，因此沒有寫成同一個公用方法。
    private fun couldBeAdd(): Boolean {
        if (context.getShare().getStoreSettings().any { !it.haveSaved }) { // 如果有false(未儲存者)要return。
            context.showMessageDialogOnlyOKButton(context.getString(R.string.dialog_notice_title), context.getString(R.string.setting_cant_add_by_not_saved_current))
            return false
        }

        if (context.getShare().getStoreSettings().size >= maxSettingSize) {
            if (context.getShare().getStoreSettings().size != maxSettingSize) //要想個辦法把「新增」和「更新」分開，不然到上限設定檔數量的時候會有問題 //收尾issue
                context.showMessageDialogOnlyOKButton(context.getString(R.string.dialog_notice_title), context.getString(R.string.setting_cant_add_by_over_max_size).format(maxSettingSize))
            return false
        }

        return true
    }

    private fun signInAction(scanContent: String) {
        //            logi(TAG, "外面收到的內容是=>${scanContent}")
        if (scanContent.startsWith("QRCodeSignIn")) { // 掃描到設定檔
            scanContent.split("※").getOrNull(1)?.let {
                if (!it.isJson())
                    return@let null

                it.toDataBean(SettingDataItem())?.let { item ->
                    activity.showDialogAndConfirmToSaveSetting(item) { itemCallBack ->
                        resumeScreenAnimation() // 應該要在couldBeAdd的按鈕裡面才恢復動畫 // 收尾issue
                        if (!couldBeAdd()) return@showDialogAndConfirmToSaveSetting false
                        itemCallBack?.let { update ->
                            nowSetting = update
                            mBinding.fabSetting.text = update.name
                        }
                        return@showDialogAndConfirmToSaveSetting true
                    }
                }
                return
            } ?: resumeScreenAnimation()
        }


        // 到這裡應該只要處理Google表單的網址，不是的話就不處理。
        if (!scanContent.startsWith("https://docs.google.com/forms/")) {
            resumeScreenAnimation()
            return
        }

        // 處理掃描到的簽到QRCode：
        val getScanSignInPersonName = scanContent.getSignInPersonByScan(context)

        if (getScanSignInPersonName == "null") {
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

    private fun initData() {

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
        val intent = Intent(activity, SettingActivity::class.java)
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


