package com.wali.live.common.gift.view;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.base.activity.assist.IBindActivityLIfeCycle;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.base.log.MyLog;
import com.base.activity.RxActivity;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.model.GiftRecvModel;
import com.mi.live.data.gift.model.giftEntity.RoomEffectGift;
import com.wali.live.common.gift.utils.AnimationPlayControlTemplate;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 房间特效播放
 * Created by chengsimin on 16/3/1.
 */
public class GiftRoomEffectView extends FrameLayout implements IBindActivityLIfeCycle{

    public static String TAG = "GiftAnimationView";

    public SimpleDraweeView mRoomEffectView = null;

    public GiftRoomEffectView(Context context) {
        super(context);
        init(context);
    }

    public GiftRoomEffectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftRoomEffectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    public void init(Context context) {
        initRoomEffectAnimationPlayControl((RxActivity) context);
    }

    private void addViewForRoomEffectIfNeed() {
        if (mRoomEffectView == null) {
            mRoomEffectView = new SimpleDraweeView(getContext());
            addView(mRoomEffectView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private void removeViewForRoomEffectIfCan() {
        if (mRoomEffectView != null) {
            removeView(mRoomEffectView);
            mRoomEffectView = null;
        }
    }

    private void showRoomEffectView() {
        addViewForRoomEffectIfNeed();
        mRoomEffectView.setVisibility(View.VISIBLE);
    }

    private void hideRoomEffectView() {
        addViewForRoomEffectIfNeed();
        mRoomEffectView.setVisibility(View.GONE);
    }

    private AnimationPlayControlTemplate mRoomEffectAnimationControl;

    private void initRoomEffectAnimationPlayControl(RxActivity rxActivity) {
        mRoomEffectAnimationControl = new AnimationPlayControlTemplate<GiftRecvModel>(rxActivity, true) {
            @Override
            public void onStart(GiftRecvModel cur) {
                RoomEffectGift gift = (RoomEffectGift) cur.getGift();
                String webpPath = gift.getRoomEffectAnimation();
                if (cur.getLeftTime() <= 0) {
                    cur.setLeftTime(gift.getEffectDuration() * 1000);
                }
                this.setEndDelayTime(cur.getLeftTime());
                //播放webp
                MyLog.d(TAG, "webpPath:" + webpPath);
                Uri uri = new Uri.Builder().scheme("file").appendPath(webpPath).build();
                showRoomEffectView();
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                        .build();
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(mRoomEffectView.getController())
                        .setImageRequest(request)
                        .setAutoPlayAnimations(true)
                        .build();
                mRoomEffectView.setController(controller);
            }

            @Override
            protected void onEnd(GiftRecvModel model) {
                if (mRoomEffectAnimationControl.hasMore()) {
                    hideRoomEffectView();
                } else {
                    removeViewForRoomEffectIfCan();
                }
            }
        };
    }


    public void onActivityCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void onActivityDestroy() {
        mRoomEffectAnimationControl.reset();
        cancelAllAnimation();
        EventBus.getDefault().unregister(this);
        if (mRoomEffectAnimationControl != null) {
            mRoomEffectAnimationControl.destroy();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GiftEventClass.GiftAttrMessage.RoomBackGround event) {
        if (event != null) {
            GiftRecvModel model = (GiftRecvModel) event.obj1;
            if (model != null) {
                mRoomEffectAnimationControl.add(model, model.isFromSelf());
            }
        }
    }

//    @Subscribe(threadMode = ThreadMode.POSTING)
//    public void onEvent(EventClass.SwitchAnchor event) {
//        mRoomEffectAnimationControl.reset();
//        cancelAllAnimation();
//    }

    /**
     * 取消动画
     */
    public void cancelAllAnimation() {
        if (mRoomEffectView != null) {
            DraweeController controller = mRoomEffectView.getController();
            if (controller != null) {
                Animatable animation = controller.getAnimatable();
                if (animation != null) {
                    animation.stop();
                }
            }
        }
        hideRoomEffectView();
    }

}
