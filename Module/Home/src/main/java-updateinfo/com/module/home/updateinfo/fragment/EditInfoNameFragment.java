package com.module.home.updateinfo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;

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
        mTitlebar = (CommonTitleBar)mRootView.findViewById(R.id.titlebar);
        mNicknameEt = (NoLeakEditText)mRootView.findViewById(R.id.nickname_et);

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
        
        
    }

    private void clickComplete() {
        String nickName = mNicknameEt.getText().toString().trim();
        // TODO: 2018/12/24 判断并做后续请求 
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
