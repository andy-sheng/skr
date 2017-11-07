
package com.wali.live.watchsdk.recipient.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SectionIndexer;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;

public class IndexableRecyclerView extends RecyclerView {
    private IndexScroller mScroller = null;
    private SectionIndexer mIndexer = null;

    public static final int HEADER_TOP_MARGIN_ADJUST = DisplayUtils.dip2px(5);

    private int mHeaderPositionOffset = HEADER_TOP_MARGIN_ADJUST;

    private OnScrollListener mOuterScrollListener;

    private OnScrollListener onScrollListener = new OnScrollListener() {
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (mOuterScrollListener != null) {
                mOuterScrollListener.onScrollStateChanged(recyclerView, newState);
            }
        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            updateCurrentSection(recyclerView);
            if (mOuterScrollListener != null) {
                mOuterScrollListener.onScrolled(recyclerView, dx, dy);
            }
        }
    };

    public void updateCurrentSection(RecyclerView recyclerView) {
        LayoutManager mLayoutManager = recyclerView.getLayoutManager();
        if (mLayoutManager != null && mLayoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mLayoutManager;
            for (int i = layoutManager.findFirstVisibleItemPosition(); i < layoutManager.findLastVisibleItemPosition(); i++) {
                View visibleItem = recyclerView.getLayoutManager().findViewByPosition(i);
                if (visibleItem != null) {
                    if (layoutManager.getDecoratedTop(visibleItem) <= mHeaderPositionOffset && layoutManager.getDecoratedBottom(visibleItem) > mHeaderPositionOffset) {
                        if (mScroller != null) {
                            mScroller.updateCurrentSection(i);
                        }
                        break;
                    }
                }
            }
        }
    }

    public IndexableRecyclerView(Context context) {
        this(context, null);
    }

    public IndexableRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScroller = new IndexScroller(context, this);
    }

    public void enableScrollListener(boolean enable) {
        if (enable) {
            addOnScrollListener(onScrollListener);
        }
    }

    public void setOuterScrollListener(OnScrollListener listener) {
        mOuterScrollListener = listener;
    }

    /**
     * 列表滑动时，是否高亮index bar对应的字符 这个方法要在setSectionIndexer()方法之后调用才生效
     *
     * @param enable
     */
    public void enableHighlightIndexBar(boolean enable) {
        if (mScroller != null) {
            mScroller.enableHighlightIndexBar(enable);
        }
    }

    /**
     * 是否加粗index bar的字符
     *
     * @param enable
     */
    public void enableBoldIndexBar(boolean enable) {
        if (mScroller != null) {
            mScroller.enableBoldIndexBar(enable);
        }
    }

    /**
     * 更新字母条index bar
     */
    public void refreshIndexBar() {
        if (mScroller != null) {
            mScroller.refreshIndexBar();
        }
    }

    /**
     * 设置SectionIndexer，如果indexer为空，该ListView将变成普通的ListView
     *
     * @param indexer
     */
    public void setSectionIndexer(SectionIndexer indexer) {
        this.mIndexer = indexer;
        if (mIndexer != null) {
            if (mScroller == null) {
                mScroller = new IndexScroller(getContext(), this);
            }
            mScroller.setIndexer(mIndexer);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (mScroller != null) {
            mScroller.setAdapter(adapter);
        }

    }

    public void hideIndexBar() {
        if (mScroller != null) {
            setPadding(0, 0, 0, 0);
            mScroller.hide();
        }
    }

    public void showIndexBar() {
        if (mScroller != null) {
            setPadding(0, 0, DisplayUtils.dip2px(20), 0);
            mScroller.show();
        }
    }

    public void showIndexBarWithoutPadding() {
        if (mScroller != null) {
            setPadding(0, 0, 0, 0);
            mScroller.show();
        }
    }

//    @Override
//    public void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        MyLog.d("mScroller","onDraw mRecyclerView");
//        if (mScroller != null) {
//            MyLog.d("mScroller","onDraw mScroller");
//            mScroller.draw(canvas);
//        }
//    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        MyLog.d("mScroller", "dispatchDraw mRecyclerView");
        if (mScroller != null) {
            MyLog.d("mScroller", "dispatchDraw mScroller");
            mScroller.draw(canvas);
        }
    }

    public boolean onTouchScrollerEvent(MotionEvent ev) {

        if (mScroller != null && mScroller.isShown()) {
            if (mScroller.contains(ev.getX(), ev.getY())) {
                return mScroller.onTouchEvent(ev);
            } else {
                if (mScroller.isIndexing()) {
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    mScroller.onTouchEvent(ev);
                    postInvalidate();
                }
            }

        }
        return false;
    }

    public boolean isTouchScroller(MotionEvent ev) {
        return mScroller != null && mScroller.isShown() && mScroller.contains(ev.getX(), ev.getY());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mScroller != null && mScroller.isShown()) {
            if (mScroller.contains(ev.getX(), ev.getY())) {
                if (mScroller.onTouchEvent(ev)) {
                    return true;
                }
            } else {
                if (mScroller.isIndexing()) {
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    mScroller.onTouchEvent(ev);
                    postInvalidate();
                }
            }
            //return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mScroller != null && mScroller.isShown() && mScroller.contains(ev.getX(), ev.getY())) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mScroller != null) {
            mScroller.onSizeChanged(w, h, oldw, oldh);
        }
    }

    public void setIndexbarMargin(int top, int bottom) {
        if (mScroller != null) {
            mScroller.setIndexbarMargin(top, bottom);
        }
    }

    public void setPositionOffset(int offset) {
        if (mScroller != null) {
            mScroller.setPositionOffset(offset);
            mHeaderPositionOffset = offset + HEADER_TOP_MARGIN_ADJUST;
        }
    }
}
