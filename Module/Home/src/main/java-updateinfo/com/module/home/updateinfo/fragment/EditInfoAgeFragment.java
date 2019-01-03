package com.module.home.updateinfo.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.module.home.updateinfo.UploadAccountInfoActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


public class EditInfoAgeFragment extends BaseFragment {

    boolean isUpload = false; //当前是否是完善个人资料

    CommonTitleBar mTitlebar;
    FrameLayout mFrameLayout;

    TimePickerView pvCustomLunar;
    ExTextView mCompleteTv;

    @Override
    public int initView() {
        return R.layout.edit_info_age_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {


        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mFrameLayout = (FrameLayout) mRootView.findViewById(R.id.frame_layout);
        mCompleteTv = (ExTextView) mRootView.findViewById(R.id.complete_tv);


        initTimePicker();

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().popFragment(EditInfoAgeFragment.this);
                    }
                });

        RxView.clicks(mTitlebar.getRightTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        pvCustomLunar.returnData();
                    }
                });

        RxView.clicks(mCompleteTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        pvCustomLunar.returnData();
                    }
                });


        Bundle bundle = getArguments();
        if (bundle != null) {
            mTitlebar.getRightTextView().setText("3/3");
            mTitlebar.getCenterTextView().setText("完善个人信息");
            mTitlebar.getRightTextView().setTextSize(16);
            mTitlebar.getRightTextView().setTextColor(getResources().getColor(R.color.white_trans_70));
            mTitlebar.getRightTextView().setClickable(false);

            mCompleteTv.setVisibility(View.VISIBLE);
            isUpload = bundle.getBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD);
        }
    }

    private void initTimePicker() {
        String birthday = MyUserInfoManager.getInstance().getMyUserInfo().getBirthday();
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        if (TextUtils.isEmpty(birthday)) {
            selectedDate.set(1990, 0, 1);
        } else {
            String[] strings = birthday.split("-");
            int year = Integer.valueOf(strings[0]);
            int month = Integer.valueOf(strings[1]);
            int date = Integer.valueOf(strings[2]);
            selectedDate.set(year, month - 1, date);
        }
        Calendar startDate = Calendar.getInstance();
        startDate.set(1900, 1, 1);
        Calendar endDate = Calendar.getInstance();

        //时间选择器 ，自定义布局
        pvCustomLunar = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                if (!isUpload) {
                    // 修改个人信息
                    String bir = U.getDateTimeUtils().formatDateString(date);
                    MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                            .setBirthday(bir)
                            .build());
                    U.getFragmentUtils().popFragment(EditInfoAgeFragment.this);
                } else {
                    // 完善个人信息
                    String bir = U.getDateTimeUtils().formatDateString(date);
                    MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                            .setBirthday(bir)
                            .build());
                    getActivity().finish();
                }

            }
        })
                .setDate(selectedDate)
                .setRangDate(startDate, endDate)
                .setLayoutRes(R.layout.pickerview_custom_lunar, new CustomListener() {

                    @Override
                    public void customLayout(final View v) {
//                        final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
//
//                        tvSubmit.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                pvCustomLunar.returnData();
//                                pvCustomLunar.dismiss();
//                            }
//                        });
//
//                        //公农历切换
//                        CheckBox cb_lunar = (CheckBox) v.findViewById(R.id.cb_lunar);
//                        cb_lunar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                            @Override
//                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                                pvCustomLunar.setLunarCalendar(!pvCustomLunar.isLunarCalendar());
//                                //自适应宽
//                                setTimePickerChildWeight(v, isChecked ? 0.8f : 1f, isChecked ? 1f : 1.1f);
//                            }
//                        });

                    }

                    /**
                     * 公农历切换后调整宽
                     * @param v
                     * @param yearWeight
                     * @param weight
                     */
                    private void setTimePickerChildWeight(View v, float yearWeight, float weight) {
                        ViewGroup timePicker = (ViewGroup) v.findViewById(R.id.timepicker);
                        View year = timePicker.getChildAt(0);
                        LinearLayout.LayoutParams lp = ((LinearLayout.LayoutParams) year.getLayoutParams());
                        lp.weight = yearWeight;
                        year.setLayoutParams(lp);
                        for (int i = 1; i < timePicker.getChildCount(); i++) {
                            View childAt = timePicker.getChildAt(i);
                            LinearLayout.LayoutParams childLp = ((LinearLayout.LayoutParams) childAt.getLayoutParams());
                            childLp.weight = weight;
                            childAt.setLayoutParams(childLp);
                        }
                    }
                })
                .setType(new boolean[]{true, true, true, false, false, false})
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setDividerColor(Color.GRAY)
                .setDecorView(mFrameLayout) //非dialog模式下,设置ViewGroup, pickerView将会添加到这个ViewGroup中
                .setOutSideCancelable(false)
                .build();

        pvCustomLunar.setKeyBackCancelable(false); //系统返回键监听屏蔽掉
        pvCustomLunar.show();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
