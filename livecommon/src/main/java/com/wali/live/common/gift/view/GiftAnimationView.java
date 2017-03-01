package com.wali.live.common.gift.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.activity.BaseRotateSdkActivity;
import com.base.activity.RxActivity;
import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.event.SdkEventClass;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.model.GiftRecvModel;
import com.mi.live.data.gift.model.giftEntity.BigAnimationGift;
import com.wali.live.common.gift.utils.AnimationPlayControlTemplate;
import com.wali.live.utils.vm.VMArguUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 大动画播放
 * Created by chengsimin on 16/3/1.
 */
public class GiftAnimationView extends RelativeLayout implements IBindActivityLIfeCycle {

    public static String TAG = "GiftAnimationView";

    public SimpleDraweeView mForegroundAnimationView = null;

    public SimpleDraweeView mBackgroundAnimationView = null;

    public GiftMoveAnimationView mMoveAnimationView = null;

    public GiftAnimationView(Context context) {
        super(context);
        init(context);
    }

    public GiftAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftAnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    public void init(Context context) {
        initBigAnimationPlayControl((RxActivity) context);
    }

    private boolean mIsLandscape = false;//是否是竖屏

    private void addViewForBigAnimationIfNeed() {
        if (mBackgroundAnimationView == null) {
            mBackgroundAnimationView = new SimpleDraweeView(getContext());
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (mBigAnimationGift != null) {
                processLayoutByOrient(lp, mIsLandscape ? mBigAnimationGift.getBackgroundLandscapeConfig() : mBigAnimationGift.getBackgroundPortraitConfig());
            }
            addView(mBackgroundAnimationView, lp);
        } else {
            if (mBigAnimationGift != null) {
                processLayoutByOrient((LayoutParams) mBackgroundAnimationView.getLayoutParams(), mIsLandscape ? mBigAnimationGift.getBackgroundLandscapeConfig() : mBigAnimationGift.getBackgroundPortraitConfig());
            }
        }

        if (mMoveAnimationView == null) {
            mMoveAnimationView = new GiftMoveAnimationView(getContext());
            addView(mMoveAnimationView);
        }

        if (mForegroundAnimationView == null) {
            mForegroundAnimationView = new SimpleDraweeView(getContext());
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            if (mBigAnimationGift != null) {
                processLayoutByOrient(lp, mIsLandscape ? mBigAnimationGift.getForegroundLandscapeConfig() : mBigAnimationGift.getForegroundPortraitConfig());
            }
            addView(mForegroundAnimationView, lp);
        } else {
            if (mBigAnimationGift != null) {
                processLayoutByOrient((LayoutParams) mForegroundAnimationView.getLayoutParams(), mIsLandscape ? mBigAnimationGift.getForegroundLandscapeConfig() : mBigAnimationGift.getForegroundPortraitConfig());
            }
        }
    }

    private void orientViewForBigAnimationIfNeed() {
        if (mBackgroundAnimationView != null) {
            if (mBigAnimationGift != null) {
                processLayoutByOrient((RelativeLayout.LayoutParams) mBackgroundAnimationView.getLayoutParams(), mIsLandscape ? mBigAnimationGift.getBackgroundLandscapeConfig() : mBigAnimationGift.getBackgroundPortraitConfig());
            }
        }

        if (mMoveAnimationView != null) {

        }

        if (mForegroundAnimationView != null) {
            if (mBigAnimationGift != null) {
                processLayoutByOrient((RelativeLayout.LayoutParams) mForegroundAnimationView.getLayoutParams(), mIsLandscape ? mBigAnimationGift.getForegroundLandscapeConfig() : mBigAnimationGift.getForegroundPortraitConfig());
            }
        }
    }

