package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.wali.live.watchsdk.channel.view.LiveGroupCardView;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;


//48.一行两列，直播间和直播组的任意组合，四种情况
public class LiveOrLiveGroupHolder extends TwoCardHolder {

    private LiveGroupCardView[] mLiveCardGroup;

    public LiveOrLiveGroupHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void newContentView() {
        super.newContentView();
        mLiveCardGroup = new LiveGroupCardView[mViewSize];
    }

    @Override
    protected void resetItem(int i) {
        super.resetItem(i);
        if (mLiveCardGroup[i] != null) {
            mLiveCardGroup[i].setVisibility(View.GONE);
        }
    }

    @Override
    protected void bindLiveGroupItem(ChannelLiveViewModel.LiveGroupItem item, int index) {
        if (mLiveCardGroup[index] == null) {
            mLiveCardGroup[index] = new LiveGroupCardView(itemView.getContext());
            if (mContentViews[index] != null) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, mIvIds[index]);
                mContentViews[index].addView(mLiveCardGroup[index], layoutParams);
            }
        }
        mLiveCardGroup[index].bindData(item);
        mLiveCardGroup[index].setVisibility(View.VISIBLE);
    }
}
