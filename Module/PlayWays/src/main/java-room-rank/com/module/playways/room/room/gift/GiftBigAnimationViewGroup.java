package com.module.playways.room.room.gift;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.ObjectPlayControlTemplate;
import com.common.log.MyLog;
import com.module.playways.BaseRoomData;
import com.module.playways.R;
import com.module.playways.grab.room.event.SwitchRoomEvent;
import com.module.playways.room.gift.event.GiftBrushMsgEvent;
import com.module.playways.room.room.comment.model.CommentGiftModel;
import com.module.playways.room.room.event.PretendCommentMsgEvent;
import com.module.playways.room.room.gift.model.GiftPlayModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.module.playways.room.room.gift.model.GiftPlayControlTemplate.BIG_GIFT;

public class GiftBigAnimationViewGroup extends RelativeLayout {

    public final String TAG = GiftBigAnimationViewGroup.class.getSimpleName();

    static final int MAX_CONSUMER_NUM = 1;

    private List<GiftBaseAnimationView> mFeedGiftAnimationViews = new ArrayList<>(MAX_CONSUMER_NUM);
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

    ObjectPlayControlTemplate<GiftPlayModel, GiftBaseAnimationView> mGiftPlayControlTemplate = new ObjectPlayControlTemplate<GiftPlayModel, GiftBaseAnimationView>() {

        @Override
        protected GiftBaseAnimationView accept(GiftPlayModel cur) {
            return isIdle(cur);
        }

        @Override
        public void onStart(GiftPlayModel model, GiftBaseAnimationView giftBigAnimationView) {
            giftBigAnimationView.play(GiftBigAnimationViewGroup.this, model);
            mGiftBigContinueView.setVisibility(VISIBLE);
            mGiftBigContinueView.play(model);
            EventBus.getDefault().post(new PretendCommentMsgEvent(new CommentGiftModel(model, mRoomData)));
        }

        @Override
        protected void onEnd(GiftPlayModel model) {
            mGiftBigContinueView.setVisibility(GONE);
        }
    };

    private void init(Context context) {
        inflate(context, R.layout.gift_big_animation_view_group_layout, this);
    }

    private GiftBaseAnimationView isIdle(GiftPlayModel cur) {
        // 同时只能有一个大礼物动画 必须所有动画都空闲时可播放
        for (GiftBaseAnimationView giftBigAnimationView : mFeedGiftAnimationViews) {
            if (!giftBigAnimationView.isIdle()) {
                return null;
            }
        }

        // 全都空闲 选择指定动画View播放动画
        for (GiftBaseAnimationView giftBigAnimationView : mFeedGiftAnimationViews) {
            if (giftBigAnimationView.isSupport(cur)) {
                return giftBigAnimationView;
            }
        }

        // 控制只有一组可用动画
        if (mFeedGiftAnimationViews.size() < MAX_CONSUMER_NUM) {
            GiftBaseAnimationView giftBigAnimationView = new GiftBigAnimationView(getContext());
            GiftBaseAnimationView giftBigVideoAnimationView = new GiftBigVideoAnimationView(getContext());

            giftBigAnimationView.setListener(new GiftBaseAnimationView.Listener() {
                @Override
                public void onFinished(GiftBaseAnimationView giftBigAnimationView, GiftPlayModel giftPlayModel) {
                    //把view移除
                    mGiftPlayControlTemplate.endCurrent(giftPlayModel);
                }
            });

            giftBigVideoAnimationView.setListener(new GiftBaseAnimationView.Listener() {
                @Override
                public void onFinished(@NotNull GiftBaseAnimationView animationView, @NotNull GiftPlayModel giftPlayModel) {
                    //把view移除
                    mGiftPlayControlTemplate.endCurrent(giftPlayModel);
                }
            });

            // 优先用video播
            mFeedGiftAnimationViews.add(giftBigVideoAnimationView);
            mFeedGiftAnimationViews.add(giftBigAnimationView);
            if(giftBigVideoAnimationView.isSupport(cur)){
                return giftBigVideoAnimationView;
            }else{
                return giftBigAnimationView;
            }
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
        for (GiftBaseAnimationView giftBaseAnimationView : mFeedGiftAnimationViews) {
            giftBaseAnimationView.destroy();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SwitchRoomEvent grabSwitchRoomEvent) {
        mGiftPlayControlTemplate.reset();
        for (GiftBaseAnimationView giftBaseAnimationView : mFeedGiftAnimationViews) {
            giftBaseAnimationView.reset();
        }
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
