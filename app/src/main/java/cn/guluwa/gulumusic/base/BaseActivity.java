package cn.guluwa.gulumusic.base;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

import cn.guluwa.gulumusic.R;
import cn.guluwa.gulumusic.ui.viewmodel.MainViewModel;

/**
 * Created by guluwa on 2018/1/11.
 */

public abstract class BaseActivity extends AppCompatActivity {

    public abstract int getViewLayoutId();

    protected abstract void initViews();

    protected abstract void initViewModel();

    public ViewDataBinding mViewDataBinding;

    public MainViewModel mViewModel;

    // 需要进行检测的权限数组
    protected String[] needPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private static final int PERMISSION_REQUEST_CODE = 0;

    private boolean isNeedCheck = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewDataBinding = DataBindingUtil.setContentView(this, getViewLayoutId());
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        bindServiceConnection();
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Fade());
        initViews();
        initViewModel();
    }

    public void bindServiceConnection() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedCheck) {
            checkPermissions(needPermissions);
        }
    }

    private void checkPermissions(String... permissions) {
        List<String> needRequestPermissionList = findDeniedPermissions(permissions);
        if (null != needRequestPermissionList
                && needRequestPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    needRequestPermissionList.toArray(
                            new String[needRequestPermissionList.size()]),
                    PERMISSION_REQUEST_CODE);
        }
    }

    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, perm)) {
                needRequestPermissionList.add(perm);
            }
        }
        return needRequestPermissionList;
    }

    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] paramArrayOfInt) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                isNeedCheck = false;
            }
        }
    }

    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("亲，应用缺少必要权限，将无法正常运行。\n\n请点击\"设置\"-\"权限\"-打开所需权限。");
        builder.setNegativeButton("取消", (dialog, which) -> finish());
        builder.setPositiveButton("设置", (dialog, which) -> startAppSettings());
        builder.setCancelable(false);
        AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
    }

    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    public void showSnackBar(String msg) {
        Snackbar snackbar = Snackbar.make(mViewDataBinding.getRoot(), msg, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.green));
        snackbar.show();
    }

    public void showSnackBarWithAction(String msg, String action) {
        Snackbar snackbar = Snackbar.make(mViewDataBinding.getRoot(), msg, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.green));
        snackbar.setAction(action, view -> {
            // TODO: 2018/1/24 可以加入回调接口
        });
        snackbar.show();
    }
}
