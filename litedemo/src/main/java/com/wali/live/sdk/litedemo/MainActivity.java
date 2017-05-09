package com.wali.live.sdk.litedemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.thread.ThreadPool;
import com.mi.liveassistant.login.ILoginCallback;
import com.mi.liveassistant.login.LoginManager;
import com.mi.liveassistant.michannel.presenter.ChannelPresenter;
import com.mi.liveassistant.michannel.presenter.IChannelView;
import com.mi.liveassistant.michannel.viewmodel.BaseViewModel;
import com.mi.liveassistant.michannel.viewmodel.ChannelLiveViewModel;
import com.mi.liveassistant.milink.MiLinkClientAdapter;
import com.mi.liveassistant.utils.RSASignature;
import com.wali.live.sdk.litedemo.account.AccountManager;
import com.wali.live.sdk.litedemo.activity.GameLiveActivity;
import com.wali.live.sdk.litedemo.activity.NormalLiveActivity;
import com.wali.live.sdk.litedemo.activity.WatchActivity;
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
    private Button mThirdPartyLoginBtn;
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

        mThirdPartyLoginBtn = $(R.id.third_party_login_btn);
        mThirdPartyLoginBtn.setOnClickListener(this);

        mGameLiveBtn = $(R.id.game_live_btn);
        mGameLiveBtn.setOnClickListener(this);

        mNormalLiveBtn = $(R.id.normal_live_btn);
        mNormalLiveBtn.setOnClickListener(this);

        mWatchBtn = $(R.id.watch_btn);
        mWatchBtn.setOnClickListener(this);

        ThreadPool.runOnWorker(new Runnable() {
            @Override
            public void run() {
                if (!LoginManager.checkAccount()) {
                    mLoginBtn.setOnClickListener(MainActivity.this);
                } else {
                    mLoginBtn.post(new Runnable() {
                        @Override
                        public void run() {
                            mLoginBtn.setText("已登录");
                            mLoginBtn.setEnabled(false);
                        }
                    });
                }
            }
        });
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
            case R.id.third_party_login_btn:
                clickThirdPartyLoginBtn();
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
                        LoginManager.loginByMiAccountOAuth(50001, code, new ILoginCallback() {
                            @Override
                            public void notifyFail(int i) {
                                mLoginBtn.setText("登录失败");
                            }

                            @Override
                            public void notifySuccess() {
                                mLoginBtn.setText("已登录");
                                mLoginBtn.setEnabled(false);
                            }
                        });
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                });
    }

    private static final String RSA_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMC8ISWECSak6Z1X" +
            "tgTy9jrq85dZ7Z95CndJ6Sz0ty5fiVqiJ4WrRf7d+78hlEOvlE0fwLQraHZ28gkD" +
            "kdNX1ycFDV+SBDTn+rFnRJQZjA8t3cQGiJmpyFIpaSzpz9PMTScxDmmxygUzsTXe" +
            "sCcFV8p9thCyJj5kGsUFxzkfwR7dAgMBAAECgYAqUCMmzVoE9eej94GqjHyqarKX" +
            "49JbVIOLtNpQWFlvAOJy12691eBEGBAQ4hpe0clJNVNlOrJwb6SrffEh6QL+2Aht" +
            "oocO7ST4kGpYTk53ofkK9AOwdZkhhzn226qRlDFN+OyAedLsv5sZ3166KTfxaCkO" +
            "5/KeXuD9BucT4eHTMQJBAPUGugXFJBVUZimsqwi5PKBGtmqQEJAi5M0vZvGz4vtq" +
            "H8pXQVYHOwAQA2Kmx7LSWqUa5EZCfKQIHE88dhmcru8CQQDJXd825pM0FW6ENr/9" +
            "IZLZBMgOlFG06WkVa442trbViGP0TPJMeEzBHoCDtlxDUxKcbFworXvVk+f8SYUo" +
            "6g7zAkAJSIb1vwFd+YOhYpRcUUBVxjgVE349J8VJbNlWoP0hj2TC8slb7Aw1NWYb" +
            "b7wzLzsV9E3fx5cXU+NWsTC8Sa5rAkEAw8DL4/UWmQVUoJcQ4KUoumwZh4LMQ1C8" +
            "5SPf5nSNHNwwPygmTAyOoRZj3KcE3jX9267DkI/F2ISmeu2F05Zl3QJAX8qggola" +
            "wpkdbvZn81X80lFuye6b0KjSWqlrrQLtjSR9/ov/avbuEDI+Ni4rDZn5a0rkGuaN" +
            "DzBZBemtWvPkjg==";

    private void clickThirdPartyLoginBtn() {
        MyLog.w(TAG, "checkLogin isTouristMode=" + MiLinkClientAdapter.getsInstance().isTouristMode());
        if (!MiLinkClientAdapter.getsInstance().isTouristMode()) {
            return;
        }
        String uid = "100067";
        String name = "游不动的鱼";
        String headUrl = "";
        int sex = 1;
        int channelId = 50001;
        String signStr = "channelId=" + channelId + "&headUrl=" + headUrl + "&nickname=" + name + "&sex=" + sex + "&xuid=" + uid;
        String sign = RSASignature.sign(signStr, RSA_PRIVATE_KEY, "UTF-8");

        LoginManager.thirdPartLogin(channelId, uid, name, headUrl, sex, sign, new ILoginCallback() {
            @Override
            public void notifyFail(int errCode) {
                MyLog.d(TAG, "notifyFail");
                mThirdPartyLoginBtn.setText("登录失败");
            }

            @Override
            public void notifySuccess() {
                MyLog.d(TAG, "notifySuccess");
                mThirdPartyLoginBtn.setText("已登录");
                mThirdPartyLoginBtn.setEnabled(false);
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
        WatchActivity.openActivity(this, mLiveItem.getUser().getUid(), mLiveItem.getId());
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
        WatchActivity.openActivity(this, mLiveItem.getUser().getUid(), mLiveItem.getId());
    }

    @Override
    public void finishRefresh() {
    }

    @Override
    public void doRefresh() {
    }
}
