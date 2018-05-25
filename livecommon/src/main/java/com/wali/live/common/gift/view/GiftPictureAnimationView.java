package com.wali.live.common.gift.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.live.module.common.R;
import com.mi.live.data.gift.model.giftEntity.NormalEffectGift;

import java.util.ArrayList;
import java.util.List;


/**
 * @Module 连送的图片区域动画
 * Created by chengsimin on 16/6/20.
 */
public class GiftPictureAnimationView extends RelativeLayout {
    private static final java.lang.String TAG = "GiftPictureAnimationView";

    ImageView mSwitchAnimationIv;


    BaseImageView mGiftPictureIv;

    private AnimationDrawable resources;
    private String mImagePath; //当前的图片路径
    private String mNextImagePath;//下一张要播放的图片路径

    public GiftPictureAnimationView(Context context) {
        super(context);
        init(context);
    }

    public GiftPictureAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftPictureAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.gift_picture_animation_layout, this);
        mSwitchAnimationIv = (ImageView) this.findViewById(R.id.switch_animation_iv);
        mGiftPictureIv = (BaseImageView) this.findViewById(R.id.gift_picture_iv);

    }

    public void fillPictureByNumber(String picturePath, int number) {
        this.mImagePath = picturePath;
        if (!mFlags.isEmpty()) {
            for (NormalEffectGift.Flag f : mFlags) {
                if (number >= f.startCount) {
                    this.mImagePath = f.giftImage;
                } else {
                    break;
                }
            }
        }
        fillPictureInternal();
    }

    public void reset() {
        mImagePath = null;
        mNextImagePath = null;
        mFlags.clear();
    }

    //动画开始接口
    private void tryPlay(String nextPicturePath) {
        mNextImagePath = nextPicturePath;
        if (TextUtils.isEmpty(mImagePath)) {
            //如果当前路径为空,第一次填充替换，直接加载图片
            mImagePath = mNextImagePath;
            fillPictureInternal();
        } else {
            //已经有原始图片路径了
            if (mImagePath.equals(mNextImagePath)) {
                //路径相同，不需要播放改变动画
                return;
            } else {
                //需要
                startSwitchAnimation();
            }
        }
    }

    private void fillPictureInternal() {
        if (TextUtils.isEmpty(mImagePath)) {
            return;
        }
        if (mImagePath.startsWith("http")) {
            BaseImage baseImage = ImageFactory.newHttpImage(mImagePath).build();
            FrescoWorker.loadImage(mGiftPictureIv, baseImage);
        } else {
            BaseImage baseImage = ImageFactory.newLocalImage(mImagePath).build();
            FrescoWorker.loadImage(mGiftPictureIv, baseImage);
            Uri uri = new Uri.Builder().scheme("file").appendPath(mImagePath).build();//TEST
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(mGiftPictureIv.getController())
                    .setImageRequest(request)
                    .setAutoPlayAnimations(true)
                    .build();
            mGiftPictureIv.setController(controller);
        }
    }

    private void startSwitchAnimation() {
        zoomOut();
    }

    AnimatorSet mZoomOutAnimatorSet;

    private void zoomOut() {
        if (mZoomOutAnimatorSet == null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mGiftPictureIv, "scaleX", 1.0f, 0.2f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mGiftPictureIv, "scaleY", 1.0f, 0.2f);
            mZoomOutAnimatorSet = new AnimatorSet();
            mZoomOutAnimatorSet.setDuration(60);
            mZoomOutAnimatorSet.playTogether(scaleX, scaleY);
            mZoomOutAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mImagePath = mNextImagePath;
                    fillPictureInternal();
                    startBgAnimator();
                }
            });
        }
        mZoomOutAnimatorSet.start();
    }

    AnimatorSet mStartBgAnimatorSet;

    //背景图片动画
    private void startBgAnimator() {
        if (mStartBgAnimatorSet == null) {
            ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(mGiftPictureIv, "scaleX", 0.4f, 1.0f);
            ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(mGiftPictureIv, "scaleY", 0.4f, 1.0f);
            mStartBgAnimatorSet = new AnimatorSet();
            mStartBgAnimatorSet.setDuration(300);
            mStartBgAnimatorSet.playTogether(scaleX1, scaleY1);
            mStartBgAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSwitchAnimationIv.setImageResource(R.drawable.gift_upgrade_animation);
                    resources = (AnimationDrawable) mSwitchAnimationIv.getDrawable();
                    if (resources.isRunning()) {
                        resources.stop();
                        mSwitchAnimationIv.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    mSwitchAnimationIv.setVisibility(View.VISIBLE);
                    mSwitchAnimationIv.setImageResource(R.drawable.gift_upgrade_animation);
                    resources = (AnimationDrawable) mSwitchAnimationIv.getDrawable();
                    if (!resources.isRunning()) {
                        resources.start();
                    }
                }
            });
        }
        mStartBgAnimatorSet.start();
    }

    public void tryPlayIfNeed(int mCurNumber,boolean isBatchGift) {
        for (NormalEffectGift.Flag f : mFlags) {
            if (mCurNumber < f.startCount) {
                break;
            }
            if(!isBatchGift){
                if (mCurNumber == f.startCount && f.startCount != 1) {
                    tryPlay(f.giftImage);
                    break;
                }
            }else{
                if ((mCurNumber == f.startCount||mCurNumber == f.startCount+1) && f.startCount != 1) {
                    tryPlay(f.giftImage);
                    break;
                }
            }

        }
    }

    private List<NormalEffectGift.Flag> mFlags = new ArrayList<>();

    public void setFlags(List<NormalEffectGift.Flag> l) {
        if (l != null) {
            mFlags.clear();
            mFlags.addAll(l);
        }
    }

    List<NormalEffectGift.BigContinue> bigCons = new ArrayList<>();//小礼物连击触发大礼物

    public void setBigCons(List<NormalEffectGift.BigContinue> l) {
        if (l != null) {
            bigCons.clear();
            bigCons.addAll(l);
        }
    }

    public void destroy() {
        if (mZoomOutAnimatorSet != null) {
            mZoomOutAnimatorSet.removeAllListeners();
            if (mZoomOutAnimatorSet.isRunning()) {
                mZoomOutAnimatorSet.cancel();
                mZoomOutAnimatorSet = null;
            }
        }

        if (mStartBgAnimatorSet != null) {
            mStartBgAnimatorSet.removeAllListeners();
            if (mStartBgAnimatorSet.isRunning()) {
                mStartBgAnimatorSet.cancel();
                mStartBgAnimatorSet = null;
            }
        }
    }
}
