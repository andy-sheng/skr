package com.wali.live.watchsdk.income.records;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.View;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.income.records.model.RecordsItem;


/**
 * Created by zhaomin on 17-6-27.
 */
public class StickyItemDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = "StickyItemDecoration";
    private IHeaderInfoCallBack mHeaderInfoCallback;
    private int mHeight = DisplayUtils.dip2px(23.3f);
    private Paint mPaint;
    private TextPaint mTextPaint;
    private int mTextLeftMargin = DisplayUtils.dip2px(10);
    private int mHintLeftMargin = DisplayUtils.dip2px(41);

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        int position = ((LinearLayoutManager)parent.getLayoutManager()).findFirstVisibleItemPosition();
        int firstCompleteVisiblePos = ((LinearLayoutManager)parent.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        RecordsItem item = mHeaderInfoCallback.getInfo(position);
        boolean showHint = mHeaderInfoCallback.isTodayAndHasIncome(position); // 是否显示当日金额累加中这个文案
        RecordsItem completeVisibleItem = mHeaderInfoCallback.getInfo(firstCompleteVisiblePos);
        if (item == null || completeVisibleItem == null) {
            MyLog.d(TAG, "onDrawOver  item null " + item );
            return;
        }
        MyLog.d(TAG, "onDrawOver  item DAY " + item.getDay() );
        int left = parent.getLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int height = mHeight;
        if (completeVisibleItem.ismIsDayHint()) {
            View view = parent.getLayoutManager().findViewByPosition(firstCompleteVisiblePos);
            height = (view.getTop() > 0 && view.getTop() < mHeight) ? view.getTop() : mHeight;
        }
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        Rect rect = new Rect(left, height - mHeight, right, height);
        int baseline = (rect.bottom + rect.top - fontMetrics.ascent - fontMetrics.descent) / 2;
        c.drawRect(rect, mPaint);
        String text = String.valueOf(item.getDay()) + GlobalData.app().getResources().getString(R.string.lable_day);
        c.drawText(text, mTextLeftMargin, baseline, mTextPaint);
        if (showHint) {
            String hint =  GlobalData.app().getResources().getString(R.string.today_has_income);
            c.drawText(hint, mHintLeftMargin, baseline, mTextPaint);
        }
    }

    public StickyItemDecoration(Context context, IHeaderInfoCallBack mHeaderInfoCallback) {
        this.mHeaderInfoCallback = mHeaderInfoCallback;
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setColor(context.getResources().getColor(R.color.color_f2f2f2));
        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(DisplayUtils.dip2px(12));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(GlobalData.app().getResources().getColor(R.color.color_black_trans_50));
    }
}
