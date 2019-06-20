package com.module.playways.grab.room.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.U;

import java.util.HashMap;
import java.util.Map;

// 提示
public class GameTipsManager {

    private Map<String, GameTipsView> mGameTipsViewMap;

    private boolean isShowing(String tag) {
        if (mGameTipsViewMap != null) {
            return mGameTipsViewMap.containsKey(tag);
        }
        return false;
    }

    public void dismiss(String tag) {
        dismiss(tag, null);
    }

    public void dismiss(String tag, Activity activity) {
        if (mGameTipsViewMap == null || !mGameTipsViewMap.containsKey(tag)) {
            return;
        }
        GameTipsView gameTipsView = mGameTipsViewMap.get(tag);
        if (gameTipsView != null) {
            if (activity != null) {
                if (activity.isDestroyed() || activity.isFinishing()) {

                } else {
                    gameTipsView.dismiss();
                    mGameTipsViewMap.remove(tag);
                }
            } else {
                gameTipsView.dismiss();
                mGameTipsViewMap.remove(tag);
            }
        }
    }

    private void save(String tag, GameTipsView view) {
        dismiss(tag);
        if (mGameTipsViewMap == null) {
            mGameTipsViewMap = new HashMap<>();
        }
        mGameTipsViewMap.put(tag, view);
    }

    public void destory() {
        if (mGameTipsViewMap == null) {
            return;
        }

        for (Map.Entry<String, GameTipsView> entry : mGameTipsViewMap.entrySet()) {
            entry.getValue().dismiss();
        }

        mGameTipsViewMap = null;
    }

    public View getViewByKey(String tag) {
        if (mGameTipsViewMap != null) {
            GameTipsView gameTipsView = mGameTipsViewMap.get(tag);
            if (gameTipsView != null) {
                return gameTipsView.getTipsView();
            }
        }
        return null;
    }

    public void setBaseTranslateY(String tag, int baseTranslateY) {
        if (mGameTipsViewMap != null) {
            GameTipsView gameTipsView = mGameTipsViewMap.get(tag);
            if (gameTipsView != null) {
                gameTipsView.setBaseTranslateY(baseTranslateY);
            }
        }
    }


    public static final class GameTipsView {
        private View tipsView; // view 本体，如果不特殊设置的话，就是imageView
        private int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        private int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        private int resId;
        private int leftMargin = 0;
        private int rightMargin = 0;
        private int topMargin = 0;
        private int bottomMargin = 0;
        private ViewGroup parentView;
        private Map<Integer, Integer> rulesMap = new HashMap<>();
        private int index = -1;
        private View indexView = null;
        private String tag;
        private boolean hasAnimation = false;
        private int maxShowCount = -1; // 最大显示次数，绑定app生命周期，会存入sharepref，-1代表不限制
        private Animator tipViewAnimator; // 该view 的显示的动画，如果不特殊设置就是抖动动画
        private Activity activity;
        private int mBaseTranslateY;// 基于的Y偏移，会在这个基础上播动画啊

        public GameTipsView(ViewGroup viewGroup, int resId) {
            this.parentView = viewGroup;
            this.resId = resId;
        }

        public GameTipsView(ViewGroup viewGroup, View tipsView) {
            this.parentView = viewGroup;
            this.tipsView = tipsView;
        }

        public GameTipsView setActivity(Activity activity) {
            this.activity = activity;
            return this;
        }

        public GameTipsView setTipsView(View tipsView) {
            this.tipsView = tipsView;
            return this;
        }

        public View getTipsView() {
            return tipsView;
        }

        public GameTipsView setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public GameTipsView setImgRes(int val) {
            resId = val;
            return this;
        }

        public GameTipsView setIndex(int val) {
            index = val;
            return this;
        }

        public GameTipsView setIndexView(View view) {
            indexView = view;
            return this;
        }

        public GameTipsView setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public GameTipsView addRule(int verb, int subject) {
            rulesMap.put(verb, subject);
            return this;
        }

        public GameTipsView hasAnimation(boolean hasAnimation) {
            this.hasAnimation = hasAnimation;
            return this;
        }

