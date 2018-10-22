package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.utils.U;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;
import com.wali.live.modulechannel.view.LiveCardContainer;

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
        int width = U.getDisplayUtils().getScreenWidth() - SIDE_MARGIN * 2;
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
