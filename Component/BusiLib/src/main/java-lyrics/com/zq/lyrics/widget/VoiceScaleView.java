package com.zq.lyrics.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.zq.lyrics.model.LyricsLineInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 音阶view
 */
public class VoiceScaleView extends View {
    public final static String TAG = "VoiceScaleView";
    int mSpeed = U.getDisplayUtils().dip2px(36);   //一秒钟走多少

    int mRedLine = U.getDisplayUtils().dip2px(87);  //中间红点的线

    int srcollLength;   //滚动的距离
    int srcollTime;     //滚动的时间

    //歌词
    List<LyricsLineInfo> mLyricsLineInfoList;
    //歌词开始播放时间
    int starLyricsLine = 0;

    boolean hasRed = false;  // 是否有圆点
    int redWith;   //圆心点
    int redHight;  //圆心点

    //毫秒为单位
    int showTime = U.getDisplayUtils().getScreenWidth() / mSpeed * 1000;// 可显示时长

    HandlerTaskTimer mTaskTimer;

    long mStartTs = 0;

    public VoiceScaleView(Context context) {
        this(context, null);
    }

    public VoiceScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void startWithData(List<LyricsLineInfo> lyricsLineInfoList, int beginTime) {
        MyLog.d(TAG, "startWithData" + " lyricsLineInfoList=" + lyricsLineInfoList);
        this.mLyricsLineInfoList = lyricsLineInfoList;

        starLyricsLine = beginTime;

        mStartTs = System.currentTimeMillis();
        postInvalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mLyricsLineInfoList == null || mLyricsLineInfoList.size() == 0){
            return;
        }

        srcollTime = (int) (System.currentTimeMillis() - mStartTs);
        srcollLength = srcollTime * mSpeed / 1000;

        if (srcollTime >= showTime) {
            drawView(canvas, srcollTime - showTime, srcollTime);
        } else if (srcollTime != 0) {
            drawView(canvas, 0, srcollTime);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTaskTimer != null) {
            mTaskTimer.dispose();
        }
    }

    private void drawView(Canvas canvas, int startTime, int endTime) {
        MyLog.d(TAG, "drawView" + " canvas=" + canvas + " startTime=" + startTime + " endTime=" + endTime);
        Paint leftPaint = new Paint();
        leftPaint.setColor(Color.parseColor("#F5A623"));

        Paint rightPaint = new Paint();
        rightPaint.setColor(Color.parseColor("#474A5F"));

//        List<LyricsLineInfo> canShowLyrics = getCanShowLyrics(mLyricsLineInfoList, startTime, endTime);
//        if (canShowLyrics == null || canShowLyrics.size() <= 0) {
//            if (mTaskTimer != null) {
//                mTaskTimer.dispose();
//            }
//            return;
//        }

        // 假设一句歌词长度为40dp
        boolean isLowStart = true;
        for (LyricsLineInfo lyricsLineInfo : mLyricsLineInfoList) {
            //屏幕宽度 - 已经过去的长度 + 一行歌词真正开始的位置 - 再减去红点到屏幕右边的位置
            int left = U.getDisplayUtils().getScreenWidth() - srcollLength + (lyricsLineInfo.getStartTime() - starLyricsLine) * mSpeed / 1000 - (U.getDisplayUtils().getScreenWidth() - mRedLine);
            int right = left + (lyricsLineInfo.getEndTime() - lyricsLineInfo.getStartTime()) * mSpeed / 1000;
            int top = isLowStart ? U.getDisplayUtils().dip2px(40) : U.getDisplayUtils().dip2px(20);
            int bottom = top + U.getDisplayUtils().dip2px(7);

            if (left < mRedLine && right <= mRedLine) {
                RectF rectF = new RectF();
                rectF.left = left;
                rectF.right = right;
                rectF.top = top;
                rectF.bottom = bottom;
                canvas.drawRoundRect(rectF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), leftPaint);
            } else if (left < mRedLine) {
                RectF rectLeftF = new RectF();
                rectLeftF.left = left;
                rectLeftF.right = mRedLine;
                rectLeftF.top = top;
                rectLeftF.bottom = bottom;
                canvas.drawRoundRect(rectLeftF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), leftPaint);
                RectF rectRightF = new RectF();
                rectRightF.left = mRedLine;
                rectRightF.right = right;
                rectRightF.top = top;
                rectRightF.bottom = bottom;
                canvas.drawRoundRect(rectRightF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), rightPaint);
                hasRed = true;
                redWith = mRedLine;
                redHight = (top + bottom) / 2;
            } else {
                RectF rectF = new RectF();
                rectF.left = left;
                rectF.right = right;
                rectF.top = top;
                rectF.bottom = bottom;
                canvas.drawRoundRect(rectF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), rightPaint);
            }
            isLowStart = !isLowStart;
        }

        if (hasRed) {
            Paint mOutpaint = new Paint(); //外圈
            mOutpaint.setColor(Color.parseColor("#EF5E85"));
            mOutpaint.setAntiAlias(true);
            canvas.drawCircle(redWith, redHight, U.getDisplayUtils().dip2px(9), mOutpaint);
            Paint mInpaint = new Paint(); //内圈
            mInpaint.setColor(Color.parseColor("#CA2C60"));
            mInpaint.setAntiAlias(true);
            canvas.drawCircle(redWith, redHight, U.getDisplayUtils().dip2px(6), mInpaint);
        }
        hasRed = false;

        postInvalidateDelayed(30);
    }

    public void cancelTaskTimer() {
        if (mTaskTimer != null) {
            mTaskTimer.dispose();
        }
    }

    // 得到可显示的歌词
    private List<LyricsLineInfo> getCanShowLyrics(List<LyricsLineInfo> lyricsLineInfoList, int startTime, int endTime) {
        if (lyricsLineInfoList == null || lyricsLineInfoList.size() <= 0) {
            return null;
        }

        List<LyricsLineInfo> lyricsLineInfos = new ArrayList<>();
        for (LyricsLineInfo lyricsLineInfo : lyricsLineInfoList) {
            if ((lyricsLineInfo.getStartTime() > (starLyricsLine + startTime) && lyricsLineInfo.getStartTime() < (starLyricsLine + endTime))
                    || (lyricsLineInfo.getEndTime() > (starLyricsLine + startTime) && lyricsLineInfo.getEndTime() < (starLyricsLine + endTime))) {
                lyricsLineInfos.add(lyricsLineInfo);
            }
        }
        return lyricsLineInfos;
    }
}
