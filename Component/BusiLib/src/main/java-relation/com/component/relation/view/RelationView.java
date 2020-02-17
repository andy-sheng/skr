package com.component.relation.view;

import android.app.Activity;
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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseActivity;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.event.RemarkChangeEvent;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.notification.event.FollowNotifyEvent;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.rxretrofit.ControlType;
import com.common.rxretrofit.RequestControl;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.component.relation.adapter.RelationAdapter;
import com.component.relation.callback.FansEmptyCallback;
import com.component.relation.callback.FollowEmptyCallback;
import com.component.relation.callback.FriendsEmptyCallback;
import com.component.relation.fragment.SearchFriendFragment;
import com.dialog.view.TipsDialogView;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.RouterConstants;
import com.module.home.IHomeService;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RelationView extends RelativeLayout {

    public final String TAG = "RelationView" + hashCode();

    private int mMode = UserInfoManager.RELATION.FRIENDS.getValue();
    private int mOffset = 0; // 偏移量
    private int DEFAULT_COUNT = 30; // 每次拉去最大值

    ExRelativeLayout mSearchArea;
    RecyclerView mRecyclerView;
    SmartRefreshLayout mRefreshLayout;

    private UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);

    LoadService mLoadService;

    RelationAdapter mRelationAdapter;

    DialogPlus mDialogPlus;

    Handler mHandler = new Handler();

    boolean hasInitData = false;

    int mFrom = 0;  //默认为0，1为从赠送礼物来的
    public String mExtra = "";

    TipsDialogView tipsDialogView;

    public RelationView(Context context, int mode, int from) {
        super(context);
        init(context, mode, from);
    }

    private void init(Context context, int mode, int from) {
        inflate(context, R.layout.relation_view, this);
        this.mMode = mode;
        this.mFrom = from;

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
                        .addDataBeforeAdd(1, mFrom)
                        .addDataBeforeAdd(2, mExtra)
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

                } else if (view.getId() == R.id.send_tv) {
                    if (mFrom == 1) {
                        showGiveDialog(userInfoModel);
                    } else if (mFrom == 2) {
                        checkRelation(userInfoModel);
                    }
                }
            }
        });
        mRelationAdapter.mFrom = mFrom;
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

    private void showGiveDialog(UserInfoModel userInfoModel) {
        if (tipsDialogView != null) {
            tipsDialogView.dismiss(false);
        }

        IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();

        tipsDialogView = new TipsDialogView.Builder((Activity) getContext())
                .setMessageTip("是否赠送给" + userInfoModel.getNickname() + channelService.getSelectedMallName() + "?")
                .setCancelBtnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tipsDialogView != null) {
                            tipsDialogView.dismiss(false);
                        }
                    }
                })
                .setCancelTip("取消")
                .setConfirmBtnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        channelService.selectGiveMallUserFinish(userInfoModel.getUserId());
                        ((Activity) getContext()).finish();
                    }
                })
                .setConfirmTip("赠送")
                .build();
        tipsDialogView.showByDialog();
    }

    private void checkRelation(UserInfoModel userInfoModel) {
        if (U.getStringUtils().isJSON(mExtra)) {
            JSONObject jsonObject = JSONObject.parseObject(mExtra);
            HashMap map = new HashMap();
            map.put("goodsID", jsonObject.getIntValue("goodsID"));
            map.put("otherUserID", userInfoModel.getUserId());
            RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
            ApiMethods.subscribe(userInfoServerApi.checkCardRelation(body), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        String msg = result.getData().getString("noticeMsg");
                        showInviteCardDialog(userInfoModel, msg);
                    } else {
                        //ErrAlreadyHasRelation           = 8428114; //对方已是你的闺蜜，不能再发送邀请哦～
                        //ErrApplyAfter24Hour             = 8428115; //24小时之后才能再次发送关系申请哦～
                        //ErrAlreadyHasOtherRelation      = 8428116; //对方已是你的闺蜜，对方接受邀请将自动解除你们原来的关系哦～
                        if (8428114 == result.getErrno()) {
                            U.getToastUtil().showShort(result.getErrmsg());
                        } else if (8428115 == result.getErrno()) {
                            U.getToastUtil().showShort(result.getErrmsg());
                        } else if (8428116 == result.getErrno()) {
                            showInviteCardDialog(userInfoModel, result.getErrmsg());
                        }
                    }
                }
            }, new RequestControl("checkCardRelation", ControlType.CancelLast));
        }
    }

    /**
     * 邀请好友发生关系
     *
     * @param userInfoModel
     */
    private void showInviteCardDialog(UserInfoModel userInfoModel, String msg) {
        if (tipsDialogView != null) {
            tipsDialogView.dismiss(false);
        }

        IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();

        tipsDialogView = new TipsDialogView.Builder((Activity) getContext())
                .setMessageTip(msg)
                .setCancelBtnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tipsDialogView != null) {
                            tipsDialogView.dismiss(false);
                        }
                    }
                })
                .setCancelTip("取消")
                .setConfirmBtnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((Activity) getContext()).finish();
                        channelService.inviteToRelationCardFinish(userInfoModel.getUserId());
                    }
                })
                .setConfirmTip("邀请")
                .build();
        tipsDialogView.showByDialog();
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
            hasInitData = true;
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
            UserInfoManager.getInstance().getMyFollow(UserInfoManager.ONLINE_PULL_NORMAL, true, new UserInfoManager.UserInfoListCallback() {
                @Override
                public void onSuccess(UserInfoManager.FROM from, int offset, final List<UserInfoModel> list) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
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
            }, false);
        } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
            UserInfoManager.getInstance().getFans(offset, DEFAULT_COUNT, new UserInfoManager.UserInfoListCallback() {
                @Override
                public void onSuccess(UserInfoManager.FROM from, final int offset, final List<UserInfoModel> list) {
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
        hasInitData = false;
//        loadData(0);
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
        hasInitData = false;
//        loadData(0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RemarkChangeEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        this.mOffset = 0;
        hasInitData = false;
//        loadData(0);
    }
}
