package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;
import com.wali.live.modulechannel.view.VideoCollectionCardView;

//type=47,一行两列，直播与视频集的随意组合
public class LiveOrVideoCollectionHolder extends TwoCardHolder {

    private VideoCollectionCardView[] mVideoCollectionCards;

    public LiveOrVideoCollectionHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void newContentView() {
        super.newContentView();
        mVideoCollectionCards = new VideoCollectionCardView[mViewSize];
    }

    @Override
    protected void resetItem(int i) {
        super.resetItem(i);
        if (mVideoCollectionCards[i] != null) {
            mVideoCollectionCards[i].setVisibility(View.GONE);
        }
    }

    @Override
    protected void bindImageItem(ChannelLiveViewModel.ImageItem item, int index) {
        MyLog.d(TAG, "bindImageItem "+ index);
        if (mVideoCollectionCards[index] == null) {
            mVideoCollectionCards[index] = new VideoCollectionCardView(itemView.getContext());
            if (mContentViews[index] != null) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, mIvIds[index]);
                mContentViews[index].addView(mVideoCollectionCards[index], layoutParams);
            }
        }
        mVideoCollectionCards[index].setVisibility(View.VISIBLE);
        mVideoCollectionCards[index].bindData(item);
    }
}
