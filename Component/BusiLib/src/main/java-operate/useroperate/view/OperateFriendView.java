package useroperate.view;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

import useroperate.adapter.OperateFirendAdapter;
import useroperate.callback.OperateFansEmptyCallback;
import useroperate.callback.OperateFollowEmptyCallback;
import useroperate.callback.OperateFriendsEmptyCallback;
import useroperate.inter.IOperateFriendView;
import useroperate.inter.IOperateStub;

public class OperateFriendView extends RelativeLayout implements IOperateFriendView {

    SmartRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;
    OperateFirendAdapter mInviteFirendAdapter;

    private int mMode = UserInfoManager.RELATION.FRIENDS.getValue();
    private int mOffset = 0; // 偏移量
    private int DEFAULT_COUNT = 30; // 每次拉去最大值
    private boolean mHasMore = true;

    Handler mHandler = new Handler(Looper.myLooper());

    BaseFragment mBaseFragment;
    LoadService mLoadService;

    public OperateFriendView(BaseFragment fragment, int mode, IOperateStub<UserInfoModel> stub) {
        super(fragment.getContext());
        init(fragment, mode, stub);
    }

    private void init(BaseFragment fragment, int mode, IOperateStub<UserInfoModel> stub) {
        this.mBaseFragment = fragment;
        this.mMode = mode;
        inflate(fragment.getContext(), R.layout.operate_view_layout, this);

        mRefreshLayout = (SmartRefreshLayout) findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mInviteFirendAdapter = new OperateFirendAdapter(new OperateFirendAdapter.OnInviteClickListener() {

            @Override
            public void onClick(UserInfoModel model, ExTextView view) {

            }

            @Override
            public void onClickSearch() {
//                Bundle bundle = new Bundle();
//                bundle.putSerializable(InviteSearchFragment.INVITE_SEARCH_FROM, mFrom);
//                bundle.putSerializable(InviteSearchFragment.INVITE_SEARCH_MODE, mMode);
//                bundle.putSerializable(InviteSearchFragment.INVITE_ROOM_ID, mRoomID);
//                bundle.putSerializable(InviteSearchFragment.INVITE_TAG_ID, mTagID);
//                U.getFragmentUtils().addFragment(FragmentUtils
//                        .newAddParamsBuilder((BaseActivity) getContext(), InviteSearchFragment.class)
//                        .setUseOldFragmentIfExist(false)
//                        .setBundle(bundle)
//                        .setAddToBackStack(true)
//                        .setHasAnimation(true)
//                        .build());
            }
        }, true, stub, mMode);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mInviteFirendAdapter);

//        mGrabInvitePresenter = new OperateViewPresenter(mBaseFragment, this, mRoomID, mFrom);

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
                .addCallback(new OperateFriendsEmptyCallback())
                .addCallback(new OperateFansEmptyCallback())
                .build();
        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                loadData(mOffset);
            }
        });

        loadData(mOffset);
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
                                mInviteFirendAdapter.setDataList(list);
                            } else {
                                mRefreshLayout.finishLoadMore();
                                if (mInviteFirendAdapter.getDataList() == null || mInviteFirendAdapter.getDataList().size() == 0) {
                                    // 第一次拉数据
                                    if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
                                        mLoadService.showCallback(OperateFriendsEmptyCallback.class);
                                    } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                                        mLoadService.showCallback(OperateFansEmptyCallback.class);
                                    } else if (mMode == UserInfoManager.RELATION.FOLLOW.getValue()) {
                                        mLoadService.showCallback(OperateFollowEmptyCallback.class);
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
                                mInviteFirendAdapter.setDataList(list);
                            } else {
                                mRefreshLayout.finishLoadMore();
                                if (mInviteFirendAdapter.getDataList() == null || mInviteFirendAdapter.getDataList().size() == 0) {
                                    // 第一次拉数据
                                    if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
                                        mLoadService.showCallback(OperateFriendsEmptyCallback.class);
                                    } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                                        mLoadService.showCallback(OperateFansEmptyCallback.class);
                                    } else if (mMode == UserInfoManager.RELATION.FOLLOW.getValue()) {
                                        mLoadService.showCallback(OperateFollowEmptyCallback.class);
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
                            mInviteFirendAdapter.setDataList(list);
                        } else {
                            mInviteFirendAdapter.setDataList(list);
                        }
                        mRefreshLayout.setEnableLoadMore(true);
                    } else {
                        mRefreshLayout.setEnableLoadMore(false);
                        mRefreshLayout.finishLoadMore();
                        if (mInviteFirendAdapter.getDataList() == null || mInviteFirendAdapter.getDataList().size() == 0) {
                            // 第一次拉数据
                            if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
                                mLoadService.showCallback(OperateFriendsEmptyCallback.class);
                            } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                                mLoadService.showCallback(OperateFansEmptyCallback.class);
                            } else if (mMode == UserInfoManager.RELATION.FOLLOW.getValue()) {
                                mLoadService.showCallback(OperateFollowEmptyCallback.class);
                            }
                        }
                    }
                    mOffset = offset;
                }
            });
        }
    }

    @Override
    public void addInviteModelList(List<UserInfoModel> grabFriendModelList, int oldOffset, int newOffset) {
        if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
            mRefreshLayout.setEnableLoadMore(false);
            mLoadService.showSuccess();
            mInviteFirendAdapter.getDataList().clear();
            mInviteFirendAdapter.getDataList().addAll(grabFriendModelList);
            mInviteFirendAdapter.notifyDataSetChanged();
        } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
            this.mOffset = newOffset;
            if (grabFriendModelList != null && grabFriendModelList.size() > 0) {
                mHasMore = true;
                mRefreshLayout.setEnableLoadMore(true);
                mLoadService.showSuccess();
                if (oldOffset == 0) {
                    mInviteFirendAdapter.getDataList().clear();
                }
                mInviteFirendAdapter.getDataList().addAll(grabFriendModelList);
                mInviteFirendAdapter.notifyDataSetChanged();
            } else {
                mHasMore = false;
                mRefreshLayout.setEnableLoadMore(false);
                if (mInviteFirendAdapter.getDataList() == null || mInviteFirendAdapter.getDataList().size() == 0) {
                    // 没有数据
                    if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
                        mLoadService.showCallback(OperateFriendsEmptyCallback.class);
                    } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                        mLoadService.showCallback(OperateFansEmptyCallback.class);
                    }
                } else {
                    // 没有更多了
                }
            }
        }
    }

    @Override
    public void updateInvited(ExTextView view) {
        if (view != null) {
            view.setAlpha(0.5f);
            view.setText("已邀请");
            view.setClickable(false);
            view.setVisibility(VISIBLE);
        }
    }

    @Override
    public void finishRefresh() {
        mRefreshLayout.finishLoadMore();
    }
}
