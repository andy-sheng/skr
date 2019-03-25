package com.zq.relation.view;

import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.cache.BuddyCache;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.log.MyLog;
import com.common.notification.event.FollowNotifyEvent;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.dialog.view.TipsDialogView;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.person.fragment.OtherPersonFragment;
import com.zq.relation.adapter.RelationAdapter;
import com.zq.relation.callback.FansEmptyCallback;
import com.zq.relation.callback.FriendsEmptyCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.common.core.userinfo.UserInfoManager.RELATION_FOLLOW;
import static com.common.core.userinfo.event.RelationChangeEvent.FOLLOW_TYPE;
import static com.common.core.userinfo.event.RelationChangeEvent.UNFOLLOW_TYPE;

public class RelationView extends RelativeLayout {

    public final static String TAG = "RelationView";

    private int mMode = UserInfoManager.RELATION_FRIENDS;
    private int mOffset = 0; // 偏移量
    private int DEFAULT_COUNT = 30; // 每次拉去最大值
    private boolean hasMore = true; // 是否还有数据

    RecyclerView mRecyclerView;
    SmartRefreshLayout mRefreshLayout;

    LoadService mLoadService;

    RelationAdapter mRelationAdapter;

    DialogPlus mDialogPlus;

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

        mRecyclerView = (RecyclerView) this.findViewById(R.id.recycler_view);
        mRefreshLayout = (SmartRefreshLayout) this.findViewById(R.id.refreshLayout);

