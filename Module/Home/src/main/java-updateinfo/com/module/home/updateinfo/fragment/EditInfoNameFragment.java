package com.module.home.updateinfo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.TipsDialogView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

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
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mNicknameEt = (NoLeakEditText) mRootView.findViewById(R.id.nickname_et);

        mNicknameEt.setText(MyUserInfoManager.getInstance().getNickName());

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().popFragment(EditInfoNameFragment.this);
                    }
                });

        RxView.clicks(mTitlebar.getRightTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // 完成
                        clickComplete();
                    }
                });

        mNicknameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = U.getStringUtils().getStringLength(editable.toString());
                int selectionStart = mNicknameEt.getSelectionStart();
                int selectionEnd = mNicknameEt.getSelectionEnd();
                if (length > 14) {
                    editable.delete(selectionStart - 1, selectionEnd);
                    mNicknameEt.setText(editable.toString());
                    int selection = editable.length();
                    mNicknameEt.setSelection(selection);
                    U.getToastUtil().showShort("昵称不能超过7哥汉字或14个英文");
                }
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

        if (nickName.equals(MyUserInfoManager.getInstance().getNickName())) {
            // 昵称一样,没改
            U.getFragmentUtils().popFragment(EditInfoNameFragment.this);
        } else {
            TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                    .setMessageTip(String.format(getString(R.string.edit_nickname_confim_tips), nickName))
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
                                    MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
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
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
