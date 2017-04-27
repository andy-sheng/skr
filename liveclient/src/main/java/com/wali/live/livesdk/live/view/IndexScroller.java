
package com.wali.live.livesdk.live.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.widget.SectionIndexer;


public class IndexScroller {

    private static final int STATE_HIDDEN = 0;

    private static final int STATE_SHOWN = 1;

    private static final int INDEX_BAR_TEXT_SIZE = 10;

    private static final int PREVIEW_TEXT_SIZE = 40;

    private static final int PREVIEW_RADIUS = 40;

    private static final int PREVIEW_PADDING = 10;

    private static final int CURRENT_INDEX_RADIUS = 10;

    private float mIndexbarWidth;

    private float mIndexbarMargin;

    private float mIndexbarMarginTop = 0, mIndexbarMarginBottom = 0;

    private int mPositionOffset = 0;

    private float mDensity;

    private float mScaledDensity;

    private int mState = STATE_HIDDEN;

    private int mRecyclerViewWidth;

    private int mRecyclerViewHeight;

    private int mCurrentSection = -1; // 列表滚动时的index bar的section
    /*
     * 按住index bar时的section 单独使用变量记下按住index bar时的section是因为setSelection()会触发列表滚动的代码，会更新mCurrentSection 导致index
     * bar上最后一个字符永远无法按住
     */
    private int mCurrentIndexingSection = -1;

    private boolean mIsIndexing = false;

    private IndexableRecyclerView mRecyclerView = null;

    private SectionIndexer mIndexer = null;

    private String[] mSections = null;

    private RectF mIndexbarRect;

    private Context mContext;

    private boolean mEnableHighlightIndexBar = true;

    private boolean mEnableBoldIndexBar = false;

    //private int mCurrentIndexColor=0xFF6600;
    private int mCurrentIndexColor = Color.WHITE;

    private int mOtherIndexColor = 0xff969696;

    private int mPreviewBackGround = 0xe5aa1e;

    public IndexScroller(Context context, IndexableRecyclerView lv) {

        mContext = context;
        mDensity = mContext.getResources().getDisplayMetrics().density;
        mScaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
        mRecyclerView = lv;
        mIndexbarWidth = 25 * mDensity;
        mIndexbarMargin = 5 * mDensity;
        //mCurrentIndexColor = mContext.getResources().getColor(R.color.text_color_orange);
        //mOtherIndexColor = mContext.getResources().getColor(R.color.index_bar_font_color);
    }

    public void updateCurrentSection(int position) {
        if (mEnableHighlightIndexBar && mIndexer != null) {
            mCurrentSection = mIndexer.getSectionForPosition(position);
            mRecyclerView.invalidate();
        }
    }

    /**
     * 列表滑动时，是否高亮index bar对应的字符
     *
     * @param enable
     */
    public void enableHighlightIndexBar(boolean enable) {
        this.mEnableHighlightIndexBar = enable;
    }

    /**
     * 是否加粗index bar的字符
     *
     * @param enable
     */
    public void enableBoldIndexBar(boolean enable) {
        this.mEnableBoldIndexBar = enable;
    }

    /**
     * 更新字母条
     */
    public void refreshIndexBar() {
        if (mIndexer != null) {
            mSections = (String[]) mIndexer.getSections();
            mRecyclerView.invalidate();
        }
    }

