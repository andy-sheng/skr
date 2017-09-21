package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelViewModel;

import static android.view.View.GONE;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 显示head的抽象基类
 */
public abstract class HeadHolder extends BaseHolder<ChannelViewModel> {

    public final int HEAD_TYPE_LEFT = 2; // title样式 ,表示标题和箭头在左上角
    public final int HEAD_TYPE_HEAD_ICON = 3; // title样式 ,header icon

    protected View mHeadArea;
    protected BaseImageView mHeadIv;
    protected TextView mHeadTv;
    protected TextView mSubHeadTv;
    protected TextView mMoreTv;
    protected View mSplitLine;
    protected View mSplitArea;

    public HeadHolder(View itemView) {
        super(itemView);
    }

    protected boolean needTitleView() {
        return true;
    }

    protected void initView() {
        if (needTitleView()) {
            initTitleView();
        }
        initContentView();
    }

    protected void initTitleView() {
        mHeadArea = $(R.id.title_area);
        if (mHeadArea != null) {
            mHeadArea.getLayoutParams().height = DisplayUtils.dip2px(43.33f);
        }

        mHeadTv = $(R.id.head_tv);
        mHeadIv = $(R.id.head_iv);
        mSubHeadTv = $(R.id.sub_head_tv);
        mMoreTv = $(R.id.more_tv);
        mSplitLine = $(R.id.split_line);
        if (mSplitLine != null) {
            mSplitLine.setVisibility(View.GONE);
        }
        mSplitArea = $(R.id.split_area);
        if (mSplitArea != null) {
            mSplitArea.setVisibility(View.GONE);
        }
    }

    protected abstract void initContentView();

    @Override
    protected void bindView() {
        if (needTitleView()) {
            bindTitleView();
        }
    }

    private void bindTitleView() {
        // 处理分隔区域，现在由服务器下发分割线，所以不再需要特殊处理
        // 如果有标题，显示标题；有更多，显示更多
        if (mHeadArea == null) {
            return;
        }

        if (mViewModel.hasHead()) {
            mHeadArea.setVisibility(View.VISIBLE);
            mHeadTv.setText(mViewModel.getHead());
            mHeadIv.setVisibility(View.GONE);

            if (mViewModel.hasSubHead()) {
                mSubHeadTv.setVisibility(View.VISIBLE);
                mSubHeadTv.setText(mViewModel.getSubHead());
            } else {
                mSubHeadTv.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(mViewModel.getHeadUri())) {
                if (mViewModel.hasSubHead()) {
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            jumpMore();
                        }
                    });
                    mMoreTv.setVisibility(View.GONE);
                    mHeadTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.home_more, 0);
                    mHeadTv.setCompoundDrawablePadding(DisplayUtils.dip2px(3.33f));
                } else if (mViewModel.getHeadType() == HEAD_TYPE_LEFT) {
                    mMoreTv.setVisibility(View.GONE);
                    mHeadTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.more_right_arrow_bg, 0);
                    mHeadTv.setCompoundDrawablePadding(DisplayUtils.dip2px(1.33f));
                    mHeadTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            jumpMore();
                        }
                    });
                    itemView.setOnClickListener(null);
                } else {
                    mHeadTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    itemView.setOnClickListener(null);
                    if (!TextUtils.isEmpty(mViewModel.getHeadMoreText())) {
                        mMoreTv.setText(mViewModel.getHeadMoreText());
                    }
                    mMoreTv.setVisibility(View.VISIBLE);
                    mMoreTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            jumpMore();
                        }
                    });
                }
            } else {
                mMoreTv.setVisibility(View.GONE);
                itemView.setOnClickListener(null);
                mHeadTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            // 调整位置
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHeadTv.getLayoutParams();
            if (mViewModel.hasSubHead()) {
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                lp.addRule(RelativeLayout.CENTER_VERTICAL, 0);
                lp.leftMargin = 0;

                lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                lp.topMargin = DisplayUtils.dip2px(15f);

                mHeadArea.getLayoutParams().height = DisplayUtils.dip2px(66.67f);
            } else if (mViewModel.getHeadType() == HEAD_TYPE_HEAD_ICON && !TextUtils.isEmpty(mViewModel.getHeadIconUrl())) {
                //调整位置
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

                RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams) mHeadIv.getLayoutParams();
                lp1.leftMargin = DisplayUtils.dip2px(6.66f);
                lp1.width = DisplayUtils.dip2px(22);
                lp1.height = DisplayUtils.dip2px(22);
                FrescoWorker.loadImage(mHeadIv,
                        ImageFactory.newHttpImage(mViewModel.getHeadIconUrl())
                                .build());
                mHeadIv.setLayoutParams(lp1);
                mHeadIv.setVisibility(View.VISIBLE);

                lp.addRule(RelativeLayout.RIGHT_OF, R.id.head_iv);
                lp.leftMargin = DisplayUtils.dip2px(3.33f);
                mHeadTv.setLayoutParams(lp);

                mHeadArea.getLayoutParams().height = DisplayUtils.dip2px(43.33f);
            } else {
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
                lp.topMargin = 0;

                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                lp.leftMargin = DisplayUtils.dip2px(10f);

                mHeadArea.getLayoutParams().height = DisplayUtils.dip2px(43.33f);
            }
        } else {
            mHeadArea.setVisibility(View.GONE);
        }
    }

    private void jumpMore() {
        if (!TextUtils.isEmpty(mViewModel.getHeadUri())) {
            mJumpListener.jumpScheme(mViewModel.getHeadUri());
        } else {
            MyLog.e(TAG, "HeadHolder jumpMore uri is empty");
        }
    }
}
