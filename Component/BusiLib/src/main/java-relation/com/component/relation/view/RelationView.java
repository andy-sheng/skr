package com.component.relation.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.event.RemarkChangeEvent;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.notification.event.FollowNotifyEvent;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.dialog.view.TipsDialogView;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.RouterConstants;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.component.relation.adapter.RelationAdapter;
import com.component.relation.callback.FansEmptyCallback;
import com.component.relation.callback.FollowEmptyCallback;
import com.component.relation.callback.FriendsEmptyCallback;
import com.component.relation.fragment.SearchFriendFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class RelationView extends RelativeLayout {

    public final String TAG = "RelationView";

    private int mMode = UserInfoManager.RELATION.FRIENDS.getValue();
    private int mOffset = 0; // 偏移量
    private int DEFAULT_COUNT = 30; // 每次拉去最大值

    ExRelativeLayout mSearchArea;
    RecyclerView mRecyclerView;
    SmartRefreshLayout mRefreshLayout;

    LoadService mLoadService;

    RelationAdapter mRelationAdapter;

    DialogPlus mDialogPlus;

    Handler mHandler = new Handler();

    boolean hasInitData = false;

    public RelationView(Context context, int mode) {
        super(context);
        init(context, mode);
    }

    private void init(Context context, int mode) {
        inflate(context, R.layout.relation_view, this);
        this.mMode = mode;

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mSearchArea = this.findViewById(R.id.search_area);
        mRecyclerView = this.findViewById(R.id.recycler_view);
        mRefreshLayout = this.findViewById(R.id.refreshLayout);

        mSearchArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(SearchFriendFragment.BUNDLE_SEARCH_MODE, mMode);
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newAddParamsBuilder((BaseActivity) getContext(), SearchFriendFragment.class)
                        .setUseOldFragmentIfExist(false)
                        .setBundle(bundle)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        });

        mRelationAdapter = new RelationAdapter(mMode, new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                UserInfoModel userInfoModel = (UserInfoModel) model;
                if (view.getId() == R.id.content) {
                    // 跳到他人的个人主页
                    Bundle bundle = new Bundle();
                    bundle.putInt("bundle_user_id", userInfoModel.getUserId());
                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                            .with(bundle)
                            .navigation();

                } else if (view.getId() == R.id.follow_tv) {
                    // 关注和好友都是有关系的人
                    if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                        if (userInfoModel.isFriend()) {
                            unFollow(userInfoModel);
                        } else {
                            UserInfoManager.getInstance().mateRelation(userInfoModel.getUserId(), UserInfoManager.RA_BUILD, userInfoModel.isFriend());
                        }
                    } else {
                        unFollow(userInfoModel);
                    }

                }
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRelationAdapter);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadData(mOffset);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });


        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new FriendsEmptyCallback())
                .addCallback(new FansEmptyCallback())
                .addCallback(new FollowEmptyCallback())
                .build();
        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                loadData(0);
            }
        });
    }

    private void unFollow(final UserInfoModel userInfoModel) {
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                .setTitleTip("取消关注")
                .setMessageTip("是否取消关注")
                .setConfirmTip("取消关注")
                .setCancelTip("不了")
                .setConfirmBtnClickListener(new DebounceViewClickListener() {

                    @Override
                    public void clickValid(View v) {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                        UserInfoManager.getInstance().mateRelation(userInfoModel.getUserId(), UserInfoManager.RA_UNBUILD, userInfoModel.isFriend());
                    }
                })
                .setCancelBtnClickListener(new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                    }
                })
                .build();

        mDialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(@NonNull DialogPlus dialog) {

                    }
                })
                .create();
        mDialogPlus.show();
    }

    public void initData(boolean flag) {
        if (!flag && hasInitData) {
            // 已经初始化过了 并且falg为false
        } else {
            loadData(0);
        }
    }

    private void loadData(final int offset) {
        this.mOffset = offset;
        if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
            UserInfoManager.getInstance().getMyFriends(UserInfoManager.ONLINE_PULL_NORMAL, new UserInfoManager.UserInfoListCallback() {
                @Override
                public void onSuccess(UserInfoManager.FROM from, int offset, final List<UserInfoModel> list) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hasInitData = true;
                            mRefreshLayout.setEnableLoadMore(false);
                            if (list != null && list.size() != 0) {
                                mRefreshLayout.finishLoadMore();
                                mLoadService.showSuccess();
                                mRelationAdapter.setData(list);
                            } else {
                                mRefreshLayout.finishLoadMore();
                                if (mRelationAdapter.getData() == null || mRelationAdapter.getData().size() == 0) {
                                    // 第一次拉数据
                                    if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
                                        mLoadService.showCallback(FriendsEmptyCallback.class);
                                    } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                                        mLoadService.showCallback(FansEmptyCallback.class);
                                    } else if (mMode == UserInfoManager.RELATION.FOLLOW.getValue()) {
                                        mLoadService.showCallback(FollowEmptyCallback.class);
                                    }
                                }
                            }
                        }
                    });
                }
            });
        } else if (mMode == UserInfoManager.RELATION.FOLLOW.getValue()) {
            UserInfoManager.getInstance().getMyFollow(UserInfoManager.ONLINE_PULL_NORMAL, new UserInfoManager.UserInfoListCallback() {
                @Override
                public void onSuccess(UserInfoManager.FROM from, int offset, final List<UserInfoModel> list) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hasInitData = true;
                            mRefreshLayout.setEnableLoadMore(false);
                            if (list != null && list.size() != 0) {
                                mRefreshLayout.finishLoadMore();
                                mLoadService.showSuccess();
                                mRelationAdapter.setData(list);
                            } else {
                                mRefreshLayout.finishLoadMore();
                                if (mRelationAdapter.getData() == null || mRelationAdapter.getData().size() == 0) {
                                    // 第一次拉数据
                                    if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
                                        mLoadService.showCallback(FriendsEmptyCallback.class);
                                    } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                                        mLoadService.showCallback(FansEmptyCallback.class);
                                    } else if (mMode == UserInfoManager.RELATION.FOLLOW.getValue()) {
                                        mLoadService.showCallback(FollowEmptyCallback.class);
                                    }
                                }
                            }
                        }
                    });
                }
            });
        } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
            UserInfoManager.getInstance().getFans(offset, DEFAULT_COUNT, new UserInfoManager.UserInfoListCallback() {
                @Override
                public void onSuccess(UserInfoManager.FROM from, final int offset, final List<UserInfoModel> list) {
                    hasInitData = true;
                    if (list != null && list.size() != 0) {
                        mRefreshLayout.finishLoadMore();
                        mLoadService.showSuccess();
                        if (mOffset == 0) {
                            // 如果是从0来的，则是刷新数据
                            mRelationAdapter.setData(list);
                        } else {
                            mRelationAdapter.addData(list);
                        }
                        mRefreshLayout.setEnableLoadMore(true);
                    } else {
                        mRefreshLayout.setEnableLoadMore(false);
                        mRefreshLayout.finishLoadMore();
                        if (mRelationAdapter.getData() == null || mRelationAdapter.getData().size() == 0) {
                            // 第一次拉数据
                            if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
                                mLoadService.showCallback(FriendsEmptyCallback.class);
                            } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                                mLoadService.showCallback(FansEmptyCallback.class);
                            } else if (mMode == UserInfoManager.RELATION.FOLLOW.getValue()) {
                                mLoadService.showCallback(FollowEmptyCallback.class);
                            }
                        }
                    }
                    mOffset = offset;
                }
            });
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDialogPlus != null && mDialogPlus.isShowing()) {
            mDialogPlus.dismiss(false);
        }
    }

    public void destroy() {
        if (mDialogPlus != null && mDialogPlus.isShowing()) {
            mDialogPlus.dismiss();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * 别人关注的事件,所有的关系都是从我出发
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FollowNotifyEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        this.mOffset = 0;
        loadData(0);
    }

    /**
     * 自己主动关注或取关事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        MyLog.d(TAG, "RelationChangeEvent" + " event type = " + event.type + " isFriend = " + event.isFriend);
        this.mOffset = 0;
        loadData(0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RemarkChangeEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        this.mOffset = 0;
        loadData(0);
    }
}