    public void draw(Canvas canvas) {
        if (mState == STATE_HIDDEN) {
            //MyLog.d("mScroller","draw mScroller　mState == STATE_HIDDEN");
            return;
        }
        // index bar 背景
        Paint indexbarPaint = new Paint();
        indexbarPaint.setColor(Color.BLACK);
        indexbarPaint.setAlpha(0);
        indexbarPaint.setAntiAlias(true);
        canvas.drawRoundRect(mIndexbarRect, 12 * mDensity, 12 * mDensity, indexbarPaint);

        //MyLog.d("mScroller","draw mIndexbarRect");

        if (mSections != null && mSections.length > 0) {
            // index bar
            Paint indexPaint = new Paint();
            indexPaint.setColor(Color.rgb(128, 128, 128));
            indexPaint.setAntiAlias(true);
            if (mEnableBoldIndexBar) {
                indexPaint.setTypeface(Typeface.DEFAULT_BOLD);
            }
            // 使用scaledDensity会跟随系统字体变化，导致变形
            // indexPaint.setTextSize(INDEX_BAR_TEXT_SIZE * mScaledDensity);
            indexPaint.setTextSize(INDEX_BAR_TEXT_SIZE * mDensity);
            float sectionHeight = (mIndexbarRect.height() - 2 * mIndexbarMargin) / mSections.length;
            float paddingTop = (sectionHeight - (indexPaint.descent() - indexPaint.ascent())) / 2;
            for (int i = 0; i < mSections.length; i++) {

                float paddingLeft = (mIndexbarWidth - indexPaint.measureText(mSections[i])) / 2;
                if (mEnableHighlightIndexBar && i == mCurrentSection) {
//                    indexPaint.setColor(mCurrentIndexColor);
                    indexPaint.setColor(Color.rgb(0xe5, 0xaa, 0x1e));
                    indexPaint.setTypeface(Typeface.DEFAULT_BOLD);
                    // 当前索引区域
                    Paint previewPaint = new Paint();
                    previewPaint.setColor(mPreviewBackGround);
                    previewPaint.setAlpha(255);
                    previewPaint.setAntiAlias(true);
                    previewPaint.setShadowLayer(5, 0, 0, Color.argb(64, 0, 0, 0));
//                    canvas.drawCircle(mIndexbarRect.left + mIndexbarWidth/2,
//                            mIndexbarRect.top + mIndexbarMargin + sectionHeight * i
//                                    + paddingTop - previewPaint.ascent(), CURRENT_INDEX_RADIUS * mDensity, previewPaint);
//                    canvas.drawCircle(mIndexbarRect.left + mIndexbarWidth / 2,
//                            mIndexbarRect.top + mIndexbarMargin + sectionHeight * (i + 0.5f), CURRENT_INDEX_RADIUS * mDensity, previewPaint);

                } else {
                    indexPaint.setColor(mOtherIndexColor);
                    indexPaint.setTypeface(Typeface.DEFAULT);
                }

                //MyLog.d("mScroller","draw drawText");
                canvas.drawText(mSections[i], mIndexbarRect.left + paddingLeft,
                        mIndexbarRect.top + mIndexbarMargin + sectionHeight * i
                                + paddingTop - indexPaint.ascent(), indexPaint);
            }

            if (mIsIndexing && mCurrentIndexingSection >= 0) {
                //MyLog.d("mScroller","draw preview");
                // 预览区域
                Paint previewPaint = new Paint();
                previewPaint.setColor(mPreviewBackGround);
                previewPaint.setAlpha(128);
                previewPaint.setAntiAlias(true);
                previewPaint.setShadowLayer(5, 0, 0, Color.argb(64, 0, 0, 0));
                canvas.drawCircle(mRecyclerViewWidth / 2, mRecyclerViewHeight / 2, PREVIEW_RADIUS * mDensity, previewPaint);
                // 预览区域的文本
                Paint previewTextPaint = new Paint();
                previewTextPaint.setColor(Color.WHITE);
                previewTextPaint.setAntiAlias(true);
                previewTextPaint.setTextSize(PREVIEW_TEXT_SIZE * mDensity);
                float previewTextWidth = previewTextPaint.measureText(mSections[mCurrentIndexingSection]);
                float previewSize = previewTextPaint.descent() - previewTextPaint.ascent();
                canvas.drawText(mSections[mCurrentIndexingSection], mRecyclerViewWidth / 2
                        - previewTextWidth / 2 + 1, mRecyclerViewHeight / 2
                        + previewSize / 2 - PREVIEW_PADDING * mDensity, previewTextPaint);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {

        if (mIndexer == null) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mState != STATE_HIDDEN && contains(ev.getX(), ev.getY())) {
                    mIsIndexing = true;
                    mCurrentIndexingSection = getSectionByPoint(ev.getY());
                    int position = mIndexer.getPositionForSection(mCurrentIndexingSection);
                    mCurrentSection = mIndexer.getSectionForPosition(position);
                    ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, mPositionOffset);
                    mRecyclerView.invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsIndexing) {
                    if (contains(ev.getX(), ev.getY())) {
                        mCurrentIndexingSection = getSectionByPoint(ev.getY());
                        int position = mIndexer.getPositionForSection(mCurrentIndexingSection);
                        mCurrentSection = mIndexer.getSectionForPosition(position);
                        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, mPositionOffset);
                    }
                    mRecyclerView.invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsIndexing = false;
                mCurrentIndexingSection = -1;
                mRecyclerView.invalidate();
                return true;
        }
        return false;
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        mRecyclerViewWidth = w;
        mRecyclerViewHeight = h;
        mIndexbarRect = new RectF(w - mIndexbarMargin - mIndexbarWidth,
                mIndexbarMargin + mIndexbarMarginTop, w - mIndexbarMargin, h - mIndexbarMargin - mIndexbarMarginBottom);
    }

    public void setIndexbarMargin(int top, int bottom) {
        mIndexbarMarginTop = top;
        mIndexbarMarginBottom = bottom;
        onSizeChanged(mRecyclerViewWidth, mRecyclerViewHeight, 0, 0);
    }

    public void setPositionOffset(int offset) {
        mPositionOffset = offset;
    }

    public void show() {
        if (mState == STATE_HIDDEN) {
            setState(STATE_SHOWN);
        }
    }

    public void hide() {
        if (mState == STATE_SHOWN) {
            setState(STATE_HIDDEN);
        }
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter instanceof SectionIndexer) {
            mIndexer = (SectionIndexer) adapter;
            mSections = (String[]) mIndexer.getSections();
        }
    }

    public void setIndexer(SectionIndexer indexer) {
        if (indexer != null) {
            mIndexer = indexer;
            mSections = (String[]) mIndexer.getSections();
            //mRecyclerView.forceLayout();
            //mRecyclerView.invalidate();
        }
    }

    private void setState(int state) {
        if (state < STATE_HIDDEN || state > STATE_SHOWN) {
            return;
        }
        mState = state;
        mRecyclerView.invalidate();
    }

    public boolean isShown() {
        return mState == STATE_SHOWN;
    }

    public boolean isIndexing() {
        return mIsIndexing;
    }

    /*
     * 判断点(x, y)是否在index bar的区域内（包括right margin）
     */
    public boolean contains(float x, float y) {
        return (x >= (mIndexbarRect.left) && y >= mIndexbarRect.top && y <= mIndexbarRect.top + mIndexbarRect.height());
    }

    private int getSectionByPoint(float y) {
        if (mSections == null || mSections.length == 0) {
            return 0;
        }
        if (y < mIndexbarRect.top + mIndexbarMargin) {
            return 0;
        }
        if (y >= mIndexbarRect.top + mIndexbarRect.height() - mIndexbarMargin) {
            return mSections.length - 1;
        }
        return (int) ((y - mIndexbarRect.top - mIndexbarMargin) / ((mIndexbarRect
                .height() - 2 * mIndexbarMargin) / mSections.length));
    }

}
