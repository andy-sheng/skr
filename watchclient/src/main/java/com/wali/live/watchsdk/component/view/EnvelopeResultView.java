package com.wali.live.watchsdk.component.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.adapter.EnvelopeItemAdapter;
import com.wali.live.watchsdk.component.presenter.EnvelopePresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wmj on 17-7-13.
 *
 * @module 抢红包
 */
public class EnvelopeResultView extends BaseBottomPanel<RelativeLayout, RelativeLayout>
        implements View.OnClickListener {
    private static final String TAG = "EnvelopeResultView";

    private EnvelopePresenter.EnvelopeInfo mEnvelopeInfo;
    private IPresenter mPresenter;

    private View mBottomView;
    private BaseImageView mSenderAvatarIv;
    private ImageView mUserBadgeIv;
    private TextView mInfoTv;
    private TextView mDiamondNumTv;
    private ImageView mDiamondIv;
    private TextView mTipsTv;
    private TextView mLookOtherBtn;
    private TextView mEmptyTipsTv;
    private TextView mTopAnchorTv;
    private View mLeftLine;
    private View mRightLine;
    private View mAnchorItem;
    private ViewGroup mTopContainer;
    private View mTopInfoTv;

    private RecyclerView mRecyclerView;
    private EnvelopeItemAdapter mEnvelopeItemAdapter;

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
            // TODO 加入点击事件响应
        } else if (id == R.id.look_other_btn) {
            testItems();
            // mPresenter.syncEnvelopeDetail();
        }
    }

    private void testItems() {
        List<EnvelopeItemAdapter.WinnerItem> envelopeItem = new ArrayList<>();
        Collections.addAll(envelopeItem,
                new EnvelopeItemAdapter.WinnerItem("test0", 111, 0l, false),
                new EnvelopeItemAdapter.WinnerItem("test1", 111, 1l, false),
                new EnvelopeItemAdapter.WinnerItem("test2", 112, 2l, false),
                new EnvelopeItemAdapter.WinnerItem("test3", 113, 3l, false),
                new EnvelopeItemAdapter.WinnerItem("test4", 114, 4l, false),
                new EnvelopeItemAdapter.WinnerItem("test5", 115, 5l, false),
                new EnvelopeItemAdapter.WinnerItem("test6", 116, 6l, false),
                new EnvelopeItemAdapter.WinnerItem("test7", 117, 7l, false),
                new EnvelopeItemAdapter.WinnerItem("test8", 118, 8l, false),
                new EnvelopeItemAdapter.WinnerItem("test9", 119, 9l, true));
        mEnvelopeItemAdapter.setItemData(envelopeItem);
    }

    public void setEnvelopeInfo(EnvelopePresenter.EnvelopeInfo envelopeInfo) {
        mEnvelopeInfo = envelopeInfo;
        if (envelopeInfo == null || mContentView == null) {
            return;
        }
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();

        mBottomView = $(R.id.bottom_view);
        mSenderAvatarIv = $(R.id.sender_avatar_iv);
        mUserBadgeIv = $(R.id.user_badge_iv);
        mInfoTv = $(R.id.info_tv);
        mDiamondNumTv = $(R.id.diamond_num_tv);
        mDiamondIv = $(R.id.diamond_iv);
        mTipsTv = $(R.id.tips_tv);
        mEmptyTipsTv = $(R.id.empty_tip_tv);
        mTopAnchorTv = $(R.id.top_anchor_tv);
        mLeftLine = $(R.id.left_line);
        mRightLine = $(R.id.right_line);
        mAnchorItem = $(R.id.anchor_item);
        mTopContainer = $(R.id.top_container);
        mTopInfoTv = $(R.id.top_info_tv);
        mRecyclerView = $(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mParentView.getContext()));
        mEnvelopeItemAdapter = new EnvelopeItemAdapter();
        mRecyclerView.setAdapter(mEnvelopeItemAdapter);

        $click(R.id.close_iv, this);
        $click(R.id.look_other_btn, this);

        setEnvelopeInfo(mEnvelopeInfo);
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
