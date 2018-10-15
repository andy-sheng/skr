package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelShowViewModel;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 列表样式，左图右文的item
 */
public class OneListHolder extends RepeatHolder {
    private int[] mAvatarIvIds;
    private int[] mNameTvIds;
    private int[] mUserContainerIds;
    private BaseImageView[] mAvatarIvs;
    private TextView[] mNameTvs;
    private View[] mUserContainers;

    private View mListSplitLine;

    public OneListHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mViewSize = 1;
        mParentIds = new int[]{
                R.id.content_area,
        };
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.single_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.single_tv);
        mAvatarIvIds = new int[mViewSize];
        Arrays.fill(mAvatarIvIds, R.id.avatar_iv);
        mNameTvIds = new int[mViewSize];
        Arrays.fill(mNameTvIds, R.id.name_tv);
        mUserContainerIds = new int[mViewSize];
        Arrays.fill(mUserContainerIds, R.id.user_container);
    }

    @Override
    protected void initContentView() {
        super.initContentView();

        mAvatarIvs = new BaseImageView[mViewSize];
        mNameTvs = new TextView[mViewSize];
        mUserContainers = new View[mViewSize];

        for (int i = 0; i < mViewSize; i++) {
            mAvatarIvs[i] = $(mAvatarIvIds[i]);
            mNameTvs[i] = $(mNameTvIds[i]);
            mUserContainers[i] = $(mUserContainerIds[i]);
        }

        mListSplitLine = $(R.id.list_split_line);
    }

    @Override
    protected boolean isChangeImageSize() {
        return true;
    }

    @Override
    protected int getImageWidth() {
        return DisplayUtils.dip2px(80f);
    }

    @Override
    protected int getImageHeight() {
        return DisplayUtils.dip2px(80f);
    }

    @Override
    protected boolean isCircle() {
        return false;
    }

    @Override
    protected void bindShowModel(ChannelShowViewModel viewModel) {
        super.bindShowModel(viewModel);
        if (mViewModel.isFirst()) {
            mSplitLine.setVisibility(View.VISIBLE);
            mListSplitLine.setVisibility(View.VISIBLE);
        } else if (mViewModel.isLast()) {
            mSplitLine.setVisibility(View.GONE);
            mListSplitLine.setVisibility(View.GONE);
        } else {
            mSplitLine.setVisibility(View.GONE);
            mListSplitLine.setVisibility(View.VISIBLE);
        }
    }

    protected void bindItemOnShowModel(ChannelShowViewModel.OneTextItem item, int i) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (item.getUser() != null) {
            mNameTvs[i].setText(item.getUser().getNickname());
            mUserContainers[i].setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //跳转个人主页页面
                        }
                    }
            );
        }
    }
}
