package com.module.home.updateinfo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.titlebar.CommonTitleBar;
import com.module.home.R;
import com.zq.live.proto.Common.ESex;

//编辑性别
public class EditInfoSexFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    ExImageView mMale;
    ExImageView mFemale;

    int sex = 0;// 未知、非法参数

    @Override
    public int initView() {
        return R.layout.edit_info_sex_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) getRootView().findViewById(R.id.titlebar);
        mMale = (ExImageView) getRootView().findViewById(R.id.male);
        mFemale = (ExImageView) getRootView().findViewById(R.id.female);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
//                        U.getSoundUtils().play(EditInfoActivity.TAG, R.raw.normal_back, 500);
                U.getFragmentUtils().popFragment(EditInfoSexFragment.this);
            }
        });

        mTitlebar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                clickComplete();
            }
        });

        mMale.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                selectSex(true);
            }
        });

        mFemale.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                selectSex(false);
            }
        });

        if (MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue()) {
            selectSex(true);
        } else if (MyUserInfoManager.getInstance().getSex() == ESex.SX_FEMALE.getValue()) {
            selectSex(false);
        }
    }

    private void clickComplete() {
        if (this.sex == 0) {
            U.getFragmentUtils().popFragment(EditInfoSexFragment.this);
            return;
        } else if (this.sex == MyUserInfoManager.getInstance().getSex()) {
            U.getFragmentUtils().popFragment(EditInfoSexFragment.this);
            return;
        } else {
            MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                    .setSex(sex)
                    .build(), false, false, new MyUserInfoManager.ServerCallback() {
                @Override
                public void onSucess() {
                    U.getToastUtil().showShort("性别更新成功");
                    U.getFragmentUtils().popFragment(EditInfoSexFragment.this);
                }

                @Override
                public void onFail() {

                }
            });
        }
    }


    // 选一个，另一个需要缩放动画
    private void selectSex(boolean isMale) {
        this.sex = isMale ? ESex.SX_MALE.getValue() : ESex.SX_FEMALE.getValue();
        mMale.setSelected(isMale ? true : false);
        mFemale.setSelected(isMale ? false : true);

        mMale.setClickable(isMale ? false : true);
        mFemale.setClickable(isMale ? true : false);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
