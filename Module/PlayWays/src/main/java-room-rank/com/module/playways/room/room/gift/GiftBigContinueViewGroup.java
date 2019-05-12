package com.module.playways.room.room.gift;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.module.playways.BaseRoomData;
import com.module.playways.R;
import com.module.playways.grab.room.event.GrabSwitchRoomEvent;
import com.module.playways.room.gift.event.GiftBrushMsgEvent;
import com.module.playways.room.room.gift.model.GiftPlayControlTemplate;
import com.module.playways.room.room.gift.model.GiftPlayModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static com.module.playways.room.room.gift.model.GiftPlayControlTemplate.BIG_GIFT;

public class GiftBigContinueViewGroup extends RelativeLayout {
    public static String TAG = GiftBigContinueViewGroup.class.getSimpleName();

    private List<GiftBigContinuousView> mFeedGiftContinueViews = new ArrayList<>(1);
    private BaseRoomData mRoomData;

    Handler mHandler = new Handler(Looper.myLooper());

    public GiftBigContinueViewGroup(Context context) {
        super(context);
        init(context);
    }

    public GiftBigContinueViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftBigContinueViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    GiftPlayControlTemplate mGiftPlayControlTemplate = new GiftPlayControlTemplate() {
        @Override
        protected void needNotify() {
            //通知有新的礼物
            for (GiftBigContinuousView giftContinuousView : mFeedGiftContinueViews) {
                giftContinuousView.tryNotifyHasGiftCanPlay();
            }
        }

        protected boolean isGiftModelIsPlayingExpectOwer(@NonNull GiftPlayModel giftPlayModel, int id) {
            if (giftPlayModel == null) {
                return false;
            }

            /**
             * 判断当前对象是否在期望被播放
             */
            for (GiftBigContinuousView giftContinuousView : mFeedGiftContinueViews) {
                GiftPlayModel curModel = giftContinuousView.getCurGiftPlayModel();
                if (curModel != null
                        && giftPlayModel.getSender().getUserId() == curModel.getSender().getUserId()
                        && giftPlayModel.getContinueId() == curModel.getContinueId()
                        && id != giftContinuousView.getId()) {
                    MyLog.d(TAG, "isGiftModelIsPlayingExpectOwer view id is " + id + ", other view is playing gift " + giftPlayModel.getGift().getGiftID());
                    return true;
                }
            }

            MyLog.d(TAG, "isGiftModelIsPlayingExpectOwer view id is " + id + ", gift" + giftPlayModel.getGift().getGiftID() + " is own or not playing");
            return false;
        }
    };

    public void init(Context context) {
        inflate(context, R.layout.gift_big_continue_view_group_layout, this);
        bindView();
    }

    protected void bindView() {
        GiftBigContinuousView mGiftBigContinueView = (GiftBigContinuousView) findViewById(R.id.gift_big_continue_view);
        mFeedGiftContinueViews.add(mGiftBigContinueView);

        for (int i = 0; i < mFeedGiftContinueViews.size(); i++) {
            GiftBigContinuousView gv = mFeedGiftContinueViews.get(i);
            gv.setMyId(i + 1);
            gv.setGiftProvider(mGiftPlayControlTemplate);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        mGiftPlayControlTemplate.destroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSwitchRoomEvent grabSwitchRoomEvent) {
        mGiftPlayControlTemplate.clear();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GiftBrushMsgEvent giftPresentEvent) {
        // 收到一条礼物消息,进入生产者队列
        if (giftPresentEvent.getGPrensentGiftMsg().getGiftInfo().getDisplayType() == BIG_GIFT) {
            GiftPlayModel playModel = GiftPlayModel.parseFromEvent(giftPresentEvent.getGPrensentGiftMsg(), mRoomData);
            mGiftPlayControlTemplate.add(playModel, false);
        }
    }

    public void setRoomData(BaseRoomData roomData) {
        mRoomData = roomData;
    }
}
