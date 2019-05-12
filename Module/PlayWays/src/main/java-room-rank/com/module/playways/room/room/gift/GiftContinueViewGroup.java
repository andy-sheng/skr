package com.module.playways.room.room.gift;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.callback.Callback;
import com.common.log.MyLog;
import com.module.playways.grab.room.event.GrabSwitchRoomEvent;
import com.module.playways.room.gift.event.GiftBrushMsgEvent;
import com.module.playways.room.msg.event.SpecialEmojiMsgEvent;
import com.module.playways.room.room.gift.model.GiftPlayControlTemplate;
import com.module.playways.room.room.gift.model.GiftPlayModel;
import com.module.playways.BaseRoomData;
import com.module.playways.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static com.module.playways.room.room.gift.model.GiftPlayControlTemplate.BIG_GIFT;

/**
 * Created by chengsimin on 16/2/20.
 *
 * @Module 礼物连送动画区
 */
public class GiftContinueViewGroup extends RelativeLayout {
    public static String TAG = GiftContinueViewGroup.class.getSimpleName();

    private List<GiftContinuousView> mFeedGiftContinueViews = new ArrayList<>(2);
    private BaseRoomData mRoomData;

    Handler mHandler = new Handler(Looper.myLooper());

    public GiftContinueViewGroup(Context context) {
        super(context);
        init(context);
    }

    public GiftContinueViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftContinueViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    GiftPlayControlTemplate mGiftPlayControlTemplate = new GiftPlayControlTemplate() {
        @Override
        protected void needNotify() {
            //通知有新的礼物
            for (GiftContinuousView giftContinuousView : mFeedGiftContinueViews) {
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
            for (GiftContinuousView giftContinuousView : mFeedGiftContinueViews) {
                GiftPlayModel curModel = giftContinuousView.getCurGiftPlayModel();
                if (curModel != null
                        && giftPlayModel.getSender().getUserId() == curModel.getSender().getUserId()
                        && giftPlayModel.getContinueId() == curModel.getContinueId()
                        && id != giftContinuousView.getId()) {

                    return true;
                }
            }


            return false;
        }
    };

    public void init(Context context) {
        inflate(context, R.layout.gift_continue_view_group_layout, this);
        bindView();
    }

    protected void bindView() {
        GiftContinuousView v1 = (GiftContinuousView) findViewById(R.id.gift_continue_v1);
        mFeedGiftContinueViews.add(v1);

        GiftContinuousView v2 = (GiftContinuousView) findViewById(R.id.gift_continue_v2);
        mFeedGiftContinueViews.add(v2);

        for (int i = 0; i < mFeedGiftContinueViews.size(); i++) {
            GiftContinuousView gv = mFeedGiftContinueViews.get(i);
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
        if (giftPresentEvent.getGPrensentGiftMsg().getGiftInfo().getDisplayType() != BIG_GIFT) {
            GiftPlayModel playModel = GiftPlayModel.parseFromEvent(giftPresentEvent.getGPrensentGiftMsg(), mRoomData);
            mGiftPlayControlTemplate.add(playModel, false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SpecialEmojiMsgEvent event) {
        // 收到一条礼物消息,进入生产者队列
        GiftPlayModel playModel = GiftPlayModel.parseFromEvent(event, mRoomData);
        // 如果消息能被当前忙碌的view接受
        mGiftPlayControlTemplate.add(playModel, false);
    }

    public void setRoomData(BaseRoomData roomData) {
        mRoomData = roomData;
    }

    public interface GiftProvider {
        void tryGetGiftModel(GiftPlayModel giftPlayModel, int beginNum, int id, Callback<GiftPlayModel> callback, Callback<GiftPlayModel> callbackInUiThread);
    }
}
