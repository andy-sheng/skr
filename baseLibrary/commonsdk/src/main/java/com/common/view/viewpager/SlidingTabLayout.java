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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;


/**
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as to
 * the user's scroll progress.
 * <p/>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link android.support.v4.app.Fragment} call
 * {@link #setViewPager(ViewPager)} providing it the ViewPager this layout is being used for.
 * <p/>
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via {@link #setSelectedIndicatorColors(int...)}. The
 * alternative is via the {@link TabColorizer} interface which provides you complete control over
 * which color is used for any individual position.
 * <p/>
 * The views used as tabs can be customized by calling {@link #setCustomTabView(int, int)},
 * providing the layout ID of your custom layout.
 */
public class SlidingTabLayout extends HorizontalScrollView {
    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * {@link #setCustomTabColorizer(TabColorizer)}.
     */
    public interface TabColorizer {

        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        @ColorInt
        int getIndicatorColor(int position);

    }

    private static final int TITLE_OFFSET_DIPS = 24;
    private static final int TAB_VIEW_PADDING_DIPS = 16;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 12;

    /**
     * 不对加入TabStrip的每个Tab的LayoutParams做任何处理
     */
    public static final int DISTRIBUTE_MODE_NONE = 0;
    /**
     * 如果有n个Tab，则把屏幕宽度分为n部分，Tab的标题在每部分居中
     */
    public static final int DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER = 1;
    /**
     * 如果有n个Tab，则把屏幕宽度分为n+1部分，Tab的标题的中线与每部分的分隔线重合
     */
    public static final int DISTRIBUTE_MODE_TAB_AS_DIVIDER = 2;

    /**
     * 正常的平移变化效果
     */
    public static final int ANI_MODE_NORMAL = 0;
    /**
     * 带小尾巴效果，一级界面用的效果，@author zhaomin
     */
    public static final int ANI_MODE_TAIL = 1;
    /**
     * 无平移过渡效果
     */
    public static final int ANI_MODE_NONE = 2;

    private int mTitleOffset;

    private int mTabViewLayoutId;
    private int mTabViewTextViewId;
    private int mDistributeMode = DISTRIBUTE_MODE_NONE;
    private float titleSize = 0;
    private float selectedTitleSize = 0;
    private ViewPager mViewPager;
    private SparseArray<String> mContentDescriptions = new SparseArray<String>();
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;
    private CustomUiListener mCustomUiListener;

    private final SlidingTabStrip mTabStrip;

    public SlidingTabStrip getTabStrip() {
        return mTabStrip;
    }

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);
        // Make sure that the Tab Strips fills this View
        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        mTabStrip = new SlidingTabStrip(context);
        // 设定内容居中，chengsimin
        if (mDistributeMode > DISTRIBUTE_MODE_NONE) {
            mTabStrip.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    /**
     * 与{@link #setSelectedIndicatorColors(int...)}二选其一<br>
     * Set the custom {@link TabColorizer} to be used.
     * <p/>
     * If you only require simple custmisation then you can use
     * {@link #setSelectedIndicatorColors(int...)} to achieve
     * similar effects.
     */
    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        mTabStrip.setCustomTabColorizer(tabColorizer);
    }

    /**
     * 与{@link #setCustomTabColorizer(TabColorizer)}二选其一<br>
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(@ColorInt int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    ColorStateList titleColorRes = null;

    public void setSelectedTitleColor(ColorStateList titleColor) {
        this.titleColorRes = titleColor;
    }

    public void setTitleSize(float titleSize) {
        this.titleSize = titleSize;
    }

    public void setSelectedTilleSize(float selectedTitleSize) {
        this.selectedTitleSize = selectedTitleSize;
    }

    /**
     * 设置把屏幕宽度分为和Tab数目相等的等份
     *
     * @see #DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER
     * @see #setDistributeMode(int)
     */
    public void setDistributeEvenly(boolean distributeEvenly) {
        mDistributeMode = distributeEvenly ? DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER : DISTRIBUTE_MODE_NONE;
    }

