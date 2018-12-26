package com.module.home.updateinfo.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

// 签名编辑
public class EditInfoSignFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    NoLeakEditText mSignEt;
    ExTextView mSignTextSize;

    @Override
    public int initView() {
        return R.layout.edit_info_sign_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);


        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mSignEt = (NoLeakEditText) mRootView.findViewById(R.id.sign_et);
        mSignTextSize = (ExTextView) mRootView.findViewById(R.id.sign_text_size);


        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getContext());
                        U.getFragmentUtils().popFragment(EditInfoSignFragment.this);
                    }
                });

        RxView.clicks(mTitlebar.getRightTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // 完成
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getContext());
                        clickComplete();
                    }
                });


        mSignEt.setText(MyUserInfoManager.getInstance().getSignature());
        if (!TextUtils.isEmpty(MyUserInfoManager.getInstance().getSignature())) {
            mSignTextSize.setText("" + MyUserInfoManager.getInstance().getSignature().length() + "/20");
        } else {
            mSignTextSize.setText("0/20");
        }
        mSignEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                mSignTextSize.setText("" + length + "/20");
                int selectionStart = mSignEt.getSelectionStart();
                int selectionEnd = mSignEt.getSelectionEnd();
                if (length > 20) {
                    editable.delete(selectionStart - 1, selectionEnd);
                    mSignEt.setText(editable.toString());
                    int selection = editable.length();
                    mSignEt.setSelection(selection);
                }
            }
        });
    }


    private void clickComplete() {
        String sign = mSignEt.getText().toString().trim();

        if (TextUtils.isEmpty(sign)) {
            U.getToastUtil().showShort("签名不能为空");
            return;
        }

        if (sign.equals(MyUserInfoManager.getInstance().getSignature())) {
            // 签名一样没改
            U.getFragmentUtils().popFragment(EditInfoSignFragment.this);
        } else {
            MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                    .setSign(sign)
                    .build());
            U.getFragmentUtils().popFragment(EditInfoSignFragment.this);
        }
    }


    @Override
    public boolean useEventBus() {
        return false;
    }
}
