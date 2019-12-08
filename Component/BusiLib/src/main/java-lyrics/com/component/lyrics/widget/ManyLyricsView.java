package com.component.lyrics.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.common.log.MyLog;
import com.common.utils.U;
import com.component.busilib.R;
import com.component.lyrics.LyricsReader;
import com.component.lyrics.model.LyricsInfo;
import com.component.lyrics.model.LyricsLineInfo;
import com.component.lyrics.utils.LyricsUtils;
import com.component.lyrics.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


/**
 * @Description: 多行歌词:歌词行号和view所在位置关联,Scroller只做动画处理，不去移动view
 * @param:
 * @return:
 * @throws
 * @author: zhangliangming
 * @date: 2018-04-21 20:28
 */
public class ManyLyricsView extends AbstractLrcView {
    public final String TAG = "ManyLyricsView";
    /**
     * 初始
     */
    private final int TOUCHEVENTSTATUS_INIT = 0;

    /**
     * 滑动越界
     */
    private final int TOUCHEVENTSTATUS_OVERSCROLL = 1;
    /**
     * 快速滑动
     */
    private final int TOUCHEVENTSTATUS_FLINGSCROLL = 2;

    /**
     * 触摸状态
     */
    private int mTouchEventStatus = TOUCHEVENTSTATUS_INIT;

    /////////////////////////////////////////////////
    /**
     * 画时间线指示器
     ***/
    private Paint mPaintIndicator;
    /**
     * 画线
     */
    private Paint mPaintLine;
    private Paint mCirclePaint;
    private Paint mWhoTurnsPaint;

    /**
     * 画线颜色
     */
    private int mPaintLineColor = Color.WHITE;

    /**
     * 绘画播放按钮
     */
    private Paint mPaintPlay;
    /**
     * 播放按钮区域
     */
    private Rect mPlayBtnRect;

    /**
     * 是否在播放按钮区域
     */
    private boolean isInPlayBtnRect = false;
    /**
     * 播放按钮区域字体大小
     */
    private int mPlayRectSize = 25;
    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;
    /**
     *
     */
    private Scroller mScroller;
    /**
     * Y轴移动的时间
     */
    private int mDuration = 350;

    ///////////////////////////////////////////////////
    /**
     * 歌词在Y轴上的偏移量
     */
    private float mOffsetY = 0;
    /**
     * 视图y中间
     */
    private float mCentreY = 0;
    /**
     * 颜色渐变梯度
     */
    private int mMaxAlpha = 255;
    private int mMinAlpha = 50;
    //渐变的高度
    private int mShadeHeight = 0;
    /**
     * 记录手势
     */
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    //用于判断拦截
    private int mInterceptX = 0;
    private int mInterceptY = 0;
    /**
     * 触摸最后一次的坐标
     */
    private int mLastY;
    /**
     * 是否直接拦截
     */
    private boolean mIsTouchIntercept = false;

    /**
     * 是否允许触摸
     */
    private boolean mTouchAble = true;

    /**
     * 是否绘画时间线
     */
    private boolean mIsDrawIndicator = true;

    //////////////////////////////////////////////////////

    /**
     * 还原歌词视图
     */
    private final int RESETLRCVIEW = 1;
    /**
     *
     */
    private int mResetDuration = 3000;

    /**
     * 高亮以上画多少行
     */
    private int mUpLineNum = 100;

    /**
     * 高亮以下画多少行
     */
    private int mDownLineNum = 100;

    /**
     * 要不要展示上传者
     */
    private boolean mShowAuthor = false;

    /**
     * 要不要展示歌名
     */
    private boolean mShowSongName = false;

    private String mAuthorName;

    private String mSongName;

    //    <flag name="center" value="0" />
    //    <flag name="left" value="1" />
    private int mLyricGravity = 0;

    // 合唱别人演唱的高亮颜色
    protected int mPaintHLColorsForOthers[] = null;

    public float shiftY = 0.5f; // 中间高亮歌词的偏移量
    // 合唱分割歌词的依据数组
//    protected ArrayList<Integer> mSplitChorusArray = null;

    // 合唱我是第一个唱还是第二个唱
//    protected boolean mFirstSingByMe = true;

//    public void setSplitChorusArray(ArrayList mSplitChorusArray) {
//        this.mSplitChorusArray = mSplitChorusArray;
//    }

//    public void setFirstSingByMe(boolean mFirstSingByMe) {
//        this.mFirstSingByMe = mFirstSingByMe;
//    }

    /**
     * Handler处理滑动指示器隐藏和歌词滚动到当前播放的位置
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case RESETLRCVIEW:
                    if (mScroller.computeScrollOffset()) {
                        //发送还原
                        mHandler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);
                    } else {

                        mIsTouchIntercept = false;
                        mTouchEventStatus = TOUCHEVENTSTATUS_INIT;
                        int lyricsLineNum = mLyricsLineNum;
                        int deltaY = getLineAtHeightY(lyricsLineNum) - mScroller.getFinalY();
                        mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
                        invalidateView();
                    }

                    break;
            }
        }
    };
    /**
     * 歌词快进事件
     */
    private OnLrcClickListener mOnLrcClickListener;

    GestureDetector mGestureDetector;

    public ManyLyricsView(Context context) {
        super(context);
        init(context, null);
    }

