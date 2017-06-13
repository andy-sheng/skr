package com.wali.live.video.widget.player;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.base.log.MyLog;
import com.base.view.RotatedSeekBar;
import com.live.module.common.R;
import com.wali.live.proto.HotSpotProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiyangli on 16-10-14.
 */
public class HotSpotSeekBar extends RotatedSeekBar {

    private Paint mFill_paint;
    private long mDuration = -1l;

    public HotSpotSeekBar(Context context) {
        super(context);
    }

    public HotSpotSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HotSpotSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //测试数据
//        points.add(600606l);
//        points.add(982993l);
//        points.add(1507995l);

        init();
    }

    private void init() {
        mFill_paint = new Paint();
        mFill_paint.setStrokeWidth(4);
        mFill_paint.setStyle(Paint.Style.FILL);
        mFill_paint.setAntiAlias(true);
        mFill_paint.setColor(getResources().getColor(R.color.white));
    }

    public void setTotalDuration(long duration) {
        mDuration = duration;
    }

    int start = 0;
    List<Long> points = new ArrayList<>();
    List<HotSpotProto.HotSpotInfo> spotInfoList = new ArrayList<>();

    public void addPoints(long point) {
        if (point > 0) {
            points.add(point);
        } else {
            MyLog.e("hotspot point < 0");
        }
    }

    public void setHotSpotInfoList(List<HotSpotProto.HotSpotInfo> list) {
        spotInfoList.clear();
        spotInfoList.addAll(list);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        isReplay = true;

        canvas.save();
        canvas.rotate(mDegree, mX, mY);

        mPaint.setAlpha(128);
        mPaint.setShadowLayer(2, 2, 4, getResources().getColor(R.color.color_black_trans_6));

        if (mBitmapBK != null) {
            canvas.drawBitmap(mBitmapBK, mSrcRectBK, mDstRectBK, mPaint);
        } else if (mShapeSize >= 0) {
            mPaint.setColor(mShapeColorBK);
            mPaint.setStrokeWidth(mShapeSize);
            canvas.drawLine(mDstRectFT.left, mY, mDstRectBK.right, mY, mPaint);
        }

        if (mBitmapFT != null && isEnabled()) {
            canvas.drawBitmap(mBitmapFT, mSrcRectFT, mDstRectFT, mPaint);
        } else if (mShapeSize >= 0 && isEnabled()) {
            mPaint.setColor(mShapeColorFT);
            mPaint.setStrokeWidth(mShapeSize);
            canvas.drawLine(mDstRectFT.left, mY, mDstRectFT.right, mY, mPaint);
        }

        if (start == 0) {
            start = mDstRectFT.left;
        }
        //画实心圆
        for (int i = 0; i < points.size(); i++) {
            if (timeStamp2width(points.get(i)) > mSeekBarWidth) {
                canvas.drawCircle(mSeekBarWidth + start - 8, mY, 8, mFill_paint);
            } else {
                canvas.drawCircle(timeStamp2width(points.get(i)) + start, mY, 8, mFill_paint);
            }
        }
        mPaint.setShadowLayer(0, 0, 0, getResources().getColor(R.color.white));
        canvas.restore();
        if (mThumb != null) {
            if (mThumbPress != null && mIsPressed) {
                canvas.drawBitmap(mThumbPress, mSrcRectBtnPress, mDstRectBtnPress, mPaint);
            } else {
                canvas.drawBitmap(mThumb, mSrcRectBtn, mDstRectBtn, mPaint);
            }
        }
        super.onDraw(canvas);
    }

    protected int timeStamp2width(long timeStamp) {
        return (int) (mSeekBarWidth * (timeStamp * 1.0 / mDuration));
    }

    protected int width2timeStamp(int width) {
        return (int) ((width * 1.0 * mDuration) / mSeekBarWidth);
    }

    /**
     * 重写onMeasure
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureDimension(260, widthMeasureSpec);
        int height = measureDimension(20, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public int measureDimension(int defaultSize, int measureSpec) {
        int result;

        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize;
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    boolean isMoving = false;
    boolean isConsume = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isConsume && event.getAction() == MotionEvent.ACTION_MOVE) {
            isMoving = true;
            if (isConsume) {
                isConsume = false;
                return showHotSpot(v, event);
            } else {
                return super.onTouch(v, event);
            }
        } else {
            if (HotSpotView.isShowing()) {
                HotSpotView.hideHotSpot();
                return true;
            }
            if (!isMoving && event.getAction() == MotionEvent.ACTION_DOWN) {
                return showHotSpot(v, event);
            } else if (isConsume && event.getAction() == MotionEvent.ACTION_UP) {
                isConsume = false;
                isMoving = false;
                return true;
            }
        }
        isConsume = false;
        isMoving = false;
        return super.onTouch(v, event);

    }

    public boolean showHotSpot(View v, MotionEvent event) {
        for (int i = 0; i < points.size(); i++) {
            if (event.getX() < timeStamp2width(points.get(i)) + start + 30 && event.getX() > timeStamp2width(points.get(i)) - 30 + start) {
                List<HotSpotProto.HotSpotInfo> clickNearSpots = new ArrayList<>();
                //热点前后20像素的所有点
                for (int j = 0; j < spotInfoList.size(); j++) {
                    if (spotInfoList.get(j).getHotTimeOffset() < points.get(i) + width2timeStamp(20) && spotInfoList.get(j).getHotTimeOffset() > points.get(i) - width2timeStamp(20)) {
                        clickNearSpots.add(spotInfoList.get(j));
                    }
                }
                if (event.getAction() != MotionEvent.ACTION_MOVE) {
                    HotSpotView.showHotSpotContent(getContext(), v, timeStamp2width(points.get(i)) + start, getY(), clickNearSpots);
                    isConsume = true;
                    return true;
                }
            }
        }
        isConsume = false;
        isMoving = false;
        return super.onTouch(v, event);
    }

    public void seekBarGone() {
        HotSpotView.hideHotSpot();
    }
}
