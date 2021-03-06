package com.smile.wechat.activity;

import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.smile.wechat.R;
import com.smile.wechat.model.Contact;
import com.smile.wechat.nimsdk.NimFriendSDK;
import com.smile.wechat.utils.UIUtils;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.constant.FriendFieldEnum;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @创建者 CYN
 * @描述 修改备注信息
 */
public class AliasActivity extends BaseActivity {

    private String alias;
    private Contact mContact;

    public static final int REQ_CHANGE_ALIAS = 100;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.btnOk)
    Button mBtnOk;
    @BindView(R.id.etAlias)
    EditText mEtAlias;
    @BindView(R.id.ibClearAlias)
    ImageButton mIbClearAlias;
    @BindView(R.id.etTag)
    EditText mEtTag;
    @BindView(R.id.ibClearTag)
    ImageButton mIbClearTag;
    @BindView(R.id.etPhone)
    EditText mEtPhone;
    @BindView(R.id.ibClearPhone)
    ImageButton mIbClearPhone;
    @BindView(R.id.etDesc)
    EditText mEtDesc;
    @BindView(R.id.ibClearDesc)
    ImageButton mIbClearDesc;
    @BindView(R.id.etPicture)
    EditText mEtPicture;
    @BindView(R.id.ibClearPicture)
    ImageButton mIbClearPicture;

    @OnClick({R.id.btnOk})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.btnOk:
                saveAliasChange();
                break;
        }
    }

    @Override
    public void init() {
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        if (mContact == null) {
            interrupt();
            return;
        }
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_alias);
        ButterKnife.bind(this);
        initToolbar();

        String alias = mContact.getFriend().getAlias();
        if (!TextUtils.isEmpty(alias)) {
            mEtAlias.setText(alias);
            mEtAlias.setSelection(alias.length());
        }
    }

    @Override
    public void initData() {
        alias = mContact.getFriend().getAlias();
    }

    @Override
    public void initListener() {
        mEtAlias.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mIbClearAlias.setVisibility(View.VISIBLE);
                } else {
                    mIbClearAlias.setVisibility(View.GONE);
                }
            }
        });
        mIbClearAlias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtAlias.setText("");
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!alias.equals(mEtAlias.getText().toString().trim())) {
            showMaterialDialog("", "保存本次编辑?", "保存", "不保存", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveAliasChange();
                    hideMaterialDialog();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideMaterialDialog();
                }
            });
            return;
        }
        super.onBackPressed();
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

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("备注信息");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
        mBtnOk.setVisibility(View.VISIBLE);
    }

    private void saveAliasChange() {
        String alias = mEtAlias.getText().toString().trim();
        showWaitingDialog("请稍等");
        Map<FriendFieldEnum, Object> map = new HashMap<>(1);
        map.put(FriendFieldEnum.ALIAS, alias);
        NimFriendSDK.updateFriendFields(mContact.getAccount(), map, new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                UIUtils.showToast("修改备注信息成功");
                hideWaitingDialog();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailed(int code) {
                UIUtils.showToast("修改备注信息失败" + code);
                hideWaitingDialog();
            }

            @Override
            public void onException(Throwable exception) {
                exception.printStackTrace();
                hideWaitingDialog();
            }
        });
    }
}