    public ManyLyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    /**
     * @throws
     * @Description: 初始
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-04-21 9:08
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.many_line_lrc_view);
        if (typedArray.hasValue(R.styleable.many_line_lrc_view_ly_enable_scroll)) {
            mTouchAble = typedArray.getBoolean(R.styleable.many_line_lrc_view_ly_enable_scroll, true);
        }

        if (typedArray.hasValue(R.styleable.many_line_lrc_view_ly_top_line_num)) {
            int upLineNum = typedArray.getInteger(R.styleable.many_line_lrc_view_ly_top_line_num, 2);
            //上下一样
            mUpLineNum = upLineNum;
            mDownLineNum = upLineNum;
        }

        if (typedArray.hasValue(R.styleable.many_line_lrc_view_ly_show_author)) {
            mShowAuthor = typedArray.getBoolean(R.styleable.many_line_lrc_view_ly_show_author, false);
        }

        if (typedArray.hasValue(R.styleable.many_line_lrc_view_ly_show_song_name)) {
            mShowSongName = typedArray.getBoolean(R.styleable.many_line_lrc_view_ly_show_song_name, false);
        }

        if (typedArray.hasValue(R.styleable.many_line_lrc_view_custom_gravity)) {
            mLyricGravity = typedArray.getInteger(R.styleable.many_line_lrc_view_custom_gravity, 0);
        }

        if (typedArray.hasValue(R.styleable.many_line_lrc_view_ly_high_light_paint_color_from2) && typedArray.hasValue(R.styleable.many_line_lrc_view_ly_high_light_paint_color_to2)) {
            mPaintHLColorsForOthers = new int[]{typedArray.getColor(R.styleable.many_line_lrc_view_ly_high_light_paint_color_from2, Color.BLUE), typedArray.getColor(R.styleable.many_line_lrc_view_ly_high_light_paint_color_to2, Color.BLUE)};
        }

        typedArray.recycle();
        //初始化
        mScroller = new Scroller(context, new LinearInterpolator());
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        //画指时间示器
        mPaintIndicator = new com.common.view.ExPaint();
        mPaintIndicator.setDither(true);
        mPaintIndicator.setAntiAlias(true);

        //画线
        mPaintLine = new com.common.view.ExPaint();
        mPaintLine.setDither(true);
        mPaintLine.setAntiAlias(true);
        mPaintLine.setStyle(Paint.Style.FILL);


        //绘画播放按钮
        mPaintPlay = new com.common.view.ExPaint();
        mPaintPlay.setDither(true);
        mPaintPlay.setAntiAlias(true);
        mPaintPlay.setStrokeWidth(2);

        mCirclePaint = new com.common.view.ExPaint();
        mWhoTurnsPaint = new com.common.view.ExPaint();


        setGotoSearchTextColor(Color.WHITE);
        setGotoSearchTextPressedColor(U.getColorUtils().parserColor("#0288d1"));

        //获取屏幕宽度
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int screensWidth = displayMetrics.widthPixels;

        //设置歌词的最大宽度
        int textMaxWidth = screensWidth / 3 * 2;
        mTextMaxWidth = textMaxWidth;

        //设置画笔大小
        mPaintIndicator.setTextSize(mPlayRectSize);
        mPaintLine.setTextSize(mPlayRectSize);
        mPaintPlay.setTextSize(mPlayRectSize);

        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (onLyricViewTapListener != null) {
                    onLyricViewTapListener.onDoubleTap();
                }
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (onLyricViewTapListener != null) {
                    int scrollLrcLineNum = getScrollLrcLineNum(mOffsetY);
                    int startTime = mLrcLineInfos.get(scrollLrcLineNum).getStartTime();

                    //用户随便点的
                    if (scrollLrcLineNum == mLyricsLineNum) {
                        onLyricViewTapListener.onSigleTap(-1);
                    } else {
                        onLyricViewTapListener.onSigleTap(startTime);
                    }
                }
                return super.onSingleTapConfirmed(e);
            }
        });
        // 控件双击事件响应，这里的控件是上一篇的移动的圆

    }

    @Override
    protected void onDrawLrcView(Canvas canvas) {
        if (mShadeHeight == 0) {
            mShadeHeight = getHeight() / 2;
        }
        drawManyLrcView(canvas);
    }

    @Override
    protected void updateView(long playProgress) {
        updateManyLrcView(playProgress);
    }

    /**
     * 绘画歌词
     *
     * @param canvas
     */
    private void drawManyLrcView(Canvas canvas) {

        //画当前行歌词
        //获取分割后的歌词列表 里面会有每个字的信息时间戳

        LyricsLineInfo lyricsLineInfo = mLrcLineInfos.get(mLyricsLineNum);

        // 这行是否是合唱时 切换的行  《轮到你唱了》 在上显示 在下显示
//        boolean thisLineIsDownSplit = false;

//        boolean thisLineIsUpSplit = false;

        long curProgress = getCurPlayingTime() + getPlayerSpendTime();
//        MyLog.d(TAG, "drawManyLrcView" + " mLyricsLineNum=" + mLyricsLineNum + " lyricsLineInfo:" + lyricsLineInfo.toSimpleString());
//        boolean firstSingerTime = false;
//        if (mSplitChorusArray != null) {
//            firstSingerTime = mSplitChorusArray.size() % 2 == 0;
//            // 判断当前的进度歌词归属于第几个人唱
//            // 歌词的绘制归属不严格按当前划分
//            for (int i = 0; i < mSplitChorusArray.size(); i++) {
//                if (lyricsLineInfo != null && nextLyricsLineInfo != null) {
//                    if (lyricsLineInfo.getStartTime() < mSplitChorusArray.get(i) && nextLyricsLineInfo.getStartTime() > mSplitChorusArray.get(i)) {
//                        thisLineIsDownSplit = true;
//                    }
//                }
//                if (lyricsLineInfo != null ) {
//                    if(lastLyricsLineInfo != null){
//                        if (lyricsLineInfo.getStartTime() > mSplitChorusArray.get(i) && lastLyricsLineInfo.getStartTime() < mSplitChorusArray.get(i)) {
//                            thisLineIsUpSplit = true;
//                        }
//                    }else{
//                        thisLineIsUpSplit = true;
//                    }
//                }
//                // 这里要按歌词的开始时间决定归属 不能用 curProgress 会导致一个间奏内 归属权的变化
//                if (lyricsLineInfo.getStartTime() < mSplitChorusArray.get(i)) {
//                    if (i % 2 == 0) {
//                        firstSingerTime = true;
//                    } else {
//                        firstSingerTime = false;
//                    }
//                    //MyLog.d(TAG,"drawManyLrcView" + " firstSingerTime="+firstSingerTime +" mSplitChorusArray[i]="+mSplitChorusArray.get(i) +" i="+i);
//                    break;
//                }
//            }
//        }
        //获取数据
        //获取中间位置
        mCentreY = (getHeight() + LyricsUtils.getTextHeight(mPaintHL)) * shiftY + getLineAtHeightY(mLyricsLineNum) - mOffsetY;


        List<LyricsLineInfo> splitLyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
        int[] subPaintHLColors = new int[]{getSubPaintHLColor(), getSubPaintHLColor()};
        int[] paintHLColors;

        if (mPaintHLColorsForOthers != null) {
            if (lyricsLineInfo.singByMe) {
                paintHLColors = mPaintHLColors;
            } else {
                paintHLColors = mPaintHLColorsForOthers;
            }
        } else {
            paintHLColors = mPaintHLColors;
        }
        float newCenterY = mCentreY;
        if (lyricsLineInfo.spilit) {
            newCenterY = drawWhoTurns(lyricsLineInfo.singByMe, canvas, mCentreY, false);
        }

        // 画当前演唱那行的歌词
        float lineBottomY = drawDownLyrics("drawManyLrcView1", canvas, mPaint, subPaintHLColors, mPaintHL, paintHLColors, splitLyricsLineInfos, mSplitLyricsLineNum, mSplitLyricsWordIndex, mSpaceLineHeight, mLyricsWordHLTime, mCentreY);

        if (mLyricsLineNum == mLrcLineInfos.size() - 1) {
            if (mShowAuthor)
                drawAuthor(mPaint, canvas, lineBottomY);
        }

        if (mLyricsLineNum == 0) {
            if (mShowSongName) {
                drawSongName(mPaint, canvas, lineBottomY);
            }
        }

        //画倒计时圆点
        if (getNeedCountDownLine().contains(mLyricsLineNum)) {
            boolean result = drawCountDownPoint(canvas, paintHLColors[0], lineBottomY);
            if (result) {
                //成功画 3 2 1 倒计时了
            } else {
                //如果还没画 说明时间还没到 合唱一般显示轮到谁唱

            }
        }
        //画额外歌词
        lineBottomY = drawDownExtraLyrics(canvas, mExtraLrcPaint, mExtraLrcPaintHL, mLyricsLineNum, mExtraSplitLyricsLineNum, mExtraSplitLyricsWordIndex, mExtraLrcSpaceLineHeight, mLyricsWordHLTime, mTranslateLyricsWordHLTime, lineBottomY);

        {
            //画当前行下面的歌词
            int maxDownLineNum = mLrcLineInfos.size() - mLyricsLineNum > mDownLineNum ? mLyricsLineNum + mDownLineNum + 1 : mLrcLineInfos.size();
            for (int i = mLyricsLineNum + 1; i < maxDownLineNum; i++) {
                LyricsLineInfo downLyricsLineInfo = mLrcLineInfos
                        .get(i);
                if (downLyricsLineInfo.spilit) {
                    lineBottomY = drawWhoTurns(downLyricsLineInfo.singByMe, canvas, lineBottomY, true);
                }
                //获取分割后的歌词列表
                List<LyricsLineInfo> lyricsLineInfos = downLyricsLineInfo.getSplitLyricsLineInfos();
                lineBottomY = drawDownLyrics("drawManyLrcView2", canvas, mPaint, subPaintHLColors, mPaintHL, paintHLColors, lyricsLineInfos, -1, -2, mSpaceLineHeight, -1, lineBottomY);
                //画额外歌词
                lineBottomY = drawDownExtraLyrics(canvas, mExtraLrcPaint, mExtraLrcPaintHL, i, -1, -2, mExtraLrcSpaceLineHeight, -1, -1, lineBottomY);
                //最后一行
                if (i == mLrcLineInfos.size() - 1) {
                    if (mShowAuthor)
                        drawAuthor(mPaint, canvas, lineBottomY);
                }
            }
        }

        {
            int maxUpLineNum = mLyricsLineNum - mUpLineNum;
            maxUpLineNum = maxUpLineNum < 0 ? 0 : maxUpLineNum;
            // 画当前歌词之前的歌词
            float lineTopY = newCenterY;
            for (int i = mLyricsLineNum - 1; i >= maxUpLineNum; i--) {
                LyricsLineInfo upLyricsLineInfo = mLrcLineInfos
                        .get(i);
                //获取分割后的歌词列表
                List<LyricsLineInfo> lyricsLineInfos = upLyricsLineInfo.getSplitLyricsLineInfos();
                lineTopY = drawUpExtraLyrics(canvas, mPaint, lyricsLineInfos, i, mExtraLrcSpaceLineHeight, lineTopY);
                if (upLyricsLineInfo.spilit) {
                    lineTopY = drawWhoTurns(upLyricsLineInfo.singByMe, canvas, lineTopY, false);
                }
            }
        }

        //绘画时间、播放按钮等
        if ((mIsTouchIntercept || mTouchEventStatus != TOUCHEVENTSTATUS_INIT) && mIsDrawIndicator) {
            drawIndicator(canvas);
        }
    }