    /**
     * 设置Tab全宽度分割模式
     *
     * @param distributeMode 可选值：
     *                       <ul>
     *                       <li>{@link #DISTRIBUTE_MODE_NONE}默认</li>
     *                       <li>{@link #DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER}</li>
     *                       <li>{@link #DISTRIBUTE_MODE_TAB_AS_DIVIDER}</li>
     *                       </ul>
     */
    public void setDistributeMode(int distributeMode) {
        mDistributeMode = distributeMode;
        mTabStrip.setTabAsDividerMode(isTabAsDividerMode());
    }

    /**
     * Set the {@link ViewPager.OnPageChangeListener}. When using {@link SlidingTabLayout} you are
     * required to set any {@link ViewPager.OnPageChangeListener} through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId  id of the {@link TextView} in the inflated view
     */
    public void setCustomTabView(int layoutResId, int textViewId) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(ViewPager viewPager) {
        mTabStrip.removeAllViews();

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    /**
     * 设置自定义title listener ,外部好灵活控制显示隐藏title的一部分
     *
     * @param mCustomUiListener
     */
    public void setCustomUiListener(CustomUiListener mCustomUiListener) {
        this.mCustomUiListener = mCustomUiListener;
    }

    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * {@link #setCustomTabView(int, int)}.
     */
    protected TextView createDefaultTabView(Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                outValue, true);
        textView.setBackgroundResource(outValue.resourceId);
        textView.setAllCaps(true);

        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);

        return textView;
    }

    private void populateTabStrip() {
        final PagerAdapter adapter = mViewPager.getAdapter();
        final OnClickListener tabClickListener = new TabClickListener();

        for (int i = 0; i < adapter.getCount(); i++) {
            if (isTabAsDividerMode() && i == 0) {
                addPaddingViewForCenterMode();
            }

            View tabView = null;
            TextView tabTitleView = null;

            if (mTabViewLayoutId != 0) {
                // If there is a custom tab view layout id set, try and inflate it
                tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip, false);
                tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
            }

            if (tabView == null) {
                tabView = createDefaultTabView(getContext());
            }

            if (tabView != null && mCustomUiListener != null) {
                mCustomUiListener.onCustomTitle(tabView, i);
            }

            if (tabTitleView == null && TextView.class.isInstance(tabView)) {
                tabTitleView = (TextView) tabView;
            }

            if (mDistributeMode > DISTRIBUTE_MODE_NONE) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                distributeTab(lp);
            }

            if (titleSize > 0) {
                tabTitleView.setTextSize(titleSize);
            }
            if (titleColorRes != null) {
                tabTitleView.setTextColor(titleColorRes);
            }

            tabTitleView.setText(adapter.getPageTitle(i));

            tabView.setOnClickListener(tabClickListener);
            String desc = mContentDescriptions.get(i, null);
            if (desc != null) {
                tabView.setContentDescription(desc);
            }

            mTabStrip.addView(tabView);
            if (i == mViewPager.getCurrentItem()) {
                tabView.setSelected(true);
                if (selectedTitleSize > 0) {
                    tabTitleView.setTextSize(selectedTitleSize);
                }
            }

