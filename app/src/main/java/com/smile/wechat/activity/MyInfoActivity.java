package com.smile.wechat.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;
import com.lqr.imagepicker.ui.ImageGridActivity;
import com.lqr.optionitemview.OptionItemView;
import com.smile.wechat.R;
import com.smile.wechat.imageloader.ImageLoaderManager;
import com.smile.wechat.model.UserCache;
import com.smile.wechat.nimsdk.NimUserInfoSDK;
import com.smile.wechat.utils.UIUtils;
import com.smile.wechat.view.CustomDialog;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.smile.wheelviews.CityPickerView;
import com.smile.wheelviews.TimePickerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;

/**
 * @创建者 CYN
 * @描述 个人信息
 */
public class MyInfoActivity extends BaseActivity {

    Intent mIntent;
    private NimUserInfo mNimUserInfo;

    private View mGenderDialogView;
    private CustomDialog mDialog;
    private TextView mTvMale;
    private TextView mTvFemale;
    private Drawable mSelectedDrawable;
    private Drawable mUnSelectedDrawable;

    Observer<List<NimUserInfo>> userInfoUpdateObserver = new Observer<List<NimUserInfo>>() {
        @Override
        public void onEvent(List<NimUserInfo> nimUserInfos) {
            initData();
        }
    };


    @BindView(R.id.tv_account_birth)
    TextView mTvBirthDay;
    @BindView(R.id.tv_account_location)
    TextView mTvLocation;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.llHeader)
    LinearLayout mLlHeader;
    @BindView(R.id.ivHeader)
    ImageView mIvHeader;
    @BindView(R.id.oivName)
    OptionItemView mOivName;
    @BindView(R.id.oivQRCordCard)
    OptionItemView mOivQRCordCard;
    @BindView(R.id.oivAccount)
    OptionItemView mOivAccount;
    @BindView(R.id.oivGender)
    OptionItemView mOivGender;
    @BindView(R.id.oivSignature)
    OptionItemView mOivSignature;

    @OnClick({R.id.tv_account_location,R.id.tv_account_birth,R.id.llHeader, R.id.ivHeader, R.id.oivName, R.id.oivQRCordCard, R.id.oivGender, R.id.oivSignature})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.tv_account_location:
                setLocation();
                break;
            case R.id.tv_account_birth:
                setBirthday();
                break;
            case R.id.llHeader:
                mIntent = new Intent(this, ImageGridActivity.class);
                startActivityForResult(mIntent, SessionActivity.IMAGE_PICKER);
                break;
            case R.id.ivHeader:
                if (mNimUserInfo == null)
                    return;
                mIntent = new Intent(this, ShowBigImageActivity.class);
                mIntent.putExtra("url", mNimUserInfo.getAvatar());
                startActivity(mIntent);
                break;
            case R.id.oivName:
                mIntent = new Intent(this, ChangeNameActivity.class);
                mIntent.putExtra("name", mNimUserInfo.getName());
                startActivity(mIntent);
                break;
            case R.id.oivQRCordCard:
                mIntent = new Intent(this, QRCodeCardActivity.class);
                mIntent.putExtra(QRCodeCardActivity.QRCODE_USER, mNimUserInfo);
                startActivity(mIntent);
                break;
            case R.id.oivGender:
                if (mGenderDialogView == null) {
                    mGenderDialogView = View.inflate(this, R.layout.dialog_gender, null);
                    mTvMale = (TextView) mGenderDialogView.findViewById(R.id.tvMale);
                    mTvFemale = (TextView) mGenderDialogView.findViewById(R.id.tvFemale);
                    mDialog = new CustomDialog(this, mGenderDialogView, R.style.dialog);
                    mTvMale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateGender(GenderEnum.MALE);
                        }
                    });
                    mTvFemale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateGender(GenderEnum.FEMALE);
                        }
                    });
                }
                updateGenderView(mNimUserInfo.getGenderEnum());
                mDialog.show();
                break;
            case R.id.oivSignature:
                mIntent = new Intent(this, ChangeSignatureActivity.class);
                mIntent.putExtra("signature", mNimUserInfo.getSignature());
                startActivity(mIntent);
                break;
