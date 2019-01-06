package com.module.playways.rank.room.gift;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.AnimationPlayControlTemplate;
import com.module.playways.rank.msg.event.SpecialEmojiMsgEvent;
import com.module.playways.rank.room.gift.model.GiftPlayControlTemplate;
import com.module.playways.rank.room.gift.model.GiftPlayModel;
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
public class GiftContinueViewGroup extends RelativeLayout {
    public static String TAG = GiftContinueViewGroup.class.getSimpleName();

    private List<GiftContinuousView> mFeedGiftContinueViews = new ArrayList<>(2);

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

    GiftPlayControlTemplate mGiftPlayControlTemplate = new GiftPlayControlTemplate(2) {
        @Override
        protected GiftContinuousView accept(GiftPlayModel cur) {
            return isIdle();
        }

        @Override
        public void onStart(GiftPlayModel model, GiftContinuousView giftContinuousView) {
            if (giftContinuousView != null) {
                // 播放动画
                giftContinuousView.play(model);
            }
        }

        @Override
        protected void onEnd(GiftPlayModel model) {

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
            gv.setListener(new GiftContinuousView.Listener() {
                @Override
                public void onPlayOver(GiftContinuousView giftContinuousView, GiftPlayModel giftPlayModel) {
                    // 动画播放结束
                    mGiftPlayControlTemplate.endCurrent(giftPlayModel);
                }
            });
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
    }

    private GiftContinuousView isIdle() {
        for (GiftContinuousView giftContinuousView : mFeedGiftContinueViews) {
            if (giftContinuousView.isIdle()) {
                return giftContinuousView;
            }
        }
        return null;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(SpecialEmojiMsgEvent event) {
        // 收到一条礼物消息,进入生产者队列
        GiftPlayModel playModel = GiftPlayModel.parseFromEvent(event);
        // 如果消息能被当前忙碌的view接受
        for (GiftContinuousView giftContinuousView : mFeedGiftContinueViews) {
            if (!giftContinuousView.isIdle()) {
                if (giftContinuousView.accept(playModel)) {
                    // 被这个view接受了
                    giftContinuousView.tryTriggerAnimation();
                    return;
                }
            }
        }
        mGiftPlayControlTemplate.add(playModel, false);
    }

}
