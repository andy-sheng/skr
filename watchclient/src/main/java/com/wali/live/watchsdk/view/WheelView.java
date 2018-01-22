package com.wali.live.watchsdk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;

import java.util.LinkedList;
import java.util.List;

public class WheelView extends View {
    private static final String TAG = WheelView.class.getSimpleName();

    public static final String WHEEL_VIEW_TEXT_SIZE = "WheelViewTextSize";
    public static final String WHEEL_VIEW_ITEM_HEIGHT = "WheelViewItemHeight";
    public static final String WHEEL_VIEW_ITEM_SPACE = "WheelViewItemSpace";

    private static final int DEFAULT_ADDITIONAL_ITEM_HEIGHT = DisplayUtils.dip2px(30);
    //private static final int DEFAULT_ADDITIONAL_ITEMS_SPACE = DisplayUtils.dip2px(-3);
    private static final int DEFAULT_ADDITIONAL_ITEMS_SPACE = 0;
    private static final int SCROLLING_DURATION = 400;

    /**
     * Minimum delta for scrolling
     */
    private static final int MIN_DELTA_FOR_SCROLLING = 1;

    /**
     * Current value & label text color
     */
    private static final int VALUE_TEXT_COLOR = GlobalData.app().getResources().getColor(R.color.color_red_ff2966);

    private static final int VALUE_TEXT_SIZE = DisplayUtils.dip2px(15f);
    private static final int LABLE_TEXT_SIZE = DisplayUtils.dip2px(9f);
    /**
     * Items text color
     */
    private static final int GONE_ITEMS_TEXT_COLOR = GlobalData.app().getResources().getColor(R.color.color_black_trans_30);

    private static final int ITEMS_TEXT_COLOR = GlobalData.app().getResources().getColor(R.color.color_black_trans_50);

    private static final int TEXT_SIZE_DEFAULT = DisplayUtils.dip2px(12f);

    /**
     * Top and bottom items offset (to hide that)
     */
    private static final int ITEM_OFFSET = DisplayUtils.dip2px(10f);

    /**
     * Label offset
     */
    private static int LABEL_TOP_OFFSET = DisplayUtils.dip2px(3f);

    private static int LABEL_OFFSET = DisplayUtils.dip2px(3f);

    /*左右边距*/
    private static int PADDING = DisplayUtils.dip2px(6f);

    /*默认可见数量*/
    private static final int DEF_VISIBLE_ITEMS = 5;

    private WheelAdapter adapter = null;
    private int currentItem = 0;
    private int itemsWidth = 0;
    private int labelWidth = 0;

    /*可见数量*/
    private int visibleItems = DEF_VISIBLE_ITEMS;

    private int itemHeight = 0;
    private TextPaint goneItemsPaint;
    private TextPaint itemsPaint;
    private TextPaint valuePaint;
    private TextPaint lablePaint;

    private StaticLayout goneItemsLayout;
    private StaticLayout itemsLayout;
    private StaticLayout labelLayout;
    private StaticLayout valueLayout;

    // Label & background
    private String label;
    private Drawable centerDrawable;

    // Scrolling
    private boolean isScrollingPerformed;
    private int scrollingOffset;

    // Scrolling animation
    private GestureDetector gestureDetector;
    private Scroller scroller;
    private int lastScrollY;

    // 是否是循环列表
    private boolean isCyclic = false;

    // Listeners
    private List<OnWheelChangedListener> changingListeners = new LinkedList<>();
    private List<OnWheelScrollListener> scrollingListeners = new LinkedList<>();

    private int textSize;

    /**
     * Additional items height (is added to standard text item height)
     */
    private int mAdditionItemHeight = DEFAULT_ADDITIONAL_ITEM_HEIGHT;

    /**
     * Additional width for items layout
     */
    private int mAdditionalItemSpace = DEFAULT_ADDITIONAL_ITEMS_SPACE;

