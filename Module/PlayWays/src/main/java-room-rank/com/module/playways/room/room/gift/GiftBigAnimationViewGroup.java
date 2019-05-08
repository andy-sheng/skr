package com.module.playways.room.room.gift;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.ObjectPlayControlTemplate;
import com.module.playways.BaseRoomData;
import com.module.playways.R;
import com.module.playways.RoomDataUtils;
import com.module.playways.room.gift.event.BigGiftMsgEvent;
import com.module.playways.room.room.gift.model.GiftPlayModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class GiftBigAnimationViewGroup extends RelativeLayout {

    public final static String TAG = GiftBigAnimationViewGroup.class.getSimpleName();

    static final int MAX_CONSUMER_NUM = 1;

    private List<GiftBigAnimationView> mFeedGiftAnimationViews = new ArrayList<>(MAX_CONSUMER_NUM);
    private BaseRoomData mRoomData;

    public GiftBigAnimationViewGroup(Context context) {
        super(context);
        init(context);
    }

    public GiftBigAnimationViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftBigAnimationViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    ObjectPlayControlTemplate<GiftPlayModel, GiftBigAnimationView> mGiftPlayControlTemplate = new ObjectPlayControlTemplate<GiftPlayModel, GiftBigAnimationView>() {

        @Override
        protected GiftBigAnimationView accept(GiftPlayModel cur) {
            return isIdle();
        }

        @Override
        public void onStart(GiftPlayModel model, GiftBigAnimationView giftBigAnimationView) {
            if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
                mGiftPlayControlTemplate.endCurrent(model);
                return;
            }
            giftBigAnimationView.play(GiftBigAnimationViewGroup.this, model);
        }

        @Override
        protected void onEnd(GiftPlayModel model) {

        }
    };

    private void init(Context context) {
        inflate(context, R.layout.gift_big_animation_view_group_layout, this);
    }

    private GiftBigAnimationView isIdle() {
        for (GiftBigAnimationView giftBigAnimationView : mFeedGiftAnimationViews) {
            if (giftBigAnimationView.isIdle()) {
                return giftBigAnimationView;
            }
        }
        if (mFeedGiftAnimationViews.size() < MAX_CONSUMER_NUM) {
            GiftBigAnimationView giftBigAnimationView = new GiftBigAnimationView(getContext());
            giftBigAnimationView.setListener(new GiftBigAnimationView.Listener() {
                @Override
                public void onFinished(GiftBigAnimationView giftBigAnimationView, GiftPlayModel giftPlayModel) {
                    //把view移除
                    mGiftPlayControlTemplate.endCurrent(giftPlayModel);
                }
            });
            mFeedGiftAnimationViews.add(giftBigAnimationView);
            return giftBigAnimationView;
        }
        return null;
    }

    public void setRoomData(BaseRoomData roomData) {
        mRoomData = roomData;
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
        mGiftPlayControlTemplate.destroy();
        for (GiftBigAnimationView giftBigAnimationView : mFeedGiftAnimationViews) {
            giftBigAnimationView.destroy();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(BigGiftMsgEvent bigGiftMsgEvent) {
        // 收到一条礼物消息,进入生产者队列
        GiftPlayModel playModel = GiftPlayModel.parseFromEvent(bigGiftMsgEvent.getGPrensentGiftMsg(), mRoomData);
        // 如果消息能被当前忙碌的view接受
        mGiftPlayControlTemplate.add(playModel, true);
    }
}
