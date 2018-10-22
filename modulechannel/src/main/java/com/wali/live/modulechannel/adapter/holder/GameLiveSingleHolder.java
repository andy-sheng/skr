package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

/**
 * Created by liuting on 18-9-11.
 * 游戏直播间横屏下　更多直播　单排频道列表　UiType = 66
 */

public class GameLiveSingleHolder extends FixedHolder {
    private LinearLayout mCoverContainer;
    private BaseImageView mCoverImage;
    private TextView mLiveTitle;
    private TextView mLiveAnchor;
    private TextView mLiveViewer;

    public GameLiveSingleHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mCoverContainer = $(R.id.cover_container);
        mCoverImage = $(R.id.live_cover);
        mLiveTitle = $(R.id.live_title);
        mLiveAnchor = $(R.id.live_anchor);
        mLiveViewer = $(R.id.live_viewer);
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel model) {
        final ChannelLiveViewModel.BaseItem item = model.getFirstItem();
        if (item == null) {
            return;
        }
        exposureItem(item);
        setItemSelected(false);
        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            bindBaseLiveItem((ChannelLiveViewModel.BaseLiveItem) item);
        }
    }

    protected void bindBaseLiveItem(final ChannelLiveViewModel.BaseLiveItem item) {
        bindText(mLiveTitle, item.getTitleText());
        FrescoWorker.loadImage(mCoverImage,
                ImageFactory.newHttpImage(item.getImageUrl())
                        .setIsCircle(false)
                        .setCornerRadius(U.getDisplayUtils().dip2px(5.33f))
                        .setLoadingDrawable(U.app().getResources().getDrawable(R.drawable.live_show_avatar_loading))
                        .setFailureDrawable(U.app().getResources().getDrawable(R.drawable.live_show_avatar_loading))
                        .build()
        );

        if (item.getUser() != null) {
            bindText(mLiveAnchor, item.getUser().getUserNickname());
        }

        if (item instanceof ChannelLiveViewModel.LiveItem) {
            bindText(mLiveViewer, parseCountString(((ChannelLiveViewModel.LiveItem) item).getHotScore()));
        } else {
            bindText(mLiveViewer, parseCountString(item.getViewerCnt()));
        }


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpItem(item);
            }
        });
    }

    private void setItemSelected(boolean isSelected) {
        mCoverContainer.setBackground(isSelected ? U.app().getResources().getDrawable(R.drawable.channel_live_cover_border_bg) : null);
        mLiveTitle.setSelected(isSelected);
        mLiveAnchor.setSelected(isSelected);
        mLiveViewer.setSelected(isSelected);
    }
}
