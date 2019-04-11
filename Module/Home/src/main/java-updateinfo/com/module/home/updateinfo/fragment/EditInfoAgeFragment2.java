package com.module.home.updateinfo.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.core.login.view.SeparatedEditText;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.TipsDialogView;
import com.module.home.R;
import com.module.home.updateinfo.UploadAccountInfoActivity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.Calendar;
import java.util.Date;

public class EditInfoAgeFragment2 extends BaseFragment {

    boolean mIsUpload = false; //当前是否是完善个人资料
    String mUploadNickname;    //完善资料的昵称
    int mUploadSex;            // 未知、非法参数

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RelativeLayout mBirthdayArea;
    SeparatedEditText mYear;
    SeparatedEditText mMonth;
    SeparatedEditText mDay;
    ExTextView mAgeTv;
    View mDivider;
    ExTextView mConTv;
    ExImageView mCompleteIv;

    DialogPlus mDialogPlus;

    String mDayDown = "";
    String mDayUp = "";
    String mMothDown = "";
    String mMothUp = "";

    String mYearDate;   //年份
    String mMonthDate;  //月份
    String mDayDate;    //日

    @Override
    public int initView() {
        return R.layout.edit_info_age_fragment2;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mBirthdayArea = (RelativeLayout) mRootView.findViewById(R.id.birthday_area);
        mYear = (SeparatedEditText) mRootView.findViewById(R.id.year);
        mMonth = (SeparatedEditText) mRootView.findViewById(R.id.month);
        mDay = (SeparatedEditText) mRootView.findViewById(R.id.day);
        mAgeTv = (ExTextView) mRootView.findViewById(R.id.age_tv);
        mDivider = (View) mRootView.findViewById(R.id.divider);
        mConTv = (ExTextView) mRootView.findViewById(R.id.con_tv);
        mCompleteIv = (ExImageView) mRootView.findViewById(R.id.complete_iv);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(EditInfoAgeFragment2.this);
            }
        });

        mTitlebar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // TODO: 2019/4/11 修改年龄完成
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                // 修改个人信息
                String bir = mYearDate + "-" + mMonthDate + "-" + mDayDate;
                if (bir.equals(MyUserInfoManager.getInstance().getBirthday())) {
                    // 无任何变化
                    U.getFragmentUtils().popFragment(EditInfoAgeFragment2.this);
                } else {
                    changeAge(bir);
                }
            }
        });

        mCompleteIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // TODO: 2019/4/11 完成完善过程
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                String bir = mYearDate + "-" + mMonthDate + "-" + mDayDate;
                MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                        .setNickName(mUploadNickname).setSex(mUploadSex).setBirthday(bir)
                        .build(), true, true, new MyUserInfoManager.ServerCallback() {
                    @Override
                    public void onSucess() {
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                        StatisticsAdapter.recordCountEvent("signup", "success", null);
                    }

                    @Override
                    public void onFail() {

                    }
                });
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            mMainActContainer.setBackgroundColor(Color.parseColor("#7187FF"));

            mTitlebar.getRightTextView().setText("2/2");
            mTitlebar.getCenterTextView().setText("完善个人信息");
            mTitlebar.setStatusBarColor(Color.parseColor("#7187FF"));
            mTitlebar.setTitleBarColor(Color.parseColor("#7187FF"));
            mTitlebar.setBottomLineColor(Color.parseColor("#7187FF"));
            mTitlebar.getRightTextView().setTextSize(16);
            mTitlebar.getRightTextView().setClickable(false);

            mCompleteIv.setVisibility(View.VISIBLE);
            mIsUpload = bundle.getBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD);
            mUploadNickname = bundle.getString(UploadAccountInfoActivity.BUNDLE_UPLOAD_NICKNAME);
            mUploadSex = bundle.getInt(UploadAccountInfoActivity.BUNDLE_UPLOAD_SEX);

            mDivider.setBackgroundColor(Color.WHITE);
            mAgeTv.setTextColor(Color.WHITE);
            mConTv.setTextColor(Color.WHITE);
        }

        mYear.setTextChangedListener(new SeparatedEditText.TextChangedListener() {
            @Override
            public void textChanged(CharSequence changeText) {

            }

            @Override
            public void textCompleted(CharSequence text) {
                mMonth.requestFocus();
                // 展示年龄
                mYearDate = mYear.getText().toString();
                int year = Calendar.getInstance().get(Calendar.YEAR) - Integer.valueOf(mYearDate);
                if (year >= 0) {
                    mAgeTv.setText(year + "岁");
                }
            }
        });


        mMonth.setTextChangedListener(new SeparatedEditText.TextChangedListener() {
            @Override
            public void textChanged(CharSequence changeText) {

            }

            @Override
            public void textCompleted(CharSequence text) {
                mDay.requestFocus();
            }
        });

        mMonth.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mMothDown = mMonth.getText().toString();
                }

                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_UP) {
                    mMothUp = mMonth.getText().toString();

                    if (TextUtils.isEmpty(mMothDown) && TextUtils.isEmpty(mMothUp)) {
                        Editable year = mYear.getText();
                        if (!TextUtils.isEmpty(year)) {
                            int length = year.length();
                            mYear.getText().delete(length - 1, length);
                            mYear.setSelection(length - 1);
                        }
                        mYear.requestFocus();
                    }
                }
                return false;
            }
        });

        mDay.setTextChangedListener(new SeparatedEditText.TextChangedListener() {
            @Override
            public void textChanged(CharSequence changeText) {

            }

            @Override
            public void textCompleted(CharSequence text) {
                // 展示星座
                mMonthDate = mMonth.getText().toString();
                mDayDate = mDay.getText().toString();
                String constellation = U.getDateTimeUtils().getConstellation(Integer.valueOf(mMonthDate), Integer.valueOf(mDayDate));
                if (!TextUtils.isEmpty(constellation)) {
                    mConTv.setText(constellation);
                }
            }
        });

        mDay.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mDayDown = mDay.getText().toString();
                }

                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_UP) {
                    mDayUp = mDay.getText().toString();

                    if (TextUtils.isEmpty(mDayDown) && TextUtils.isEmpty(mDayUp)) {
                        Editable month = mMonth.getText();
                        if (!TextUtils.isEmpty(month)) {
                            int length = month.length();
                            mMonth.getText().delete(length - 1, length);
                            mMonth.setSelection(length - 1);
                        }
                        mMonth.requestFocus();
                    }
                }
                return false;
            }
        });

        if (!TextUtils.isEmpty(MyUserInfoManager.getInstance().getBirthday())) {
            String[] array = MyUserInfoManager.getInstance().getBirthday().split("-");

            if (array.length >= 1) {
                mYear.setText(array[0]);
            }
            if (array.length >= 2) {
                mMonth.setText(array[1]);
            }
            if (array.length >= 3) {
                mDay.setText(array[2]);
            }

            mAgeTv.setText(MyUserInfoManager.getInstance().getAge() + "岁");
            mConTv.setText(MyUserInfoManager.getInstance().getConstellation());
            if (U.getKeyBoardUtils().isSoftKeyboardShowing(getActivity())) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
            }
        } else {
            mYear.requestFocus();
            U.getKeyBoardUtils().showSoftInputKeyBoard(getActivity());
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }


    private void changeAge(String bir) {
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                .setMessageTip("年龄只能修改一次哦～\n确认修改吗？")
                .setConfirmTip("确认修改")
                .setCancelTip("取消")
                .build();

        mDialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(com.component.busilib.R.color.transparent)
                .setOverlayBackgroundResource(com.component.busilib.R.color.black_trans_80)
                .setExpanded(false)
                .setOnClickListener(new com.orhanobut.dialogplus.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        if (view instanceof ExTextView) {
                            if (view.getId() == com.component.busilib.R.id.confirm_tv) {
                                dialog.dismiss();
                                MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                                        .setBirthday(bir)
                                        .build(), false);
                                U.getFragmentUtils().popFragment(EditInfoAgeFragment2.this);
                            }

                            if (view.getId() == com.component.busilib.R.id.cancel_tv) {
                                dialog.dismiss();
                            }
                        }
                    }
                })
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(@NonNull DialogPlus dialog) {

                    }
                })
                .create();
        mDialogPlus.show();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (U.getKeyBoardUtils().isSoftKeyboardShowing(getActivity())) {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
        }

        if (mDialogPlus != null) {
            mDialogPlus.dismiss(false);
        }
    }
}
