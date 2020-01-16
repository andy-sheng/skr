package com.module.home.updateinfo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.TipsDialogView;
import com.module.home.R;
import com.zq.live.proto.Common.ESex;

//编辑性别
public class EditInfoSexFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    ExImageView mMale;
    ExImageView mFemale;
    ImageView maleSelect;
    ImageView femaleSelect;

    int sex = 0;// 未知、非法参数

    TipsDialogView mTipsDialogView;

    @Override
    public int initView() {
        return R.layout.edit_info_sex_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = getRootView().findViewById(R.id.titlebar);
        mMale = getRootView().findViewById(R.id.male);
        mFemale = getRootView().findViewById(R.id.female);
        maleSelect = getRootView().findViewById(R.id.male_select);
        femaleSelect = getRootView().findViewById(R.id.female_select);

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
                if (mTipsDialogView != null) {
                    mTipsDialogView.dismiss(false);
                }
                mTipsDialogView = new TipsDialogView.Builder(getActivity())
                        .setMessageTip("性别只能修改一次\n确定修改么")
                        .setCancelTip("取消")
                        .setConfirmTip("确定")
                        .setConfirmBtnClickListener(new DebounceViewClickListener() {
                            @Override
                            public void clickValid(View v) {
                                if (mTipsDialogView != null) {
                                    mTipsDialogView.dismiss(true);
                                }
                                clickComplete();
                            }
                        })
                        .setCancelBtnClickListener(new DebounceViewClickListener() {
                            @Override
                            public void clickValid(View v) {
                                if (mTipsDialogView != null) {
                                    mTipsDialogView.dismiss(false);
                                }
                            }
                        })
                        .build();
                mTipsDialogView.showByDialog();
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

        if (MyUserInfoManager.INSTANCE.getSex() == ESex.SX_MALE.getValue()) {
            selectSex(true);
        } else if (MyUserInfoManager.INSTANCE.getSex() == ESex.SX_FEMALE.getValue()) {
            selectSex(false);
        }
    }

    private void clickComplete() {
        if (this.sex == 0) {
            U.getFragmentUtils().popFragment(EditInfoSexFragment.this);
            return;
        } else if (this.sex == MyUserInfoManager.INSTANCE.getSex()) {
            U.getFragmentUtils().popFragment(EditInfoSexFragment.this);
            return;
        } else {
            MyUserInfoManager.INSTANCE.updateInfo(MyUserInfoManager.INSTANCE.newMyInfoUpdateParamsBuilder()
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
        if (isMale) {
            maleSelect.setVisibility(View.VISIBLE);
            femaleSelect.setVisibility(View.GONE);
        } else {
            maleSelect.setVisibility(View.GONE);
            femaleSelect.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mTipsDialogView != null) {
            mTipsDialogView.dismiss(false);
        }
    }
}