    private void processLayoutByOrient(RelativeLayout.LayoutParams lp, BigAnimationGift.LayoutConfig layoutConfig) {
        if (layoutConfig != null) {
            lp.width = layoutConfig.width;
            lp.height = layoutConfig.height;
            if (mIsLandscape) {
                int left = (int) (layoutConfig.left * Math.max(DisplayUtils.getScreenHeight(), DisplayUtils.getScreenWidth()));
                int top = (int) (layoutConfig.top * Math.min(DisplayUtils.getScreenHeight(), DisplayUtils.getScreenWidth()));
                lp.setMargins(left, top, 0, 0);
            } else {
                int left = (int) (layoutConfig.left * Math.min(DisplayUtils.getScreenHeight(), DisplayUtils.getScreenWidth()));
                int top = (int) (layoutConfig.top * Math.max(DisplayUtils.getScreenHeight(), DisplayUtils.getScreenWidth()));
                lp.setMargins(left, top, 0, 0);
            }
        }
        MyLog.d(TAG, "lp.width=" + lp.width + ",lp.height:" + lp.height + ",lp.topMargin:" + lp.topMargin + ",lp.leftMargin:" + lp.leftMargin);
    }

    private void removeViewForBigAnimationIfCan() {
        removeAllViews();
        if (mBackgroundAnimationView != null) {
            stopWepAnimation(mBackgroundAnimationView);
            mBackgroundAnimationView = null;
        }
        if (mMoveAnimationView != null) {
            mMoveAnimationView = null;
        }
        if (mForegroundAnimationView != null) {
            stopWepAnimation(mForegroundAnimationView);
            mForegroundAnimationView = null;
        }
    }

    private void stopWepAnimation(SimpleDraweeView simpleDraweeView) {
        if (simpleDraweeView != null) {
            DraweeController controller = simpleDraweeView.getController();
            if (controller != null) {
                Animatable animation = controller.getAnimatable();
                if (animation != null) {
                    animation.stop();
                }
            }
        }
    }

    private void showBigAnimationView() {
        addViewForBigAnimationIfNeed();
        mBackgroundAnimationView.setVisibility(View.VISIBLE);
        mMoveAnimationView.setVisibility(View.VISIBLE);
        mForegroundAnimationView.setVisibility(View.VISIBLE);
    }

    private void hideBigAnimationView() {
        addViewForBigAnimationIfNeed();
        mBackgroundAnimationView.setVisibility(View.GONE);
        mMoveAnimationView.setVisibility(View.GONE);
        mForegroundAnimationView.setVisibility(View.GONE);
    }


    private AnimationPlayControlTemplate mBigAnimationControl;

    private void initBigAnimationPlayControl(RxActivity rxActivity) {
        mBigAnimationControl = new AnimationPlayControlTemplate<GiftRecvModelWithCpu>(rxActivity, false) {
            @Override
            public void onStart(GiftRecvModelWithCpu cur) {
                prepare(cur);
            }

            @Override
            protected void onEnd(GiftRecvModelWithCpu model) {
                if (mBigAnimationControl.hasMore()) {
                    hideBigAnimationView();
                } else {
                    removeViewForBigAnimationIfCan();
                }
                mBigAnimationGift = null;
            }

            @Override
            protected void processInBackGround(GiftRecvModelWithCpu model) {
                float cpuUsage = VMArguUtils.getCpuUsage(false);
                model.cpuIdle = cpuUsage > 0.35 ? false : true;
                MyLog.d(AnimationPlayControlTemplate.TAG, "cpuidle:" + model.cpuIdle);
            }
        };
    }

    BigAnimationGift mBigAnimationGift;

    private String getAnimationPathFromConfig(BigAnimationGift.LayoutConfig config, boolean cpuIdle) {
        if (config != null) {
            if (!cpuIdle && !TextUtils.isEmpty(config.animationLowPath)) {
                return config.animationLowPath;
            }
            return config.animationPath;
        }
        return null;
    }

