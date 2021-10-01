package project.main.activity

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
import com.buddha.qrcodeweb.R
import com.buddha.qrcodeweb.databinding.ActivityScanBinding
import project.main.activity.const.PERMISSIONS_REQUEST_CODE
import project.main.activity.const.permissionPerms
import project.main.base.BaseActivity
import project.main.database.getRecordDao
import project.main.database.insertNewRecord
import tool.*
import uitool.ViewTool
import utils.*


class ScanActivity : BaseActivity<ActivityScanBinding>({ ActivityScanBinding.inflate(it) }), EasyPermissions.PermissionCallbacks {


    private val liveResult by lazy { MutableLiveData<String>() }
//    private val livePassword by lazy { MutableLiveData<String>() }

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


    private fun setSettingFabText() { // 如果有儲存的設定值才要設定fab按鍵內容(要顯示當前的設定檔名稱)。

        if (!context.getShare().isFirstTimeStartThisApp()) { //不是第一次進入才要顯示設定檔名稱
            val nowSetting = context.getShare().getNowUseSetting()
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
        liveResult.observe(activity, Observer {
            // 掃描到的QRCode將在這裡處理。

            if (it.getUrlKey(context.getShare().getKeyName()) == "null") {
                if (textDialog == null) {
                    textDialog = activity.showSignInErrorDialog {
                        resumeScreenAnimation()
                        textDialog = null
                    }
                }
                return@Observer
            }
            val signInTime = Date().time
            signInResult = "${signInTime.toString("yyyy/MM/dd HH:mm:ss")}\n${it.getUrlKey(context.getShare().getKeyName())}簽到完成。"

//          // 導向至網頁
            val sendRequest = it.concatSettingColumn(context.getShare().getNowUseSetting())
//            Intent().apply {
//                action = Intent.ACTION_VIEW
//                data = Uri.parse(sendRequest)
//                startActivity(this)
//            }

            // 應用程式內打API
            MainScope().launch {
                if (activity.sendApi(sendRequest) { }) {
                    // 關閉進度框、顯示簽到結果視窗。
                    if (textDialog == null && signInResult.isNotEmpty()) {
                        textDialog = activity.showSignInCompleteDialog(signInResult) {
                            signInResult = ""
                            resumeScreenAnimation()
                            textDialog = null
                            activity.getRecordDao().insertNewRecord(signInTime, it, sendRequest, activity.getShare().getNowUseSetting() ?: return@showSignInCompleteDialog)
                        }
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
        })

//        livePassword.observe(activity, {
//            //設定的密碼將在這裡收到。
//            context.getShare().setPassword(it)
//        })

    }


    private fun initData() {

    }

    private fun initView() {
        mBinding.lvScanQrcodeMotion.initialLottieByFileName(context, AnimationFileName.SCAN_MOTION, true)
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

        mBinding.fabRecord.setOnClickListener {
            clickToRecordPage()
        }

        mBinding.fabSetting.setOnClickListener {
            clickToSettingPage()
//            clickPasswordInputAction()
        }

//        forSettingResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            if (it.resultCode == Activity.RESULT_OK) {
//
//                logi("clickToSettingPage", "OK，我正確的返回了。")
//            } else {
//                logi("clickToSettingPage", "我不正確的返回了。")
//            }
//        }
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

//    private fun clickPasswordInputAction() {
//        if (inputDialog == null) {
//            inputDialog = PasswordInputDialog(context).apply {
//
//                title = context.getString(R.string.password_dialog_title)
//                limitTextSize = 0
//                editText = context.getShare().getStorePassword()
//                hintText = context.getString(R.string.password_dialog_hint)
//                //                var editString = context.getShare().getStorePassword()
//                dialogBinding.edtText.addTextChangedListener {
//                    editText = it.toString()
//                }
//                dialogBinding.btnLift.setOnClickListener {
//                    inputDialog = null
//                    dialog.dismiss()
//                }
//                dialogBinding.btnRight.setOnClickListener {
//                    inputDialog = null
//                    if (editText.isNotEmpty()) {
//                        livePassword.postValue(editText)
//                        context.getShare().setPassword(editText)
//                        dialog.dismiss()
//                    } else {
//                        if (textDialog == null) {
//                            textDialog = showMessageDialogOnlyOKButton(context, context.getString(R.string.dialog_notice_title), "${context.getString(R.string.password_dialog_hint)}!") {
//                                textDialog = null
//                            }
//                        }
//                    }
//                }
//
//            }
//            inputDialog?.show()
//        }
//    }

    override fun onResume() {
        super.onResume()
        requestPermissions() //若沒有請求權限會是一片黑屏 // 無論之前是否有權限都要再次請求權限，因為要開相機(實測過後發現)

//        // 顯示簽到完成對話框。
//        if (textDialog == null)
//            showSignInCompleteDialog {
//                signInResult = ""
//                textDialog = null
//            }

        resumeScreenAnimation()

        setSettingFabText()
    }

    override fun onPause() {
        super.onPause()
        pauseScrennAnimation()
    }

    override fun finish() {
        super.finish()
        activity.overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up)
    }

    private fun pauseScrennAnimation() {
        mBinding.zxingQrcodeScanner.pause()
        mBinding.lvScanQrcodeMotion.pauseAnimation()
    }

    private fun resumeScreenAnimation() {
        mBinding.zxingQrcodeScanner.resume()
        mBinding.lvScanQrcodeMotion.resumeAnimation()
    }

    //    private fun checkPermission(): Boolean {
//        return EasyPermissions.hasPermissions(this, *permissionPerms)
//    }
//
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
                textDialog = context.showMessageDialogOnlyOKButton(context.getString(R.string.dialog_notice_title), context.getString(R.string.permission_request)) {
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