    public void setAuthorName(String authorName) {
        mAuthorName = authorName;
    }

    public void setSongName(String songName) {
        mSongName = songName;
    }

    private void drawAuthor(Paint paint, Canvas canvas, float lineBottomY) {
        if (TextUtils.isEmpty(mAuthorName)) {
            return;
        }

        float size = paint.getTextSize();
        paint.setTextSize(size * 0.8f);
        float textWidth = LyricsUtils.getTextWidth(paint, "上传者：" + mAuthorName);
        float textX = 0.0f;
        if (mLyricGravity == 0) {
            textX = (getWidth() - textWidth) * 0.5f;
        } else if (mLyricGravity == 1) {
            textX = 0.0f;
        }

        LyricsUtils.drawText(canvas, paint, mPaintColors, "上传者：" + mAuthorName, textX, lineBottomY, getMeasuredWidth());
        paint.setTextSize(size);
    }

    private void drawSongName(Paint paint, Canvas canvas, float lineBottomY) {
        if (TextUtils.isEmpty(mSongName)) {
            return;
        }

        lineBottomY = lineBottomY - LyricsUtils.getTextHeight(paint) - mSpaceLineHeight * 2 - U.getDisplayUtils().dip2px(10);
        float size = paint.getTextSize();
        paint.setTextSize(size * 0.8f);
        float textWidth = LyricsUtils.getTextWidth(paint, mSongName);
        float textX = 0.0f;
        if (mLyricGravity == 0) {
            textX = (getWidth() - textWidth) * 0.5f;
        } else if (mLyricGravity == 1) {
            textX = 0.0f;
        }

        LyricsUtils.drawText(canvas, paint, mPaintColors, mSongName, textX, lineBottomY, getMeasuredWidth());
        paint.setTextSize(size);
    }

    public void setUpLineNum(int upLineNum) {
        mUpLineNum = upLineNum;
    }

    public void setDownLineNum(int downLineNum) {
        mDownLineNum = downLineNum;
    }


    private boolean drawCountDownPoint(Canvas canvas, int color, float y) {
        LyricsLineInfo currentLine = mLrcLineInfos.get(mLyricsLineNum);
        int splitLyricsLineNum = mSplitLyricsLineNum;
        LyricsLineInfo realInfo = currentLine.getSplitLyricsLineInfos().get(splitLyricsLineNum);
//        MyLog.v(TAG, "lyricsLineNum " + lyricsLineNum + " 词:" + realInfo.getLineLyrics());

        long startTime = realInfo.getStartTime();
        long lyricProgress = mPlayerSpendTime + getCurPlayingTime();
//        MyLog.d(TAG, "startTime " + startTime + " lyricProgress " + lyricProgress);

        long degree = startTime - lyricProgress;

        float textWidth = LyricsUtils.getTextWidth(mPaintHL, realInfo.getLineLyrics());
        float textX = (getWidth() - textWidth) * 0.5f + 10;
        int textHeight = LyricsUtils.getTextHeight(mPaintHL);


        mCirclePaint.setColor(color);
//        MyLog.v(TAG, "degree " + degree);

        if (degree <= 0) {
//            MyLog.v(TAG, "倒计时 0");
            return false;
        }
        float radius = U.getDisplayUtils().dip2px(3.3f);
        float dy = y - textHeight - radius * 2 - U.getDisplayUtils().dip2px(33);

        if (degree <= 1000) {
//            MyLog.v(TAG, "倒计时 1");
            canvas.drawCircle(textX, dy, radius, mCirclePaint);
            return true;
        }

        if (degree <= 2000) {
//            MyLog.v(TAG, "倒计时 2");
            canvas.drawCircle(textX, dy, radius, mCirclePaint);
            canvas.drawCircle(textX + 40, dy, radius, mCirclePaint);
            return true;
        }

        if (degree <= 3000) {
//            MyLog.v(TAG, "倒计时 3");
            canvas.drawCircle(textX, dy, radius, mCirclePaint);
            canvas.drawCircle(textX + 40, dy, radius, mCirclePaint);
            canvas.drawCircle(textX + 80, dy, radius, mCirclePaint);
            return true;
        }
        return false;
    }