    public void prepare(GiftRecvModelWithCpu modelWithCpu) {
        GiftRecvModel model = modelWithCpu.giftRecvModel;
        BigAnimationGift gift = (BigAnimationGift) model.getGift();
        mBigAnimationGift = gift;
        showBigAnimationView();
        // 背景
        String animationBackgroundPath = gift.getAnimationBackgroundName();
        if (mIsLandscape) {
            String animationPath = getAnimationPathFromConfig(gift.getBackgroundLandscapeConfig(), modelWithCpu.cpuIdle);
            if (!TextUtils.isEmpty(animationPath)) {
                animationBackgroundPath = animationPath;
            }
        } else {
            String animationPath = getAnimationPathFromConfig(gift.getBackgroundPortraitConfig(), modelWithCpu.cpuIdle);
            if (!TextUtils.isEmpty(animationPath)) {
                animationBackgroundPath = animationPath;
            }
        }
        prepareBackground(animationBackgroundPath);

        // 前景
        String animationForegroundPath = gift.getAnimationForegroundName();
        if (mIsLandscape) {
            String animationPath = getAnimationPathFromConfig(gift.getForegroundLandscapeConfig(), modelWithCpu.cpuIdle);
            if (!TextUtils.isEmpty(animationPath)) {
                animationForegroundPath = animationPath;
            }
        } else {
            String animationPath = getAnimationPathFromConfig(gift.getForegroundPortraitConfig(), modelWithCpu.cpuIdle);
            if (!TextUtils.isEmpty(animationPath)) {
                animationForegroundPath = animationPath;
            }
        }
        prepareForground(animationForegroundPath);

        //可移动区域
        mMoveAnimationView.setNameAndGift(model.getSenderName(), model.getGiftName(), model.getUserId(), model.getCertificationType(), model.getLevel());

        List<BigAnimationGift.AnimationStep> list = gift.getAnimationListForPortrait();
        if (mIsLandscape) {
            List<BigAnimationGift.AnimationStep> temp = gift.getAnimationListForLandscape();
            if (temp != null && !temp.isEmpty()) {
                list = temp;
            }
        }
        playWebp(list, 0, model);
    }