    public WheelView(Context context) {
        super(context);
        textSize = TEXT_SIZE_DEFAULT;

        initData(context);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textSize = attrs.getAttributeIntValue(null, WHEEL_VIEW_TEXT_SIZE, TEXT_SIZE_DEFAULT);
        mAdditionItemHeight = attrs.getAttributeIntValue(null, WHEEL_VIEW_ITEM_HEIGHT, DEFAULT_ADDITIONAL_ITEM_HEIGHT);
        mAdditionalItemSpace = attrs.getAttributeIntValue(null, WHEEL_VIEW_ITEM_SPACE, DEFAULT_ADDITIONAL_ITEMS_SPACE);

        initData(context);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        textSize = attrs.getAttributeIntValue(null, WHEEL_VIEW_TEXT_SIZE, TEXT_SIZE_DEFAULT);
        mAdditionItemHeight = attrs.getAttributeIntValue(null, WHEEL_VIEW_ITEM_HEIGHT, DEFAULT_ADDITIONAL_ITEM_HEIGHT);
        mAdditionalItemSpace = attrs.getAttributeIntValue(null, WHEEL_VIEW_ITEM_SPACE, DEFAULT_ADDITIONAL_ITEMS_SPACE);

        initData(context);
    }

    private void initData(Context context) {
//        textSize = getPxWithM2Px(textSize);
//        mAdditionItemHeight = getPxWithM2Px(mAdditionItemHeight);
//        mAdditionalItemSpace = getPxWithM2Px(mAdditionalItemSpace);

        if (!CommonUtils.isChineseLocale()) {
            LABEL_OFFSET = DisplayUtils.dip2px(8f);
            PADDING = DisplayUtils.dip2px(8f);
        }

        gestureDetector = new GestureDetector(context, gestureListener);
        gestureDetector.setIsLongpressEnabled(false);

        scroller = new Scroller(context);
    }

    public WheelAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(WheelAdapter adapter) {
        this.adapter = adapter;
        invalidateLayouts();
        invalidate();
    }

    public void setInterpolator(Interpolator interpolator) {
        scroller.forceFinished(true);
        scroller = new Scroller(getContext(), interpolator);
    }

    public int getVisibleItems() {
        return visibleItems;
    }

    public void setVisibleItems(int count) {
        visibleItems = count;
        invalidate();
    }

    /**
     * Gets label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets label
     */
    public void setLabel(String newLabel) {
        if (label == null || !label.equals(newLabel)) {
            label = newLabel;
            labelLayout = null;
            invalidate();
        }
    }

    /**
     * Adds wheel changing listener
     */
    public void addChangingListener(OnWheelChangedListener listener) {
        changingListeners.add(listener);
    }

    /**
     * Removes wheel changing listener
     */
    public void removeChangingListener(OnWheelChangedListener listener) {
        changingListeners.remove(listener);
    }

    /**
     * Notifies changing listeners
     */
    protected void notifyChangingListeners(int oldValue, int newValue) {
        for (OnWheelChangedListener listener : changingListeners) {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    /**
     * Adds wheel scrolling listener
     */
    public void addScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.add(listener);
    }

    /**
     * Removes wheel scrolling listener
     */
    public void removeScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.remove(listener);
    }

