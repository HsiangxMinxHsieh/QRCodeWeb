package com.buddha.qrcodeweb

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
import uitool.ViewTool


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private val activity = this
    private val context: Context = this

    private val liveResult by lazy { MutableLiveData<String>() }
    private val livePassword by lazy { MutableLiveData<String>() }

    lateinit var mBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        mBinding = DataBindingUtil.setContentView(activity, R.layout.activity_main)

        initData()

        initObserver()

        initView()

        initEvent()

    }

    override fun onResume() {
        super.onResume()
        requestPermissions() //若沒有請求權限會是一片黑屏 // 無論之前是否有權限都要再次請求權限，因為要開相機(實測過後發現)

        // 顯示簽到完成對話框。
        showSignInCompleteDialog() {
            signInResult = ""
        }

        resumeScreenAnimation()

        setSettingFabText()
    }

    private fun setSettingFabText() { // 如果有儲存的設定值才要設定fab按鍵內容(要顯示當前的設定檔名稱)。

        val nowSetting = context.getShare().getNowUseSetting()
        if (nowSetting != null && !context.getShare().getStoreSettings().isNullOrEmpty()) {
            mBinding.fabSetting.icon = null
            mBinding.fabSetting.text = nowSetting.name

            ConstraintSet().apply { // 動態設定ConstraintLayout相依關係：
                clone(mBinding.clMain)
                setMargin(R.id.fab_setting, ConstraintSet.TOP, ViewTool.DpToPx(context, 16f))
                connect(R.id.fab_record, ConstraintSet.BOTTOM, R.id.fab_setting, ConstraintSet.TOP, ViewTool.DpToPx(context, 16f))
                constrainWidth(R.id.fab_setting, ConstraintSet.WRAP_CONTENT)
                applyTo(mBinding.clMain)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pauseScrennAnimation()
    }

    private fun showSignInCompleteDialog(okButtonClickAction: () -> Unit = {}): Dialog? {
        if (signInResult.isNotEmpty() && textDialog == null) {
            textDialog = showMessageDialogOnlyOKButton(context, context.getString(R.string.dialog_sign_in_success_title), signInResult) {
                okButtonClickAction.invoke()
                textDialog = null
            }
        }
        return textDialog
    }

    private var signInResult = ""
    private fun initObserver() {
        liveResult.observe(activity, Observer {
            //掃描到的QRCode將在這裡處理。

            if (it.getUrlKey("entry.44110693") == "null") {
                showSignInErrorDialog()
                return@Observer
            }
            signInResult = "${Date().toString("yyyy/MM/dd HH:mm:ss")}\n${it.getUrlKey("entry.44110693")}簽到完成。"

//            //導向至網頁
//            Intent().apply {
//                action = Intent.ACTION_VIEW
//                data = Uri.parse("$it&entry.1199127502=${context.getShare().getStorePassword()}")
//                startActivity(this)
//            }

            //打API(暫定需要嘗試，嘉伸講師還沒提，蝦米擅自先做。)
            val progressDialog = ProgressDialog(context)
            CoroutineScope(Dispatchers.IO).launch {
                MainScope().launch { // 顯示進度框
                    pauseScrennAnimation() // 暫停播放動畫
                    progressDialog.show()
                }
                val response = getURLResponse("$it&entry.1199127502=${context.getShare().getStorePassword()}")
                MainScope().launch { // 關閉進度框、顯示簽到完成視窗。
                    progressDialog.dismiss()
                    if (response == null) {
                        if (textDialog == null) {
                            showSignInCompleteDialog {
                                signInResult = ""
                                resumeScreenAnimation()
                            }
                        }
                    } else {
//
                        showSignInErrorDialog()
                    }

                }
            }
        })

        livePassword.observe(activity, {
            //設定的密碼將在這裡收到。
            context.getShare().setPassword(it)
        })

    }

    private fun showSignInErrorDialog() {
        if (textDialog == null) {
            textDialog = showMessageDialogOnlyOKButton(context, context.getString(R.string.dialog_notice_title), context.getString(R.string.dialog_error_message)) {
                resumeScreenAnimation()
                textDialog = null
            }
        }
    }

    private fun initData() {

    }

    private fun initView() {
        mBinding.lvScanQrcodeMotion.initialLottieByFileName(context, AnimationFileName.SCAN_MOTION, true)
    }

    var textDialog: Dialog? = null
    var inputDialog: Dialog? = null
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
            clickPasswordInputAction()
        }
    }

    private fun clickToRecordPage() {
        
    }

    private fun clickPasswordInputAction() {
        if (inputDialog == null) {
            inputDialog = PasswordInputDialog(context).apply {

                title = context.getString(R.string.password_dialog_title)
                limitTextSize = 0
                editText = context.getShare().getStorePassword()
                hintText = context.getString(R.string.password_dialog_hint)
                //                var editString = context.getShare().getStorePassword()
                dialogBinding.edtText.addTextChangedListener {
                    editText = it.toString()
                }
                dialogBinding.btnLift.setOnClickListener {
                    inputDialog = null
                    dialog.dismiss()
                }
                dialogBinding.btnRight.setOnClickListener {
                    inputDialog = null
                    if (editText.isNotEmpty()) {
                        livePassword.postValue(editText)
                        context.getShare().setPassword(editText)
                        dialog.dismiss()
                    } else {
                        if (textDialog == null) {
                            textDialog = showMessageDialogOnlyOKButton(context, context.getString(R.string.dialog_notice_title), "${context.getString(R.string.password_dialog_hint)}!") {
                                textDialog = null
                            }
                        }
                    }
                }

            }
            inputDialog?.show()
        }
    }

    private fun pauseScrennAnimation() {
        mBinding.zxingQrcodeScanner.pause()
        mBinding.lvScanQrcodeMotion.pauseAnimation()
    }

    private fun resumeScreenAnimation() {
        mBinding.zxingQrcodeScanner.resume()
        mBinding.lvScanQrcodeMotion.resumeAnimation()
    }

    private fun checkPermission(): Boolean {
        val perms = arrayOf(Manifest.permission.CAMERA)
        return EasyPermissions.hasPermissions(this, *perms)
    }

    private val PERMISSIONS_REQUEST_CODE = 548

    private fun requestPermissions() {
        val perms = arrayOf(Manifest.permission.CAMERA)
        EasyPermissions.requestPermissions(this, activity.getString(R.string.permission_request), PERMISSIONS_REQUEST_CODE, *perms)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        //權限被拒
        if (requestCode == PERMISSIONS_REQUEST_CODE) {

        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        //權限允許
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            mBinding.zxingQrcodeScanner.resume()
        }
    }

}