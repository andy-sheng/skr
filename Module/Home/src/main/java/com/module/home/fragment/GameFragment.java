package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

public class GameFragment extends BaseFragment {

    CardView mCardPk;

    CardView mCardHappy;

    ExTextView mTestBtn;

    @Override
    public int initView() {
        return R.layout.game_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mCardPk = (CardView)mRootView.findViewById(R.id.card_pk);
        mCardHappy = (CardView)mRootView.findViewById(R.id.card_happy);
        mTestBtn = (ExTextView)mRootView.findViewById(R.id.test_btn);

        mTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(U.getCommonUtils().isFastDoubleClick()){
                    return;
                }
                ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKINGMODE).greenChannel().navigation();
            }
        });

        RxView.clicks(mCardPk)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKINGMODE).greenChannel().navigation();
                    }
                });

        RxView.clicks(mCardHappy)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getToastUtil().showShort("娱乐pk");
                    }
                });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
