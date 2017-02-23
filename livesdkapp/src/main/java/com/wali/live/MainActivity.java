package com.wali.live;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.wali.live.base.BaseSdkActivity;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.repository.GiftRepository;
import com.mi.liveassistant.R;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.channel.adapter.ChannelRecyclerAdapter;
import com.wali.live.channel.presenter.ChannelPresenter;
import com.wali.live.channel.presenter.IChannelPresenter;
import com.wali.live.channel.presenter.IChannelView;
import com.wali.live.channel.viewmodel.BaseViewModel;

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
public class MainActivity extends BaseSdkActivity implements IChannelView {
    protected SwipeRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected ChannelRecyclerAdapter mRecyclerAdapter;

    private TextView mTestLiveTv;

    protected IChannelPresenter mPresenter;
    protected long mChannelId = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (!UserAccountManager.getInstance().hasAccount()) {
//            LoginActivity.openActivity(this);
//            finish();
//            return;
//        }

        initViews();
        initPresenters();

        initData();
        getChannelFromServer();
    }

    private void initData() {
        syncGiftList();
    }

    private void syncGiftList() {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                GiftRepository.syncGiftList();
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((RxActivity) this).bindUntilEvent(ActivityEvent.DESTROY))
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
                doRefresh();
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerAdapter = new ChannelRecyclerAdapter(this, mChannelId);
        mRecyclerView.setAdapter(mRecyclerAdapter);

//        mTestLiveTv = $(R.id.live_test_tv);
//        mTestLiveTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!UserAccountManager.getInstance().hasAccount()) {
//                    LoginPresenter loginPresenter = new LoginPresenter(MainActivity.this);
//                    loginPresenter.systemLogin();
//                } else {
//                    LiveSdkActivity.openActivity(MainActivity.this);
//                }
//            }
//        });
    }

    private void initPresenters() {
        mPresenter = new ChannelPresenter(this, this);
        mPresenter.setChannelId(mChannelId);
    }

    private void getChannelFromServer() {
        mPresenter.start();
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
        if (mPresenter != null) {
            mPresenter.stop();
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
}
