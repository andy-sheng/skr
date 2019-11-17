package com.module.home.updateinfo.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.MyUserInfoServerApi;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.component.toast.CommonToastView;
import com.dialog.view.TipsDialogView;
import com.module.home.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

// 昵称编辑
public class EditInfoNameFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    NoLeakEditText mNicknameEt;

    @Override
    public int initView() {
        return R.layout.edit_info_name_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) getRootView().findViewById(R.id.titlebar);
        mNicknameEt = (NoLeakEditText) getRootView().findViewById(R.id.nickname_et);

        mNicknameEt.setText(MyUserInfoManager.INSTANCE.getNickName());

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
//                U.getSoundUtils().play(EditInfoActivity.TAG, R.raw.normal_back, 500);
                U.getFragmentUtils().popFragment(EditInfoNameFragment.this);
            }
        });

        mTitlebar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 完成
                clickComplete();
            }
        });

        mNicknameEt.addTextChangedListener(new TextWatcher() {
            String preString = "";

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                preString = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = charSequence.toString();
                int length = U.getStringUtils().getStringLength(str);
                if (length > 14) {
                    int selectIndex = preString.length();
                    mNicknameEt.setText(preString);
                    mNicknameEt.setSelection(selectIndex);
                    U.getToastUtil().showShort("昵称不能超过7个汉字或14个英文");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void clickComplete() {
        String nickName = mNicknameEt.getText().toString().trim();
        if (TextUtils.isEmpty(nickName)) {
            U.getToastUtil().showShort("昵称不能为空");
            return;
        }

        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());

        if (nickName.equals(MyUserInfoManager.INSTANCE.getNickName())) {
            // 昵称一样,没改
            U.getFragmentUtils().popFragment(EditInfoNameFragment.this);
        } else {
            MyUserInfoServerApi myUserInfoServerApi = ApiManager.getInstance().createService(MyUserInfoServerApi.class);
            ApiMethods.subscribe(myUserInfoServerApi.checkNickName(nickName), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        boolean isValid = result.getData().getBooleanValue("isValid");
                        String unValidReason = result.getData().getString("unValidReason");
                        if (isValid) {
                            MyUserInfoManager.INSTANCE.updateInfo(MyUserInfoManager.INSTANCE.newMyInfoUpdateParamsBuilder()
                                    .setNickName(nickName)
                                    .build(), false, false, new MyUserInfoManager.ServerCallback() {
                                @Override
                                public void onSucess() {
                                    U.getToastUtil().showShort("昵称更新成功");
                                    U.getFragmentUtils().popFragment(EditInfoNameFragment.this);
                                }

                                @Override
                                public void onFail() {

                                }
                            });
                        } else {
                            U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                                    .setImage(R.drawable.touxiangshezhishibai_icon)
                                    .setText(unValidReason)
                                    .build());
                        }
                    }
                }
            }, this);

        }
    }

    private void showConfirmDialog(String nickName) {
        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append("确认将昵称修改为")
                .append(nickName).setBold().setForegroundColor(Color.parseColor("#0288D0"))
                .append("吗?\n三个月以后才能再次修改喔～")
                .create();
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                .setMessageTip(stringBuilder)
                .setConfirmTip("确认修改")
                .setCancelTip("取消")
                .build();

        DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        if (view instanceof ExTextView) {
                            if (view.getId() == R.id.confirm_tv) {
                                dialog.dismiss();
                                MyUserInfoManager.INSTANCE.updateInfo(MyUserInfoManager.INSTANCE.newMyInfoUpdateParamsBuilder()
                                        .setNickName(nickName)
                                        .build(), false);
                                U.getFragmentUtils().popFragment(EditInfoNameFragment.this);
                            }

                            if (view.getId() == R.id.cancel_tv) {
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
                .create().show();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
    }
}
