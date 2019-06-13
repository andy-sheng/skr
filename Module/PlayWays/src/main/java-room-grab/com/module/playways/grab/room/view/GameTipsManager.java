package com.module.playways.grab.room.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.U;

import java.util.HashMap;
import java.util.Map;

// 提示
public class GameTipsManager {

    private static Map<String, GameTipsView> mGameTipsViewMap;
    private static Map<String, Integer> mGameTipsTimeMap;  // 记录每个tag已经展示的次数

    private static class GameTipsManagerHolder {
        private static final GameTipsManager INSTANCE = new GameTipsManager();
    }

    public static final GameTipsManager getInstance() {
        return GameTipsManagerHolder.INSTANCE;
    }

    private GameTipsManager() {

    }

    public void dismiss(String tag) {
        if (mGameTipsViewMap == null || !mGameTipsViewMap.containsKey(tag)) {
            return;
        }

        GameTipsView gameTipsView = mGameTipsViewMap.get(tag);
        if (gameTipsView != null) {
            gameTipsView.dismiss();
            mGameTipsViewMap.remove(tag);
        }
    }

    public void show(String tag) {
        if (mGameTipsViewMap == null || !mGameTipsViewMap.containsKey(tag)) {
            return;
        }

        GameTipsView gameTipsView = mGameTipsViewMap.get(tag);
        if (gameTipsView != null) {
            gameTipsView.show();
        }
    }

    public void destory() {
        if (mGameTipsViewMap == null) {
            return;
        }

        for (Map.Entry<String, GameTipsView> entry : mGameTipsViewMap.entrySet()) {
            entry.getValue().dismiss();
        }

        mGameTipsViewMap = null;
        mGameTipsTimeMap = null;
    }


    public static final class Builder {
        private Context mContext;
        private int mWidth;
        private int mHeight;
        private int resId;
        private int leftMargin;
        private int rightMargin;
        private int topMargin;
        private int bottomMargin;

        private ViewGroup mViewGroup;
        private Map<Integer, Integer> mMap = new HashMap<>();
        private int index = -1;

        private String mTag;

        private boolean mHasAnimation;
        ValueAnimator mTipViewAnimator;

        private int mCount = -1;

        public Builder(Context context, ViewGroup viewGroup) {
            mContext = context;
            mViewGroup = viewGroup;
        }

        public Builder setSize(int width, int height) {
            mWidth = width;
            mHeight = height;
            return this;
        }

        public Builder setImgRes(int val) {
            resId = val;
            return this;
        }

        public Builder setIndex(int val) {
            index = val;
            return this;
        }

        public Builder setTag(String tag) {
            mTag = tag;
            return this;
        }

        public Builder addRule(int verb, int subject) {
            mMap.put(verb, subject);
            return this;
        }

        public Builder hasAnimation(boolean hasAnimation) {
            mHasAnimation = hasAnimation;
            return this;
        }

        public Builder setMargins(int left, int top, int right, int bottom) {
            leftMargin = left;
            topMargin = top;
            rightMargin = right;
            bottomMargin = bottom;
            return this;
        }

        public Builder setShowCount(int count) {
            this.mCount = count;
            return this;
        }

        // TODO: 2019-06-12 可能返回一个空
        public GameTipsView build() {
            if (mCount != -1 && !TextUtils.isEmpty(mTag)) {
                if (mGameTipsTimeMap != null && mGameTipsTimeMap.containsKey(mTag)) {
                    if (mGameTipsTimeMap.get(mTag) < mCount) {
                        // 次数还够
                    } else {
                        return null;
                    }
                } else {
                    int showTimes = U.getPreferenceUtils().getSettingInt(mTag, 0);
                    if (mGameTipsTimeMap == null) {
                        mGameTipsTimeMap = new HashMap<>();
                    }
                    mGameTipsTimeMap.put(mTag, showTimes);
                    if (showTimes < mCount) {
                        // 次数还够
                    } else {
                        return null;
                    }
                }
            }

            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(resId);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mWidth, mHeight);
            layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
            for (Map.Entry<Integer, Integer> entry : mMap.entrySet()) {
                layoutParams.addRule(entry.getKey(), entry.getValue());
            }
            imageView.setLayoutParams(layoutParams);

