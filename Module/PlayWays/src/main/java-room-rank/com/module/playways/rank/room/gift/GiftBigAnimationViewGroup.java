package com.module.playways.rank.room.gift;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.AnimationPlayControlTemplate;
import com.module.playways.rank.msg.event.SpecialEmojiMsgEvent;
import com.module.playways.rank.room.gift.model.GiftPlayModel;
import com.module.playways.rank.room.model.RoomData;
import com.module.playways.rank.room.model.RoomDataUtils;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16/2/20.
 *
 * @Module 礼物连送动画区
 */
public class GiftBigAnimationViewGroup extends RelativeLayout {
    public static String TAG = GiftBigAnimationViewGroup.class.getSimpleName();

    static final int MAX_CONSUMER_NUM = 5;

    private List<GiftBigAnimationView> mFeedGiftAnimationViews = new ArrayList<>(MAX_CONSUMER_NUM);
    private RoomData mRoomData;

    public GiftBigAnimationViewGroup(Context context) {
        super(context);
        init(context);
    }

    public GiftBigAnimationViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftBigAnimationViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    AnimationPlayControlTemplate<GiftPlayModel, GiftBigAnimationView> mGiftPlayControlTemplate = new AnimationPlayControlTemplate<GiftPlayModel, GiftBigAnimationView>(MAX_CONSUMER_NUM) {

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

    public void init(Context context) {
        inflate(context, R.layout.gift_big_animation_view_group_layout, this);
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
    }

    private GiftBigAnimationView isIdle() {
        for (GiftBigAnimationView giftContinuousView : mFeedGiftAnimationViews) {
            if (giftContinuousView.isIdle()) {
                return giftContinuousView;
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


    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(SpecialEmojiMsgEvent event) {
        if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
            return;
        }
        // 收到一条礼物消息,进入生产者队列
        GiftPlayModel playModel = GiftPlayModel.parseFromEvent(event);
        mGiftPlayControlTemplate.add(playModel, false);
    }

    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
    }

}
