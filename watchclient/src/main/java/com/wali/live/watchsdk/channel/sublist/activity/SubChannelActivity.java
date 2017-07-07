package com.wali.live.watchsdk.channel.sublist.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.permission.PermissionUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.adapter.ChannelRecyclerAdapter;
import com.wali.live.watchsdk.channel.sublist.presenter.ISubChannelView;
import com.wali.live.watchsdk.channel.sublist.presenter.SubChannelParam;
import com.wali.live.watchsdk.channel.sublist.presenter.SubChannelPresenter;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by lan on 16/6/30.
 *
 * @module 频道
 * @description 推荐频道的二级页面
 */
public class SubChannelActivity extends BaseSdkActivity implements View.OnClickListener, ISubChannelView {
    public static final String EXTRA_PARAM = "extra_param";
    public static final String EXTRA_SELECT = "extra_select";
    public static final int SHOW_SELECT_TV = 1; // select = 1 时显示选择男女

    BackTitleBar mTitleBar;
    SwipeRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mTvSelect;

    private String[] mGenderArray = GlobalData.app().getResources().getStringArray(R.array.subchannel_select_gender);
    private LinearLayoutManager mLayoutManager;
    private ChannelRecyclerAdapter mRecyclerAdapter;

    private SubChannelPresenter mPresenter;
    private SubChannelParam mParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_sub_list);
        ButterKnife.bind(this);

        initExtraData();

        if (null != mParam) {
            initContentView();
            mPresenter = new SubChannelPresenter(this);
            mPresenter.setParam(mParam);
            mPresenter.start();

            updateSelectText(0);
            mTvSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CharSequence[] items = GlobalData.app().getResources().getStringArray(R.array.subchannel_select_gender);
                    MyAlertDialog.Builder builder = new MyAlertDialog.Builder(SubChannelActivity.this);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                case 1:
                                case 2:
                                    int gender = getGenderBy(which);
                                    mPresenter.setGender(gender);
                                    doRefresh();
                                    updateSelectText(which);
                                case 3:
                                    break;
                            }
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            });
        } else {
            finish();
        }
    }

    private void updateSelectText(int i) {
        if (i >= 0 && i < mGenderArray.length) {
            mTvSelect.setText(mGenderArray[i]);
        }
    }

    /**
     * 获得男女编号 根据选择的item
     *
     * @return
     */
    private int getGenderBy(int i) {
        if (i == 1) {
            return 2;
        } else if (i == 2) {
            return 1;
        } else {
            return 0;
        }

    }

    private void initExtraData() {
        Intent data = getIntent();
        if (data != null) {
            mParam = (SubChannelParam) data.getSerializableExtra(EXTRA_PARAM);
            int select = data.getIntExtra(EXTRA_SELECT, 0);
            if (select == SHOW_SELECT_TV) {
                mTvSelect.setVisibility(View.VISIBLE);
                //代表附近
                PermissionUtils.checkPermissionByType(this, PermissionUtils.PermissionType.ACCESS_FINE_LOCATION, new PermissionUtils.IPermissionCallback() {
                    @Override
                    public void okProcess() {

                    }
                });
            }
        }
    }

    private void initContentView() {
        if (null != mParam) {
            mTitleBar = $(R.id.title_bar);
            mRefreshLayout = $(R.id.swipe_refresh_layout);
            mRecyclerView = $(R.id.recycler_view);
            mTvSelect = $(R.id.tv_select);

            mTitleBar.getBackBtn().setOnClickListener(this);
            mTitleBar.getTitleTv().setText(mParam.getTitle());
            mRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            doRefresh();
                        }
                    });
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setHasFixedSize(true);

            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            mRecyclerAdapter = new ChannelRecyclerAdapter(this, mParam.getChannelId());
            mRecyclerView.setAdapter(mRecyclerAdapter);

            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }
    }

    public void doRefresh() {
        mRefreshLayout.setRefreshing(true);
        mPresenter.start();
    }

    public void destroy() {
        mPresenter.stop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void updateView(List<? extends BaseViewModel> models) {
        mRecyclerAdapter.setData(models);
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void finishRefresh() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (event != null) {
            boolean focus = event.eventType == FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW;
//            mRecyclerAdapter.refreshFocus(event.uuid, focus);
        }
    }

    private static Intent getIntent(Activity activity, SubChannelParam param) {
        Intent intent = new Intent(activity, SubChannelActivity.class);
        intent.putExtra(EXTRA_PARAM, param);
        return intent;
    }

    public static void openActivity(Activity activity, SubChannelParam param) {
        activity.startActivity(getIntent(activity, param));
    }

    public static void openActivity(Activity activity, int id, String title, long channelId, String key, int keyId, int animation, int source, int select) {
        SubChannelParam param = new SubChannelParam(id, title, channelId, key, keyId, animation, source);
        Intent intent = getIntent(activity, param);
        intent.putExtra(EXTRA_SELECT, select);
        activity.startActivity(intent);
    }
}
