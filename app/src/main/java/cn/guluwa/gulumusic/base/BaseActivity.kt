package cn.guluwa.gulumusic.base

import android.Manifest
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.transition.Explode
import android.transition.Fade

import cn.guluwa.gulumusic.R
import cn.guluwa.gulumusic.ui.viewmodel.MainViewModel

/**
 * Created by guluwa on 2018/1/11.
 */

abstract class BaseActivity : AppCompatActivity() {

    abstract val viewLayoutId: Int

    lateinit var mViewDataBinding: ViewDataBinding

    lateinit var mViewModel: MainViewModel

    // 需要进行检测的权限数组
    private var needPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    private var isNeedCheck = true

    protected abstract fun initViews()

    protected abstract fun initViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewDataBinding = DataBindingUtil.setContentView(this, viewLayoutId)
        mViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        bindServiceConnection()
        window.enterTransition = Explode()
        window.exitTransition = Fade()
        initViews()
        initViewModel()
    }

    open fun bindServiceConnection() {

    }

    override fun onResume() {
        super.onResume()
        if (isNeedCheck) {
            checkPermissions(needPermissions)
        }
    }

    private fun checkPermissions(permissions: Array<String>) {
        val needRequestPermissionList = findDeniedPermissions(permissions)
        if (needRequestPermissionList.isNotEmpty()) {
            ActivityCompat.requestPermissions(this,
                    needRequestPermissionList.toTypedArray(),
                    PERMISSION_REQUEST_CODE)
        }
    }

    private fun findDeniedPermissions(permissions: Array<String>): List<String> {
        return permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }
    }

    private fun verifyPermissions(grantResults: IntArray): Boolean {
        return grantResults.none { it != PackageManager.PERMISSION_GRANTED }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, paramArrayOfInt: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog()
                isNeedCheck = false
            }
        }
    }

    private fun showMissingPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("提示")
        builder.setMessage("亲，应用缺少必要权限，将无法正常运行。\n\n请点击\"设置\"-\"权限\"-打开所需权限。")
        builder.setNegativeButton("取消") { _, _ -> finish() }
        builder.setPositiveButton("设置") { _, _ -> startAppSettings() }
        builder.setCancelable(false)
        val dialog = builder.show()
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
    }

    private fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + packageName)
        if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                println("no activity found")
            }
        }
    }

    fun showSnackBar(msg: String) {
        val snackBar = Snackbar.make(mViewDataBinding.root, msg, Snackbar.LENGTH_SHORT)
        snackBar.view.setBackgroundColor(resources.getColor(R.color.green))
        snackBar.show()
    }

    fun showSnackBarWithAction(msg: String, action: String) {
        val snackBar = Snackbar.make(mViewDataBinding.root, msg, Snackbar.LENGTH_SHORT)
        snackBar.view.setBackgroundColor(resources.getColor(R.color.green))
        snackBar.setAction(action) {
            // TODO: 2018/1/24 可以加入回调接口
        }
        snackBar.show()
    }

    companion object {

        private const val PERMISSION_REQUEST_CODE = 0
    }
}
