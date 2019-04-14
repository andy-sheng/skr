package com.module.home.updateinfo.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
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

    int before;  // 记录之前的位置

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

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                U.getFragmentUtils().popFragment(EditInfoSignFragment.this);
            }
        });

        mTitlebar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 完成
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
                before = i;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                mSignTextSize.setText("" + length + "/20");
                int selectionEnd = mSignEt.getSelectionEnd();
                if (length > 20) {
                    editable.delete(before, selectionEnd);
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
            U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
            MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                    .setSign(sign)
                    .build(), false, false, new MyUserInfoManager.ServerCallback() {
                @Override
                public void onSucess() {
                    U.getToastUtil().showShort("签名更新成功");
                    U.getFragmentUtils().popFragment(EditInfoSignFragment.this);
                }

                @Override
                public void onFail() {

                }
            });

        }
    }


    @Override
    public boolean useEventBus() {
        return false;
    }
}
