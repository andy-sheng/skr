package com.module.playways.room.room.gift;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.ObjectPlayControlTemplate;
import com.module.playways.BaseRoomData;
import com.module.playways.R;
import com.module.playways.grab.room.event.GrabSwitchRoomEvent;
import com.module.playways.room.gift.event.GiftBrushMsgEvent;
import com.module.playways.room.room.comment.model.CommentGiftModel;
import com.module.playways.room.room.event.PretendCommentMsgEvent;
import com.module.playways.room.room.gift.model.GiftPlayModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static com.module.playways.room.room.gift.model.GiftPlayControlTemplate.BIG_GIFT;

public class GiftBigAnimationViewGroup extends RelativeLayout {

    public final static String TAG = GiftBigAnimationViewGroup.class.getSimpleName();

    static final int MAX_CONSUMER_NUM = 1;

    private List<GiftBigAnimationView> mFeedGiftAnimationViews = new ArrayList<>(MAX_CONSUMER_NUM);
    private BaseRoomData mRoomData;

    GiftBigContinuousView mGiftBigContinueView;

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
            giftBigAnimationView.play(GiftBigAnimationViewGroup.this, model);
            mGiftBigContinueView.setVisibility(VISIBLE);
            mGiftBigContinueView.play(model);
            EventBus.getDefault().post(new PretendCommentMsgEvent(new CommentGiftModel(model)));
        }

        @Override
        protected void onEnd(GiftPlayModel model) {
            mGiftBigContinueView.setVisibility(GONE);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSwitchRoomEvent grabSwitchRoomEvent) {
        mGiftPlayControlTemplate.reset();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GiftBrushMsgEvent giftPresentEvent) {
        // 收到一条礼物消息,进入生产者队列
        if (giftPresentEvent.getGPrensentGiftMsg().getGiftInfo().getDisplayType() == BIG_GIFT) {
            GiftPlayModel playModel = GiftPlayModel.parseFromEvent(giftPresentEvent.getGPrensentGiftMsg(), mRoomData);
            mGiftPlayControlTemplate.add(playModel, true);
        }
    }

    public void setGiftBigContinuousView(GiftBigContinuousView giftBigContinueView) {
        mGiftBigContinueView = giftBigContinueView;
    }
}
