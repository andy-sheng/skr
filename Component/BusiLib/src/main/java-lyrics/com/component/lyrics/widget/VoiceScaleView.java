package com.component.lyrics.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.common.log.MyLog;
import com.common.utils.U;
import com.component.busilib.R;
import com.component.lyrics.model.LyricsLineInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 以秒为基准
 * 音阶view
 */
public class VoiceScaleView extends View {
    public final String TAG = "VoiceScaleView";
    static final int SPEED = U.getDisplayUtils().dip2px(72);// 每秒走72个像素单位
    float mReadLineX = 0.2f;// 红线大约在距离左边 20% 的位置
    float mRedCy;
    int mWidth = -1;// view的宽度
    int mHeight = -1;// view的高度
    long mLocalBeginTs = -1;// 本地开始播放的时间戳，本地基准时间
    long mTranslateTX = 0;// 调用方告知的偏移量

    List<LyricsLineInfo> mLyricsLineInfoList = new ArrayList<>(); // 歌词

    Paint mLeftBgPaint;
    Paint mRightBgPaint;
    Paint mRedLinePaint;
    Paint mRedOutpaint;
    Paint mRedInnerpaint;
    Paint mLeftPaint;
    Paint mRightPaint;

    int mLeftBgPaintColor = Color.parseColor("#252736");
    int mRightBgPaintColor = Color.parseColor("#292B3A");
    int mRedLinePaintColor = Color.parseColor("#494C62");
    int mRedOutpaintColor = Color.parseColor("#EF5E85");
    int mRedInnerpaintColor = Color.parseColor("#CA2C60");
    int mLeftPaintColor = Color.parseColor("#F5A623");
    int mRightPaintColor = Color.parseColor("#474A5F");

    public VoiceScaleView(Context context) {
        this(context, null);
        init(context, null);
    }

