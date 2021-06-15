package com.smile.wechat.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.smile.wechat.App;
import com.smile.wechat.R;
import com.smile.wechat.view.CustomDialog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;


/**
 * @创建者 CYN
 * @描述 AppCompatActivity的基类
 */
public class BaseActivity extends AppCompatActivity {

    private CustomDialog mDialogWaiting;
    private MaterialDialog mMaterialDialog;
    private boolean interrupt = false;//onCreate是否中断

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        init();
        if (interrupt) {
            finish();
            return;
        }
        initView();
        initData();
        initListener();
        setStatusBarColor(getWindow(),getResources(),R.color.theme_color);
        App.activities.add(this);
    }

   
    /**
     * 中断onCreate
     */
    public void interrupt() {
        this.interrupt = true;
    }

    /**
     * 设置沉浸式状态栏
     *
     */
    public static void fixSystemUI(Activity mActivity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //获取最顶层的View
            mActivity.getWindow().getDecorView()
                      .setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                          View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            mActivity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 自定义状态栏颜色
     */
    public static void setStatusBarColor(Window window, Resources resources, int id){
        //After LOLLIPOP not translucent status bar
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //Then call setStatusBarColor.
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(resources.getColor(id));
    }

    /**
     * 获取状态栏的高度
     *
     * @return
     */
    public int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void init() {
    }

    public void initView() {
    }

    public void initData() {
    }

    public void initListener() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.activities.remove(this);
    }

    /**
     * 显示等待提示框
     */
    public Dialog showWaitingDialog(String tip) {
        hideWaitingDialog();
        View view = View.inflate(this, R.layout.dialog_waiting, null);
        if (!TextUtils.isEmpty(tip))
            ((TextView) view.findViewById(R.id.tvTip)).setText(tip);
        mDialogWaiting = new CustomDialog(this, view, R.style.dialog);
        mDialogWaiting.show();
        mDialogWaiting.setCancelable(false);
        return mDialogWaiting;
    }

    /**
     * 隐藏等待提示框
     */
    public void hideWaitingDialog() {
        if (mDialogWaiting != null) {
            mDialogWaiting.dismiss();
            mDialogWaiting = null;
        }
    }

    /**
     * 显示MaterialDialog
     */
    public MaterialDialog showMaterialDialog(String tip, String message, String positiveText, String negativeText, View.OnClickListener positiveButtonClickListener, View.OnClickListener negativeButtonClickListener) {
        hideMaterialDialog();
        mMaterialDialog = new MaterialDialog(this);
        if (!TextUtils.isEmpty(tip)) {
            mMaterialDialog.setTitle(tip);
        }
        if (!TextUtils.isEmpty(message)) {
            mMaterialDialog.setMessage(message);
        }
        if (!TextUtils.isEmpty(positiveText)) {
            mMaterialDialog.setPositiveButton(positiveText, positiveButtonClickListener);
        }
        if (!TextUtils.isEmpty(negativeText)) {
            mMaterialDialog.setNegativeButton(negativeText, negativeButtonClickListener);
        }
        mMaterialDialog.show();
        return mMaterialDialog;
    }

    /**
     * 隐藏MaterialDialog
     */
    public void hideMaterialDialog() {
        if (mMaterialDialog != null) {
            mMaterialDialog.dismiss();
            mMaterialDialog = null;
        }
    }

    //实时监测权限情况

    //申请运行时权限的Code
    private static final int PERMISSION_REQUEST_CODE = 1000;
    //申请窗口权限的Code
    public static final int PERMISSION_WINDOW_REQUEST_CODE = 1001;

    //申明所需权限
    private final String[] mStrPermission = {
              Manifest.permission.READ_PHONE_STATE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE,
              Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.CAMERA,
              Manifest.permission.READ_CONTACTS,
              Manifest.permission.ACCESS_COARSE_LOCATION,
              Manifest.permission.RECORD_AUDIO,
              Manifest.permission.CALL_PHONE,
              Manifest.permission.ACCESS_FINE_LOCATION

    };

    //保存没有同意的权限
    private final List<String> mPerList = new ArrayList<>();
    //保存没有同意的失败权限
    private final List<String> mPerNoList = new ArrayList<>();

    private OnPermissionsResult permissionsResult;

    /**
     * 一个方法请求权限
     *
     * @param permissionsResult
     */
    protected void request(OnPermissionsResult permissionsResult) {
        if (!checkPermissionsAll()) {
            requestPermissionAll(permissionsResult);
        }
    }

    /**
     * 判断单个权限
     *
     * @param permissions
     * @return
     */
    protected boolean checkPermissions(String permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = checkSelfPermission(permissions);
            return check == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    /**
     * 判断是否需要申请权限
     *
     * @return
     */
    protected boolean checkPermissionsAll() {
        mPerList.clear();
        for (int i = 0; i < mStrPermission.length; i++) {
            boolean check = checkPermissions(mStrPermission[i]);
            //如果不同意则请求
            if (!check) {
                mPerList.add(mStrPermission[i]);
            }
        }
        return mPerList.size() <= 0;
    }

    /**
     * 请求权限
     *
     * @param mPermissions
     */
    protected void requestPermission(String[] mPermissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(mPermissions, PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * 申请所有权限
     *
     * @param permissionsResult
     */
    protected void requestPermissionAll(OnPermissionsResult permissionsResult) {
        this.permissionsResult = permissionsResult;
        requestPermission(mPerList.toArray(new String[mPerList.size()]));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mPerNoList.clear();
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        //有失败的权限
                        mPerNoList.add(permissions[i]);
                    }
                }
                if (permissionsResult != null) {
                    if (mPerNoList.size() == 0) {
                        permissionsResult.OnSuccess();
                    } else {
                        permissionsResult.OnFail(mPerNoList);
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected interface OnPermissionsResult {
        void OnSuccess();

        void OnFail(List<String> noPermissions);
    }

    /**
     * 判断窗口权限
     *
     * @return
     */
    protected boolean checkWindowPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    /**
     * 请求窗口权限
     */
    protected void requestWindowPermissions() {
        Toast.makeText(this, "申请窗口权限，暂时没做UI交互", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                  , Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, PERMISSION_WINDOW_REQUEST_CODE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}
