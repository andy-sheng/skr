package com.module.home.updateinfo.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.userinfo.UserInfoManager;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.TipsDialogView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.module.home.updateinfo.EditInfoActivity;
import com.module.home.updateinfo.UploadAccountInfoActivity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.live.proto.Common.ESex;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


public class EditInfoAgeFragment extends BaseFragment {

    boolean isUpload = false; //当前是否是完善个人资料
    String uploadNickname;    //完善资料的昵称
    int uploadSex;            // 未知、非法参数


    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    View mDivider;
    FrameLayout mFrameLayout;

    TimePickerView pvCustomLunar;
    ExTextView mCompleteTv;

    DialogPlus mDialogPlus;

    @Override
    public int initView() {
        return R.layout.edit_info_age_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mDivider = (View) mRootView.findViewById(R.id.divider);
        mFrameLayout = (FrameLayout) mRootView.findViewById(R.id.frame_layout);
        mCompleteTv = (ExTextView) mRootView.findViewById(R.id.complete_tv);

        initTimePicker();

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(EditInfoActivity.TAG, R.raw.normal_back, 500);
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
            mMainActContainer.setBackgroundColor(Color.parseColor("#F9F4F1"));

            mTitlebar.getRightTextView().setText("1/2");
            mTitlebar.getCenterTextView().setText("完善个人信息");
            mTitlebar.setStatusBarColor(Color.parseColor("#F9F4F1"));
            mTitlebar.setTitleBarColor(Color.parseColor("#F9F4F1"));
            mDivider.setBackgroundColor(Color.parseColor("#F9F4F1"));
            mTitlebar.getCenterTextView().setTextColor(Color.parseColor("#0C2275"));
            mTitlebar.getRightTextView().setTextSize(16);
            mTitlebar.getRightTextView().setTextColor(Color.parseColor("#0C2275"));
            mTitlebar.getRightTextView().setClickable(false);

            mCompleteTv.setVisibility(View.VISIBLE);
            isUpload = bundle.getBoolean(UploadAccountInfoActivity.BUNDLE_IS_UPLOAD);
            uploadNickname = bundle.getString(UploadAccountInfoActivity.BUNDLE_UPLOAD_NICKNAME);
            uploadSex = bundle.getInt(UploadAccountInfoActivity.BUNDLE_UPLOAD_SEX);
        }
    }

    private void initTimePicker() {
        String birthday = MyUserInfoManager.getInstance().getMyUserInfo().getBirthday();
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        if (TextUtils.isEmpty(birthday)) {
            selectedDate.set(2000, 11, 12);
        } else {
            String[] strings = birthday.split("-");
            int year = Integer.valueOf(strings[0]);
            int month = Integer.valueOf(strings[1]);
            int date = Integer.valueOf(strings[2]);
            selectedDate.set(year, month - 1, date);
        }
        Calendar startDate = Calendar.getInstance();
        startDate.set(1900, 0, 1);
        Calendar endDate = Calendar.getInstance();

        //时间选择器 ，自定义布局
        pvCustomLunar = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                MyLog.d(TAG, "onTimeSelect" + " date = " + date + " v=" + v);
                if (!checkBirthday(date)) {
                    U.getToastUtil().showShort("当前选择的出生年月无效");
                    return;
                }
                if (isUpload) {
                    // 上传个人信息
                    String bir = U.getDateTimeUtils().formatDateString(date);
                    MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                            .setNickName(uploadNickname).setSex(uploadSex).setBirthday(bir)
                            .build(), true, false, new MyUserInfoManager.ServerCallback() {
                        @Override
                        public void onSucess() {
                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                        }

                        @Override
                        public void onFail() {

                        }
                    });
                } else {
                    // 修改个人信息
                    String bir = U.getDateTimeUtils().formatDateString(date);
                    if (bir.equals(MyUserInfoManager.getInstance().getBirthday())) {
                        // 无任何变化
                        U.getFragmentUtils().popFragment(EditInfoAgeFragment.this);
                    } else {
                        changeAge(bir);
                    }
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
                                U.getFragmentUtils().popFragment(EditInfoAgeFragment.this);
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

    private boolean checkBirthday(Date date) {
        if (date == null) {
            return false;
        }

        Date now = Calendar.getInstance().getTime();
        if (now.getTime() < date.getTime()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
