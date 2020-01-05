package com.module.home.updateinfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.module.RouterConstants;
import com.module.home.IHomeService;
import com.module.home.R;
import com.module.playways.IPlaywaysModeService;
import com.component.person.view.AgeTagView;
import com.zq.live.proto.Common.ESex;

@Route(path = RouterConstants.ACTIVITY_UPLOAD_SEX_AGE)
public class UploadSexAndAgeTagActivity extends BaseActivity {

    CommonTitleBar mTitlebar;
    ExImageView mMaleIv;
    ImageView mMaleSelect;
    ExImageView mFemaleIv;
    ImageView mFemaleSelect;
    AgeTagView mAgeTagView;
    ExTextView mSubmitTv;

    String mNickName;
    int mSex;
    int mAgaTag;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.upload_account_age_tag_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getStatusBarUtil().setTransparentBar(this, false);
        mTitlebar = findViewById(R.id.titlebar);
        mMaleIv = findViewById(R.id.male_iv);
        mMaleSelect = findViewById(R.id.male_select);
        mFemaleIv = findViewById(R.id.female_iv);
        mFemaleSelect = findViewById(R.id.female_select);
        mAgeTagView = findViewById(R.id.age_tag_view);

        mSubmitTv = findViewById(R.id.submit_tv);

        // 初始化数据
        mNickName = getIntent().getStringExtra("nickname");

//        mAgeTagView.setTextColor(U.getColor(R.color.white_trans_50));
        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        mMaleIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                setSex(ESex.SX_MALE.getValue());
            }
        });

        mFemaleIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                setSex(ESex.SX_FEMALE.getValue());
            }
        });

        mAgeTagView.setListener(new AgeTagView.Listener() {
            @Override
            public void onUnSelect() {
                // 无选中的
                mAgaTag = 0;
                mSubmitTv.setAlpha(0.5f);
                mSubmitTv.setClickable(false);
            }

            @Override
            public void onSelectedAge(int ageTag) {
                // 有选中的
                mAgaTag = ageTag;
                mSubmitTv.setAlpha(1f);
                mSubmitTv.setClickable(true);
            }
        });

        mSubmitTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mSex != ESex.SX_MALE.getValue() && mSex != ESex.SX_FEMALE.getValue()) {
                    U.getToastUtil().showShort("您当前选择的性别为空");
                } else if (mAgaTag == 0) {
                    U.getToastUtil().showShort("您当前选择的年龄段为空");
                } else {
                    MyUserInfoManager.INSTANCE.updateInfo(MyUserInfoManager.INSTANCE.newMyInfoUpdateParamsBuilder()
                            .setNickName(mNickName)
                            .setSex(mSex)
                            .setAgeStage(mAgaTag)
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

        setSex(MyUserInfoManager.INSTANCE.getSex());

        mSubmitTv.setClickable(false);
        mSubmitTv.setAlpha(0.5f);
    }

    public void setSex(int sex) {
        this.mSex = sex;
        if (sex == ESex.SX_MALE.getValue()) {
            mMaleIv.setSelected(true);
            mFemaleIv.setSelected(false);
            mMaleSelect.setVisibility(View.VISIBLE);
            mFemaleSelect.setVisibility(View.GONE);
        } else if (sex == ESex.SX_FEMALE.getValue()) {
            mMaleIv.setSelected(false);
            mFemaleIv.setSelected(true);
            mMaleSelect.setVisibility(View.GONE);
            mFemaleSelect.setVisibility(View.VISIBLE);
        } else {
            mMaleIv.setSelected(false);
            mFemaleIv.setSelected(false);
            mMaleSelect.setVisibility(View.GONE);
            mFemaleSelect.setVisibility(View.GONE);
        }
    }

    private void goNewMatch() {
        if (!U.getActivityUtils().isHomeActivityExist()) {
            IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();
            if (channelService != null) {
                channelService.goHomeActivity(this);
            }
        }
        if (U.getChannelUtils().getChannel().startsWith("CHORUS")) {
            finish();
        } else {
            IPlaywaysModeService playwaysModeService = (IPlaywaysModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
            if (playwaysModeService != null) {
                playwaysModeService.tryGoNewGrabMatch(this);
            }
        }
        // TODO: 2019/5/16 因为fastLogin的标记为用在是否要完善资料上了
        MyUserInfoManager.INSTANCE.setFirstLogin(false);
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
