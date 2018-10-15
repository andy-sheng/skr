package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.common.gift.view.GiftContinuousView;

import java.util.List;

/**
 * Created by zhujianning on 18-8-10.
 * 新版游戏直播间的小礼物展示
 */

public class GameGiftContinueViewGroup extends GiftContinueViewGroup {
    private static final String TAG = "GameGiftContinueViewGroup";
    private static final int PX_LANDSCAPE_MARGIN_BOTTOM = DisplayUtils.dip2px(88.67f);
    private static final int PX_PORTRAIT_MARGIN_BOTTOM = DisplayUtils.dip2px(200f);

    public GameGiftContinueViewGroup(Context context) {
        super(context);
    }

    public GameGiftContinueViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameGiftContinueViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void orient(boolean isLandscape) {
        mIsLandscape = isLandscape;
        mGiftScheduler.setGiftContinuousViews(getFeedViews());
        MyLog.d(TAG, "isLandscape:" + isLandscape);
        if (isLandscape) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) this.getLayoutParams();
            lp.alignWithParent = true;
            lp.bottomMargin = PX_LANDSCAPE_MARGIN_BOTTOM;
        } else {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) this.getLayoutParams();
            lp.alignWithParent = true;
            lp.bottomMargin = PX_PORTRAIT_MARGIN_BOTTOM;
        }
        //TODO-这个外移得测下
        for (GiftContinuousView v : getFeedViews()) {
            v.tryAwake();
        }
    }

    @Override
    public List<GiftContinuousView> getFeedViews() {
        return mFeedGiftContinueViews;
    }
}