            GameTipsView gameTipsView = new GameTipsView(imageView, this);
            if (mGameTipsViewMap == null) {
                mGameTipsViewMap = new HashMap<>();
            }
            if (mGameTipsViewMap.containsKey(mTag)) {
                mGameTipsViewMap.get(mTag).dismiss();
            }
            mGameTipsViewMap.put(mTag, gameTipsView);

            return gameTipsView;
        }
    }


    public static class GameTipsView {
        Activity mActivity;
        ImageView mImageView;
        ViewGroup mViewGroup;
        int index;
        boolean hasAnimation;
        ValueAnimator mTipViewAnimator;
        int mCount;
        String mTag;

        public GameTipsView(ImageView imageView, Builder builder) {
            this.mImageView = imageView;
            this.mViewGroup = builder.mViewGroup;
            this.index = builder.index;
            this.hasAnimation = builder.mHasAnimation;
            this.mTipViewAnimator = builder.mTipViewAnimator;

            this.mCount = builder.mCount;
            this.mTag = builder.mTag;
        }

        public ImageView getImageView() {
            return mImageView;
        }

        public void setImageView(ImageView imageView) {
            mImageView = imageView;
        }

        public ViewGroup getViewGroup() {
            return mViewGroup;
        }

        public void setViewGroup(ViewGroup viewGroup) {
            mViewGroup = viewGroup;
        }

        public void dismiss() {
            if (mTipViewAnimator != null) {
                mTipViewAnimator.removeAllUpdateListeners();
                mTipViewAnimator.cancel();
            }
            if (mImageView != null && mViewGroup != null) {
                mViewGroup.removeView(mImageView);
                mImageView = null;
            }
        }

        public void show() {
            if (mImageView != null && mViewGroup != null) {
                if (mCount != -1 && !TextUtils.isEmpty(mTag)) {
                    if (mGameTipsTimeMap != null && mGameTipsTimeMap.containsKey(mTag)) {
                        int showTime = mGameTipsTimeMap.get(mTag);
                        if (showTime < mCount) {
                            mGameTipsTimeMap.put(mTag, showTime + 1);
                            U.getPreferenceUtils().setSettingInt(mTag, showTime + 1);
                        } else {
                            return;
                        }
                    } else {
                        int showTime = U.getPreferenceUtils().getSettingInt(mTag, 0);
                        if (mGameTipsTimeMap == null) {
                            mGameTipsTimeMap = new HashMap<>();
                        }
                        mGameTipsTimeMap.put(mTag, showTime + 1);
                        U.getPreferenceUtils().setSettingInt(mTag, showTime + 1);
                    }
                }

                if (index == -1) {
                    mViewGroup.addView(mImageView);
                } else {
                    mViewGroup.addView(mImageView, index);
                }
                if (hasAnimation) {
                    tipViewAnimate(mImageView);
                }
            }
        }

        public void tipViewAnimate(View... viewList) {
            if (mTipViewAnimator != null) {
                mTipViewAnimator.removeAllUpdateListeners();
                mTipViewAnimator.cancel();
            }
            mTipViewAnimator = ValueAnimator.ofInt(0, 20, 0);
            mTipViewAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mTipViewAnimator.setDuration(2500);
            mTipViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    boolean hasSetableView = false;
                    for (View view : viewList) {
                        if (view != null && view.getParent() != null) {
                            hasSetableView = true;
                            view.setTranslationY((int) animation.getAnimatedValue());
                        }
                    }

                    if (!hasSetableView) {
                        mTipViewAnimator.cancel();
                    }
                }
            });
            mTipViewAnimator.start();
        }
    }
}


