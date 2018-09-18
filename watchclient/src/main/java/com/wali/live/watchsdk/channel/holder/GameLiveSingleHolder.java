package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.utils.display.DisplayUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

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
                        .setCornerRadius(DisplayUtils.dip2px(5.33f))
                        .setLoadingDrawable(GlobalData.app().getResources().getDrawable(R.drawable.live_show_avatar_loading))
                        .setFailureDrawable(GlobalData.app().getResources().getDrawable(R.drawable.live_show_avatar_loading))
                        .build()
        );

        if (item.getUser() != null) {
            bindText(mLiveAnchor, item.getUser().getNickname());
        }

        bindText(mLiveViewer, parseCountString(item.getViewerCnt()));

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpItem(item);
            }
        });
    }

    public static String parseCountString(int count) {
        String sCount = String.valueOf(count);
        if (count > 10000) {
            String unit = "w";
            sCount = String.format("%.1f" + unit, (float) (count / 10000.0));
        }
        return sCount;
    }

    private void setItemSelected(boolean isSelected) {
        mCoverContainer.setBackground(isSelected ? GlobalData.app().getResources().getDrawable(R.drawable.live_cover_border_bg) : null);
        mLiveTitle.setSelected(isSelected);
        mLiveAnchor.setSelected(isSelected);
        mLiveViewer.setSelected(isSelected);
    }
}
