package com.wali.live.sdk.litedemo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mi.liveassistant.account.AccountManager;
import com.mi.liveassistant.account.callback.IAccountCallback;
import com.mi.liveassistant.account.callback.IAccountListener;
import com.mi.liveassistant.michannel.presenter.ChannelPresenter;
import com.mi.liveassistant.michannel.presenter.IChannelView;
import com.mi.liveassistant.michannel.viewmodel.BaseViewModel;
import com.mi.liveassistant.michannel.viewmodel.ChannelLiveViewModel;
import com.mi.liveassistant.utils.RSASignature;
import com.wali.live.sdk.litedemo.activity.GameLiveActivity;
import com.wali.live.sdk.litedemo.activity.NormalLiveActivity;
import com.wali.live.sdk.litedemo.activity.WatchActivity;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

import java.util.List;

public class MainActivity extends RxActivity implements View.OnClickListener, IChannelView {
    public static final int REQUEST_MEDIA_PROJECTION = 2000;

    public static final String RSA_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMC8ISWECSak6Z1X" +
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

    private Button mLoginBtn;
    private Button mGameLiveBtn;
    private Button mNormalLiveBtn;
    private Button mWatchBtn;

    // 拉个频道，防止每次更换地址
    private ChannelPresenter mChannelPresenter;
    private ChannelLiveViewModel.BaseLiveItem mLiveItem;

    private boolean mHasAccount;
    private long mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initPresenter();
        initManager();
    }

    private void initView() {
        mLoginBtn = $(R.id.login_btn);
        mLoginBtn.setOnClickListener(MainActivity.this);

        mGameLiveBtn = $(R.id.game_live_btn);
        mGameLiveBtn.setOnClickListener(this);

        mNormalLiveBtn = $(R.id.normal_live_btn);
        mNormalLiveBtn.setOnClickListener(this);

        mWatchBtn = $(R.id.watch_btn);
        mWatchBtn.setOnClickListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mUserId = AccountManager.getInstance().getAccount();
                mHasAccount = mUserId != 0;
                if (mHasAccount) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLoginBtn.setText("注销登录 " + mUserId);
                        }
                    });
                }
            }
        }).start();
    }

    private void initPresenter() {
        mChannelPresenter = new ChannelPresenter(this);
        mChannelPresenter.setChannelId(201);
    }

    private void initManager() {
        AccountManager.getInstance().setAccountListener(new IAccountListener() {
            @Override
            public void forbidAccount() {
                ToastUtils.showToast("forbidAccount");
                mHasAccount = false;
                mLoginBtn.setText("请先登录");
            }

            @Override
            public void logoffAccount() {
                ToastUtils.showToast("logoffAccount");
                mHasAccount = false;
                mLoginBtn.setText("请先登录");
            }

            @Override
            public void kickAccount() {
                ToastUtils.showToast("kickAccount");
                mHasAccount = false;
                mLoginBtn.setText("请先登录");
            }
        });
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
        if (mHasAccount) {
            AccountManager.getInstance().logoff();
            mHasAccount = false;
            mLoginBtn.setText("请先登录");
        } else {
            thirdPartyLogin();
        }
    }

    private void thirdPartyLogin() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入第三方用户id");
        final EditText et = new EditText(this);
        builder.setView(et);
        et.setText("100067");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String uid = et.getText().toString();
                if (TextUtils.isEmpty(uid)) {
                    ToastUtils.showToast("用户id不能为空");
                    return;
                }

                String name = "游不动的鱼";
                String headUrl = "";
                int sex = 1;
                int channelId = 50001;
                String signStr = "channelId=" + channelId + "&headUrl=" + headUrl + "&nickname=" + name + "&sex=" + sex + "&xuid=" + uid;
                String sign = RSASignature.sign(signStr, RSA_PRIVATE_KEY, "UTF-8");

                AccountManager.getInstance().thirdPartLogin(channelId, uid, name, headUrl, sex, sign, new IAccountCallback() {
                    @Override
                    public void notifyFail(int errCode) {
                        Log.d(TAG, "notifyFail");
                        mHasAccount = false;
                        mLoginBtn.setText("登录失败，重新登录");
                    }

                    @Override
                    public void notifySuccess(long uid) {
                        Log.d(TAG, "notifySuccess");
                        mHasAccount = true;
                        mUserId = uid;
                        mLoginBtn.setText("注销登录 " + mUserId);
                    }
                });
            }
        });
        builder.show();
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
