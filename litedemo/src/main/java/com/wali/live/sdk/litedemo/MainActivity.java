package com.wali.live.sdk.litedemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mi.liveassistant.login.LoginManager;
import com.mi.liveassistant.michannel.BaseViewModel;
import com.mi.liveassistant.michannel.ChannelLiveViewModel;
import com.mi.liveassistant.michannel.ChannelPresenter;
import com.mi.liveassistant.michannel.IChannelView;
import com.wali.live.sdk.litedemo.account.AccountManager;
import com.wali.live.sdk.litedemo.activity.GameLiveActivity;
import com.wali.live.sdk.litedemo.activity.NormalLiveActivity;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends RxActivity implements View.OnClickListener, IChannelView {
    public static final int REQUEST_MEDIA_PROJECTION = 2000;

    private Button mLoginBtn;
    private Button mGameLiveBtn;
    private Button mNormalLiveBtn;
    private Button mWatchBtn;

    // 拉个频道，防止每次更换地址
    private ChannelPresenter mChannelPresenter;
    private ChannelLiveViewModel.BaseLiveItem mLiveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initPresenter();
    }

    private void initView() {
        mLoginBtn = $(R.id.login_btn);
        mLoginBtn.setOnClickListener(this);

        mGameLiveBtn = $(R.id.game_live_btn);
        mGameLiveBtn.setOnClickListener(this);

        mNormalLiveBtn = $(R.id.normal_live_btn);
        mNormalLiveBtn.setOnClickListener(this);

        mWatchBtn = $(R.id.watch_btn);
        mWatchBtn.setOnClickListener(this);
    }

    private void initPresenter() {
        mChannelPresenter = new ChannelPresenter(this);
        mChannelPresenter.setChannelId(201);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                clickLoginBtn();
                break;
            case R.id.normal_live_btn:
                clickNormalBtn();
                break;
            case R.id.game_live_btn:
                clickGameBtn();
                break;
            case R.id.watch_btn:
                clickWatchBtn();
                break;
        }
    }

    private void clickLoginBtn() {
        Observable.just(0)
                .map(new Func1<Integer, String>() {
                    @Override
                    public String call(Integer integer) {
                        return AccountManager.getOAuthCode(MainActivity.this);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String code) {
                        LoginManager.loginByMiAccountOAuth(50001, code);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
    }


    private void clickNormalBtn() {
        NormalLiveActivity.openActivity(this);
    }

    private void clickGameBtn() {
        GameLiveActivity.openActivity(this);
    }

    private void clickWatchBtn() {
        if (mLiveItem == null) {
            mChannelPresenter.start();
            return;
        }
        ToastUtils.showToast(mLiveItem.getUser().getUid() + ":" + mLiveItem.getId());
    }

    @Override
    public void updateView(List<? extends BaseViewModel> list) {
        ChannelLiveViewModel model = (ChannelLiveViewModel) list.get(0);
        mLiveItem = (ChannelLiveViewModel.BaseLiveItem) model.getFirstItem();

        if (mLiveItem == null) {
            ToastUtils.showToast("live item is null");
            return;
        }
        ToastUtils.showToast(mLiveItem.getUser().getUid() + ":" + mLiveItem.getId());
    }

    @Override
    public void finishRefresh() {
    }

    @Override
    public void doRefresh() {
    }
}