    private float drawWhoTurns(boolean singByMe, Canvas canvas, float lineY, boolean down) {

        //歌词和空行高度
//        float lineHeight = LyricsUtils.getTextHeight(paint) + spaceLineHeight;
//        //往下绘画歌词
//
//            String text = splitLyricsLineInfos.get(i).getLineLyrics();
//
//            lineBottomY = fristLineTextY + i * lineHeight;


        //lineBottomY = lineBottomY - LyricsUtils.getTextHeight(paint) - mSpaceLineHeight * 2 - U.getDisplayUtils().dip2px(10);

        mWhoTurnsPaint.setTextSize(mFontSize * 0.8f);
        float textWidth = LyricsUtils.getTextWidth(mWhoTurnsPaint, "[轮到你演唱]");
        float textX = 0.0f;
        if (mLyricGravity == 0) {
            textX = (getWidth() - textWidth) * 0.5f;
        } else if (mLyricGravity == 1) {
            textX = 0.0f;
        }
        int[] colors;
        String text;
        if (singByMe) {
            colors = mPaintHLColors;
            text = "[轮到你演唱]";
        } else {
            colors = mPaintHLColorsForOthers;
            text = "[轮到对方演唱]";
        }

        if (down) {
            LyricsUtils.drawText(canvas, mWhoTurnsPaint, colors, text, textX, lineY, getMeasuredWidth());
            lineY = lineY + LyricsUtils.getTextHeight(mWhoTurnsPaint) + mSpaceLineHeight;
        } else {
            lineY = lineY - LyricsUtils.getTextHeight(mWhoTurnsPaint) - mSpaceLineHeight;
            if (lineY > 0) {
                LyricsUtils.drawText(canvas, mWhoTurnsPaint, colors, text, textX, lineY, getMeasuredWidth());
            }
        }
        return lineY;
    }

    /**
     * 向下绘画动感歌词
     *
     * @param canvas
     * @param paint
     * @param paintHL
     * @param splitLyricsLineInfos 分隔歌词集合
     * @param splitLyricsLineNum   分隔歌词行索引
     * @param splitLyricsWordIndex 分隔歌词字索引
     * @param spaceLineHeight      空行高度
     * @param lyricsWordHLTime     歌词高亮时间
     * @param fristLineTextY       第一行文字位置
     * @return
     */
    private float drawDownLyrics(String from, Canvas canvas, Paint paint, int paintColor[], Paint paintHL, int paintHLColor[], List<LyricsLineInfo> splitLyricsLineInfos, int splitLyricsLineNum, int splitLyricsWordIndex, float spaceLineHeight, float lyricsWordHLTime, float fristLineTextY) {
        //MyLog.d(TAG,"drawDownLyrics" + " from=" + from + " canvas=" + canvas + " paint=" + paint + " paintHL=" + paintHL + " splitLyricsLineInfos=" + splitLyricsLineInfos + " splitLyricsLineNum=" + splitLyricsLineNum + " splitLyricsWordIndex=" + splitLyricsWordIndex + " spaceLineHeight=" + spaceLineHeight + " lyricsWordHLTime=" + lyricsWordHLTime + " fristLineTextY=" + fristLineTextY);
        //获取数据
        //
        float lineBottomY = 0;

        int curLyricsLineNum = splitLyricsLineNum;

        //歌词和空行高度
        float lineHeight = LyricsUtils.getTextHeight(paint) + spaceLineHeight;
        //往下绘画歌词
        for (int i = 0; i < splitLyricsLineInfos.size(); i++) {

            String text = splitLyricsLineInfos.get(i).getLineLyrics();

            lineBottomY = fristLineTextY + i * lineHeight;

            //超出上视图
            if (lineBottomY < lineHeight) {
                continue;
            }
            //超出下视图
            if (lineBottomY + spaceLineHeight > getHeight()) {
                break;
            }

            //计算颜色透明度
            int alpha = mMaxAlpha;

            //颜色透明度过渡

            if (lineBottomY < mShadeHeight) {
                alpha = mMaxAlpha - (int) ((mShadeHeight - lineBottomY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            } else if (lineBottomY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((lineBottomY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            }

//            MyLog.v(TAG, "alpha " + alpha);
//            alpha = (int) (Math.max(alpha, 0) * 0.8f);
            paint.setAlpha(alpha);
            paintHL.setAlpha(alpha);

//            float textWidth = LyricsUtils.getTextWidth(paint, text);
//            float textX = (getWidth() - textWidth) * 0.5f;
            //
            if (i < curLyricsLineNum) {
                float textWidth = LyricsUtils.getTextWidth(paint, text);
                float textX = (getWidth() - textWidth) * 0.5f;
                LyricsUtils.drawText(canvas, paint, mPaintColors, text, textX, lineBottomY, getMeasuredWidth());
                // TODO: 2018/12/14 不应该画2次
//                LyricsUtils.drawText(canvas, paintHL, paintHLColors, text, textX, lineBottomY);

            } else if (i == curLyricsLineNum) {

                //这行歌词过去的时间,在话动感歌词的时候需要用到
                long currentLineSpendTime = getCurPlayingTime() + mPlayerSpendTime - (long) splitLyricsLineInfos.get(i).getStartTime();
                //根据时间算出渐变文字大小
                float drawHLTextPaintSize = LyricsUtils.getDrawDynamicTextPaintSize(currentLineSpendTime, paint.getTextSize(), paintHL.getTextSize());
//                MyLog.v("ManyLyricsView", "drawHLTextPaintSize " + drawHLTextPaintSize + " currentLineSpendTime " + currentLineSpendTime + ", paint.getTextSize" + paint.getTextSize() + ", paintHL text size " + paintHL.getTextSize());
                //先把原始的拿出来
                float paintOriginalTextSize = paint.getTextSize();
                float paintHLOriginalTextSize = paintHL.getTextSize();
                paint.setTextSize(drawHLTextPaintSize);
                paintHL.setTextSize(drawHLTextPaintSize);
                //绘画动感歌词
                float textWidth = LyricsUtils.getTextWidth(paintHL, text);
                float textX = 0.0f;
                if (mLyricGravity == 0) {
                    textX = (getWidth() - textWidth) * 0.5f;
                } else if (mLyricGravity == 1) {
                    textX = 0.0f;
                }
                float lineLyricsHLWidth = LyricsUtils.getLineLyricsHLWidth(mLyricsReader.getLyricsType(), paintHL, splitLyricsLineInfos.get(i), splitLyricsWordIndex, lyricsWordHLTime);

                LyricsUtils.drawDynamicText(canvas, paint, paintHL, paintColor, paintHLColor, text, lineLyricsHLWidth, textX, lineBottomY, getMeasuredWidth());

                //再把原来的大小设置进去
                paint.setTextSize(paintOriginalTextSize);
                paintHL.setTextSize(paintHLOriginalTextSize);
            } else if (i > curLyricsLineNum) {
                float textWidth = LyricsUtils.getTextWidth(paint, text);
                float textX = 0.0f;
                if (mLyricGravity == 0) {
                    textX = (getWidth() - textWidth) * 0.5f;
                } else if (mLyricGravity == 1) {
                    textX = 0.0f;
                }
                LyricsUtils.drawText(canvas, paint, mPaintColors, text, textX, lineBottomY, getMeasuredWidth());
            }

//            canvas.drawLine(0, lineBottomY - getTextHeight(paint), 720, lineBottomY - getTextHeight(paint), paint);
//            canvas.drawLine(0, lineBottomY, 720, lineBottomY, paint);
        }
        //考虑部分歌词越界，导致高度不正确，这里重新获取基本歌词结束后的y轴位置
        lineBottomY = fristLineTextY + lineHeight * (splitLyricsLineInfos.size());

        return lineBottomY;
    }

    /**
     * 绘画向下的额外歌词
     *
     * @param canvas
     * @param paint
     * @param paintHL
     * @param lyricsLineNum
     * @param extraSplitLyricsLineNum
     * @param extraSplitLyricsWordIndex
     * @param extraLrcSpaceLineHeight
     * @param lyricsWordHLTime
     * @param translateLyricsWordHLTime
     * @param lineBottomY
     * @return
     */
    private float drawDownExtraLyrics(Canvas canvas, Paint paint, Paint paintHL, int lyricsLineNum, int extraSplitLyricsLineNum, int extraSplitLyricsWordIndex, float extraLrcSpaceLineHeight, float lyricsWordHLTime, float translateLyricsWordHLTime, float lineBottomY) {
        //获取数据
        int extraLrcStatus = getExtraLrcStatus();

        //
        if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
            //画翻译歌词
            if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
                //以动感歌词的形式显示翻译歌词
                List<LyricsLineInfo> translateSplitLyricsLineInfos = mTranslateLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineBottomY += extraLrcSpaceLineHeight - mSpaceLineHeight;
                if (mLyricsReader.getLyricsType() == LyricsInfo.DYNAMIC && extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC && mTranslateDrawType == AbstractLrcView.TRANSLATE_DRAW_TYPE_DYNAMIC) {
                    lineBottomY = drawDownLyrics("drawDownExtraLyrics1", canvas, paint, new int[]{getSubPaintHLColor(), getSubPaintHLColor()}, paintHL, mPaintHLColors, translateSplitLyricsLineInfos, extraSplitLyricsLineNum, extraSplitLyricsWordIndex, extraLrcSpaceLineHeight, translateLyricsWordHLTime, lineBottomY);
                } else {
                    //画lrc歌词
                    lineBottomY = drawDownLyrics("drawDownExtraLyrics2", canvas, paint, new int[]{getSubPaintHLColor(), getSubPaintHLColor()}, paintHL, mPaintHLColors, translateSplitLyricsLineInfos, -1, -2, extraLrcSpaceLineHeight, -1, lineBottomY);
                }
                lineBottomY += mSpaceLineHeight - extraLrcSpaceLineHeight;
            }
        } else if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
            //画音译歌词
            if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
                //获取分割后的音译歌词行
                List<LyricsLineInfo> transliterationSplitLrcLineInfos = mTransliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineBottomY += extraLrcSpaceLineHeight - mSpaceLineHeight;
                lineBottomY = drawDownLyrics("drawDownExtraLyrics3", canvas, paint, new int[]{getSubPaintHLColor(), getSubPaintHLColor()}, paintHL, mPaintHLColors, transliterationSplitLrcLineInfos, extraSplitLyricsLineNum, extraSplitLyricsWordIndex, extraLrcSpaceLineHeight, lyricsWordHLTime, lineBottomY);
                lineBottomY += mSpaceLineHeight - extraLrcSpaceLineHeight;
            }
        }
        return lineBottomY;
    }