//            case R.id.llHeader:
//                break;
        }
    }

    @Override
    public void init() {
        // 监听用户信息更新
        NimUserInfoSDK.observeUserInfoUpdate(userInfoUpdateObserver, true);

        mSelectedDrawable = UIUtils.getResource().getDrawable(R.mipmap.list_selected);
        mUnSelectedDrawable = UIUtils.getResource().getDrawable(R.mipmap.list_unselected);
        mSelectedDrawable.setBounds(0, 0, mSelectedDrawable.getMinimumWidth(), mSelectedDrawable.getMinimumHeight());
        mUnSelectedDrawable.setBounds(0, 0, mUnSelectedDrawable.getMinimumWidth(), mUnSelectedDrawable.getMinimumHeight());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 销毁用户信息更新监听
        NimUserInfoSDK.observeUserInfoUpdate(userInfoUpdateObserver, false);
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_my_info);
        ButterKnife.bind(this);
        initToolbar();
    }

    @Override
    public void initData() {
        mNimUserInfo = NimUserInfoSDK.getUser(UserCache.getAccount());
        if (mNimUserInfo == null) {
            getUserInfoFromRemote();
        } else {
            //头像
            if (!TextUtils.isEmpty(mNimUserInfo.getAvatar())) {
                ImageLoaderManager.LoadNetImage(mNimUserInfo.getAvatar(), mIvHeader);
            }
            //用户名、账号、签名、性别
            mOivName.setRightText(mNimUserInfo.getName());
            mOivAccount.setRightText(mNimUserInfo.getAccount());
            mOivSignature.setRightText(TextUtils.isEmpty(mNimUserInfo.getSignature()) ? "未填写" : mNimUserInfo.getSignature());
            mOivGender.setRightText(mNimUserInfo.getGenderEnum() == GenderEnum.FEMALE ? "女" : mNimUserInfo.getGenderEnum() == GenderEnum.MALE ? "男" : "");
        }
        String birthday = mNimUserInfo.getBirthday();
        if (TextUtils.isEmpty(birthday)) {
            mTvBirthDay.setText("未填写");
        } else {
            mTvBirthDay.setText(birthday);
        }
        String location = mNimUserInfo.getExtension();
        if (TextUtils.isEmpty(location)) {
            mTvLocation.setText("未填写");
        } else {
            mTvLocation.setText(location);
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {//返回多张照片
            if (data != null) {
                //是否发送原图
//                boolean isOrig = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
                showWaitingDialog("上传头像...");
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                if (images != null && images.size() > 0) {
                    //取第一张照片
                    File file = new File(images.get(0).path);
                    NimUserInfoSDK.uploadFile(file, "image/jpeg", new RequestCallbackWrapper<String>() {
                        @Override
                        public void onResult(int code, String url, Throwable exception) {

                            if (code == ResponseCode.RES_SUCCESS
                                    && !TextUtils.isEmpty(url)) {// 上传成功得到Url
                                Map<UserInfoFieldEnum, Object> fields = new HashMap<UserInfoFieldEnum, Object>(
                                        1);
                                fields.put(UserInfoFieldEnum.AVATAR, url);
                            }

                            Map<UserInfoFieldEnum, Object> fields = new HashMap(1);
                            fields.put(UserInfoFieldEnum.AVATAR, url);
                            NimUserInfoSDK.updateUserInfo(fields, new RequestCallbackWrapper<Void>() {
                                @Override
                                public void onResult(int code, Void result, Throwable exception) {
                                    if (code == ResponseCode.RES_SUCCESS) {// 修改成功
                                        UIUtils.showToast("修改成功");
                                        getUserInfoFromRemote();// 重新加载个人资料
                                    } else {// 修改失败
                                        UIUtils.showToast("修改失败，请重试");
                                    }
                                    hideWaitingDialog();
                                }
                            });
                        }
                    });
                }
            }
        }
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("个人信息");
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
    }

    private void getUserInfoFromRemote() {
        List<String> accountList = new ArrayList<>();
        accountList.add(UserCache.getAccount());
        NimUserInfoSDK.getUserInfosFormServer(accountList, new RequestCallback<List<NimUserInfo>>() {
            @Override
            public void onSuccess(List<NimUserInfo> param) {
                initData();
            }

            @Override
            public void onFailed(int code) {
                UIUtils.showToast("获取用户信息失败" + code);
            }

            @Override
            public void onException(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    private void updateGender(final GenderEnum gender) {
        updateGenderView(gender);
        showWaitingDialog("请稍等");
        Map<UserInfoFieldEnum, Object> fields = new HashMap(1);
        fields.put(UserInfoFieldEnum.GENDER, gender.getValue());
        NimUserInfoSDK.updateUserInfo(fields, new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                hideWaitingDialog();
                if (code == ResponseCode.RES_SUCCESS) {
                    UIUtils.showToast("修改成功");
                    mDialog.dismiss();
                } else {
                    UIUtils.showToast("修改失败");
                }
            }
        });
    }

    private void updateGenderView(GenderEnum gender) {
        if (gender == GenderEnum.MALE) {
            mTvMale.setCompoundDrawables(null, null, mSelectedDrawable, null);
            mTvFemale.setCompoundDrawables(null, null, mUnSelectedDrawable, null);
        } else if (gender == GenderEnum.FEMALE) {
            mTvMale.setCompoundDrawables(null, null, mUnSelectedDrawable, null);
            mTvFemale.setCompoundDrawables(null, null, mSelectedDrawable, null);
        } else {
            mTvMale.setCompoundDrawables(null, null, mUnSelectedDrawable, null);
            mTvFemale.setCompoundDrawables(null, null, mUnSelectedDrawable, null);
        }
    }

    /**
     * 设置生日
     */
    private void setBirthday() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_birthday, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        TimePickerView timePickerView = (TimePickerView) view.findViewById(R.id.date_picker);
        timePickerView.setSelectedListener(new TimePickerView.OnDateSelectedListener() {
            @Override
            public void selectedDate(int year, int month, int day) {
                String yearString = String.valueOf(year);
                String monthString = String.valueOf(month);
                String dayString = String.valueOf(day);
                if (monthString.length() == 1){
                    monthString = "0" + monthString;
                }
                if (dayString.length() == 1){
                    dayString = "0" + dayString;
                }
                String birthday = String.format("%s-%s-%s", yearString, monthString, dayString);
                if (!birthday.equals(mTvBirthDay.getText().toString())) {
                    showWaitingDialog("请稍等");
                    Map<UserInfoFieldEnum, Object> fields = new HashMap(1);
                    fields.put(UserInfoFieldEnum.BIRTHDAY, birthday);
                    NimUserInfoSDK.updateUserInfo(fields, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void result, Throwable exception) {
                            hideWaitingDialog();
                            if (code == ResponseCode.RES_SUCCESS) {
                                UIUtils.showToast("修改成功");
                                dialog.dismiss();
                            } else {
                                UIUtils.showToast("修改失败");
                            }
                        }
                    });
                    mTvBirthDay.setText(birthday);
                }
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    /**
     * 设置学校
     */
    private void setLocation(){
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_location, null);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        CityPickerView cityPickerView = (CityPickerView) view.findViewById(R.id.city_picker);
        cityPickerView.setCitySelectedListener(new CityPickerView.OnCitySelectedListener() {
            @Override
            public void citySelected(String province, String city) {
                String location = province + "/" + city;
                if (!location.equals(mTvLocation.getText().toString())) {
                    showWaitingDialog("请稍等");
                    Map<UserInfoFieldEnum, Object> fields = new HashMap(1);
                    fields.put(UserInfoFieldEnum.EXTEND, location);
                    NimUserInfoSDK.updateUserInfo(fields, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void result, Throwable exception) {
                            hideWaitingDialog();
                            if (code == ResponseCode.RES_SUCCESS) {
                                UIUtils.showToast("修改成功");
                                dialog.dismiss();
                            } else {
                                UIUtils.showToast("修改失败");
                            }
                        }
                    });
                    mTvLocation.setText(location);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