    public VoiceScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public VoiceScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceScaleView);
        mLeftBgPaintColor = typedArray.getColor(R.styleable.VoiceScaleView_leftBgPaintColor, mLeftBgPaintColor);
        mRightBgPaintColor = typedArray.getColor(R.styleable.VoiceScaleView_rightBgPaintColor, mRightBgPaintColor);
        mRedLinePaintColor = typedArray.getColor(R.styleable.VoiceScaleView_redLinePaintColor, mRedLinePaintColor);
        mRedOutpaintColor = typedArray.getColor(R.styleable.VoiceScaleView_redOutpaintColor, mRedOutpaintColor);
        mRedInnerpaintColor = typedArray.getColor(R.styleable.VoiceScaleView_redInnerpaintColor, mRedInnerpaintColor);
        mLeftPaintColor = typedArray.getColor(R.styleable.VoiceScaleView_leftPaintColor, mLeftPaintColor);
        mRightPaintColor = typedArray.getColor(R.styleable.VoiceScaleView_rightPaintColor, mRightPaintColor);
        typedArray.recycle();

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        mLeftBgPaint = new com.common.view.ExPaint();
        mLeftBgPaint.setColor(mLeftBgPaintColor);

        mRightBgPaint = new com.common.view.ExPaint();
        mRightBgPaint.setColor(mRightBgPaintColor);

        mRedLinePaint = new com.common.view.ExPaint();
        mRedLinePaint.setColor(mRedLinePaintColor);

        mRedOutpaint = new com.common.view.ExPaint(); //外圈
        mRedOutpaint.setColor(mRedOutpaintColor);
        mRedOutpaint.setAntiAlias(true);

        mRedInnerpaint = new com.common.view.ExPaint(); //内圈
        mRedInnerpaint.setColor(mRedInnerpaintColor);
        mRedInnerpaint.setAntiAlias(true);

        mLeftPaint = new com.common.view.ExPaint();
        mLeftPaint.setMaskFilter(new BlurMaskFilter(U.getDisplayUtils().dip2px(5), BlurMaskFilter.Blur.SOLID));
        mLeftPaint.setColor(mLeftPaintColor);

        mRightPaint = new com.common.view.ExPaint();
        mRightPaint.setColor(mRightPaintColor);
    }

    /**
     * @param lyricsLineInfoList
     * @param translateTX        调用方告知的偏移量
     */
    public void startWithData(List<LyricsLineInfo> lyricsLineInfoList, int translateTX) {
        MyLog.d(TAG, "startWithData" + " lyricsLineInfoList=" + lyricsLineInfoList);
        this.mLyricsLineInfoList = lyricsLineInfoList;
        this.mLocalBeginTs = -1;
        this.mTranslateTX = translateTX;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth < 0) {
            mWidth = getWidth();
        }
        if (mHeight < 0) {
            mHeight = getHeight();
        }
        if (mLocalBeginTs < 0) {
            mLocalBeginTs = System.currentTimeMillis();
        }
        float divideLineTX = mReadLineX * mWidth;

        initBackground(canvas, divideLineTX);

        boolean isLowStart = true;
        boolean isRedFlag = false;         //标记当前此时歌词是否与圆点重合
        long duration = System.currentTimeMillis() - mLocalBeginTs;// 流逝了这么多的物理时间
        for (LyricsLineInfo lyricsLineInfo : mLyricsLineInfoList) {
            float left = divideLineTX + (lyricsLineInfo.getStartTime() - mTranslateTX - duration) * SPEED / 1000;
            float right = divideLineTX + (lyricsLineInfo.getEndTime() - mTranslateTX - duration) * SPEED / 1000;
            float h = U.getDisplayUtils().dip2px(7);
            float top = isLowStart ? getHeight() * 2 / 3 - h / 2 : getHeight() * 1 / 3 - h / 2;
            float bottom = top + h;
            if (right < left) {
                MyLog.w(TAG, "right<left? error");
                continue;
            }

            if (right <= divideLineTX) {
                RectF rectF = new RectF();
                rectF.left = left;
                rectF.right = right;
                rectF.top = top;
                rectF.bottom = bottom;
                canvas.drawRoundRect(rectF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), mLeftPaint);
            } else if (left < divideLineTX && right > divideLineTX) {
                RectF rectLeftF = new RectF();
                rectLeftF.left = left;
                rectLeftF.right = divideLineTX;
                rectLeftF.top = top;
                rectLeftF.bottom = bottom;
                canvas.drawRoundRect(rectLeftF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), mLeftPaint);
                RectF rectRightF = new RectF();
                rectRightF.left = divideLineTX;
                rectRightF.right = right;
                rectRightF.top = top;
                rectRightF.bottom = bottom;
                canvas.drawRoundRect(rectRightF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), mRightPaint);
                isRedFlag = true;
                mRedCy = (top + bottom) / 2;
            } else if (left >= divideLineTX) {
                RectF rectF = new RectF();
                rectF.left = left;
                rectF.right = right;
                rectF.top = top;
                rectF.bottom = bottom;
                canvas.drawRoundRect(rectF, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(10), mRightPaint);
            }
            isLowStart = !isLowStart;
        }

        if (!isRedFlag) {
            mRedCy = getHeight() / 2.0f;
        }
        canvas.drawCircle(divideLineTX, mRedCy, U.getDisplayUtils().dip2px(9), mRedOutpaint);
        canvas.drawCircle(divideLineTX, mRedCy, U.getDisplayUtils().dip2px(6), mRedInnerpaint);

        if (!mLyricsLineInfoList.isEmpty()) {
            LyricsLineInfo last = mLyricsLineInfoList.get(mLyricsLineInfoList.size() - 1);
            if (divideLineTX / SPEED * 1000 + last.getEndTime() - mTranslateTX - duration > 0) {
                // 还能画，让时间继续流逝
                postInvalidateDelayed(30);
            }
        }

    }

    private void initBackground(Canvas canvas, float divideLineTX) {
        RectF leftBgF = new RectF();
        leftBgF.left = 0;
        leftBgF.right = divideLineTX;
        leftBgF.top = 0;
        leftBgF.bottom = mHeight;
        canvas.drawRect(leftBgF, mLeftBgPaint);

        RectF rightBgF = new RectF();
        rightBgF.left = divideLineTX;
        rightBgF.right = mWidth;
        rightBgF.top = 0;
        rightBgF.bottom = mHeight;
        canvas.drawRect(rightBgF, mRightBgPaint);

        RectF redLineF = new RectF();
        redLineF.left = divideLineTX - U.getDisplayUtils().dip2px(1) / 2;
        redLineF.right = divideLineTX + U.getDisplayUtils().dip2px(1) / 2;
        redLineF.top = 0;
        redLineF.bottom = mHeight;
        canvas.drawRect(redLineF, mRedLinePaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }
}
