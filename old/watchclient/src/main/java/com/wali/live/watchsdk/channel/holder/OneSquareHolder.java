package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.view.ViewGroup;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.view.LiveCardContainer;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

import java.util.List;

/**
 * 一个方图卡片
 */
public class OneSquareHolder extends FixedHolder {

    private LiveCardContainer mCardContainer;

    private View mContentView;

    public OneSquareHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mContentView = $(R.id.content_area);
        int width = DisplayUtils.getScreenWidth() - SIDE_MARGIN * 2;
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mContentView.getLayoutParams();
        layoutParams.height = width;
        mContentView.setLayoutParams(layoutParams);
        mCardContainer = new LiveCardContainer(mContentView);
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        List<ChannelLiveViewModel.BaseItem> itemDatas = viewModel.getItemDatas();
        if (itemDatas != null && itemDatas.size() > 0) {
            final ChannelLiveViewModel.BaseItem item = itemDatas.get(0);
            if (item != null) {
                MyLog.d(TAG, "bindLiveModel imageUrl : " + item.getImageUrl());
                mCardContainer.resetItem();
                mCardContainer.bindItemOnLiveModel(item);
                mContentView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpItem(item);
                    }
                });
                exposureItem(item);
            }
        }
    }
}
