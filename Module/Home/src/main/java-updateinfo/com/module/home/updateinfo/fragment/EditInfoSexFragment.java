package com.module.home.updateinfo.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.TipsDialogView;
import com.module.home.R;
import com.module.home.updateinfo.UploadAccountInfoActivity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.live.proto.Common.ESex;

//编辑性别
public class EditInfoSexFragment extends BaseFragment {

    boolean isUpload = false; //当前是否是完善个人资料
    String uploadNickname;    //完善资料的昵称

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    ExImageView mMale;
    ExImageView mFemale;
    ExTextView mNextTv;

    DialogPlus mDialogPlus;

    int sex = 0;// 未知、非法参数

    @Override
    public int initView() {
        return R.layout.edit_info_sex_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mMale = (ExImageView) mRootView.findViewById(R.id.male);
        mFemale = (ExImageView) mRootView.findViewById(R.id.female);
        mNextTv = (ExTextView) mRootView.findViewById(R.id.next_tv);

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

        mNextTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                clickNext();
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            mMainActContainer.setBackgroundColor(Color.parseColor("#EFEFEF"));

            mTitlebar.getRightTextView().setText("2/3");
            mTitlebar.getCenterTextView().setText("完善个人信息");
            mTitlebar.getRightTextView().setTextSize(16);
            mTitlebar.getRightTextView().setTextColor(getResources().getColor(R.color.white_trans_70));
            mTitlebar.getRightTextView().setClickable(false);

            mNextTv.setVisibility(View.VISIBLE);

            isUpload = bundle.getBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD);
            uploadNickname = bundle.getString(UploadAccountInfoActivity.BUNDLE_UPLOAD_NICKNAME);
        }

        if (MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue()) {
            selectSex(true);
        } else if (MyUserInfoManager.getInstance().getSex() == ESex.SX_FEMALE.getValue()) {
            selectSex(false);
        }
    }

    private void clickNext() {
        if (sex == 0) {
            U.getToastUtil().showShort("请选择性别");
            return;
        }

        if (isUpload) {
            if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getBirthday())) {
                // 无出生年月数据
                Bundle bundle = new Bundle();
                bundle.putBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD, isUpload);
                bundle.putString(UploadAccountInfoActivity.BUNDLE_UPLOAD_NICKNAME, uploadNickname);
                bundle.putInt(UploadAccountInfoActivity.BUNDLE_UPLOAD_SEX, sex);
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newAddParamsBuilder(getActivity(), EditInfoAgeFragment2.class)
                        .setBundle(bundle)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            } else {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
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
            TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                    .setMessageTip("确定修改性别？\n 性别只能修改一次哦～")
                    .setConfirmTip("确认修改")
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener(new AnimateClickListener() {
                        @Override
                        public void click(View view) {
                            if (mDialogPlus != null) {
                                mDialogPlus.dismiss(false);
                            }
                            MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                                    .setSex(sex)
                                    .build(), false, false, new MyUserInfoManager.ServerCallback() {
                                @Override
                                public void onSucess() {
                                    U.getFragmentUtils().popFragment(EditInfoSexFragment.this);
                                }

                                @Override
                                public void onFail() {

                                }
                            });
                        }
                    })
                    .setCancelBtnClickListener(new AnimateClickListener() {
                        @Override
                        public void click(View view) {
                            if (mDialogPlus != null) {
                                mDialogPlus.dismiss();
                            }
                        }
                    })
                    .build();

            mDialogPlus = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(tipsDialogView))
                    .setGravity(Gravity.BOTTOM)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create();
            mDialogPlus.show();

        }
    }


    // 选一个，另一个需要缩放动画
    private void selectSex(boolean isMale) {
        this.sex = isMale ? ESex.SX_MALE.getValue() : ESex.SX_FEMALE.getValue();
        mMale.setBackground(isMale ? getResources().getDrawable(R.drawable.head_man_xuanzhong) : getResources().getDrawable(R.drawable.head_man_weixuanzhong));
        mFemale.setBackground(isMale ? getResources().getDrawable(R.drawable.head_woman_weixuanzhong) : getResources().getDrawable(R.drawable.head_woman_xuanzhong));

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
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(false);
        }
    }
}
