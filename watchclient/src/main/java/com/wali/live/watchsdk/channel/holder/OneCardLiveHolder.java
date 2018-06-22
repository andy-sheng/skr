package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.video.widget.player.VideoPlayerTextureView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.util.HolderUtils;
import com.wali.live.watchsdk.channel.view.BannerVideoView;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

import java.util.List;

//46.一行一列，播放直播画面
public class OneCardLiveHolder extends FixedHolder {

    private float RATIO = 1.44f;
    private RelativeLayout mVideoContainer;
    private BannerVideoView mVideoView;
    private TextView mNameTv;
    private TextView mCountTv;
    private TextView mLeftLabelTv;
    private ImageView mVolumeIv;


    public OneCardLiveHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mVideoContainer = $(R.id.video_container);
        mVideoView = $(R.id.video_view);
        mNameTv = $(R.id.name_tv);
        mCountTv = $(R.id.count_tv);
        mLeftLabelTv = $(R.id.left_label_tv);
        mVolumeIv = mVideoView.getVolumeBtn();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mVolumeIv.getLayoutParams();
        layoutParams.bottomMargin = DisplayUtils.dip2px(36.67f);
        mVolumeIv.setLayoutParams(layoutParams);
        mVideoView.setVideoTransMode(VideoPlayerTextureView.TRANS_MODE_CENTER_CROP);
        itemView.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup parent = (ViewGroup) itemView.getParent();
                if (null != parent) {
                    int width = parent.getWidth();
                    int height = parent.getHeight();
                    MyLog.w(TAG, "parent width=" + width + ",height=" + height);
                    RATIO = height * 1.0f / width;
                    changeSize();
                }
            }
        });
    }

    private void changeSize() {
        int cardWidth = DisplayUtils.getPhoneWidth();
        int cardHeight = (int) (cardWidth * RATIO);
        ViewGroup.LayoutParams params = mVideoContainer.getLayoutParams();
        params.width = cardWidth;
        params.height = cardHeight;
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        super.bindLiveModel(viewModel);
        List<ChannelLiveViewModel.BaseItem> items = viewModel.getItemDatas();
        if (items != null && items.size() > 0) {
            final ChannelLiveViewModel.BaseItem item = items.get(0);
            if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
                exposureItem(item);
                mVideoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpItem(item);
                        mVideoView.enterWatch();
                    }
                });
                mVideoView.bindData((ChannelLiveViewModel.BaseLiveItem) item);
                HolderUtils.bindLeftLabel(item, mLeftLabelTv);
                mLeftLabelTv.setPadding(DisplayUtils.dip2px(7), 0, DisplayUtils.dip2px(10), 0);
                bindText(mNameTv, item.getUserNickName());
                bindText(mCountTv, ((ChannelLiveViewModel.BaseLiveItem) item).getCountString());
            } else {
                MyLog.w(TAG, "OneCardLiveHolder receive not liveitem.");
                return;
            }
        }
    }


    @Override
    protected void postChannelData() {
        if (mVideoView != null) {
            mVideoView.postInit(mChannelId);
        }
    }
}
