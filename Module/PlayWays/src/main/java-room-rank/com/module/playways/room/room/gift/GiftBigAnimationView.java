package com.module.playways.room.room.gift;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
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
        if(baseGift instanceof AnimationGift){
            AnimationGift animationGift = (AnimationGift) baseGift;
            AnimationGift.AnimationPrams giftParamModel = animationGift.getAnimationPrams();

            if (parent.indexOfChild(mSVGAImageView) < 0) {
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(giftParamModel.getWidth(), giftParamModel.getHeight());
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                parent.addView(mSVGAImageView, lp);
            }
            mStatus = STATUS_PLAYING;
            load(animationGift.getSourceURL());

            mUiHanlder.removeMessages(MSG_ENSURE_FINISH);
            // TODO: 2019/5/8 时间可以根据动画加上一点做保护
            mUiHanlder.sendEmptyMessageDelayed(MSG_ENSURE_FINISH, giftParamModel.getDuration() + 5000);
        }

    }

    private void load(String url) {
        if (TextUtils.isEmpty(url)) {
            onFinish();
            return;
        }
        SvgaParserAdapter.parse(url, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity videoItem) {
                onLoadComplete(videoItem);
            }

            @Override
            public void onError() {
                onFinish();
            }
        });
    }

    private void onLoadComplete(SVGAVideoEntity videoItem) {
        SVGADrawable drawable = new SVGADrawable(videoItem);
        mSVGAImageView.setImageDrawable(drawable);
        mSVGAImageView.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                GiftBigAnimationView.this.onFinish();
            }

            @Override
            public void onRepeat() {
                GiftBigAnimationView.this.onFinish();
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


    public interface Listener {
        void onFinished(GiftBigAnimationView animationView, GiftPlayModel giftPlayModel);
    }
}
