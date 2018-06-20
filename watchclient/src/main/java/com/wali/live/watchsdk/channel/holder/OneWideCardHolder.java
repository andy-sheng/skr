package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.util.HolderUtils;
import com.wali.live.watchsdk.channel.view.LiveCardContainer;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

import java.util.List;

/**
 * 一个扁的直播卡片
 */
public class OneWideCardHolder extends FixedHolder{

    private LiveCardContainer mCardContainer;

    private View mContentView;

    private TextView mTitleTv;

    public OneWideCardHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mContentView = $(R.id.content_area);
        mTitleTv = $(R.id.title_tv);
        int width = DisplayUtils.getScreenWidth() - SIDE_MARGIN * 2;
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mContentView.getLayoutParams();
        layoutParams.height = (int) (width * 0.56f);
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
                mTitleTv.setVisibility(View.GONE);
                if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
                    HolderUtils.bindText(mTitleTv, ((ChannelLiveViewModel.BaseLiveItem) item).getTitleText());
                }
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
