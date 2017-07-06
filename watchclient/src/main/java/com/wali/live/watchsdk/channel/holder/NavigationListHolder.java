package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNavigateViewModel;

import java.util.List;


/**
 * Created by zhaomin on 16-12-23.
 */
public class NavigationListHolder extends RepeatHolder {

    private int mIconId;
    private int mTextId;
    private int mDividerId;

    private LinearLayout[] mLinearLayouts;
    private View mSplitLine;

    public NavigationListHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mIconId = R.id.icon;
        mTextId = R.id.text;
        mDividerId = R.id.inner_divider_line;
    }

    @Override
    protected boolean needTitleView() {
        return false;
    }

    @Override
    protected boolean isCircle() {
        return false;
    }

    @Override
    protected void initContentView() {
        initContentViewId();
        mLinearLayouts = new LinearLayout[2];
        mLinearLayouts[0] = $(R.id.ll_container1);
        mLinearLayouts[1] = $(R.id.ll_container2);
        mViewSize = 2 * 4;
        mParentViews = new ViewGroup[mViewSize];
        mTextViews = new TextView[mViewSize];
        mImageViews = new BaseImageView[mViewSize];
        mSplitLine = $(R.id.split_line);
        for (int i = 0; i < 2; i++) {
            mParentViews[i * 4] = $(mLinearLayouts[i], R.id.item_1);
            mParentViews[i * 4 + 1] = $(mLinearLayouts[i], R.id.item_2);
            mParentViews[i * 4 + 2] = $(mLinearLayouts[i], R.id.item_3);
            mParentViews[i * 4 + 3] = $(mLinearLayouts[i], R.id.item_4);
        }

        for (int i = 0; i < mParentViews.length; i++) {
            mImageViews[i] = $(mParentViews[i], mIconId);
            mTextViews[i] = $(mParentViews[i], mTextId);
        }
    }

    @Override
    protected void bindNavigateModel(ChannelNavigateViewModel viewModel) {
        List<ChannelNavigateViewModel.NavigateItem> itemDatas = viewModel.getItemDatas();
        int minSize = Math.min(mViewSize, itemDatas.size());
        for (int i = minSize; i < mViewSize; i++) {
            mParentViews[i].setVisibility(View.INVISIBLE);
        }
        // 只有一行
        if (minSize < 5) {
            mLinearLayouts[1].setVisibility(View.GONE);
            mSplitLine.setVisibility(View.GONE);
        }
        for (int i = 0; i < minSize; i++) {
            mParentViews[i].setVisibility(View.VISIBLE);
            if (i == 3 || i == 7 || i == minSize - 1) {
                mParentViews[i].findViewById(mDividerId).setVisibility(View.INVISIBLE);
            }
            ChannelNavigateViewModel.NavigateItem item = itemDatas.get(i);
            if (item != null) {
                MyLog.i(TAG, "bindNavigateModel " + item.getIconUrl() + " txt: " + item.getText());
                if (!TextUtils.isEmpty(item.getIconUrl())) {
                    mImageViews[i].setVisibility(View.VISIBLE);
                    FrescoWorker.loadImage(mImageViews[i],
                            ImageFactory.newHttpImage(item.getIconUrl())
                                    .setIsCircle(false)
                                    .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                                    .setWidth(50)
                                    .setHeight(50)
                                    .build()
                    );
                }
                mParentViews[i].setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //scheme jump
                            }
                        });
                mTextViews[i].setText(item.getText());

                bindItemOnNavigateModel(item, i);
            }
//            exposureItem(item);
        }
    }
}