    /**
     * 绘画向上的额外歌词
     *
     * @param canvas
     * @param paint
     * @param splitLyricsLineInfos
     * @param lyricsLineNum
     * @param extraLrcSpaceLineHeight
     * @param lineTopY                @return
     */
    private float drawUpExtraLyrics(Canvas canvas, Paint paint, List<LyricsLineInfo> splitLyricsLineInfos, int lyricsLineNum, float extraLrcSpaceLineHeight, float lineTopY) {
        //获取数据
        int extraLrcStatus = getExtraLrcStatus();

        //
        if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
            //画翻译歌词
            if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
                //以动感歌词的形式显示翻译歌词
                List<LyricsLineInfo> translateSplitLyricsLineInfos = mTranslateLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineTopY -= (LyricsUtils.getTextHeight(paint) + mSpaceLineHeight);
                lineTopY = drawUpLyrics(canvas, paint, translateSplitLyricsLineInfos, extraLrcSpaceLineHeight, lineTopY);
                lineTopY -= (LyricsUtils.getTextHeight(paint) + extraLrcSpaceLineHeight);

                //
                lineTopY = drawUpLyrics(canvas, paint, splitLyricsLineInfos, mSpaceLineHeight, lineTopY);
            }
        } else if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
            //画音译歌词
            if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
                //获取分割后的音译歌词行
                List<LyricsLineInfo> transliterationSplitLrcLineInfos = mTransliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineTopY -= (LyricsUtils.getTextHeight(paint) + mSpaceLineHeight);
                lineTopY = drawUpLyrics(canvas, paint, transliterationSplitLrcLineInfos, extraLrcSpaceLineHeight, lineTopY);
                lineTopY -= (LyricsUtils.getTextHeight(paint) + extraLrcSpaceLineHeight);

                //
                lineTopY = drawUpLyrics(canvas, paint, splitLyricsLineInfos, mSpaceLineHeight, lineTopY);
            }
        } else {
            lineTopY -= (LyricsUtils.getTextHeight(paint) + mSpaceLineHeight);
            lineTopY = drawUpLyrics(canvas, paint, splitLyricsLineInfos, mSpaceLineHeight, lineTopY);
        }
        return lineTopY;
    }

    /**
     * 向上绘画歌词
     *
     * @param canvas
     * @param paint
     * @param splitLyricsLineInfos 分隔歌词集合
     * @param spaceLineHeight      空行高度
     * @param fristLineTextY       第一行文字位置
     * @return
     */
    private float drawUpLyrics(Canvas canvas, Paint paint, List<LyricsLineInfo> splitLyricsLineInfos, float spaceLineHeight, float fristLineTextY) {
        int[] paintColors = mPaintColors;


        float lineTopY = fristLineTextY;
        //歌词和空行高度
        float lineHeight = LyricsUtils.getTextHeight(paint) + spaceLineHeight;
        for (int i = splitLyricsLineInfos.size() - 1; i >= 0; i--) {
            if (i != splitLyricsLineInfos.size() - 1) {
                lineTopY -= lineHeight;
            }

            //超出上视图
            if (lineTopY < lineHeight) {
                continue;
            }
            //超出下视图
            if (lineTopY + spaceLineHeight > getHeight()) {
                break;
            }

            String text = splitLyricsLineInfos.get(i).getLineLyrics();
            //计算颜色透明度
            int alpha = mMaxAlpha;

            //颜色透明度过渡

            if (lineTopY < mShadeHeight) {
                alpha = mMaxAlpha - (int) ((mShadeHeight - lineTopY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            } else if (lineTopY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((lineTopY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            }

            alpha = Math.max(alpha, 0);
            paint.setAlpha(alpha);

            float textWidth = LyricsUtils.getTextWidth(paint, text);
            float textX = 0.0f;
            if (mLyricGravity == 0) {
                textX = (getWidth() - textWidth) * 0.5f;
            } else if (mLyricGravity == 1) {
                textX = 0.0f;
            }
            LyricsUtils.drawText(canvas, paint, paintColors, text, textX, lineTopY, getMeasuredWidth());

//            canvas.drawLine(0, lineTopY - getTextHeight(paint), 720, lineTopY - getTextHeight(paint), paint);
//            canvas.drawLine(0, lineTopY, 720, lineTopY, paint);

        }

        //考虑部分歌词越界，导致高度不正确，这里重新获取基本歌词结束后的y轴位置
        lineTopY = fristLineTextY - lineHeight * (splitLyricsLineInfos.size() - 1);
        return lineTopY;
    }


    /**
     * 绘画时间、播放按钮等
     *
     * @param canvas
     */
    private void drawIndicator(Canvas canvas) {
        //获取数据
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = mLrcLineInfos;

        //画当前时间
        int scrollLrcLineNum = getScrollLrcLineNum(mOffsetY);
        int startTime = lrcLineInfos.get(scrollLrcLineNum).getStartTime();
        String timeString = TimeUtils.parseMMSSString(startTime);
        int textHeight = LyricsUtils.getTextHeight(mPaintIndicator);
        float textWidth = LyricsUtils.getTextWidth(mPaintIndicator, timeString);
        int padding = 10;
        float textX = padding;
        float textY = (getHeight() + textHeight) / 2;
        canvas.drawText(timeString, textX, textY, mPaintIndicator);

        mPaintPlay.setStyle(Paint.Style.STROKE);
        //圆形矩形
        if (mPlayBtnRect == null)
            mPlayBtnRect = new Rect();
        //圆半径
        int circleR = mPlayRectSize;
        int linePadding = padding * 2;
        int rectR = getWidth() - linePadding;
        int rectL = rectR - circleR * 2;
        int rectT = getHeight() / 2;
        int rectB = rectT + circleR * 2;
        mPlayBtnRect.set(rectL - padding, rectT - padding, rectR + padding, rectB + padding);

        //画圆
        int cx = rectL + (rectR - rectL) / 2;
        int cy = rectT;
        canvas.drawCircle(cx, cy, circleR, mPaintPlay);

        //画三角形
        Path trianglePath = new Path();
        float startX = cx + circleR / 2;
        float startY = rectT;
        trianglePath.moveTo(startX, startY);// 此点为多边形的起点
        float pleftX = startX - (float) circleR / 4 * 3;
        float ptopY = startY - circleR * (float) Math.sqrt(3) / 4;
        float pbomY = startY + circleR * (float) Math.sqrt(3) / 4;
        trianglePath.lineTo(pleftX, ptopY);
        trianglePath.lineTo(pleftX, pbomY);
        trianglePath.close();// 使这些点构成封闭的多边形
        if (isInPlayBtnRect) {
            mPaintPlay.setStyle(Paint.Style.FILL);
        } else {
            mPaintPlay.setStyle(Paint.Style.STROKE);
        }
        canvas.drawPath(trianglePath, mPaintPlay);

        //画线
        int lineH = 2;
        float lineY = (getHeight() - lineH) / 2;
        float lineLeft = textX + textWidth + linePadding;
        float lineR = rectL - linePadding;
        LinearGradient linearGradientHL = new LinearGradient(lineLeft, lineY + lineH, lineR, lineY + lineH, new int[]{U.getColorUtils().parserColor(mPaintLineColor, 255), U.getColorUtils().parserColor(mPaintLineColor, 0), U.getColorUtils().parserColor(mPaintLineColor, 0), U.getColorUtils().parserColor(mPaintLineColor, 255)}, new float[]{0f, 0.2f, 0.8f, 1f}, Shader.TileMode.CLAMP);
        mPaintLine.setShader(linearGradientHL);
        canvas.drawRect(lineLeft, lineY, lineR, lineY + lineH, mPaintLine);

    }

    /**
     * 更新歌词视图
     *
     * @param playProgress
     */
    private void updateManyLrcView(long playProgress) {
        //获取数据
        if (mLrcLineInfos == null) {
            MyLog.d(TAG, "updateManyLrcView " + " lrcLineInfos为null");
            return;
        }

        int newLyricsLineNum = LyricsUtils.getLineNumber(mLyricsReader.getLyricsType(), mLrcLineInfos, playProgress, mLyricsReader.getPlayOffset());
        if (newLyricsLineNum != mLyricsLineNum) {
            if (mTouchEventStatus == TOUCHEVENTSTATUS_INIT && !mIsTouchIntercept) {
                //初始状态
                int duration = mDuration * getLineSizeNum(mLyricsLineNum);
                int deltaY = getLineAtHeightY(newLyricsLineNum) - mScroller.getFinalY();
                mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, duration);
                invalidateView();
            }
            mLyricsLineNum = newLyricsLineNum;
        }

        updateSplitData(playProgress);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int lrcStatus = getLrcStatus();
        if (!mTouchAble || lrcStatus != AbstractLrcView.LRCSTATUS_LRC) {
            return false;
        }

        mGestureDetector.onTouchEvent(event);
        obtainVelocityTracker(event);
        int actionId = event.getAction();
        switch (actionId) {
            case MotionEvent.ACTION_DOWN:

                mLastY = (int) event.getY();
                mInterceptX = (int) event.getX();
                mInterceptY = (int) event.getY();

                //发送还原
                mHandler.removeMessages(RESETLRCVIEW);


                if (mPlayBtnRect != null && isPlayClick(event)) {
                    isInPlayBtnRect = true;
                    invalidateView();
                }

                break;
            case MotionEvent.ACTION_MOVE:
                int curX = (int) event.getX();
                int curY = (int) event.getY();
                int deltaX = (int) (mInterceptX - curX);
                int deltaY = (int) (mInterceptY - curY);

                if (mIsTouchIntercept || (Math.abs(deltaY) > mTouchSlop && Math.abs(deltaX) < mTouchSlop)) {
                    mIsTouchIntercept = true;

                    int dy = mLastY - curY;

                    //创建阻尼效果
                    float finalY = mOffsetY + dy;

                    if (finalY < getTopOverScrollHeightY() || finalY > getBottomOverScrollHeightY()) {
                        dy = dy / 2;
                        mTouchEventStatus = TOUCHEVENTSTATUS_OVERSCROLL;


                    }

                    mScroller.startScroll(0, mScroller.getFinalY(), 0, dy, 0);
                    invalidateView();

                }

                mLastY = curY;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //判断是否在滑动和是否点击了播放按钮
                if (isInPlayBtnRect) {

                    mHandler.removeMessages(RESETLRCVIEW);

                    if (mOnLrcClickListener != null) {

                        //获取当前滑动到的歌词播放行
                        int scrollLrcLineNum = getScrollLrcLineNum(mOffsetY);
                        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = mLrcLineInfos;
                        int startTime = lrcLineInfos.get(scrollLrcLineNum).getStartTime();
                        mOnLrcClickListener.onLrcPlayClicked(startTime);

                    }
                    mIsTouchIntercept = false;
                    mTouchEventStatus = TOUCHEVENTSTATUS_INIT;
                    isInPlayBtnRect = false;
                    invalidateView();
                } else {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                    int yVelocity = (int) velocityTracker.getYVelocity();
                    int xVelocity = (int) velocityTracker.getXVelocity();

                    if (Math.abs(yVelocity) > mMinimumVelocity) {

                        int startX = 0;
                        int startY = mScroller.getFinalY();
                        int velocityX = -xVelocity;
                        int velocityY = -yVelocity;
                        int minX = 0;
                        int maxX = 0;

                        //
                        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = mLrcLineInfos;
                        int lrcSumHeight = getLineAtHeightY(lrcLineInfos.size());
                        int minY = -getHeight() / 4;
                        int maxY = lrcSumHeight + getHeight() / 4;
                        mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
                        invalidateView();

                        mTouchEventStatus = TOUCHEVENTSTATUS_FLINGSCROLL;

                        //发送还原
                        mHandler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);
                    } else {

                        if (mTouchEventStatus == TOUCHEVENTSTATUS_OVERSCROLL) {
                            resetLrcView();
                        } else {
                            //发送还原
                            mHandler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);

                        }
                    }
                }
                releaseVelocityTracker();

                mLastY = 0;
                mInterceptX = 0;
                mInterceptY = 0;

                break;
            default:
        }

        return true;
    }

    /**
     * 判断是否是播放按钮点击
     *
     * @param event
     * @return
     */
    private boolean isPlayClick(MotionEvent event) {
        if (mPlayBtnRect == null) return false;
        int x = (int) event.getX();
        int y = (int) event.getY();
        return mPlayBtnRect.contains(x, y);

    }

    /**
     * 判断该行总共有多少行歌词（原始歌词 + 分隔歌词）
     *
     * @param lyricsLineNum
     * @return
     */
    private int getLineSizeNum(int lyricsLineNum) {
        //获取数据
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = mLrcLineInfos;
        int extraLrcStatus = getExtraLrcStatus();
        List<LyricsLineInfo> translateLrcLineInfos = mTranslateLrcLineInfos;
        List<LyricsLineInfo> transliterationLrcLineInfos = mTransliterationLrcLineInfos;


        //
        int lineSizeNum = 0;
        LyricsLineInfo lyricsLineInfo = lrcLineInfos
                .get(lyricsLineNum);
        //获取分割后的歌词列表
        List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
        lineSizeNum += lyricsLineInfos.size();

        //判断是否有翻译歌词或者音译歌词
        if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
            if (translateLrcLineInfos != null && translateLrcLineInfos.size() > 0) {
                List<LyricsLineInfo> tempTranslateLrcLineInfos = translateLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineSizeNum += tempTranslateLrcLineInfos.size();
            }
        } else if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
            if (transliterationLrcLineInfos != null && transliterationLrcLineInfos.size() > 0) {
                List<LyricsLineInfo> tempTransliterationLrcLineInfos = transliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineSizeNum += tempTransliterationLrcLineInfos.size();
            }
        }

        return lineSizeNum;
    }


    /**
     * 获取所在歌词行的高度
     *
     * @param lyricsLineNum
     * @return
     */
    private int getLineAtHeightY(int lyricsLineNum) {
        //获取数据
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = mLrcLineInfos;
        Paint paint = mPaint;
        Paint extraLrcPaint = mExtraLrcPaint;
        float spaceLineHeight = mSpaceLineHeight;
        float extraLrcSpaceLineHeight = mExtraLrcSpaceLineHeight;
        int extraLrcStatus = getExtraLrcStatus();
        List<LyricsLineInfo> translateLrcLineInfos = mTranslateLrcLineInfos;
        List<LyricsLineInfo> transliterationLrcLineInfos = mTransliterationLrcLineInfos;

        //
        int lineAtHeightY = 0;
        for (int i = 0; i < lyricsLineNum; i++) {
            LyricsLineInfo lyricsLineInfo = lrcLineInfos
                    .get(i);
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
            lineAtHeightY += (LyricsUtils.getTextHeight(paint) + spaceLineHeight) * lyricsLineInfos.size();

            //判断是否有翻译歌词或者音译歌词
            if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                if (translateLrcLineInfos != null && translateLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> tempTranslateLrcLineInfos = translateLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineAtHeightY += (LyricsUtils.getTextHeight(extraLrcPaint) + extraLrcSpaceLineHeight) * tempTranslateLrcLineInfos.size();
                }
            } else if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                if (transliterationLrcLineInfos != null && transliterationLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> tempTransliterationLrcLineInfos = transliterationLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineAtHeightY += (LyricsUtils.getTextHeight(extraLrcPaint) + extraLrcSpaceLineHeight) * tempTransliterationLrcLineInfos.size();
                }
            }
        }
        return lineAtHeightY;
    }

    /**
     * 获取滑动的当前行
     *
     * @return
     */
    private int getScrollLrcLineNum(float offsetY) {
        //获取数据
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = mLrcLineInfos;
        Paint paint = mPaint;
        Paint extraLrcPaint = mExtraLrcPaint;
        float spaceLineHeight = mSpaceLineHeight;
        float extraLrcSpaceLineHeight = mExtraLrcSpaceLineHeight;
        int extraLrcStatus = getExtraLrcStatus();
        List<LyricsLineInfo> translateLrcLineInfos = mTranslateLrcLineInfos;
        List<LyricsLineInfo> transliterationLrcLineInfos = mTransliterationLrcLineInfos;


        //
        int scrollLrcLineNum = -1;
        int lineHeight = 0;
        for (int i = 0; i < lrcLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = lrcLineInfos
                    .get(i);
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
            lineHeight += (LyricsUtils.getTextHeight(paint) + spaceLineHeight) * lyricsLineInfos.size();

            //判断是否有翻译歌词或者音译歌词
            if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                if (translateLrcLineInfos != null && translateLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> tempTranslateLrcLineInfos = translateLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineHeight += (LyricsUtils.getTextHeight(extraLrcPaint) + extraLrcSpaceLineHeight) * tempTranslateLrcLineInfos.size();
                }
            } else if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                if (transliterationLrcLineInfos != null && transliterationLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> tempTransliterationLrcLineInfos = transliterationLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineHeight += (LyricsUtils.getTextHeight(extraLrcPaint) + extraLrcSpaceLineHeight) * tempTransliterationLrcLineInfos.size();
                }
            }

            if (lineHeight > offsetY) {
                scrollLrcLineNum = i;
                break;
            }
        }
        if (scrollLrcLineNum == -1) {
            scrollLrcLineNum = lrcLineInfos.size() - 1;
        }
        return scrollLrcLineNum;
    }


    /**
     * @param event
     */

    private void obtainVelocityTracker(MotionEvent event) {

        if (mVelocityTracker == null) {

            mVelocityTracker = VelocityTracker.obtain();

        }

        mVelocityTracker.addMovement(event);

    }


    /**
     * 释放
     */
    private void releaseVelocityTracker() {

        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;

        }

    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        // 更新当前的X轴偏移量
        if (mScroller.computeScrollOffset()) { // 返回true代表正在模拟数据，false 已经停止模拟数据
            mOffsetY = mScroller.getCurrY();

            invalidateView();
        } else {
            if (mTouchEventStatus == TOUCHEVENTSTATUS_FLINGSCROLL) {
                resetLrcView();
            }
        }
    }

    /**
     * 还原歌词视图
     */
    private void resetLrcView() {

        if (mOffsetY < 0) {

            int deltaY = -mScroller.getFinalY();
            mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
            invalidateView();
        } else if (mOffsetY > getBottomOverScrollHeightY()) {
            TreeMap<Integer, LyricsLineInfo> lrcLineInfos = mLrcLineInfos;

            int deltaY = getLineAtHeightY(lrcLineInfos.size
                    () - 1) - mScroller.getFinalY();
            mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
            invalidateView();

        }
    }

    /**
     * 获取底部越界
     *
     * @return
     */
    private float getBottomOverScrollHeightY() {
        if (mLrcLineInfos == null) return 0;
        return getLineAtHeightY(mLrcLineInfos.size());
    }

    /**
     * 获取顶部越界高度
     *
     * @return
     */
    private float getTopOverScrollHeightY() {
        return 0;
    }


    /**
     * 指示线颜色
     *
     * @param mPaintLineColor
     */
    public void setPaintLineColor(int mPaintLineColor) {
        this.mPaintLineColor = mPaintLineColor;
    }

    public void setTouchAble(boolean mTouchAble) {
        this.mTouchAble = mTouchAble;
    }


    /**
     * 初始歌词数据
     */
    public void initLrcData() {
        mScroller.setFinalY(0);
        mOffsetY = 0;
        mCentreY = 0;
        mTouchEventStatus = TOUCHEVENTSTATUS_INIT;
        super.initLrcData();
    }


    /**
     * 设置默认颜色
     *
     * @param paintColor
     */
    public void setPaintColor(int[] paintColor) {
        setPaintColor(paintColor, false);
    }


    /**
     * 设置高亮颜色
     *
     * @param paintHLColor
     */
    public void setPaintHLColor(int[] paintHLColor) {
        setPaintHLColor(paintHLColor, false);
    }

    /**
     * 设置高亮颜色
     *
     * @param paintHLColor     至少两种颜色
     * @param isInvalidateView 是否更新视图
     */
    public void setPaintHLColor(int[] paintHLColor, boolean isInvalidateView) {
        mPaintIndicator.setColor(paintHLColor[0]);
        mPaintPlay.setColor(paintHLColor[0]);
        super.setPaintHLColor(paintHLColor, isInvalidateView);
    }

    /**
     * 设置字体文件
     *
     * @param typeFace
     */
    public void setTypeFace(Typeface typeFace) {
        setTypeFace(typeFace, false);
    }

    /**
     * 设置字体文件
     *
     * @param typeFace
     * @param isInvalidateView 是否更新视图
     */
    public void setTypeFace(Typeface typeFace, boolean isInvalidateView) {
        if (isInvalidateView) {
            setTypeFace(typeFace, false);
            resetScrollerFinalY();
        }
        super.setTypeFace(typeFace, isInvalidateView);
    }

    /**
     * //字体大小、额外歌词显示或者空行大小改变，则对歌词的位置进行修改
     * 重置scroller的finaly
     */
    private void resetScrollerFinalY() {
        int lyricsLineNum = mLyricsLineNum;
        //字体大小、额外歌词显示或者空行大小改变，则对歌词的位置进行修改
        mOffsetY = getLineAtHeightY(lyricsLineNum);
        mScroller.setFinalY((int) mOffsetY);
    }


    /**
     * 设置空行高度
     *
     * @param spaceLineHeight
     */
    public void setSpaceLineHeight(float spaceLineHeight) {
        setSpaceLineHeight(spaceLineHeight, false);
    }

    /**
     * 设置额外空行高度
     *
     * @param extraLrcSpaceLineHeight
     */
    public void setExtraLrcSpaceLineHeight(float extraLrcSpaceLineHeight) {
        setExtraLrcSpaceLineHeight(extraLrcSpaceLineHeight, false);
    }

    /**
     * 设置额外歌词的显示状态
     *
     * @param extraLrcStatus
     */
    public void setExtraLrcStatus(int extraLrcStatus) {
        super.setExtraLrcStatus(extraLrcStatus);
        resetScrollerFinalY();
        super.setExtraLrcStatus(extraLrcStatus, true);
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     */
    public void setFontSize(float fontSize) {
        setFontSize(fontSize, false);
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     * @param isReloadData 是否重新加载数据及刷新界面
     */
    public void setFontSize(float fontSize, boolean isReloadData) {
        if (isReloadData) {
            super.setFontSize(fontSize, false);
            resetScrollerFinalY();
        }

        super.setFontSize(fontSize, isReloadData);
    }

    /**
     * 设置额外字体大小
     *
     * @param extraLrcFontSize
     */
    public void setExtraLrcFontSize(float extraLrcFontSize) {
        setExtraLrcFontSize(extraLrcFontSize, false);
    }

    /**
     * 设置额外字体大小
     *
     * @param extraLrcFontSize
     * @param isReloadData     是否重新加载数据及刷新界面
     */
    public void setExtraLrcFontSize(float extraLrcFontSize, boolean isReloadData) {
        if (isReloadData) {
            super.setExtraLrcFontSize(extraLrcFontSize, false);
            resetScrollerFinalY();
        }
        super.setExtraLrcFontSize(extraLrcFontSize, isReloadData);
    }

    /**
     * 设置歌词解析器
     *
     * @param lyricsReader
     */
    public void setLyricsReader(LyricsReader lyricsReader) {
        super.setLyricsReader(lyricsReader);
        if (lyricsReader != null && lyricsReader.getLyricsType() == LyricsInfo.DYNAMIC) {
            int extraLrcType = getExtraLrcType();
            //翻译歌词以动感歌词形式显示
            if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_BOTH || extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLATELRC) {
                super.setTranslateDrawType(AbstractLrcView.TRANSLATE_DRAW_TYPE_DYNAMIC);
            }
        }
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     * @param extraFontSize 额外歌词字体
     */
    public void setSize(int fontSize, int extraFontSize) {
        setSize(fontSize, extraFontSize, false);
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     * @param extraFontSize 额外歌词字体
     * @param isReloadData  是否重新加载数据及刷新界面
     */
    public void setSize(int fontSize, int extraFontSize, boolean isReloadData) {
        if (isReloadData) {
            super.setSize(fontSize, extraFontSize, false);
            resetScrollerFinalY();
        }
        super.setSize(fontSize, extraFontSize, isReloadData);
    }

    /**
     * 是否绘画时间指示器
     *
     * @param isDrawIndicator
     */
    public void setIsDrawIndicator(boolean isDrawIndicator) {
        this.mIsDrawIndicator = isDrawIndicator;
    }

    /**
     * 设置指示器字体大小
     *
     * @param fontSize
     */
    public void setIndicatorFontSize(int fontSize) {
        mPlayBtnRect = null;
        this.mPlayRectSize = fontSize;
        mPaintIndicator.setTextSize(mPlayRectSize);
        mPaintLine.setTextSize(mPlayRectSize);
        mPaintPlay.setTextSize(mPlayRectSize);
        invalidateView();
    }

    /**
     * 设置歌词点击事件
     *
     * @param onLrcClickListener
     */
    public void setOnLrcClickListener(OnLrcClickListener onLrcClickListener) {
        this.mOnLrcClickListener = onLrcClickListener;
    }

    OnLyricViewTapListener onLyricViewTapListener;

    public void setOnLyricViewTapListener(OnLyricViewTapListener onLyricViewTapListener) {
        this.onLyricViewTapListener = onLyricViewTapListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 歌词事件
     */
    public interface OnLrcClickListener {
        /**
         * 歌词快进播放
         *
         * @param progress
         */
        void onLrcPlayClicked(int progress);
    }

    public interface OnLyricViewTapListener {
        void onDoubleTap();

        void onSigleTap(int progress);
    }

}
