package com.wali.live.watchsdk.component.view.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.UserAccountManager;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IOrientationListener;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.view.PkScoreView;

/**
 * Created by yangli on 2017/09/11.
 * <p>
 * Generated using create_panel_with_presenter.py
 *
 * @module PK信息面板视图
 */
public class PkInfoPanel extends BaseBottomPanel<LinearLayout, RelativeLayout>
        implements View.OnClickListener, IComponentView<PkInfoPanel.IPresenter, PkInfoPanel.IView> {
    private static final String TAG = "PkInfoPanel";

    @Nullable
    protected IPresenter mPresenter;

    private ImageView mLeftResultView;
    private ImageView mRightResultView;
    private View mMiddleResultView;

    private TextView mPkTypeView;
    private TextView mTimeAreaView;
    private SimpleDraweeView mAnchorLeft;
    private SimpleDraweeView mAnchorRight;
    private TextView mTicketLeft;
    private TextView mTicketRight;
    private PkScoreView mPkScoreView;

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.pk_info_panel;
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public PkInfoPanel(@NonNull RelativeLayout parentView) {
        super(parentView);
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();
        mLeftResultView = $(R.id.left_result);
        mRightResultView = $(R.id.right_result);
        mMiddleResultView = $(R.id.middle_result);

        mPkTypeView = $(R.id.pk_type);
        mTimeAreaView = $(R.id.time_area);
        mAnchorLeft = $(R.id.anchor_1);
        mAnchorRight = $(R.id.anchor_2);
        mTicketLeft = $(R.id.ticket_1);
        mTicketRight = $(R.id.ticket_2);
        mPkScoreView = $(R.id.pk_score_view);

        AvatarUtils.loadAvatarByUidTs(mAnchorLeft, UserAccountManager.getInstance().getUuidAsLong(),
                0, AvatarUtils.SIZE_TYPE_AVATAR_SMALL, true);
        AvatarUtils.loadAvatarByUidTs(mAnchorRight, UserAccountManager.getInstance().getUuidAsLong(),
                0, AvatarUtils.SIZE_TYPE_AVATAR_SMALL, true);
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentView;
            }

            @Override
            public void showSelf(boolean useAnimation, boolean isLandscape) {
                PkInfoPanel.this.showSelf(useAnimation, isLandscape);
            }

            @Override
            public void hideSelf(boolean useAnimation) {
                PkInfoPanel.this.hideSelf(useAnimation);
            }

            @Override
            public void onPkStart(String pkType, long uuid1, long uuid2) {
                mPkTypeView.setText(pkType);
                AvatarUtils.loadAvatarByUidTs(mAnchorLeft, uuid1, 0, AvatarUtils.SIZE_TYPE_AVATAR_SMALL, true);
                AvatarUtils.loadAvatarByUidTs(mAnchorRight, uuid2, 0, AvatarUtils.SIZE_TYPE_AVATAR_SMALL, true);
            }

            @Override
            public void onUpdateRemainTime(long remainTime) {
                mTimeAreaView.setText(String.format("%02d:%02d", remainTime / 60, remainTime % 60));
            }

            @Override
            public void onUpdateScoreInfo(long ticket1, long ticket2) {
                mTicketLeft.setText(String.valueOf(ticket1));
                mTicketRight.setText(String.valueOf(ticket2));
                mPkScoreView.updateRatio(ticket1, ticket2);
            }

            @Override
            public void onPkEnd(boolean ownerWin, long ticket1, long ticket2) {
                onUpdateScoreInfo(ticket1, ticket2);
                if (ownerWin) {
                    mLeftResultView.setVisibility(View.VISIBLE);
                    mRightResultView.setVisibility(View.VISIBLE);
                    mLeftResultView.setImageResource(R.drawable.live_img_pk_win);
                    mRightResultView.setImageResource(R.drawable.live_img_pk_lost);
                } else {
                    mLeftResultView.setVisibility(View.VISIBLE);
                    mRightResultView.setVisibility(View.VISIBLE);
                    mLeftResultView.setImageResource(R.drawable.live_img_pk_lost);
                    mRightResultView.setImageResource(R.drawable.live_img_pk_win);
                }
            }

            @Override
            public void onPkEnd(long ticket1, long ticket2) {
                if (ticket1 == ticket2) {
                    onUpdateScoreInfo(ticket1, ticket2);
                    mMiddleResultView.setVisibility(View.VISIBLE);
                } else {
                    onPkEnd(ticket1 > ticket2, ticket1, ticket2);
                }
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                PkInfoPanel.this.onOrientation(isLandscape);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy, IOrientationListener {

        void showSelf(boolean useAnimation, boolean isLandscape);

        void hideSelf(boolean useAnimation);

        void onPkStart(String pkType, long uuid1, long uuid2);

        void onUpdateRemainTime(long remainTime);

        void onUpdateScoreInfo(long ticket1, long ticket2);

        /**
         * Pk提前结束
         *
         * @param ownerWin 本房主获胜
         * @param ticket1  本房主得分
         * @param ticket2  对方得分
         */
        void onPkEnd(boolean ownerWin, long ticket1, long ticket2);

        /**
         * Pk正常结束，此时以分数定输赢
         *
         * @param ticket1 本房主得分
         * @param ticket2 对方得分
         */
        void onPkEnd(long ticket1, long ticket2);
    }
}
