package com.wali.live.modulechannel.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.utils.U;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

import java.util.List;

/**
 * Created by zhaomin on 17-3-8.
 *
 * @module 带有折叠功能的view 内容可以随意填充
 */
public class FoldView extends RelativeLayout {

    public static final int STATE_INVISIABLE = 0;
    public static final int STATE_FOLD = 1;
    public static final int STATE_UNFOLD = -1;
    private static final int LEFT_PADDING = U.getDisplayUtils().dip2px(10);
    private static final int TOP_PADDING = U.getDisplayUtils().dip2px(10);

    private IFoldViewContent mVariableLengthView;
    private View mBottomControllerView;
    private View mBottomSplitLine;
    private TextView mHint;
    private ImageView mArrow;
    private int mState;
    private int mShortLength;
    private int mFullLength;

    public FoldView(Context context) {
        super(context);
    }

    public FoldView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FoldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addContentView(View variableLengthView) {
        if (variableLengthView == null || !(variableLengthView instanceof IFoldViewContent)) {
            return;
        }
        removeAllViews();
        setPadding(LEFT_PADDING, TOP_PADDING, LEFT_PADDING, 0);
        variableLengthView.setId(U.getCommonUtils().generateViewId());
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(variableLengthView, lp);
        mVariableLengthView = (IFoldViewContent) variableLengthView;
        View view = inflate(getContext(), R.layout.channel_fold_view_layout, null);
        LayoutParams lpBottom = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpBottom.addRule(BELOW, variableLengthView.getId());
        addView(view, lpBottom);
        mBottomControllerView = view.findViewById(R.id.more);
        mBottomSplitLine = view.findViewById(R.id.split_area);
        mHint = (TextView) view.findViewById(R.id.hint);
        mArrow = (ImageView) view.findViewById(R.id.hint_arrow);
        mBottomControllerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });
    }

    private void toggle() {
        if (mState == STATE_FOLD) {
            showAnimation(true);
        } else if (mState == STATE_UNFOLD) {
            showAnimation(false);
        }
        mState = -mState;
    }


    public void bindData(final List<ChannelLiveViewModel.RichText> dataList) {
        if (mVariableLengthView == null) {
            return;
        }
        mVariableLengthView.bindData(dataList);
        refreshVariableLengthView();
    }


    /**
     * 绑定数据后， 刷新底部布局
     */
    private void refreshVariableLengthView() {
        if (mVariableLengthView == null) {
            return;
        }
        mShortLength = mVariableLengthView.getShortHeight();
        mFullLength = mVariableLengthView.getFullHeight();
        if (mVariableLengthView.needFold()) {
            mState = STATE_FOLD;
            updateBottomHint();
            if (mBottomControllerView != null) {
                mBottomControllerView.setVisibility(VISIBLE);
            }
            if (mBottomSplitLine != null) {
                mBottomSplitLine.setVisibility(GONE);
            }
        } else {
            mState = STATE_INVISIABLE;
            if (mBottomControllerView != null) {
                mBottomControllerView.setVisibility(GONE);
            }
            if (mBottomSplitLine != null) {
                mBottomSplitLine.setVisibility(VISIBLE);
            }
        }
        initContentHeight();
    }

    /**
     * 滑出屏幕后， 重置为折叠状态
     */
    public void resetToFoldState() {
        if (mState == STATE_UNFOLD) {
            toggle();
        }
    }

    /**
     * 初始化高度   STATE_INVISIABLE时 高度自适应，其他状态高度是折叠时的高度
     */
    private void initContentHeight() {
        MarginLayoutParams layoutParams = (MarginLayoutParams) ((View) mVariableLengthView).getLayoutParams();
        layoutParams.height = mState == STATE_INVISIABLE ? LayoutParams.WRAP_CONTENT : mShortLength;
        ((View) mVariableLengthView).setLayoutParams(layoutParams);
    }

    /**
     * 更新底部展示文字
     */
    private void updateBottomHint() {
        if (mState == STATE_FOLD) {
            mHint.setText(getContext().getResources().getString(R.string.channel_fold_view_show_all));
            mArrow.setBackground(getContext().getResources().getDrawable(R.drawable.channel_down_arrow_bg));
        } else if (mState == STATE_UNFOLD) {
            mHint.setText(getContext().getResources().getString(R.string.channel_pack_up));
            mArrow.setBackground(getContext().getResources().getDrawable(R.drawable.home_up_arrow));
        }
    }

    /**
     * 展开收起动画
     *
     * @param shortToHeight 是否从短变长
     */
    private void showAnimation(boolean shortToHeight) {
        ValueAnimator animator = null;
        if (shortToHeight) {
            animator = ValueAnimator.ofInt(mShortLength, mFullLength);
        } else {
            animator = ValueAnimator.ofInt(mFullLength, mShortLength);
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) animation.getAnimatedValue();
                MarginLayoutParams layoutParams = (MarginLayoutParams) ((View) mVariableLengthView).getLayoutParams();
                layoutParams.height = height;
                ((View) mVariableLengthView).setLayoutParams(layoutParams);
            }
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                updateBottomHint();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(200);
        animator.start();
    }

}
