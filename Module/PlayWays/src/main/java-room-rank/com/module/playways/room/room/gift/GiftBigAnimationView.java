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
import com.module.playways.room.room.gift.model.GiftPlayModel;
import com.module.playways.BaseRoomData;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class GiftBigAnimationView {
    static final int MSG_ENSURE_FINISH = 91;

    static final int STATUS_IDLE = 1;
    static final int STATUS_PLAYING = 2;
    int mStatus = STATUS_IDLE;
    Listener mListener;

    SVGAImageView mSVGAImageView;
    GiftPlayModel mGiftPlayModel;

    Random mRandom = new Random();

    Handler mUiHanlder = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_ENSURE_FINISH) {
                onFinish();
            }
        }
    };

    public GiftBigAnimationView(@Nullable Context context) {
        init();
    }

    private void init() {
        mSVGAImageView = new SVGAImageView(U.app());
    }

    public void play(RelativeLayout parent, GiftPlayModel giftPlayModel) {
        if (parent.indexOfChild(mSVGAImageView) < 0) {
            int translateX = U.getDisplayUtils().dip2px(mRandom.nextInt(200) - 100);
            int translateY = U.getDisplayUtils().dip2px(mRandom.nextInt(200) - 100);
            SLocation l = new SLocation(0.67f + mRandom.nextFloat() / 3f, translateX, translateY);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            parent.addView(mSVGAImageView, lp);

            // 变化一下位置
            mSVGAImageView.setScaleX(l.scale);
            mSVGAImageView.setScaleY(l.scale);
            mSVGAImageView.setTranslationX(l.translateX);
            mSVGAImageView.setTranslationY(l.translateY);
        }
        mStatus = STATUS_PLAYING;
        mGiftPlayModel = giftPlayModel;
        String url = null;
        switch (giftPlayModel.getEmojiType()) {
            case SP_EMOJI_TYPE_UNLIKE:
                url = BaseRoomData.ROOM_SPECAIL_EMOJI_DABIAN;
                break;
            case SP_EMOJI_TYPE_LIKE:
                url = BaseRoomData.ROOM_SPECAIL_EMOJI_AIXIN;
                break;
        }
        load(url);
        mUiHanlder.removeMessages(MSG_ENSURE_FINISH);
        mUiHanlder.sendEmptyMessageDelayed(MSG_ENSURE_FINISH, 5000);
    }

    private void load(String url) {
        if (TextUtils.isEmpty(url)) {
            onFinish();
            return;
        }
        SvgaParserAdapter.parse( url, new SVGAParser.ParseCompletion() {
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

    public static class SLocation {
        float scale = 1.0f;
        float translateX = 0;
        float translateY = 0;

        public SLocation(float scale, float translateX, float translateY) {
            this.scale = scale;
            this.translateX = translateX;
            this.translateY = translateY;
        }
    }
}
