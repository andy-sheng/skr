package com.wali.live.watchsdk.fans.rank.view;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.mvp.specific.RxRelativeLayout;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.common.barrage.view.utils.FansInfoUtils;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.rank.data.RankFansData;
import com.wali.live.watchsdk.fans.rank.model.RankListModel;
import com.wali.live.watchsdk.fans.rank.presenter.FansRankPresenter;
import com.wali.live.watchsdk.fans.rank.presenter.IFansRankView;
import com.wali.live.watchsdk.ranking.adapter.RankRecyclerViewAdapter;

import java.util.List;

/**
 * Created by zhaomin on 17-6-12.
 */
public class FansRankView extends RxRelativeLayout implements IFansRankView {
    private static final String TAG = FansRankView.class.getSimpleName();

    private RecyclerView mRecyclerView;

    private BaseImageView mAvatarIv;
    private TextView mNameTv;
    private TextView mDisplayTv;
    private TextView mRankNumTv;
    private TextView mLevelNameTv;
    private TextView mGapExpValueTv;

    private RelativeLayout mRankingArea;

    private RankRecyclerViewAdapter mAdapter;

    private String mUiType = RankRecyclerViewAdapter.CURRENT_RANK;

    private int mRankType;
    private long mZuid;

    private boolean mIsGroupRank; // 团排名

    private boolean mHasMore = true;
    private boolean mFirstLoad = true; // 第一次加载

    private FansRankPresenter mRankPresenter;

    public FansRankView(Context context, int rankType, long zuid, boolean isGroupRank) {
        super(context);
        mRankType = rankType;
        mZuid = zuid;
        mIsGroupRank = isGroupRank;
        init();
    }

    public FansRankView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FansRankView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        inflate(getContext(), R.layout.fans_rank_view, this);

        mRecyclerView = $(R.id.recycler_view);

        mAvatarIv = $(R.id.rank_avatar);
        mNameTv = $(R.id.txt_username);
        mDisplayTv = $(R.id.display_tv);
        mRankNumTv = $(R.id.rankNum);
        mLevelNameTv = $(R.id.level_name);
        mGapExpValueTv = $(R.id.gap_value);
        mRankingArea = $(R.id.rankingRootLayout);

        mUiType = mRankType == VFansCommonProto.RankDateType.TOTAL_TYPE_VALUE ? RankRecyclerViewAdapter.TOTAL_RANK : RankRecyclerViewAdapter.CURRENT_RANK;

        mAdapter = new RankRecyclerViewAdapter((RxActivity) getContext(), mUiType);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
                    loadRankData();
                }
            }
        });

        initPresenter();
    }

    private void initPresenter() {
        mRankPresenter = new FansRankPresenter(this, VFansCommonProto.RankDateType.valueOf(mRankType), mZuid, mIsGroupRank);
        start();
    }

    private void bindMyRankView(RankFansData data) {
        if (data == null) {
            return;
        }
        mRankingArea.setVisibility(VISIBLE);
        String value = String.valueOf(data.getCatchUpExp());
        SpannableString ss;
        String numTip = "";
        if (mIsGroupRank) {
            ss = new SpannableString(value + getContext().getString(R.string.meili_value));
            numTip = getContext().getString(R.string.meili_value) + ":" + data.getExp();

            mLevelNameTv.setBackgroundResource(FansInfoUtils.getImageResourcesByCharmLevelValue(data.getLevel()));
            mLevelNameTv.setText("");
            if (!TextUtils.isEmpty(data.getGroupName())) {
                mNameTv.setText(data.getGroupName());
            }
        } else {
            ss = new SpannableString(value + getContext().getResources().getString(R.string.vfans_friendliness));
            numTip = getContext().getString(R.string.vfans_friendliness_value, data.getExp());

            mLevelNameTv.setBackgroundResource(FansInfoUtils.getGroupMemberLevelDrawable(data.getLevel()));
            mLevelNameTv.setText(data.getMedalName());
            mNameTv.setText(MyUserInfoManager.getInstance().getNickname());
        }
        ss.setSpan(new RelativeSizeSpan(1.5f), 0, value.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置字体前景色
        ss.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.color_ff2966)), 0, value.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置字体背景色
        mGapExpValueTv.setText(ss);
        mDisplayTv.setText(numTip);
        mRankNumTv.setText(String.valueOf(data.getRankNum()));
        AvatarUtils.loadAvatarByUidTs(mAvatarIv, UserAccountManager.getInstance().getUuidAsLong(), MyUserInfoManager.getInstance().getAvatar(), true);
    }

    public void start() {
        mRankPresenter.reset();
        mFirstLoad = true;
        loadRankData();
    }

    private void loadRankData() {
        if (mHasMore) {
            mRankPresenter.loadRankData();
        } else {
            MyLog.w(TAG, "loadRankData not has More");
        }
    }

    @Override
    public void notifyGetRankListSuccess(RankListModel rankListModel) {
        mHasMore = rankListModel.isHasMore();
        List<RankFansData> dataList = rankListModel.getRankDataList();
        if (dataList != null && !dataList.isEmpty()) {
            if (!mFirstLoad) {
                mAdapter.appendRankList(dataList);
            } else {
                mAdapter.setRankList(dataList);
                if (dataList.get(0).userId != UserAccountManager.getInstance().getUuidAsLong()) {
                    bindMyRankView(rankListModel.getMyRankData());
                }
            }
            mFirstLoad = false;
        } else {
            mAdapter.setEmptyHint(getContext().getString(R.string.empty_tips));
        }
    }

    @Override
    public void notifyGetRankListFailure() {
        mAdapter.setEmptyHint(getContext().getString(R.string.empty_tips));
    }
}
