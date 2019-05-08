package com.module.playways.room.room.gift;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.ObjectPlayControlTemplate;
import com.module.playways.room.msg.event.OverlayGiftBrushMsgEvent;
import com.module.playways.room.msg.event.SpecialEmojiMsgEvent;
import com.module.playways.room.room.gift.model.GiftPlayModel;
import com.module.playways.BaseRoomData;
import com.module.playways.RoomDataUtils;
import com.module.playways.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16/2/20.
 *
 * @Module 礼物连送动画区 可叠加的礼物 类似粑粑和爱心等
 */
public class GiftOverlayAnimationViewGroup extends RelativeLayout {
    public static String TAG = GiftOverlayAnimationViewGroup.class.getSimpleName();

    static final int MAX_CONSUMER_NUM = 6;

    private List<GiftOverlayAnimationView> mFeedGiftAnimationViews = new ArrayList<>(MAX_CONSUMER_NUM);
    private BaseRoomData mRoomData;

    public GiftOverlayAnimationViewGroup(Context context) {
        super(context);
        init(context);
    }

    public GiftOverlayAnimationViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftOverlayAnimationViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    ObjectPlayControlTemplate<GiftPlayModel, GiftOverlayAnimationView> mGiftPlayControlTemplate = new ObjectPlayControlTemplate<GiftPlayModel, GiftOverlayAnimationView>() {

        @Override
        protected GiftOverlayAnimationView accept(GiftPlayModel cur) {
            return isIdle();
        }

        @Override
        public void onStart(GiftPlayModel model, GiftOverlayAnimationView giftBigAnimationView) {
            if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
                mGiftPlayControlTemplate.endCurrent(model);
                return;
            }
            giftBigAnimationView.play(GiftOverlayAnimationViewGroup.this, model);
        }

        @Override
        protected void onEnd(GiftPlayModel model) {

        }
    };

    public void init(Context context) {
        inflate(context, R.layout.gift_overlay_animation_view_group_layout, this);
        bindView();
    }


    protected void bindView() {

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
        for (GiftOverlayAnimationView giftBigAnimationView : mFeedGiftAnimationViews) {
            giftBigAnimationView.destroy();
        }
    }

    private GiftOverlayAnimationView isIdle() {
        for (GiftOverlayAnimationView giftContinuousView : mFeedGiftAnimationViews) {
            if (giftContinuousView.isIdle()) {
                return giftContinuousView;
            }
        }
        if (mFeedGiftAnimationViews.size() < MAX_CONSUMER_NUM) {
            GiftOverlayAnimationView giftBigAnimationView = new GiftOverlayAnimationView(getContext());
            giftBigAnimationView.setListener(new GiftOverlayAnimationView.Listener() {
                @Override
                public void onFinished(GiftOverlayAnimationView giftBigAnimationView, GiftPlayModel giftPlayModel) {
                    //把view移除
                    mGiftPlayControlTemplate.endCurrent(giftPlayModel);
                }
            });
            mFeedGiftAnimationViews.add(giftBigAnimationView);
            return giftBigAnimationView;
        }
        return null;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(OverlayGiftBrushMsgEvent giftPresentEvent) {
        if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
            return;
        }
        // 收到一条礼物消息,进入生产者队列
        GiftPlayModel playModel = giftPresentEvent.getGiftPlayModel();
        // 如果消息能被当前忙碌的view接受
        mGiftPlayControlTemplate.add(playModel, true);
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(SpecialEmojiMsgEvent event) {
        if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
            return;
        }
        // 收到一条礼物消息,进入生产者队列
        GiftPlayModel playModel = GiftPlayModel.parseFromEvent(event, mRoomData);
        mGiftPlayControlTemplate.add(playModel, false);
    }

    public void setRoomData(BaseRoomData roomData) {
        mRoomData = roomData;
    }

}