        mRelationAdapter = new RelationAdapter(false, new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                UserInfoModel userInfoModel = (UserInfoModel) model;
                if (view.getId() == R.id.content) {
                    // 跳到他人的个人主页
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(OtherPersonFragment.BUNDLE_USER_ID, userInfoModel.getUserId());
                    U.getFragmentUtils().addFragment(FragmentUtils
                            .newAddParamsBuilder((BaseActivity) getContext(), OtherPersonFragment.class)
                            .setUseOldFragmentIfExist(false)
                            .setBundle(bundle)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .build());
                } else if (view.getId() == R.id.follow_tv) {
                    // 关注和好友都是有关系的人
                    if (mMode == UserInfoManager.RELATION_FANS) {
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
                loadData(mMode, mOffset, DEFAULT_COUNT);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });


        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new FriendsEmptyCallback())
                .addCallback(new FansEmptyCallback())
                .build();
        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                loadData(mMode, mOffset, DEFAULT_COUNT);
            }
        });

        loadData(mMode, mOffset, DEFAULT_COUNT);
    }

    private void unFollow(final UserInfoModel userInfoModel) {
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
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

    UserInfoManager.ResponseCallBack<ApiResult> mApiResultResponseCallBack = new UserInfoManager.ResponseCallBack<ApiResult>() {
        @Override
        public void onServerSucess(ApiResult result) {
            mOffset = result.getData().getIntValue("offset");
            List<UserInfoModel> userInfoModels = JSON.parseArray(result.getData().getString("users"), UserInfoModel.class);
            if (userInfoModels != null && userInfoModels.size() != 0) {
                mRefreshLayout.finishLoadMore();
                mLoadService.showSuccess();
                mRelationAdapter.addData(userInfoModels);
                mRelationAdapter.notifyDataSetChanged();
                hasMore = true;
            } else {
                hasMore = false;
                mRefreshLayout.finishLoadMoreWithNoMoreData();
                if (mOffset == 0) {
                    // 第一次拉数据
                    if (mMode == UserInfoManager.RELATION_FRIENDS) {
                        mLoadService.showCallback(FriendsEmptyCallback.class);
                    } else if (mMode == UserInfoManager.RELATION_FANS) {
                        mLoadService.showCallback(FansEmptyCallback.class);
                    } else if (mMode == RELATION_FOLLOW) {
                        mLoadService.showCallback(FriendsEmptyCallback.class);
                    }
                }
            }
        }

        @Override
        public void onServerFailed() {
            mRefreshLayout.finishLoadMore();
        }
    };

    public void loadData(final int mode, final int offset, int limit) {
        UserInfoManager.getInstance().getRelationList(mode, offset, limit, mApiResultResponseCallBack);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDialogPlus != null && mDialogPlus.isShowing()) {
            mDialogPlus.dismiss();
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

        for (UserInfoModel userInfoModel : mRelationAdapter.getData()) {
            if (userInfoModel.getUserId() == event.mUserInfoModel.getUserId()) {
                // 已经包含
                mRelationAdapter.getData().remove(userInfoModel);
                break;
            }
        }

        if (event.mUserInfoModel.isFriend()) {
            //好友
            mRelationAdapter.getData().add(0, event.mUserInfoModel);
        } else if (event.mUserInfoModel.isFollow()) {
            MyLog.w(TAG, "FollowNotifyEvent 啥玩意，又不是好友，只有我关注了他？ ");
        } else {
            // 粉丝
            if (mMode == UserInfoManager.RELATION_FANS) {
                mRelationAdapter.getData().add(0, event.mUserInfoModel);
            }
        }
        mRelationAdapter.notifyDataSetChanged();

        if (mRelationAdapter.getData() != null && mRelationAdapter.getData().size() > 0) {
            mLoadService.showSuccess();
        } else {
            if (mMode == UserInfoManager.RELATION_FRIENDS) {
                mLoadService.showCallback(FriendsEmptyCallback.class);
            } else if (mMode == UserInfoManager.RELATION_FANS) {
                mLoadService.showCallback(FansEmptyCallback.class);
            } else if (mMode == RELATION_FOLLOW) {
                mLoadService.showCallback(FriendsEmptyCallback.class);
            }
        }
    }

    /**
     * 自己主动关注或取关事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        MyLog.d(TAG, "RelationChangeEvent" + " event type = " + event.type + " isFriend = " + event.isFriend);

        BuddyCache.BuddyCacheEntry buddyCacheEntry = BuddyCache.getInstance().getBuddyNormal(event.useId, false);
        UserInfoModel userInfoModel = buddyCacheEntry.parseUserInfoMode();
        userInfoModel.setFriend(event.isFriend);
        userInfoModel.setFollow(event.isFollow);

        boolean isContainId = false; // 当前页面是否有该联系人
        for (UserInfoModel infoModel : mRelationAdapter.getData()) {
            if (infoModel.getUserId() == event.useId) {
                // 已经包含
                isContainId = true;
                mRelationAdapter.getData().remove(infoModel);
                break;
            }
        }

        if (event.type == FOLLOW_TYPE) {
            // 关注消息
            if (event.isFriend) {
                //好友
                mRelationAdapter.getData().add(0, userInfoModel);
            } else if (event.isFollow) {
                MyLog.w(TAG, "关注消息，服务返回我没关注？？？？？？");
            } else {
                // 粉丝
                if (mMode == UserInfoManager.RELATION_FANS) {
                    mRelationAdapter.getData().add(0, userInfoModel);
                }
            }
        } else if (event.type == UNFOLLOW_TYPE) {
            // 取关消息,只对粉丝有影响
            if (isContainId && mMode == UserInfoManager.RELATION_FANS) {
                mRelationAdapter.getData().add(0, userInfoModel);
            }
        }
        mRelationAdapter.notifyDataSetChanged();

        if (mRelationAdapter.getData() != null && mRelationAdapter.getData().size() > 0) {
            mLoadService.showSuccess();
        } else {
            if (mMode == UserInfoManager.RELATION_FRIENDS) {
                mLoadService.showCallback(FriendsEmptyCallback.class);
            } else if (mMode == UserInfoManager.RELATION_FANS) {
                mLoadService.showCallback(FansEmptyCallback.class);
            } else if (mMode == RELATION_FOLLOW) {
                mLoadService.showCallback(FriendsEmptyCallback.class);
            }
        }

//        if (mMode == UserInfoManager.RELATION_FRIENDS) {
//            // 好友页面
//            if (event.type == FOLLOW_TYPE) {
//                if (event.isFriend) {
//                    UserInfoModel userInfoModel = buddyCacheEntry.parseUserInfoMode();
//                    userInfoModel.setFriend(event.isFriend);
//                    userInfoModel.setFollow(event.isFollow);
//                    mRelationAdapter.getData().add(0, userInfoModel);
//                    mRelationAdapter.notifyDataSetChanged();
//                }
//            } else if (event.type == UNFOLLOW_TYPE) {
//                UserInfoModel delModel = null;
//                for (UserInfoModel model : mRelationAdapter.getData()) {
//                    if (model.getUserId() == event.useId) {
//                        delModel = model;
//                        break;
//                    }
//                }
//
//                if (delModel != null) {
//                    mRelationAdapter.getData().remove(delModel);
//                    mRelationAdapter.notifyDataSetChanged();
//                }
//            }
//        } else if (mMode == UserInfoManager.RELATION_FANS) {
//            // 粉丝页面
//            if (event.type == FOLLOW_TYPE) {
//                if (event.isFriend) {
//                    UserInfoModel delModel = null;
//                    for (UserInfoModel model : mRelationAdapter.getData()) {
//                        if (model.getUserId() == event.useId) {
//                            delModel = model;
//                            break;
//                        }
//                    }
//
//                    if (delModel != null) {
//                        // 之前已有，改状态
//                        mRelationAdapter.getData().remove(delModel);
//                    }
//
//                    UserInfoModel userInfoModel = buddyCacheEntry.parseUserInfoMode();
//                    userInfoModel.setFriend(event.isFriend);
//                    userInfoModel.setFollow(event.isFollow);
//                    mRelationAdapter.getData().add(0, userInfoModel);
//                    mRelationAdapter.notifyDataSetChanged();
//                }
//            } else if (event.type == UNFOLLOW_TYPE) {
//                // 粉丝页面收到取关，必须当前粉丝页面包括此人
//                UserInfoModel delModel = null;
//                for (UserInfoModel model : mRelationAdapter.getData()) {
//                    if (model.getUserId() == event.useId) {
//                        delModel = model;
//                        break;
//                    }
//                }
//                if (delModel != null) {
//                    mRelationAdapter.getData().remove(delModel);
//
//                    UserInfoModel userInfoModel = buddyCacheEntry.parseUserInfoMode();
//                    userInfoModel.setFriend(event.isFriend);
//                    userInfoModel.setFollow(event.isFollow);
//                    mRelationAdapter.getData().add(0, userInfoModel);
//                    mRelationAdapter.notifyDataSetChanged();
//                }
//            }
//        } else if (mMode == RELATION_FOLLOW) {
//            // 关注页面
//            if (event.type == FOLLOW_TYPE) {
//                UserInfoModel userInfoModel = buddyCacheEntry.parseUserInfoMode();
//                userInfoModel.setFriend(event.isFriend);
//                userInfoModel.setFollow(event.isFollow);
//                mRelationAdapter.getData().add(0, userInfoModel);
//                mRelationAdapter.notifyDataSetChanged();
//            } else if (event.type == UNFOLLOW_TYPE) {
//                UserInfoModel delModel = null;
//                for (UserInfoModel model : mRelationAdapter.getData()) {
//                    if (model.getUserId() == event.useId) {
//                        delModel = model;
//                        break;
//                    }
//                }
//                if (delModel != null) {
//                    mRelationAdapter.getData().remove(delModel);
//                    mRelationAdapter.notifyDataSetChanged();
//                }
//
//            }
//        }
    }

}
