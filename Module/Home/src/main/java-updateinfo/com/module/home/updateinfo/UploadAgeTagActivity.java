package com.module.home.updateinfo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.module.RouterConstants;
import com.module.home.IHomeService;
import com.module.home.R;
import com.module.playways.IPlaywaysModeService;
import com.zq.person.view.AgeTagView;

@Route(path = RouterConstants.ACTIVITY_UPLOAD_AGE)
public class UploadAgeTagActivity extends BaseActivity {

    CommonTitleBar mTitlebar;
    TextView mHintTv;
    AgeTagView mAgeTagView;
    ExTextView mSubmitTv;

    String mNickName;
    int mSex;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.upload_account_age_tag_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = findViewById(R.id.titlebar);
        mHintTv = findViewById(R.id.hint_tv);
        mAgeTagView = findViewById(R.id.age_tag_view);
        mSubmitTv = findViewById(R.id.submit_tv);

        mNickName = getIntent().getStringExtra("nickname");
        mSex = getIntent().getIntExtra("sex", 0);

        mAgeTagView.setTextColor(U.getColor(R.color.white_trans_50));
        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        mSubmitTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                int ageStage = mAgeTagView.getSelectTag();
                if (ageStage == 0) {
                    U.getToastUtil().showShort("您当前选择的年龄段为空");
                } else {
                    MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                            .setNickName(mNickName)
                            .setSex(mSex)
                            .setAgeStage(ageStage)
                            .build(), false, false, new MyUserInfoManager.ServerCallback() {
                        @Override
                        public void onSucess() {
                            goNewMatch();
                        }

                        @Override
                        public void onFail() {

                        }
                    });
                }
            }
        });
    }

    private void goNewMatch() {
        if (!U.getActivityUtils().isHomeActivityExist()) {
            IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();
            if (channelService != null) {
                channelService.goHomeActivity(this);
            }
        }
        IPlaywaysModeService playwaysModeService = (IPlaywaysModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
        if (playwaysModeService != null) {
            playwaysModeService.tryGoNewGrabMatch(this);
        }
        // TODO: 2019/5/16 因为fastLogin的标记为用在是否要完善资料上了
        MyUserInfoManager.getInstance().setFirstLogin(false);

        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if (U.getActivityUtils().isHomeActivity(activity)) {
                continue;
            }
            activity.finish();
        }
        StatisticsAdapter.recordCountEvent("signup", "success2", null, true);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        // 自己处理有键盘时的整体布局
        return true;
    }

    @Override
    public boolean canSlide() {
        return false;
    }
}
