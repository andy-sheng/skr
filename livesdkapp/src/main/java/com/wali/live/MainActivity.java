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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.channel.ReleaseChannelUtils;
import com.base.utils.toast.ToastUtils;
import com.base.utils.version.VersionManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.repository.GiftRepository;
import com.mi.liveassistant.R;
import com.mi.milink.sdk.base.debug.TraceLevel;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.livesdk.live.LiveSdkActivity;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.channel.ChannelListSdkActivity;

import activity.ContestPrepareActivity;
import com.wali.live.watchsdk.cta.CTANotifyFragment;
import com.wali.live.watchsdk.login.LoginPresenter;
import com.wali.live.watchsdk.personalcenter.PersonalCenterFragment;
import com.wali.live.watchsdk.watch.VideoDetailSdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.watchsdk.webview.WebViewActivity;

import java.util.ArrayList;
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
public class MainActivity extends BaseSdkActivity {
    protected SwipeRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected TestItemAdapter mTestItemAdapter;

    protected LoginPresenter mLoginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
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
//                ChannelListRequest channelListRequest = new ChannelListRequest(0);
//                doRefresh();
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mTestItemAdapter = new TestItemAdapter();
        mRecyclerView.setAdapter(mTestItemAdapter);

        List<TestItem> dataList = new ArrayList<>();

        dataList.add(new TestItem("登录", new Runnable() {

            @Override
            public void run() {
                if (mLoginPresenter == null) {
                    mLoginPresenter = new LoginPresenter(MainActivity.this);
                }
                mLoginPresenter.miLogin(HostChannelManager.getInstance().getChannelId());
            }
        }));


        dataList.add(new TestItem("渠道版本", new Runnable() {
            @Override
            public void run() {
                String info = String.format("渠道号%s,版本%s"
                        , ReleaseChannelUtils.getReleaseChannel()
                        , VersionManager.getCurrentVersionCode(GlobalData.app()));
                ToastUtils.showToast(info);
            }
        }));

        dataList.add(new TestItem("冲顶大会(废弃功能)", new Runnable() {
            @Override
            public void run() {
                inputDialog("输入id","0", new InputSuccessCallback() {
                    @Override
                    public void inputSuccess(String input) {
                        if (CommonUtils.isNumeric(input)) {
                            ContestPrepareActivity.open(MainActivity.this, Long.parseLong(input));
                        } else {
                            ContestPrepareActivity.open(MainActivity.this, 0);
                        }
                    }
                });
            }
        }));

        dataList.add(new TestItem("跳到直播", new Runnable() {
            @Override
            public void run() {
                inputDialog("输入主播id","29719885", new InputSuccessCallback() {
                    @Override
                    public void inputSuccess(String input) {
                        RoomInfo roomInfo = RoomInfo.Builder.newInstance(Long.parseLong(input), null, null)
                                .setLiveType(0)
                                .setEnableRelationChain(false)
                                .build();
                        WatchSdkActivity.openActivity(MainActivity.this, roomInfo);
                    }
                });
            }
        }));

        dataList.add(new TestItem("跳到回放", new Runnable() {
            @Override
            public void run() {
                RoomInfo roomInfo = RoomInfo.Builder.newInstance(101743, "101743_1471260348",
                        "http://playback.ks.zb.mi.com/record/live/101743_1471260348/hls/101743_1471260348.m3u8?playui=1")
                        .setLiveType(6)
                        .setEnableShare(true)
                        .build();
                VideoDetailSdkActivity.openActivity(MainActivity.this, roomInfo);
            }
        }));

        dataList.add(new TestItem("秀场直播", new Runnable() {
            @Override
            public void run() {
                if (AccountAuthManager.triggerActionNeedAccount(MainActivity.this)) {
                    LiveSdkActivity.openActivity(MainActivity.this, null, false, false);
                }
            }
        }));


        dataList.add(new TestItem("游戏直播", new Runnable() {

            @Override
            public void run() {
                if (AccountAuthManager.triggerActionNeedAccount(MainActivity.this)) {
                    LiveSdkActivity.openActivity(MainActivity.this, null, false, false);
                }
            }
        }));


        dataList.add(new TestItem("跳转频道", new Runnable() {

            @Override
            public void run() {
                inputDialog("输入频道id","20", new InputSuccessCallback() {
                    @Override
                    public void inputSuccess(String input) {
                        int channelId = Integer.parseInt(input);
                        getChannelById(channelId);
                    }
                });
            }
        }));

        dataList.add(new TestItem("神龙宝箱", new Runnable() {

            @Override
            public void run() {
                WebViewActivity.open(MainActivity.this, "https://activity.zb.mi.com/tbox/index.html?actId=2016111101&version=3.0&pos=window&zuid=2199938&lid=2199938_1515139027");
            }
        }));


        dataList.add(new TestItem("频道列表(小米音乐)", new Runnable() {

            @Override
            public void run() {
                HostChannelManager.getInstance().setChannelData(50019, "com.miui.player");
                ChannelListSdkActivity.openActivity(MainActivity.this);
            }
        }));

        dataList.add(new TestItem("个人中心(废弃)", new Runnable() {

            @Override
            public void run() {
                if (!UserAccountManager.getInstance().hasAccount()) {
                    ToastUtils.showToast("请先登录");
                    return;
                }
                PersonalCenterFragment.openFragment(MainActivity.this, R.id.main_act_container);
            }
        }));

        dataList.add(new TestItem("日志全开", new Runnable() {

            @Override
            public void run() {
                if (MyLog.getCurrentLogLevel() == TraceLevel.ALL) {
                    ToastUtils.showToast("已经全开");
                } else {
                    MyLog.setLogcatTraceLevel(TraceLevel.ALL, TraceLevel.ALL, "Lite");
                    MiLinkClientAdapter.getsInstance().setMilinkLogLevel(TraceLevel.ALL);
                }
            }
        }));

        mTestItemAdapter.setDataList(dataList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoginPresenter != null) {
            mLoginPresenter.stop();
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


    void inputDialog(String title,String defaultValue, final InputSuccessCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title);
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(defaultValue);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();

                if (callback != null) {
                    callback.inputSuccess(text);
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

    interface InputSuccessCallback {
        void inputSuccess(String input);
    }

    static class TestItemAdapter extends RecyclerView.Adapter {

        ArrayList<TestItem> list = new ArrayList<TestItem>();

        public void setDataList(List<TestItem> l) {
            list.clear();
            list.addAll(l);
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test_item_layout, parent, false);
            return new TestHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final TestItem testItem = list.get(position);
            if (holder instanceof TestHolder) {
                TestHolder testHolder = (TestHolder) holder;
                testHolder.titleTv.setText(testItem.title);
                testHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        testItem.runnable.run();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    static class TestItem {
        public String title;
        public Runnable runnable;

        public TestItem(String title, Runnable runnable) {
            this.title = title;
            this.runnable = runnable;
        }
    }

    static class TestHolder extends RecyclerView.ViewHolder {

        TextView titleTv;

        public TestHolder(View itemView) {
            super(itemView);

            titleTv = (TextView) itemView.findViewById(R.id.title_tv);
        }
    }
}
