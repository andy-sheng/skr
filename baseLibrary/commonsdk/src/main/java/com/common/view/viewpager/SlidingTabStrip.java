/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.common.view.viewpager;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import com.common.base.R;
import com.common.log.MyLog;

class SlidingTabStrip extends LinearLayout {

    private static final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 0;
    private static final byte DEFAULT_BOTTOM_BORDER_COLOR_ALPHA = 0x26;
    private static final float SELECTED_INDICATOR_THICKNESS_DIPS = 1.33f;//4px
    private static final int DEFAULT_SELECTED_INDICATOR_COLOR = 0xFF33B5E5;
    private static final int AVOID_DITHERING_THRESHOLD = 4; // 防抖动阈值

    private final int mBottomBorderThickness;
    private final Paint mBottomBorderPaint;

    private float mSelectedIndicatorThickness;
    private final Paint mSelectedIndicatorPaint;

    private final int mDefaultBottomBorderColor;

    private int mSelectedPosition;
    private float mSelectionOffset;
    private float mLastRight;

    private SlidingTabLayout.TabColorizer mCustomTabColorizer;
    private final SimpleTabColorizer mDefaultTabColorizer;
    /**
     * 指示器图标底部与TabStrip底部之间的距离
     */
    private int mIndicatorBottomMargin;
    /**
     * 指示器图标顶部与Tab标题底部之间的距离
     */
    private int mIndicatorTopMargin;
    private SlidingTabLayout.ITabNameBottomPositionGetter mTabNameBottomPositionGetter;
    private int mIndicatorWidth;
    private float mIndicatorCornorRadius;
    private GradientDrawable mIndicatorDrawable;

    private boolean mIsTabAsDividerMode;
    private int mIndicatorAnimationMode = SlidingTabLayout.ANI_MODE_NORMAL;

    SlidingTabStrip(Context context) {
        this(context, null);
    }

    SlidingTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        final float density = getResources().getDisplayMetrics().density;

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorForeground, outValue, true);
        final int themeForegroundColor = outValue.data;

        mDefaultBottomBorderColor = setColorAlpha(themeForegroundColor,
                DEFAULT_BOTTOM_BORDER_COLOR_ALPHA);

        mDefaultTabColorizer = new SimpleTabColorizer();
        mDefaultTabColorizer.setIndicatorColors(DEFAULT_SELECTED_INDICATOR_COLOR);

        mBottomBorderThickness = (int) (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density);
        mBottomBorderPaint = new Paint();
        mBottomBorderPaint.setColor(mDefaultBottomBorderColor);

        mIndicatorCornorRadius = getResources().getDimension(R.dimen.view_dimen_2);

        mSelectedIndicatorThickness = (SELECTED_INDICATOR_THICKNESS_DIPS * density);
        mSelectedIndicatorPaint = new Paint();
    }

    void setCustomTabColorizer(SlidingTabLayout.TabColorizer customTabColorizer) {
        mCustomTabColorizer = customTabColorizer;
        invalidate();
    }

    void setSelectedIndicatorColors(@ColorInt int... colors) {
        // Make sure that the custom colorizer is removed
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setIndicatorColors(colors);
        invalidate();
    }

    /**
     * @param position       tabPosition, not child index
     * @param positionOffset
     */
    void onViewPagerPageChanged(int position, float positionOffset, boolean fromSrolling) {
        mSelectedPosition = getChildIndex(position);

        float right = -1;
        if (getChildCount() > 0) {
            View v = getChildAt(mSelectedPosition);
            if (null != v) {
                right = v.getRight();
            }
        }
        if (positionOffset == 0 && mIndicatorAnimationMode!=SlidingTabLayout.ANI_MODE_NONE) { // 防抖动
            if (mSelectionOffset > 0 && right != -1 && Math.abs(mLastRight - right) >= AVOID_DITHERING_THRESHOLD) {
                ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
                animator.setDuration(100);
                final float finalRight = right;
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float currentValue = (float) animation.getAnimatedValue();
                        mSelectionOffset = mLastRight < finalRight ? mSelectionOffset + (1 - mSelectionOffset) * (1 - currentValue) : mSelectionOffset * currentValue;
                        invalidate();
                    }
                });
                animator.setInterpolator(new AccelerateInterpolator());
                animator.start();
                return;
            }
        }
        mSelectionOffset = positionOffset;
        if (mIndicatorAnimationMode == SlidingTabLayout.ANI_MODE_NONE) {
            if (!fromSrolling) {
                invalidate();
            }
        } else {
            invalidate();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int height = getHeight();
        final int childCount = getChildCount();
        final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
                ? mCustomTabColorizer
                : mDefaultTabColorizer;
        MyLog.d("SlidingTabStrip","mSelectedPosition="+mSelectedPosition);

        // Thick colored underline below the current selection
        if (childCount > 0) {
            View selectedTitle = getChildAt(mSelectedPosition);
            if (null != selectedTitle) {
                float left = selectedTitle.getLeft();
                float right = selectedTitle.getRight();
                float leftMargin = 0;
                if (mIndicatorAnimationMode == SlidingTabLayout.ANI_MODE_TAIL && mIndicatorWidth > 0) {
                    leftMargin = (right - left - mIndicatorWidth) / 2.0f;
                }
                int color = tabColorizer.getIndicatorColor(getTabIndex(mSelectedPosition));

                if (mSelectionOffset > 0 && mSelectedPosition < (getChildCount() - 1)) {
                    int nextColor = tabColorizer.getIndicatorColor(getTabIndex(mSelectedPosition + 1));
                    if (color != nextColor) {
                        color = blendColors(nextColor, color, mSelectionOffset);
                    }

                    // Draw the selection partway between the tabs
                    View nextTitle = getChildAt(mSelectedPosition + 1);

                    switch (mIndicatorAnimationMode) {
                        case SlidingTabLayout.ANI_MODE_TAIL: {
                            // 带小尾巴效果
                            //left = (float) (Math.pow(mSelectionOffset, 2) * (nextTitle.getLeft() - left) + left);
                            //right = (float) (Math.sqrt(mSelectionOffset) * (nextTitle.getRight() - right) + right);
                            int moveDimen = (nextTitle.getWidth() + selectedTitle.getWidth()) / 2;
                            left += Math.pow(mSelectionOffset, 2) * moveDimen;
                            right += Math.sqrt(mSelectionOffset) * moveDimen;
                        }
                        break;
                        default: {
                            // 无缩放平移效果
                            left = (int) (mSelectionOffset * nextTitle.getLeft() +
                                    (1.0f - mSelectionOffset) * left);
                            right = (int) (mSelectionOffset * nextTitle.getRight() +
                                    (1.0f - mSelectionOffset) * right);
                        }
                        break;
                    }
                    mLastRight = right;
                }


                if ((mIndicatorAnimationMode == SlidingTabLayout.ANI_MODE_NORMAL
                        || mIndicatorAnimationMode == SlidingTabLayout.ANI_MODE_NONE)
                        && mIndicatorWidth > 0) {
                    leftMargin = (right - left - mIndicatorWidth) / 2.0f;
                }
                // 绘制Indicator（指示器）
                mSelectedIndicatorPaint.setColor(color);
                if (mIndicatorTopMargin > 0) {
                    // 画圆角矩形
                    int tabTitleBottom = mTabNameBottomPositionGetter.getTabNameBottomPosition(selectedTitle);
                    drawRoundRect((int) (left + leftMargin),
                            tabTitleBottom + mIndicatorTopMargin,
                            (int) (right - leftMargin),
                            (int) (tabTitleBottom + mIndicatorTopMargin + mSelectedIndicatorThickness),
                            color, canvas);

                    // 画矩形
                    //canvas.drawRect(left + leftMargin, tabTitleBottom + mIndicatorTopMargin, right - leftMargin,
                    //        tabTitleBottom + mIndicatorTopMargin + mSelectedIndicatorThickness, mSelectedIndicatorPaint);
                } else {
                    // 画圆角矩形
                    drawRoundRect((int) (left + leftMargin),
                            (int) (height - mIndicatorBottomMargin - mSelectedIndicatorThickness),
                            (int) (right - leftMargin),
                            height - mIndicatorBottomMargin,
                            color, canvas);

                    //// 画矩形
                    //canvas.drawRect(left + leftMargin, height - mIndicatorBottomMargin - mSelectedIndicatorThickness, right - leftMargin,
                    //        height - mIndicatorBottomMargin, mSelectedIndicatorPaint);
                }
            }
        }
        // Thin underline along the entire bottom edge
        canvas.drawRect(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);
    }

    public void setIndicatorBottomMargin(int indicatorBottomMargin) {
        mIndicatorBottomMargin = indicatorBottomMargin;
        mIndicatorTopMargin = 0;
        mTabNameBottomPositionGetter = null;
        invalidate();
    }

    public void setIndicatorTopMargin(int indicatorTopMargin, @NonNull SlidingTabLayout.ITabNameBottomPositionGetter positionGetter) {
        mIndicatorTopMargin = indicatorTopMargin;
        mTabNameBottomPositionGetter = positionGetter;
        mIndicatorBottomMargin = 0;
        invalidate();
    }

    public void setIndicatorWidth(int indicatorWidth) {
        mIndicatorWidth = indicatorWidth;
        invalidate();
    }

    public void setSelectedIndicatorThickness(float mSelectedIndicatorThickness) {
        this.mSelectedIndicatorThickness = mSelectedIndicatorThickness;
    }

    public void setIndicatorCornorRadius(float indicatorCornorRadius) {
        this.mIndicatorCornorRadius = indicatorCornorRadius;
    }

    /**
     * Set the alpha value of the {@code color} to be the given {@code alpha} value.
     */
    private static int setColorAlpha(int color, byte alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 1.0 will return {@code color1}, 0.5 will give an even blend,
     *              0.0 will return {@code color2}.
     */
    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    private static class SimpleTabColorizer implements SlidingTabLayout.TabColorizer {
        private int[] mIndicatorColors;

        @Override
        public final int getIndicatorColor(int position) {
            return mIndicatorColors[position % mIndicatorColors.length];
        }

        void setIndicatorColors(@ColorInt int... colors) {
            mIndicatorColors = colors;
        }
    }

    public void setTabAsDividerMode(boolean isTabAsDividerMode) {
        mIsTabAsDividerMode = isTabAsDividerMode;
    }

    private int getChildIndex(int tabIndex) {
        if (mIsTabAsDividerMode) {
            tabIndex++;
        }
        return tabIndex;
    }

    private int getTabIndex(int childIndex) {
        if (mIsTabAsDividerMode) {
            childIndex--;
        }
        return childIndex;
    }

    private void drawRoundRect(int l, int t, int r, int b, @ColorInt int color, @NonNull Canvas canvas) {
        if (mIndicatorDrawable == null) {
            mIndicatorDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.tab_indicator);
        }
        mIndicatorDrawable.setBounds(l, t, r, b);
        mIndicatorDrawable.setColor(color);
        mIndicatorDrawable.setCornerRadius(mIndicatorCornorRadius);
        mIndicatorDrawable.draw(canvas);
    }

    public void setIndicatorAnimationMode(int mode) {
        mIndicatorAnimationMode = mode;
    }
    public int getIndicatorAnimationMode() {
        return mIndicatorAnimationMode;
    }
}