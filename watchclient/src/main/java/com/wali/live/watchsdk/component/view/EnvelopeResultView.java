package com.wali.live.watchsdk.component.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.mi.live.data.gift.redenvelope.RedEnvelopeModel;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.adapter.WinnerItemAdapter;
import com.wali.live.watchsdk.component.presenter.EnvelopePresenter;

import java.util.List;

/**
 * Created by wmj on 17-7-13.
 *
 * @module 抢红包
 */
public class EnvelopeResultView extends BaseBottomPanel<RelativeLayout, RelativeLayout>
        implements View.OnClickListener {
    private static final String TAG = "EnvelopeResultView";

    private static final int MODE_NONE = -1;
    private static final int MODE_RESULT = 0;
    private static final int MODE_DETAIL = 1;

    private EnvelopePresenter.EnvelopeInfo mEnvelopeInfo;
    private IPresenter mPresenter;
    private int mMode = MODE_NONE;

    // 红包信息相关
    private BaseImageView mSenderAvatarIv;
    private ImageView mUserBadgeIv;
    private TextView mInfoTv;

    // 红包结果相关
    private View mResultView;
    private TextView mDiamondNumTv;
    private ImageView mDiamondIv;
    private TextView mTipsTv;
    private TextView mEmptyTipsTv;

    // 红包详细信息相关
    private View mDetailView;
    private View mAnchorTips;
    private View mAnchorItem;
    private RecyclerView mRecyclerView;
    private final WinnerItemAdapter mWinnerAdapter = new WinnerItemAdapter();

    protected <T> T $(View view, int id) {
        return (T) view.findViewById(id);
    }

    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public EnvelopeResultView(@NonNull RelativeLayout parentView) {
        super(parentView);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.red_envelope_result_view;
    }

    @Override
    public void onClick(View view) {
        if (mPresenter == null) {
            return;
        }
        int id = view.getId();
        if (id == R.id.close_iv) {
            mPresenter.removeEnvelope(mEnvelopeInfo);
        } else if (id == R.id.look_other_btn) {
            mPresenter.syncEnvelopeDetail(mEnvelopeInfo);
        }
    }

    public void setEnvelopeInfo(EnvelopePresenter.EnvelopeInfo envelopeInfo) {
        mEnvelopeInfo = envelopeInfo;
        if (envelopeInfo == null || envelopeInfo.envelopeModel == null || mContentView == null) {
            return;
        }
        MyLog.w(TAG, "setEnvelopeInfo");
        RedEnvelopeModel model = envelopeInfo.envelopeModel;
        AvatarUtils.loadAvatarByUidTs(mSenderAvatarIv, model.getUserId(), model.getAvatarTimestamp(), true);
        mUserBadgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(model.getLevel()));
        mInfoTv.setText(model.getMsg());
        if (mMode != MODE_RESULT) {
            mMode = MODE_RESULT;
            mDetailView.setVisibility(View.GONE);
            mResultView.setVisibility(View.VISIBLE);
        }
        if (mEnvelopeInfo.grabCnt > 0) {
            mDiamondNumTv.setVisibility(View.VISIBLE);
            mDiamondIv.setVisibility(View.VISIBLE);
            mTipsTv.setVisibility(View.VISIBLE);
            mEmptyTipsTv.setVisibility(View.GONE);
            mDiamondNumTv.setText(String.valueOf(mEnvelopeInfo.grabCnt));
        } else {
            mDiamondNumTv.setVisibility(View.GONE);
            mDiamondIv.setVisibility(View.GONE);
            mTipsTv.setVisibility(View.GONE);
            mEmptyTipsTv.setVisibility(View.VISIBLE);
        }
    }

    public void onEnvelopeDetail(
            EnvelopePresenter.EnvelopeInfo envelopeInfo,
            WinnerItemAdapter.WinnerItem anchorItem,
            long bestId,
            List<WinnerItemAdapter.WinnerItem> otherInfo) {
        if (mContentView == null || envelopeInfo == null || envelopeInfo != mEnvelopeInfo) {
            return;
        }
        MyLog.w(TAG, "onEnvelopeDetail");
        mWinnerAdapter.setItemData(otherInfo, bestId);
        if (mMode != MODE_DETAIL) {
            mMode = MODE_DETAIL;
            mDetailView.setVisibility(View.VISIBLE);
            mResultView.setVisibility(View.GONE);
        }
        if (anchorItem != null) {
            mAnchorTips.setVisibility(View.VISIBLE);
            mAnchorItem.setVisibility(View.VISIBLE);
            new WinnerItemAdapter.WinnerHolder(mAnchorItem).bindView(anchorItem, bestId);
        } else {
            mAnchorTips.setVisibility(View.GONE);
            mAnchorItem.setVisibility(View.GONE);
        }
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();

        mSenderAvatarIv = $(R.id.sender_avatar_iv);
        mUserBadgeIv = $(R.id.user_badge_iv);
        mInfoTv = $(R.id.info_tv);

        // 红包结果相关
        mResultView = $(R.id.result_view);
        mDiamondNumTv = $(mResultView, R.id.diamond_num_tv);
        mDiamondIv = $(mResultView, R.id.diamond_iv);
        mTipsTv = $(mResultView, R.id.tips_tv);
        mEmptyTipsTv = $(mResultView, R.id.empty_tips_tv);

        // 红包详细信息相关
        mDetailView = $(R.id.detail_view);
        mAnchorTips = $(mDetailView, R.id.anchor_tips);
        mAnchorItem = $(mDetailView, R.id.anchor_item);
        mRecyclerView = $(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mParentView.getContext()));
        mRecyclerView.setAdapter(mWinnerAdapter);

        $click(R.id.close_iv, this);
        $click(R.id.look_other_btn, this);

        setEnvelopeInfo(mEnvelopeInfo);
    }

    @Override
    public void showSelf(boolean useAnimation, boolean isLandscape) {
        super.showSelf(useAnimation, isLandscape);
        mContentView.bringToFront();
    }

    @Override
    protected void orientSelf() {
    }

    public interface IPresenter {
        /**
         * 关闭红包
         */
        void removeEnvelope(EnvelopePresenter.EnvelopeInfo envelopeInfo);

        /**
         * 获取抢红结果详情
         */
        void syncEnvelopeDetail(EnvelopePresenter.EnvelopeInfo envelopeInfo);
    }
}
