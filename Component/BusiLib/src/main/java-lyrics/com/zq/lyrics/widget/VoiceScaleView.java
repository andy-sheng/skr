package com.zq.lyrics.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.zq.lyrics.model.LyricsLineInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 音阶view
 */
public class VoiceScaleView extends View {

    int mSpeed = U.getDisplayUtils().dip2px(36);   //一秒钟走多少
    int mIntervalTime = 10; //视图刷新时间，毫秒为单位,绘制频率
    int mIntervalSpeed = mSpeed * mIntervalTime / 1000; //每个周期移动的距离

    int mRedLine = U.getDisplayUtils().dip2px(87);  //中间红点的线

    int srcollLength;   //滚动的距离
    int srcollTime;     //滚动的时间

    List<LyricsLineInfo> mLyricsLineInfoList; // 歌词
    int starLyricsLine = 0;                   // 第一句歌词开始的时间

    boolean hasRed = false;  // 是否有圆点
    int redWith;   //圆心点
    int redHight;  //圆心点

    //毫秒为单位
    int showTime = U.getDisplayUtils().getScreenWidth() / mSpeed * 1000;// 可显示时长

    HandlerTaskTimer mTaskTimer;

    public VoiceScaleView(Context context) {
        this(context, null);
    }

    public VoiceScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void startWithData(List<LyricsLineInfo> lyricsLineInfoList) {
        this.mLyricsLineInfoList = lyricsLineInfoList;
        if (mLyricsLineInfoList != null && mLyricsLineInfoList.size() > 0) {
            starLyricsLine = mLyricsLineInfoList.get(0).getStartTime();
        }

        mTaskTimer = HandlerTaskTimer.newBuilder()
                .interval(mIntervalTime)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        srcollLength = integer * mIntervalSpeed;
                        srcollTime = integer * mIntervalTime;
                        postInvalidate();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                    }
                });

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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
        Paint leftPaint = new Paint();
        leftPaint.setColor(Color.parseColor("#F5A623"));

        Paint rightPaint = new Paint();
        rightPaint.setColor(Color.parseColor("#474A5F"));

        List<LyricsLineInfo> canShowLyrics = getCanShowLyrics(mLyricsLineInfoList, startTime, endTime);
        if (canShowLyrics == null || canShowLyrics.size() <= 0) {
            if (mTaskTimer != null) {
                mTaskTimer.dispose();
            }
            return;
        }

        // 假设一句歌词长度为40dp
        boolean isLowStart = true;
        for (LyricsLineInfo lyricsLineInfo : mLyricsLineInfoList) {
            int left = U.getDisplayUtils().getScreenWidth() - srcollLength + (lyricsLineInfo.getStartTime() - starLyricsLine) * mSpeed / 1000;
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
