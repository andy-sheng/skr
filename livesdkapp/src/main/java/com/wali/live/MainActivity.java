package com.wali.live;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.repository.GiftRepository;
import com.mi.liveassistant.R;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.livesdk.live.LiveSdkActivity;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.channel.adapter.ChannelRecyclerAdapter;
import com.wali.live.watchsdk.channel.list.model.ChannelShow;
import com.wali.live.watchsdk.channel.list.presenter.ChannelListPresenter;
import com.wali.live.watchsdk.channel.list.presenter.IChannelListView;
import com.wali.live.watchsdk.channel.presenter.ChannelPresenter;
import com.wali.live.watchsdk.channel.presenter.IChannelPresenter;
import com.wali.live.watchsdk.channel.presenter.IChannelView;
import com.wali.live.watchsdk.channel.list.request.ChannelListRequest;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;
import com.wali.live.watchsdk.contest.ContestPrepareActivity;
import com.wali.live.watchsdk.cta.CTANotifyFragment;
import com.wali.live.watchsdk.login.LoginPresenter;
import com.wali.live.watchsdk.watch.VideoDetailSdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.watchsdk.webview.WebViewActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 16/11/25.
 */
@Deprecated
public class MainActivity extends BaseSdkActivity implements IChannelView, IChannelListView {
    protected SwipeRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected ChannelRecyclerAdapter mRecyclerAdapter;
    protected EditText mInputEditText;

    protected ChannelListPresenter mChannelListPresenter;
    protected IChannelPresenter mChannelPresenter;
    //    protected long mChannelId = 20;
    protected LoginPresenter mLoginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initPresenters();
        if (CommonUtils.isNeedShowCtaDialog()) {
            CTANotifyFragment.openFragment(this, android.R.id.content, new CTANotifyFragment.CTANotifyButtonClickListener() {
                @Override
                public void onClickCancelButton() {
                    finish();
                }

                @Override
                public void onClickConfirmButton() {
                    popFragment();
                    syncDataFromServer();
                }
            });
        } else {
            syncDataFromServer();
        }
    }

    private void popFragment() {
        FragmentNaviUtils.popFragment(this);
    }

    private void syncDataFromServer() {
        syncGiftList();
        getChannelFromServer();
    }

    private void syncGiftList() {
        Observable
                .create(new Observable.OnSubscribe<Object>() {
                    @Override
                    public void call(Subscriber<? super Object> subscriber) {
                        GiftRepository.syncGiftList();
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Object o) {
                    }
                });
    }

    private void initViews() {
        mRefreshLayout = $(R.id.swipe_refresh_layout);
        mRecyclerView = $(R.id.recycler_view);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ChannelListRequest channelListRequest = new ChannelListRequest(0);
                doRefresh();
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mInputEditText = $(R.id.live_input_tv);

        $(R.id.contest_prepare_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mInputEditText.getText().toString();
                if (CommonUtils.isNumeric(input)) {
                    ContestPrepareActivity.open(MainActivity.this, Long.parseLong(input));
                } else {
                    ContestPrepareActivity.open(MainActivity.this, 0);
                }

            }
        });
        $(R.id.watch_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mInputEditText.getText().toString();
                if (TextUtils.isEmpty(input)) {
                    ToastUtils.showToast("主播id不能为空");
                    return;
                }
                if (CommonUtils.isNumeric(input)) {
                    RoomInfo roomInfo = RoomInfo.Builder.newInstance(Long.parseLong(input), null, null)
                            .setLiveType(0)
                            .setEnableRelationChain(false)
                            .build();
                    WatchSdkActivity.openActivity(MainActivity.this, roomInfo);
                } else {
                    ToastUtils.showToast("主播id不是数字");
                }
            }
        });

        $(R.id.show_live_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AccountAuthManager.triggerActionNeedAccount(MainActivity.this)) {
                    LiveSdkActivity.openActivity(MainActivity.this, null, false, false);
                }
            }
        });

        $(R.id.game_live_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AccountAuthManager.triggerActionNeedAccount(MainActivity.this)) {
                    LiveSdkActivity.openActivity(MainActivity.this, null, false, true);
                }
            }
        });

        ($(R.id.login_tv)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoginPresenter == null) {
                    mLoginPresenter = new LoginPresenter(MainActivity.this);
                }
                mLoginPresenter.miLogin(HostChannelManager.getInstance().getChannelId());
            }
        });
        ($(R.id.replay_tv)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomInfo roomInfo = RoomInfo.Builder.newInstance(101743, "101743_1471260348",
                        "http://playback.ks.zb.mi.com/record/live/101743_1471260348/hls/101743_1471260348.m3u8?playui=1")
                        .setLiveType(6)
                        .setEnableShare(true)
                        .build();
                VideoDetailSdkActivity.openActivity(MainActivity.this, roomInfo);
            }
        });

        ($(R.id.channel_tv)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncDataFromServer();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("input channelId");
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = input.getText().toString();
                        try {
                            int channelId = Integer.parseInt(text);
                            getChannelById(channelId);
                        } catch (Exception e) {

                        }

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });

        ($(R.id.box_tv)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewActivity.open(MainActivity.this, "https://activity.zb.mi.com/tbox/index.html?actId=2016111101&version=3.0&pos=window&zuid=2199938&lid=2199938_1515139027");
            }
        });
    }

    private void initPresenters() {
        mChannelListPresenter = new ChannelListPresenter(this, this);
        mChannelListPresenter.setFcId(0);
        mChannelPresenter = new ChannelPresenter(this, this);
//        mChannelPresenter.setChannelId(mChannelId);
    }

    private void getChannelFromServer() {
        mChannelListPresenter.start();
    }

    @Override
    public void listUpdateView(List<? extends ChannelShow> models) {
        for (ChannelShow show : models) {
            if (show.getChannelName().equals("推荐")) {
                long channelId = show.getChannelId();
                mChannelPresenter.setChannelId(channelId);
                mRecyclerAdapter = new ChannelRecyclerAdapter(this, channelId);
                mRecyclerView.setAdapter(mRecyclerAdapter);
                mChannelPresenter.start();
            }
        }
    }

    @Override
    public void updateView(List<? extends BaseViewModel> models) {
        mRecyclerAdapter.setData(models);
    }


    @Override
    public void finishRefresh() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void doRefresh() {
        getChannelFromServer();
        mRefreshLayout.setRefreshing(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChannelPresenter != null) {
            mChannelPresenter.stop();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MiLinkEvent.StatusConnected event) {
        MyLog.d(TAG, "milink is connected");
        if (event != null) {
            getChannelFromServer();
        }
    }

    public static void openActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    public void getChannelById(int channelId) {
        String uri = "livesdk://channel?channel_id=" + channelId;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.putExtra("extra_channel_id", 50001);
        intent.putExtra("extra_package_name", "com.wali.live.sdk.manager.demo");
        startActivity(intent);
    }
}
