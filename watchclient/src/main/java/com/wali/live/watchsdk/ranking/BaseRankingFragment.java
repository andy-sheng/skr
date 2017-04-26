package com.wali.live.watchsdk.ranking;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.RxActivity;
import com.base.fragment.MyRxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.event.BlockOrUnblockEvent;
import com.mi.live.data.event.FollowOrUnfollowEvent;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.proto.RankProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.ranking.adapter.RankRecyclerViewAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;

/**
 * Created by jiyangli on 16-6-30.
 */
public abstract class BaseRankingFragment extends MyRxFragment {
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    public static final String EXTRA_TICKET_NUM = "extra_ticket_num";
    public static final String EXTRA_TICKET_START = "extra_ticket_start";
    public static final String EXTRA_UUID = "extra_uuid";
    public static final String EXTRA_LIVE_ID = "extra_live_id";

    protected final int PAGE_COUNT = 20;

    protected LinearLayoutManager mLayoutManager;
    protected RankRecyclerViewAdapter mVoteRankingAdapter;
    protected RecyclerView mRecyclerView;
    protected View mLoadingView;
    protected View mEmptyView;
    protected ViewGroup mCoverView;

    protected long mUuid;
    protected String mLiveId;
    protected int mOffSet = 0;
    protected int mTicketNum = 0;
    protected volatile List<RankProto.RankUser> mResultList = new ArrayList();//为了动画流畅缓存第一次加载的数据，动画结束之后进行刷新

    protected String mFragmentType;
    protected Subscription mSubscription;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_vote_ranking, container, false);
    }

    @Override
    protected void bindView() {
        Bundle bundle = getArguments();
        initData(bundle);

        initView();
        EventBus.getDefault().register(this);
    }

    protected void initData(Bundle bundle) {
        mUuid = bundle.getLong(EXTRA_UUID);
        mLiveId = bundle.getString(EXTRA_LIVE_ID);

        if (mUuid == 0) {
            this.mUuid = UserAccountManager.getInstance().getUuidAsLong();
        }
    }

    private void initView() {
        mRecyclerView = $(R.id.rankList);
        mCoverView = $(R.id.cover_view);
        mLoadingView = $(mCoverView, R.id.loading);
        mEmptyView = $(mCoverView, R.id.empty);

        mLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                scrollRecyclerView(recyclerView);
            }
        });
        mVoteRankingAdapter = new RankRecyclerViewAdapter((RxActivity) getActivity(), mFragmentType);
        mVoteRankingAdapter.setShowTotalNumHeader(true);
        if (!TextUtils.isEmpty(mFragmentType) && mFragmentType.equals(RankRecyclerViewAdapter.TOTAL_RANK)) {
            mVoteRankingAdapter.setTotalNum(mTicketNum);
        }
        mRecyclerView.setAdapter(mVoteRankingAdapter);
        mVoteRankingAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                onClickItem(position);
            }
        });
        loadMoreData(mUuid, mLiveId, PAGE_COUNT, mOffSet);
    }

    /**
     * 滑动recyclerview加载数据
     */
    public void scrollRecyclerView(RecyclerView recyclerView) {
        if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
            loadMoreData(mUuid, mLiveId, PAGE_COUNT, mOffSet);
        }
    }

    /**
     * 点击item跳转用户详情页
     */
    public void onClickItem(int position) {
        UserListData userListData = mVoteRankingAdapter.getRankUser(position);
        if (null != userListData) {
            //TODO jump to PersonInfoFragment
        }
    }

    /**
     * 获取更多数据更新UI
     */
    public void updateView(List<RankProto.RankUser> result) {
        if (result == null || result.size() == 0) {
            if (mOffSet == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
            }
            MyLog.d(TAG, "result size is 0");
            return;
        }
        mCoverView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);

        List<UserListData> userList = new ArrayList<>();
        for (RankProto.RankUser rankUser : result) {
            userList.add(new UserListData(rankUser));
        }
        if (mOffSet == 0) {
            mVoteRankingAdapter.setRankList(userList);
        } else {
            mVoteRankingAdapter.appendRankList(userList);
        }
        mOffSet += result.size();
    }

    public void setRoomTicket(int ticket) {
        mVoteRankingAdapter.setTotalNum(ticket);
    }

    /**
     * 加载数据前 显示loadingView
     */
    public void preLoadData() {
        mCoverView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
        if (mOffSet == 0) {
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * 关注与取消关注
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FollowOrUnfollowEvent event) {
        if (event == null) {
            MyLog.v(TAG, " onEventMainThread FollowOrUnfollowEvent event is null");
            return;
        }

        int type = event.eventType;
        long uuid = event.uuid;
        List<UserListData> list = new ArrayList<>(mVoteRankingAdapter.getDatalist());
        if (list != null) {
            for (int i = 0; i < mVoteRankingAdapter.getDatalist().size(); i++) {
                UserListData user = list.get(i);
                if (user.userId == uuid) {
                    if (type == FollowOrUnfollowEvent.EVENT_TYPE_FOLLOW) {
                        user.isFollowing = true;
                        user.isBothway = event.isBothFollow;
                    } else if (type == FollowOrUnfollowEvent.EVENT_TYPE_UNFOLLOW) {
                        user.isFollowing = false;
                        user.isBothway = false;
                    }

                    Fragment fragment = FragmentNaviUtils.getTopFragment(getActivity());
                    if (!(fragment instanceof RankingPagerFragment)) {
                        mVoteRankingAdapter.notifyDataSetChanged();
                    } else {
                        mVoteRankingAdapter.notifyItemChanged(i);
                    }
                    return;
                }
            }
        }
    }

    boolean isPause = false;

    @Override
    public void onPause() {
        isPause = true;
        super.onPause();
    }

    //拉黑自动刷新关注按钮
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BlockOrUnblockEvent event) {
        if (event != null) {
            if (event.eventType == BlockOrUnblockEvent.EVENT_TYPE_BLOCK) {
                long uuid = event.uuid;
                List<UserListData> list = new ArrayList<>(mVoteRankingAdapter.getDatalist());
                if (list != null) {
                    for (UserListData user : list) {
                        if (user.userId == uuid) {
                            user.isFollowing = false;
                            user.isBothway = false;
                            mVoteRankingAdapter.notifyDataSetChanged();
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 子类加载数据
     */
    protected abstract void loadMoreData(long id, String liveId, int pageCount, int offset);
}