    private void prepareBackground(String webpPath) {
        MyLog.d(TAG, "prepareBackground webpPath:" + webpPath);
        Uri uri = new Uri.Builder().scheme("file").appendPath(webpPath).build();
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(DisplayUtils.getScreenWidth(), DisplayUtils.getScreenHeight()))
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(mBackgroundAnimationView.getController())
                .setImageRequest(request)
                .setAutoPlayAnimations(true)
                .build();
        mBackgroundAnimationView.setController(controller);
    }

    private void prepareForground(String webpPath) {
        MyLog.d(TAG, "prepareForground webpPath:" + webpPath);
        Uri uri = new Uri.Builder().scheme("file").appendPath(webpPath).build();
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(DisplayUtils.getScreenWidth(), DisplayUtils.getScreenHeight()))
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(mForegroundAnimationView.getController())
                .setImageRequest(request)
                .setAutoPlayAnimations(true)
                .build();
        mForegroundAnimationView.setController(controller);
    }

    private AnimatorSet mAnimatorSetForGiftMove;

    public void playWebp(final List<BigAnimationGift.AnimationStep> animationList, final int index, final GiftRecvModel model) {
        if (animationList == null || index >= animationList.size()) {
            mBigAnimationControl.endCurrent(null);
            // 结束了
            return;
        }
        BigAnimationGift.AnimationStep animationStep = animationList.get(index);
        //宽高
        int width = animationStep.getWidth();
        int height = animationStep.getHeight();
        int screenWidth, screenHeight;
        if (mIsLandscape) {
            screenWidth = Math.max(DisplayUtils.getScreenHeight(), DisplayUtils.getScreenWidth());
            screenHeight = Math.min(DisplayUtils.getScreenHeight(), DisplayUtils.getScreenWidth());
        } else {
            screenWidth = Math.min(DisplayUtils.getScreenHeight(), DisplayUtils.getScreenWidth());
            screenHeight = Math.max(DisplayUtils.getScreenHeight(), DisplayUtils.getScreenWidth());
        }
        List<BigAnimationGift.AnimationStep.Step> trace = animationStep.getAnimations();
        List<Path> pathList = new LinkedList<>();
        for (int i = 0; i < trace.size(); i++) {
            Path path = new Path();
            BigAnimationGift.AnimationStep.Step pathObj = trace.get(i);
            double duration = pathObj.duration;
            double x1 = pathObj.sx;
            double y1 = pathObj.sy;
            double scale1 = pathObj.sscale;
            double x2 = pathObj.ex;
            double y2 = pathObj.ey;
            double scale2 = pathObj.escale;
            path.x1 = (int) (x1 * screenWidth);
            path.y1 = (int) (y1 * screenHeight);
            path.scale1 = (float) scale1;
            path.x2 = (int) (x2 * screenWidth);
            path.y2 = (int) (y2 * screenHeight);
            path.scale2 = (float) scale2;
            path.duration = (int) (duration * 1000);
            pathList.add(path);
        }
        MyLog.d(TAG, "pathList:" + pathList);
        // 得到json所在目录，找到该目录下的webp文件
        String webpPath = animationStep.getAnimationName();
        // 设置前景
        mMoveAnimationView.setWebpPath(webpPath, width, height);
        List<Animator> animSeq = new ArrayList<>();
        for (Path p : pathList) {
            ObjectAnimator translationX = ObjectAnimator.ofFloat(mMoveAnimationView, "translationX", p.x1, p.x2);
            ObjectAnimator translationY = ObjectAnimator.ofFloat(mMoveAnimationView, "translationY", p.y1, p.y2);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mMoveAnimationView, "scaleX", p.scale1, p.scale2);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mMoveAnimationView, "scaleY", p.scale1, p.scale2);
            AnimatorSet composition = new AnimatorSet();
            composition.play(translationX).with(translationY).with(scaleX).with(scaleY);
            composition.setDuration(p.duration);
            animSeq.add(composition);
        }
        mAnimatorSetForGiftMove = new AnimatorSet();
        mAnimatorSetForGiftMove.playSequentially(animSeq);
        mAnimatorSetForGiftMove.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mHasCancel) {
                    playWebp(animationList, index + 1, model);
                } else {
                    mMoveAnimationView.setLayerType(View.LAYER_TYPE_NONE, null);
                    playWebp(null, Integer.MAX_VALUE, model);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mMoveAnimationView.setLayerType(View.LAYER_TYPE_NONE, null);
                mHasCancel = true;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mHasCancel = false;
                mMoveAnimationView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
        });
        mAnimatorSetForGiftMove.start();
    }

    private boolean mHasCancel = false;

    public void onActivityCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void onActivityDestroy() {
        mBigAnimationControl.reset();
        cancelAllAnimation();
        EventBus.getDefault().unregister(this);
        if (mBigAnimationControl != null) {
            mBigAnimationControl.destroy();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GiftEventClass.GiftAttrMessage.Big event) {
        if (event != null) {
            GiftRecvModel model = (GiftRecvModel) event.obj1;
            if (model != null) {
                mBigAnimationControl.add(new GiftRecvModelWithCpu(model), model.isFromSelf());
            }
        }
    }

    //    @Subscribe(threadMode = ThreadMode.POSTING)
//    public void onEvent(EventClass.SwitchAnchor event) {
//        mBigAnimationControl.reset();
//        cancelAllAnimation();
//    }
//
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SdkEventClass.OrientEvent event) {
        boolean isLandscape = false;
        if (event.orientation == BaseRotateSdkActivity.ORIENTATION_DEFAULT) {
            return;
        } else if (event.orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_NORMAL || event.orientation == BaseRotateSdkActivity.ORIENTATION_LANDSCAPE_REVERSED) {
            isLandscape = true;
        } else if (event.orientation == BaseRotateSdkActivity.ORIENTATION_PORTRAIT_NORMAL || event.orientation == BaseRotateSdkActivity.ORIENTATION_PORTRAIT_REVERSED) {
            isLandscape = false;
        }

        if (mIsLandscape != isLandscape) {
            mIsLandscape = isLandscape;
            // 会触发onEnd，所以背景啥的也会消失
            cancelAllAnimation();
            orientViewForBigAnimationIfNeed();
        }
    }

    /**
     * 取消动画
     */
    public void cancelAllAnimation() {
        if (mMoveAnimationView != null) {
            mMoveAnimationView.clearAnimation();
        }
        if (mAnimatorSetForGiftMove != null) {
            mAnimatorSetForGiftMove.cancel();
        }
    }

    static class Path {
        public int x1, y1, x2, y2;
        public float scale1, scale2;
        public int duration;

        @Override
        public String toString() {
            return "Path{" +
                    "x1=" + x1 +
                    ", y1=" + y1 +
                    ", x2=" + x2 +
                    ", y2=" + y2 +
                    ", scale1=" + scale1 +
                    ", scale2=" + scale2 +
                    ", duration=" + duration +
                    '}';
        }
    }

    static class GiftRecvModelWithCpu {
        public GiftRecvModel giftRecvModel;
        public boolean cpuIdle = true;

        public GiftRecvModelWithCpu(GiftRecvModel giftRecvModel) {
            this.giftRecvModel = giftRecvModel;
        }
    }
}
