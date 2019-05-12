package com.module.playways.room.room.gift;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.room.gift.model.AnimationGift;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.room.gift.model.GiftPlayModel;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

public class GiftBigAnimationView {

    public final static String TAG = "GiftBigAnimationView";

    static final int MSG_ENSURE_FINISH = 101;

    static final int STATUS_IDLE = 1;
    static final int STATUS_PLAYING = 2;
    int mStatus = STATUS_IDLE;

    Listener mListener;

    SVGAImageView mSVGAImageView;
    GiftPlayModel mGiftPlayModel;

    Handler mUiHanlder = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_ENSURE_FINISH) {
                onFinish();
            }
        }
    };

    public GiftBigAnimationView(Context context) {
        init();
    }

    private void init() {
        mSVGAImageView = new SVGAImageView(U.app());
    }

    public void play(RelativeLayout parent, GiftPlayModel giftPlayModel) {
        // TODO: 2019/5/8  播放动画 差个偏移量
        mGiftPlayModel = giftPlayModel;
        BaseGift baseGift = mGiftPlayModel.getGift();
        if (baseGift instanceof AnimationGift) {
            AnimationGift animationGift = (AnimationGift) baseGift;
            AnimationGift.AnimationPrams giftParamModel = animationGift.getAnimationPrams();
            mStatus = STATUS_PLAYING;
            load(parent, animationGift.getSourceURL(), giftParamModel);
            mUiHanlder.removeMessages(MSG_ENSURE_FINISH);
            // TODO: 2019/5/8 时间可以根据动画加上一点做保护
            mUiHanlder.sendEmptyMessageDelayed(MSG_ENSURE_FINISH, giftParamModel.getDuration() + 5000);
        }
    }

    private void load(RelativeLayout parent, String url, AnimationGift.AnimationPrams animationPrams) {
        if (TextUtils.isEmpty(url)) {
            onFinish();
            return;
        }
        SvgaParserAdapter.parse(url, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity videoItem) {
                onLoadComplete(parent, animationPrams, videoItem);
            }

            @Override
            public void onError() {
                onFinish();
            }
        });
    }

    private void onLoadComplete(RelativeLayout parent, AnimationGift.AnimationPrams animationPrams, SVGAVideoEntity videoItem) {
        SVGADrawable drawable = new SVGADrawable(videoItem);
        if (parent.indexOfChild(mSVGAImageView) < 0) {
            // 确定尺寸和位置
            if (animationPrams.isFullScreen()) {
                // 全屏
                double realWidth = videoItem.getVideoSize().getWidth();
                double realHeight = videoItem.getVideoSize().getHeight();
                if (animationPrams.isFullX()) {
                    // 横向平铺
                    if (realWidth != 0) {
                        int width = U.getDisplayUtils().getScreenWidth();
                        int height = (int) (realHeight * width / realWidth);
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
                        // 确定位置
                        if (animationPrams.getBottom() != -1) {
                            // 距离底部多远
                            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            lp.bottomMargin = U.getDisplayUtils().dip2px(animationPrams.getBottom());
                            parent.addView(mSVGAImageView, lp);
                        } else if (animationPrams.getTop() != -1) {
                            // 距离顶部多远
                            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            lp.topMargin = U.getDisplayUtils().dip2px(animationPrams.getTop());
                            parent.addView(mSVGAImageView, lp);
                        } else {
                            // 顶部和底部无要求，则居中
                            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                            parent.addView(mSVGAImageView, lp);
                        }
                    } else {
                        MyLog.w(TAG, "onLoadComplete" + " parent=" + parent + " animationPrams=" + animationPrams + " videoItem=" + videoItem + " realWidth = 0");
                    }
                } else {
                    // 纵向平铺
                    if (realHeight != 0) {
                        int height = U.getDisplayUtils().getScreenHeight();
                        int width = (int) (realWidth * height / realHeight);
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
                        // 确定位置
                        if (animationPrams.getLeft() != -1) {
                            // 距离左边多远
                            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                            lp.leftMargin = U.getDisplayUtils().dip2px(animationPrams.getLeft());
                            parent.addView(mSVGAImageView, lp);
                        } else if (animationPrams.getRight() != -1) {
                            // 距离右边多远
                            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            lp.rightMargin = U.getDisplayUtils().dip2px(animationPrams.getRight());
                            parent.addView(mSVGAImageView, lp);
                        } else {
                            // 左部和右部无要求，则居中
                            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                            parent.addView(mSVGAImageView, lp);
                        }
                    } else {
                        MyLog.w(TAG, "onLoadComplete" + " parent=" + parent + " animationPrams=" + animationPrams + " videoItem=" + videoItem + "realHeight = 0");
                    }
                }
            } else {
                // 非全屏幕
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(animationPrams.getWidth()),
                        U.getDisplayUtils().dip2px(animationPrams.getHeight()));
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                parent.addView(mSVGAImageView, lp);
            }
        }
        mSVGAImageView.setImageDrawable(drawable);
        mSVGAImageView.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mSVGAImageView != null) {
                    mSVGAImageView.setCallback(null);
                    mSVGAImageView.stopAnimation(true);
                }
                GiftBigAnimationView.this.onFinish();
            }

            @Override
            public void onRepeat() {
                if (mSVGAImageView != null && mSVGAImageView.isAnimating()) {
                    mSVGAImageView.stopAnimation(true);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
        mSVGAImageView.startAnimation();
    }

    private void onFinish() {
        if (mStatus == STATUS_PLAYING) {
            mUiHanlder.post(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onFinished(GiftBigAnimationView.this, mGiftPlayModel);
                    }
                    mStatus = STATUS_IDLE;
                    ViewGroup vg = (ViewGroup) mSVGAImageView.getParent();
                    if (vg != null) {
                        vg.removeView(mSVGAImageView);
                    }
                    mUiHanlder.removeCallbacksAndMessages(MSG_ENSURE_FINISH);
                }
            });
        }
    }

    public boolean isIdle() {
        return mStatus == STATUS_IDLE;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void destroy() {
        if (mSVGAImageView != null) {
            mSVGAImageView.setCallback(null);
            mSVGAImageView.stopAnimation(true);
        }
        mUiHanlder.removeCallbacksAndMessages(null);
    }

    public void reset() {
        if (mSVGAImageView != null) {
            mSVGAImageView.stopAnimation(true);
        }
    }

    public interface Listener {
        void onFinished(GiftBigAnimationView animationView, GiftPlayModel giftPlayModel);
    }
}