            if (isTabAsDividerMode() && i == adapter.getCount() - 1) {
                addPaddingViewForCenterMode();
            }

        }// end-for
    }

    private void addPaddingViewForCenterMode() {
        View paddingView = new View(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        mTabStrip.addView(paddingView, layoutParams);
    }

    private void distributeTab(LinearLayout.LayoutParams lp) {
        switch (mDistributeMode) {
            case DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER:
                lp.width = 0;
                lp.weight = 1;
                break;
            case DISTRIBUTE_MODE_TAB_AS_DIVIDER:
                lp.width = 0;
                lp.weight = 2;
                break;
        }
    }

    public void setContentDescription(int i, String desc) {
        mContentDescriptions.put(i, desc);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }

    }

    private void scrollToTab(int viewPagerTabIndex, int positionOffset) {
        final int tabStripChildCount = getTabStripTabCount();
        if (tabStripChildCount == 0 || viewPagerTabIndex < 0 || viewPagerTabIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(getTabStripChildIndex(viewPagerTabIndex));
        if (selectedChild != null) {

            int targetScrollX = selectedChild.getLeft() + positionOffset;

            if (viewPagerTabIndex > 0 || positionOffset > 0) {
                // If we're not at the first child and are mid-scroll, make sure we obey the offset
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);
        }
    }

    public void setIndicatorBottomMargin(int indicatorBottomMargin) {
        mTabStrip.setIndicatorBottomMargin(indicatorBottomMargin);
    }

    public void setIndicatorTopMargin(int indicatorTopMargin, @NonNull ITabNameBottomPositionGetter positionGetter) {
        mTabStrip.setIndicatorTopMargin(indicatorTopMargin, positionGetter);
    }

    public void setIndicatorWidth(int indicatorWidth) {
        mTabStrip.setIndicatorWidth(indicatorWidth);
    }

    public void setSelectedIndicatorThickness(float mSelectedIndicatorThickness) {
        mTabStrip.setSelectedIndicatorThickness(mSelectedIndicatorThickness);
    }

    public void setIndicatorCornorRadius(float indicatorCornorRadius) {
        mTabStrip.setIndicatorCornorRadius(indicatorCornorRadius);
    }


    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = getTabStripTabCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset, true);
            View selectedTitle = mTabStrip.getChildAt(getTabStripChildIndex(position));
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth())
                    : 0;
            scrollToTab(position, extraOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;
            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mTabStrip.getIndicatorAnimationMode() == SlidingTabLayout.ANI_MODE_NONE) {
                mTabStrip.onViewPagerPageChanged(position, 0f, false);
            } else {
                if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                    mTabStrip.onViewPagerPageChanged(position, 0f, false);
                    scrollToTab(position, 0);
                }
            }
            int tabStripPosition = getTabStripChildIndex(position);
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                mTabStrip.getChildAt(i).setSelected(tabStripPosition == i);
            }
            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }

    }

    private class TabClickListener extends DebounceViewClickListener {
        @Override
        public void clickValid(View tabView) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (tabView == mTabStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(getViewPagerPosition(i));
                    return;
                }
            }
        }
    }


    public interface CustomUiListener {
        public void onCustomTitle(View titleView, int position);
    }

    /**
     * 获取Tab名称底部坐标的接口
     */
    public interface ITabNameBottomPositionGetter {
        int getTabNameBottomPosition(@Nullable View selectedTitle);
    }

    public void notifyDataChange() {
        mTabStrip.removeAllViews();
        populateTabStrip();
    }

    private int getTabStripTabCount() {
        int childCount = mTabStrip.getChildCount();
        if (isTabAsDividerMode()) {
            childCount -= 2;
        }
        return childCount;
    }

    private int getTabStripChildIndex(int viewPagerPosition) {
        if (isTabAsDividerMode()) {
            viewPagerPosition++;
        }
        return viewPagerPosition;
    }

    private int getViewPagerPosition(int tabStripIndex) {
        if (isTabAsDividerMode()) {
            tabStripIndex--;
        }
        return tabStripIndex;
    }

    private boolean isTabAsDividerMode() {
        return mDistributeMode == DISTRIBUTE_MODE_TAB_AS_DIVIDER;
    }

    /**
     * <ul>
     * <li>{@link #ANI_MODE_NORMAL}默认</li>
     * <li>{@link #ANI_MODE_TAIL}</li>
     * </ul>
     *
     * @param mode
     */
    public void setIndicatorAnimationMode(int mode) {
        mTabStrip.setIndicatorAnimationMode(mode);
    }

}