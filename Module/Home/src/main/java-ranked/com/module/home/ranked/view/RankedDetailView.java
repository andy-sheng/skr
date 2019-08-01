package com.module.home.ranked.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.core.avatar.AvatarUtils;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.component.person.utils.StringFromatUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.home.R;
import com.module.home.ranked.RankedServerApi;
import com.module.home.ranked.adapter.RankedDetailAdapter;
import com.module.home.ranked.model.RankDataModel;
import com.module.home.ranked.model.RankTagModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.component.level.view.NormalLevelView2;

import java.util.List;

import io.reactivex.disposables.Disposable;

import static com.module.home.ranked.model.RankDataModel.BLUE_ZUAN;
import static com.module.home.ranked.model.RankDataModel.MEILI;
import static com.module.home.ranked.model.RankDataModel.USER_RANKING;

public class RankedDetailView extends RelativeLayout {

    SmartRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;

    ConstraintLayout mMyrankArea;
    ImageView mRewardBg;
    TextView mSeqTv;
    SimpleDraweeView mAvatarIv;
    TextView mNameTv;
    TextView mDuanDesc;
    NormalLevelView2 mLevelView;
    ImageView mRankedIconIv;
    TextView mRankedDescTv;

    RankedDetailAdapter mAdapter;

    RankTagModel mRankTagModel;
    RankedServerApi mRankedServerApi;
    Disposable mDisposable;
    Disposable mMyDisposable;

    boolean isInitList = false;
    boolean isInitMyRank = false;
    int mOffset = 0;
    int limit = 20;

    public RankedDetailView(Context context, RankTagModel tagModel) {
        super(context);
        mRankTagModel = tagModel;
        mRankedServerApi = ApiManager.getInstance().createService(RankedServerApi.class);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.ranked_detail_view_layout, this);

        mRefreshLayout = findViewById(R.id.refreshLayout);
        mRecyclerView = findViewById(R.id.recycler_view);

        mMyrankArea = findViewById(R.id.myrank_area);
        mRewardBg = findViewById(R.id.reward_bg);
        mSeqTv = findViewById(R.id.seq_tv);
        mAvatarIv = findViewById(R.id.avatar_iv);
        mNameTv = findViewById(R.id.name_tv);
        mDuanDesc = findViewById(R.id.duan_desc);
        mLevelView = findViewById(R.id.level_view);
        mRankedIconIv = findViewById(R.id.ranked_icon_iv);
        mRankedDescTv = findViewById(R.id.ranked_desc_tv);

