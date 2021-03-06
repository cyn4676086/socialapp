package com.smile.wechat.activity;

import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.smile.wechat.R;
import com.smile.wechat.nimsdk.NimTeamSDK;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.model.Team;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * @创建者 CYN
 * @描述 编辑群公告界面
 */

public class TeamAnnouncementEditActivity extends BaseActivity {

    public static final String TEAM = "team";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.btnOk)
    Button mBtnOk;

    @BindView(R.id.etContent)
    EditText mEtContent;
    private Team mTeam;

    @OnClick({R.id.btnOk})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.btnOk:
                final String content = mEtContent.getText().toString().trim();
                if (!TextUtils.isEmpty(content)) {
                    showMaterialDialog("", "该公告会通知全部群成员，是否发布?", "发布", "取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideMaterialDialog();
                            Map<TeamFieldEnum, Serializable> fields = new HashMap<>(1);
                            fields.put(TeamFieldEnum.Announcement, content);
                            NimTeamSDK.updateTeamFields(mTeam.getId(), fields);
                            showWaitingDialog("正在保存");
                            //TODO:@所有人
                            finish();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideMaterialDialog();
                        }
                    });

                }
                break;
        }
    }

    @Override
    public void init() {
        mTeam = (Team) getIntent().getSerializableExtra(TEAM);
        if (mTeam == null) {
            interrupt();
        }
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_team_announcement_edit);
        ButterKnife.bind(this);
        initToolbar();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("群公告");
        mToolbar.setNavigationIcon(R.mipmap.ic_back);
        mBtnOk.setVisibility(View.VISIBLE);
        mBtnOk.setText("完成");
    }


}