    /**
     * Notifies listeners about starting scrolling
     */
    protected void notifyScrollingListenersAboutStart() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingStarted(this);
        }
    }

    /**
     * Notifies listeners about ending scrolling
     */
    protected void notifyScrollingListenersAboutEnd() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingFinished(this);
        }
    }

    /**
     * Gets current value
     */
    public int getCurrentItem() {
        return currentItem;
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     */
    public void setCurrentItem(int index, boolean animated) {
        if (adapter == null || adapter.getItemsCount() == 0) {
            return; // throw?
        }
        if (index < 0 || index >= adapter.getItemsCount()) {
            if (isCyclic) {
                while (index < 0) {
                    index += adapter.getItemsCount();
                }
                index %= adapter.getItemsCount();
            } else {
                return; // throw?
            }
        }
        if (index != currentItem) {
            if (animated) {
                scroll(index - currentItem, SCROLLING_DURATION);
            } else {
                invalidateLayouts();

                int old = currentItem;
                currentItem = index;

                notifyChangingListeners(old, currentItem);

                invalidate();
            }
        }
    }

    /**
     * Sets the current item w/o animation. Does nothing when index is wrong.
     */
    public void setCurrentItem(int index) {
        setCurrentItem(index, false);
    }

    /**
     * Tests if wheel is cyclic. That means before the 1st item there is shown the last one
     */
    public boolean isCyclic() {
        return isCyclic;
    }

    /**
     * Set wheel cyclic flag
     */
    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;

        invalidate();
        invalidateLayouts();
    }

    /**
     * Invalidates layouts
     */
    public void invalidateLayouts() {
        itemsLayout = null;
        valueLayout = null;
        scrollingOffset = 0;
    }

    /**
     * Initializes resources
     */
    private void initResourcesIfNecessary() {
        if (itemsPaint == null) {
            itemsPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            itemsPaint.setTextSize(textSize);
            //itemsPaint.setAlpha(128);
            itemsPaint.setColor(ITEMS_TEXT_COLOR);
        }
        if (goneItemsPaint == null) {
            goneItemsPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            goneItemsPaint.setTextSize(textSize);
            goneItemsPaint.setColor(GONE_ITEMS_TEXT_COLOR);
            //goneItemsPaint.setAlpha(78);
        }

        if (valuePaint == null) {
            valuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
            valuePaint.setTextSize(VALUE_TEXT_SIZE);
            valuePaint.setColor(VALUE_TEXT_COLOR);
            //valuePaint.setShadowLayer(0.1f, 0, 0.1f, 0xFFC0C0C0);
        }
        if (lablePaint == null) {
            lablePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
            lablePaint.setTextSize(LABLE_TEXT_SIZE);
            lablePaint.setColor(VALUE_TEXT_COLOR);
        }

//        if (centerDrawable == null) {
//            centerDrawable = getContext().getResources().getDrawable(R.drawable.group_time_select_bg);
//        }
    }

    /**
     * Calculates desired height for layout
     */
    private int getDesiredHeight(Layout layout) {
        if (layout == null) {
            return 0;
        }

        //int desired = getItemHeight() * visibleItems + ITEM_OFFSET * 2;
        int desired = getItemHeight() * visibleItems + ITEM_OFFSET * 2 - mAdditionItemHeight;
        desired = Math.max(desired, getSuggestedMinimumHeight());
        return desired;
    }

    /**
     * Returns text item by index
     */
    private String getTextItem(int index) {
        if (adapter == null || adapter.getItemsCount() == 0) {
            return null;
        }
        int count = adapter.getItemsCount();
        if ((index < 0 || index >= count) && !isCyclic) {
            return null;
        } else {
            while (index < 0) {
                index = count + index;
            }
        }

        index %= count;
        return adapter.getItem(index);
    }

    /**
     * Builds text depending on current value
     */
    private String buildText(boolean useCurrentValue) {
        StringBuilder itemsText = new StringBuilder();
        int addItems = visibleItems / 2 + 1;

        for (int i = currentItem - addItems; i <= currentItem + addItems; i++) {
            if (useCurrentValue || i != currentItem) {
                String text = getTextItem(i);
                if (text != null) {
                    itemsText.append(text);
                }
            }
            if (i < currentItem + addItems) {
                itemsText.append("\n");
            }
        }
        return itemsText.toString();
    }

    /**
     * Returns the max item length that can be present
     */
    private int getMaxTextLength() {
        WheelAdapter adapter = getAdapter();
        if (adapter == null) {
            return 0;
        }

        int adapterLength = adapter.getMaximumLength();
        if (adapterLength > 0) {
            return adapterLength;
        }

        String maxText = null;
        int addItems = visibleItems / 2;
        for (int i = Math.max(currentItem - addItems, 0);
             i < Math.min(currentItem + visibleItems, adapter.getItemsCount()); i++) {
            String text = adapter.getItem(i);
            if (text != null && (maxText == null || maxText.length() < text.length())) {
                maxText = text;
            }
        }

        return maxText != null ? maxText.length() : 0;
    }

    /**
     * Returns height of wheel item
     */
    private int getItemHeight() {
        if (itemHeight != 0) {
            return itemHeight;
        } else if (itemsLayout != null && itemsLayout.getLineCount() > 2) {
            itemHeight = itemsLayout.getLineTop(2) - itemsLayout.getLineTop(1);
            return itemHeight;
        }

        return getHeight() / visibleItems;
    }

    /**
     * Calculates control width and creates text layouts
     */
    private int calculateLayoutWidth(int widthSize, int mode) {

        initResourcesIfNecessary();
        int width = widthSize;
        int maxLength = getMaxTextLength();
        if (maxLength > 0) {
            double textWidth = Math.ceil(Layout.getDesiredWidth("0", valuePaint));
            itemsWidth = (int) (maxLength * textWidth);
        } else {
            itemsWidth = 0;
        }
        itemsWidth += mAdditionalItemSpace; // make it some more

        labelWidth = 0;
        if (label != null && label.length() > 0) {
            labelWidth = (int) Math.ceil(Layout.getDesiredWidth(label, lablePaint));
        }

        boolean recalculate = false;
        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize;
            recalculate = true;
        } else {
            width = itemsWidth + labelWidth + 2 * PADDING;
            if (labelWidth > 0) {
                width += LABEL_OFFSET;
            }
            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());
            if (mode == MeasureSpec.AT_MOST && widthSize < width) {
                width = widthSize;
                recalculate = true;
            }
        }

        if (recalculate) {
            // recalculate width
            int pureWidth = width - LABEL_OFFSET - 2 * PADDING;
            if (pureWidth <= 0) {
                itemsWidth = labelWidth = 0;
            }
            if (labelWidth > 0) {
                double newWidthItems = (double) itemsWidth * pureWidth / (itemsWidth + labelWidth);
                itemsWidth = (int) newWidthItems;
                labelWidth = pureWidth - itemsWidth;
            } else {
                itemsWidth = pureWidth + LABEL_OFFSET; // no label
            }
        }

        if (itemsWidth > 0) {
            createLayouts(itemsWidth, labelWidth);
        }

        return width;
    }

    /**
     * Creates layouts
     */
    private void createLayouts(int widthItems, int widthLabel) {

//        if (goneItemsLayout == null || goneItemsLayout.getWidth() > widthItems) {
//            goneItemsLayout = new StaticLayout(buildText(isScrollingPerformed), goneItemsPaint, widthItems,
//                    Layout.Alignment.ALIGN_CENTER,
//                    1, mAdditionItemHeight, false);
//        } else {
//            goneItemsLayout.increaseWidthTo(widthItems);
//        }

        MyLog.v(TAG + " widthItems:" + widthItems +"  mAdditionalItemSpace:"+mAdditionalItemSpace);
        if (itemsLayout == null || itemsLayout.getWidth() > widthItems) {
            //143是bugfix LIVEAND-9535
            itemsLayout = new StaticLayout(buildText(isScrollingPerformed), itemsPaint, widthItems,
                    //widthLabel > 0 ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_CENTER,
                    Layout.Alignment.ALIGN_CENTER,
                    1, mAdditionItemHeight, false);

        } else {
            itemsLayout.increaseWidthTo(widthItems);
        }

        if (!isScrollingPerformed && (valueLayout == null || valueLayout.getWidth() > widthItems)) {
            String text = getAdapter() != null ? getAdapter().getItem(currentItem) : null;
            valueLayout = new StaticLayout(text != null ? text : "",
                    valuePaint, widthItems,
                    //widthLabel > 0 ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_CENTER,
                    Layout.Alignment.ALIGN_CENTER,
                    1, mAdditionItemHeight, false);
        } else if (isScrollingPerformed) {
            valueLayout = null;
        } else {
            valueLayout.increaseWidthTo(widthItems);
        }

        if (widthLabel > 0) {
            if (labelLayout == null || labelLayout.getWidth() > widthLabel) {
                labelLayout = new StaticLayout(label, lablePaint, widthLabel,
                        Layout.Alignment.ALIGN_NORMAL,
                        //Layout.Alignment.ALIGN_CENTER,
                        1, mAdditionItemHeight, false);
            } else {
                labelLayout.increaseWidthTo(widthLabel);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = calculateLayoutWidth(widthSize, widthMode);
        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getDesiredHeight(itemsLayout);
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (itemsLayout == null) {
            if (itemsWidth == 0) {
                calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
            } else {
                createLayouts(itemsWidth, labelWidth);
            }
        }

        if (itemsWidth > 0) {
            canvas.save();
            // Skip padding space and hide a part of top and bottom items
            canvas.translate(PADDING, ITEM_OFFSET);
            drawItems(canvas);
            //drawGoneItems(canvas);

            drawValue(canvas);
            canvas.restore();
        }

        //drawCenterRect(canvas);
    }

    /**
     * Draws value and label layout
     */
    private void drawValue(Canvas canvas) {

        valuePaint.drawableState = getDrawableState();
        Rect bounds = new Rect();
        itemsLayout.getLineBounds(visibleItems / 2, bounds);
        // draw current value
        if (valueLayout != null) {
            canvas.save();
            canvas.translate(2 * PADDING, bounds.top + scrollingOffset);
            valueLayout.draw(canvas);
            canvas.restore();
        }
        // draw label
        if (labelLayout != null) {
            canvas.save();
            //canvas.translate(itemsLayout.getWidth()+PADDING+ LABEL_OFFSET, bounds.top+ LABEL_OFFSET);
            canvas.translate(itemsLayout.getWidth() + LABEL_OFFSET, bounds.top + LABEL_TOP_OFFSET);
            labelLayout.draw(canvas);
            canvas.restore();
        }

    }

    private void drawItems(Canvas canvas) {
        canvas.save();
        int top = itemsLayout.getLineTop(1);
        canvas.translate(2 * PADDING, -top + scrollingOffset);
        itemsPaint.drawableState = getDrawableState();
        itemsLayout.draw(canvas);
        canvas.restore();
    }
//    private void drawGoneItems(Canvas canvas) {
//        canvas.save();
//        Rect bounds = new Rect();
//        itemsLayout.getLineBounds(0, bounds);
//        canvas.translate(2*PADDING, bounds.top + scrollingOffset);
//        goneItemsPaint.drawableState = getDrawableState();
//        goneItemsLayout.draw(canvas);
//
//        canvas.restore();
//    }
//    private void drawGoneItems(Canvas canvas) {
//        canvas.save();
//
//        int top = goneItemsLayout.getLineTop(0);
//        canvas.translate(2*PADDING, -top + scrollingOffset);
//        goneItemsPaint.drawableState = getDrawableState();
//        goneItemsLayout.draw(canvas);
//
//        canvas.restore();
//    }

    /**
     * Draws rect for current value
     */
    private void drawCenterRect(Canvas canvas) {
        int center = getHeight() / 2;
        int offset = getItemHeight() / 2;
        centerDrawable.setBounds(0, center - offset, getWidth(), center + offset);
        centerDrawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        WheelAdapter adapter = getAdapter();
        if (adapter == null) {
            return true;
        }

        if (!gestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {
            justify();
        }
        return true;
    }

    /**
     * Scrolls the wheel
     */
    private void doScroll(int delta) {
        scrollingOffset += delta;

        int count = scrollingOffset / getItemHeight();
        int pos = currentItem - count;
        if (isCyclic && adapter.getItemsCount() > 0) {
            // fix position by rotating
            while (pos < 0) {
                pos += adapter.getItemsCount();
            }
            pos %= adapter.getItemsCount();
        } else if (isScrollingPerformed) {
            if (pos < 0) {
                count = currentItem;
                pos = 0;
            } else if (pos >= adapter.getItemsCount()) {
                count = currentItem - adapter.getItemsCount() + 1;
                pos = adapter.getItemsCount() - 1;
            }
        } else {
            // fix position
            pos = Math.max(pos, 0);
            pos = Math.min(pos, adapter.getItemsCount() - 1);
        }

        int offset = scrollingOffset;
        if (pos != currentItem) {
            setCurrentItem(pos, false);
        } else {
            invalidate();
        }

        // update offset
        scrollingOffset = offset - count * getItemHeight();
        if (scrollingOffset > getHeight()) {
            scrollingOffset = scrollingOffset % getHeight() + getHeight();
        }
    }

    // gesture listener
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            if (isScrollingPerformed) {
                scroller.forceFinished(true);
                clearMessages();
                return true;
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            startScrolling();
            doScroll((int) -distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            lastScrollY = currentItem * getItemHeight() + scrollingOffset;
            int maxY = isCyclic ? 0x7FFFFFFF : adapter.getItemsCount() * getItemHeight();
            int minY = isCyclic ? -maxY : 0;
            scroller.fling(0, lastScrollY, 0, (int) -velocityY / 2, 0, 0, minY, maxY);
            setNextMessage(MESSAGE_SCROLL);
            return true;
        }
    };

    // Messages
    private final int MESSAGE_SCROLL = 0;
    private final int MESSAGE_JUSTIFY = 1;

    /**
     * Set next message to queue. Clears queue before.
     */
    private void setNextMessage(int message) {
        clearMessages();
        animationHandler.sendEmptyMessage(message);
    }

    /**
     * Clears messages from queue
     */
    private void clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }

    // animation handler
    private Handler animationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            scroller.computeScrollOffset();
            int currY = scroller.getCurrY();
            int delta = lastScrollY - currY;
            lastScrollY = currY;
            if (delta != 0) {
                doScroll(delta);
            }

            // scrolling is not finished when it comes to final Y
            // so, finish it manually
            if (Math.abs(currY - scroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING) {
                currY = scroller.getFinalY();
                scroller.forceFinished(true);
            }
            if (!scroller.isFinished()) {
                animationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {
                justify();
            } else {
                finishScrolling();
            }
        }
    };

    /**
     * Justifies wheel
     */
    private void justify() {
        if (adapter == null) {
            return;
        }

        lastScrollY = 0;
        int offset = scrollingOffset;
        int itemHeight = getItemHeight();
        boolean needToIncrease = offset > 0 ? currentItem < adapter.getItemsCount() : currentItem > 0;
        if ((isCyclic || needToIncrease) && Math.abs((float) offset) > (float) itemHeight / 2) {
            if (offset < 0)
                offset += itemHeight + MIN_DELTA_FOR_SCROLLING;
            else
                offset -= itemHeight + MIN_DELTA_FOR_SCROLLING;
        }
        if (Math.abs(offset) > MIN_DELTA_FOR_SCROLLING) {
            scroller.startScroll(0, 0, 0, offset, SCROLLING_DURATION);
            setNextMessage(MESSAGE_JUSTIFY);
        } else {
            finishScrolling();
        }
    }

    /**
     * Starts scrolling
     */
    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            notifyScrollingListenersAboutStart();
        }
    }

    /**
     * Finishes scrolling
     */
    void finishScrolling() {
        if (isScrollingPerformed) {
            notifyScrollingListenersAboutEnd();
            isScrollingPerformed = false;
        }
        invalidateLayouts();
        invalidate();
    }

    /**
     * Scroll the wheel
     */
    public void scroll(int itemsToScroll, int time) {
        scroller.forceFinished(true);

        lastScrollY = scrollingOffset;
        int offset = itemsToScroll * getItemHeight();

        scroller.startScroll(0, lastScrollY, 0, offset - lastScrollY, time);
        setNextMessage(MESSAGE_SCROLL);

        startScrolling();
    }

    //    private int getPxWithM2Px(Context context, int m2Px) {
//        DisplayMetrics metrics = new DisplayMetrics();
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        float dpi = metrics.density;
//        return (int) (m2Px / 2 * dpi);
//    }
    private int getPxWithM2Px(int m2Px) {
        return (DisplayUtils.dip2px(m2Px) / 2);
    }

    public interface OnWheelChangedListener {
        void onChanged(WheelView wheel, int oldValue, int newValue);
    }

    public interface OnWheelScrollListener {
        void onScrollingStarted(WheelView wheel);

        void onScrollingFinished(WheelView wheel);
    }

    public interface WheelAdapter {
        int getItemsCount();

        String getItem(int index);

        int getMaximumLength();
    }

    private int type = 0;

    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_MOUTH = 1;

    private static final int MOUTH_VALUE_WIDTH = DisplayUtils.dip2px(46);

    public void setType(int type) {
        this.type = type;
    }
}