        public GameTipsView setAniamtion(Animator aniamtion) {
            this.tipViewAnimator = aniamtion;
            return this;
        }

        public GameTipsView setLeftMargin(int leftMargin) {
            this.leftMargin = leftMargin;
            return this;
        }

        public GameTipsView setRightMargin(int rightMargin) {
            this.rightMargin = rightMargin;
            return this;
        }

        public GameTipsView setTopMargin(int topMargin) {
            this.topMargin = topMargin;
            return this;
        }

        public GameTipsView setBottomMargin(int bottomMargin) {
            this.bottomMargin = bottomMargin;
            return this;
        }

        public GameTipsView setMargins(int left, int top, int right, int bottom) {
            leftMargin = left;
            topMargin = top;
            rightMargin = right;
            bottomMargin = bottom;
            return this;
        }

        public GameTipsView setShowCount(int count) {
            this.maxShowCount = count;
            return this;
        }

        public GameTipsView setBaseTranslateY(int baseTranslateY) {
            mBaseTranslateY = baseTranslateY;
            return this;
        }

        protected void startAnimation() {
            if (tipViewAnimator != null && tipViewAnimator.isRunning()) {
                if (tipViewAnimator instanceof ValueAnimator) {
                    ((ValueAnimator) tipViewAnimator).removeAllUpdateListeners();
                }
                tipViewAnimator.removeAllListeners();
                tipViewAnimator.cancel();
            }
            if (tipViewAnimator == null) {
                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 20, 0);
                valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                valueAnimator.setDuration(2500);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        boolean hasSetableView = false;
                        if (tipsView != null && tipsView.getParent() != null) {
                            hasSetableView = true;
                            tipsView.setTranslationY((int) animation.getAnimatedValue() + mBaseTranslateY);
                        }

                        if (!hasSetableView) {
                            tipViewAnimator.cancel();
                        }
                    }
                });
                tipViewAnimator = valueAnimator;
            } else {
                tipViewAnimator.setTarget(tipsView);
            }
            tipViewAnimator.start();
        }

        protected void dismiss() {
            if (tipViewAnimator != null) {
                if (tipViewAnimator instanceof ValueAnimator) {
                    ((ValueAnimator) tipViewAnimator).removeAllUpdateListeners();
                }
                tipViewAnimator.removeAllListeners();
                tipViewAnimator.cancel();
            }
            if (parentView != null) {
                parentView.removeView(tipsView);
            }
        }

        public GameTipsView tryShow(GameTipsManager gameTipsManager) {
            if (activity != null) {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return null;
                }
            }
            // 判断是否已经显示了
            if (gameTipsManager.isShowing(tag)) {
                // 已经显示了
                return null;
            }
            boolean needShow = true;
            if (maxShowCount <= 0) {
                // 不限制显示次数
            } else {
                // 会限制显示次数
                if (!TextUtils.isEmpty(tag)) {
                    int showTimes = U.getPreferenceUtils().getSettingInt(tag, 0);
                    if (showTimes < maxShowCount) {
                        // 次数还有
                        U.getPreferenceUtils().setSettingInt(tag, showTimes + 1);
                    } else {
                        needShow = false;
                    }
                } else {

                }
            }
            if (!needShow) {
                return null;
            }

            if (tipsView == null) {
                ImageView imageView = new ImageView(parentView.getContext());
                imageView.setImageResource(resId);
                tipsView = imageView;
            }

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
            layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
            for (Map.Entry<Integer, Integer> entry : rulesMap.entrySet()) {
                layoutParams.addRule(entry.getKey(), entry.getValue());
            }
            tipsView.setLayoutParams(layoutParams);

            if (indexView != null) {
                index = parentView.indexOfChild(indexView);
            }
            if (index == -1) {
                parentView.addView(tipsView);
            } else {
                parentView.addView(tipsView, index);
            }
            gameTipsManager.save(tag, this);

            if (hasAnimation) {
                startAnimation();
            }else{
                tipsView.setTranslationY(mBaseTranslateY);
            }
            return this;
        }


    }

}


