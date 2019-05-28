package com.module.home.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.module.home.R;
import com.module.home.adapter.HomeGoldCheckinAdapter;
import com.module.home.model.HomeGoldModel;

import java.util.List;

public class HomeGoldCheckInView extends RelativeLayout {
    public final static String TAG = "HomeGoldCheckInView";
    RecyclerView mRecyclerView;
    ExTextView mTvNum;
    TextView mTvGold;
    ExTextView mIvReceive;
    ExTextView mTvCover;
    ImageView mOk;
    ImageView mIvSevenGoldBj;
    RelativeLayout mRlSevendayBj;
    View mTvSevenCover;
    HomeGoldCheckinAdapter mHomeGoldCheckinAdapter;

    public HomeGoldCheckInView(Context context) {
        super(context);
        init();
    }

    public HomeGoldCheckInView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HomeGoldCheckInView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.home_gold_checkin_view_layout, this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mTvNum = (ExTextView) findViewById(R.id.tv_num);
        mTvGold = (TextView) findViewById(R.id.tv_gold);
        mIvReceive = findViewById(R.id.iv_receive);
        mTvCover = (ExTextView) findViewById(R.id.tv_cover);
        mOk = (ImageView) findViewById(R.id.ok);
        mIvSevenGoldBj = (ImageView) findViewById(R.id.iv_seven_gold_bj);
        mRlSevendayBj = (RelativeLayout) findViewById(R.id.rl_sevenday_bj);
        mTvSevenCover = (ExTextView) findViewById(R.id.tv_seven_cover);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mHomeGoldCheckinAdapter = new HomeGoldCheckinAdapter();
        mRecyclerView.setAdapter(mHomeGoldCheckinAdapter);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(U.getDisplayUtils().dip2px(10)));
    }

    public View getIvReceive() {
        return mIvReceive;
    }

    public void setData(List<HomeGoldModel> homeGoldModelList) {
        mHomeGoldCheckinAdapter.setDataList(homeGoldModelList);
    }

    public void setSevenDayInfo(HomeGoldModel homeGoldModel) {
        if (homeGoldModel.getState() == 1) {
            mIvSevenGoldBj.setImageDrawable(U.getDrawable(R.drawable.sanshijinbi_dangtian));
            mRlSevendayBj.setBackground(U.getDrawable(R.drawable.qiandao_diqitiandangtianbj));
        } else {
            mIvSevenGoldBj.setImageDrawable(U.getDrawable(R.drawable.sanshijinbi_moren));
            mRlSevendayBj.setBackground(U.getDrawable(R.drawable.qiandao_diqitianbj));
        }

        //1 可签到。 2 已签到。3 过期。4 时候未到
        if (homeGoldModel.getState() == 1) {
            mTvSevenCover.setVisibility(GONE);
        } else if (homeGoldModel.getState() == 2) {
            mTvSevenCover.setVisibility(VISIBLE);
        } else if (homeGoldModel.getState() == 3) {
            mTvSevenCover.setVisibility(VISIBLE);
        } else if (homeGoldModel.getState() == 4) {
            mTvSevenCover.setVisibility(GONE);
        }

        HomeGoldModel.BonusesBean bonusesBean = homeGoldModel.getCoinBonuses();
        if (bonusesBean == null) {
            MyLog.e(TAG, "bonusesBean is null");
            return;
        }

        mTvGold.setText(bonusesBean.getAmount() + "金币");
    }

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //不是第一个的格子都设一个左边和底部的间距
            outRect.left = 0;
            outRect.bottom = space;
            //由于每行都只有3个，所以第一个都是3的倍数，把左边距设为0
            if (parent.getChildLayoutPosition(view) > 2) {
                outRect.bottom = 0;
            }
        }

    }
}