        mAdapter = new RankedDetailAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(true);

        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getListRank(mOffset);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });
    }

    public void initData() {
        if (!isInitList) {
            getListRank(0);
        }
        if (!isInitMyRank) {
            getMyRank();
        }
    }

    public void getListRank(int offset) {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mDisposable = ApiMethods.subscribe(mRankedServerApi.listRankData(mRankTagModel.getRankID(), offset, limit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    isInitList = true;
                    List<RankDataModel> list = JSON.parseArray(result.getData().getString("items"), RankDataModel.class);
                    int newOffset = result.getData().getIntValue("offset");

                    if (offset == 0) {
                        addRankList(list, newOffset, true);
                    } else {
                        addRankList(list, newOffset, false);
                    }
                } else {

                }
            }
        });
    }

    public void getMyRank() {
        if (mMyDisposable != null && !mMyDisposable.isDisposed()) {
            mMyDisposable.dispose();
        }

        mMyDisposable = ApiMethods.subscribe(mRankedServerApi.getMyRank(mRankTagModel.getRankID()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    RankDataModel rankDataModel = JSON.parseObject(result.getData().getString("rank"), RankDataModel.class);
                    showMyRank(rankDataModel);
                } else {

                }
            }
        });
    }

    private void addRankList(List<RankDataModel> list, int newOffset, boolean isClean) {
        mRefreshLayout.finishLoadMore();
        mOffset = newOffset;
        if (isClean) {
            mAdapter.getDataList().clear();
        }

        if (list != null && list.size() > 0) {
            mAdapter.getDataList().addAll(list);
            mAdapter.notifyDataSetChanged();
        } else {

            if (mAdapter.getDataList() != null && mAdapter.getDataList().size() > 0) {
                // 没有更多数据了
            } else {
                // 当前排行榜为空
            }
        }
    }

    private void showMyRank(RankDataModel rankDataModel) {
        if (rankDataModel == null) {
            return;
        }


        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(rankDataModel.getAvatar())
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColor(Color.WHITE)
                .build());
        mNameTv.setText(rankDataModel.getNickname());

        if (rankDataModel.getVType() == BLUE_ZUAN) {
            // 打赏榜
            mRankedDescTv.setVisibility(GONE);
            mLevelView.setVisibility(GONE);
            mRankedIconIv.setVisibility(VISIBLE);
            mRankedDescTv.setVisibility(VISIBLE);
            mRankedDescTv.setText("" + rankDataModel.getScore());
            mRankedIconIv.setImageResource(R.drawable.ranked_lanzuan_icon);
            if (rankDataModel.getRankSeq() == 0) {
                mSeqTv.setText("#");
                mSeqTv.setTextColor(Color.parseColor("#333B7B"));
                mRewardBg.setVisibility(GONE);
            } else if (rankDataModel.getRankSeq() <= 3) {
                mSeqTv.setText("" + StringFromatUtils.formatTenThousand(rankDataModel.getRankSeq()));
                mSeqTv.setTextColor(Color.WHITE);
                mRewardBg.setVisibility(VISIBLE);
            } else {
                mSeqTv.setText("" + StringFromatUtils.formatTenThousand(rankDataModel.getRankSeq()));
                mSeqTv.setTextColor(Color.parseColor("#333B7B"));
                mRewardBg.setVisibility(GONE);
            }
            mSeqTv.setBackground(null);
        } else if (rankDataModel.getVType() == MEILI) {
            // 人气榜
            mRewardBg.setVisibility(GONE);
            mRankedDescTv.setVisibility(GONE);
            mLevelView.setVisibility(GONE);
            mRankedIconIv.setVisibility(VISIBLE);
            mRankedDescTv.setVisibility(VISIBLE);
            mRankedIconIv.setImageResource(R.drawable.ranked_meili_icon);
            mRankedDescTv.setText("" + rankDataModel.getScore());
            if (rankDataModel.getRankSeq() <= 3) {
                mSeqTv.setText("");
                if (rankDataModel.getRankSeq() == 1) {
                    mSeqTv.setBackground(U.getDrawable(R.drawable.renqi_1));
                } else if (rankDataModel.getRankSeq() == 2) {
                    mSeqTv.setBackground(U.getDrawable(R.drawable.renqi_2));
                } else if (rankDataModel.getRankSeq() == 3) {
                    mSeqTv.setBackground(U.getDrawable(R.drawable.renqi_3));
                } else {
                    // 没有名次
                    mSeqTv.setBackground(null);
                    mSeqTv.setText("#");
                }
            } else {
                mSeqTv.setBackground(null);
                mSeqTv.setText("" + StringFromatUtils.formatTenThousand(rankDataModel.getRankSeq()));
            }
        } else if (rankDataModel.getVType() == USER_RANKING) {
            // 段位榜
            mRewardBg.setVisibility(GONE);
            mRankedDescTv.setVisibility(VISIBLE);
            mLevelView.setVisibility(VISIBLE);
            mRankedIconIv.setVisibility(GONE);
            mRankedDescTv.setVisibility(GONE);

            mDuanDesc.setText(rankDataModel.getLevelDesc());
            mLevelView.bindData(rankDataModel.getMainRanking(), rankDataModel.getSubRanking());
            if (rankDataModel.getRankSeq() <= 3) {
                mSeqTv.setText("");
                if (rankDataModel.getRankSeq() == 1) {
                    mSeqTv.setBackground(U.getDrawable(R.drawable.renqi_1));
                } else if (rankDataModel.getRankSeq() == 2) {
                    mSeqTv.setBackground(U.getDrawable(R.drawable.renqi_2));
                } else if (rankDataModel.getRankSeq() == 3) {
                    mSeqTv.setBackground(U.getDrawable(R.drawable.renqi_3));
                } else {
                    // 没有名次
                    mSeqTv.setBackground(null);
                    mSeqTv.setText("#");
                }
            } else {
                mSeqTv.setBackground(null);
                mSeqTv.setText("" + StringFromatUtils.formatTenThousand(rankDataModel.getRankSeq()));
            }
        }


    }

    public void destory() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mMyDisposable != null) {
            mMyDisposable.dispose();
        }
    }
}
