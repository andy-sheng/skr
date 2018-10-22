package com.wali.live.modulechannel.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.U;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

import java.util.List;

/**
 * Created by zhaomin on 17-3-8.
 *
 * @module 可变长度的标签， 支持任意颜色的按钮背景图
 */
public class MultiLineTagLayout extends RelativeLayout implements IFoldViewContent {

    private static final String TAG = MultiLineTagLayout.class.getSimpleName();
    private static final int MAX_LINE = 2;
    private static final int MARGIN_BETWEEN_ITEMS = U.getDisplayUtils().dip2px(10);
    private static final int MARGIN_BETWEEN_LINES = U.getDisplayUtils().dip2px(10);
    private static final int ITEM_HEIGHT = U.getDisplayUtils().dip2px(30);
    private static final int ITEM_ROUND_CORNER = U.getDisplayUtils().dip2px(20);
    private static final int ITEM_STROKE_WIDTH = U.getDisplayUtils().dip2px(0.66f);
    private static final int ITEM_TEXT_SIZE = 12;
    private static final int ITEM_TEXT_COLOR = R.color.black_trans_60;
    private static final int LEFT_PADDING = 0;

    private int mSearchTagLeftAndRightPadding = U.getDisplayUtils().dip2px(13.33f);     //热门搜索　每个tag的padding


    private static final int[] ITEMCOLORS = {
            R.color.channel_color_channel_tag_stroke_bg_1,
            R.color.channel_color_channel_tag_stroke_bg_2,
            R.color.channel_color_channel_tag_stroke_bg_3};

    private int mTextSize = ITEM_TEXT_SIZE;
    private int mTextColor = ITEM_TEXT_COLOR;
    private int mTextViewHeight = ITEM_HEIGHT;

    private int mLine = 1;
    private int mLeftWidth;
    private int mTotalWidth;
    private int mMaxLineHeight;
    private int mRealHeight;

    private OnItemClickListener mItemClickListener;

    public MultiLineTagLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MultiLineTagLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultiLineTagLayout(Context context) {
        super(context);
        init();
    }

    private void init() {

    }

    public void setItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    private void reset() {
        mTotalWidth = U.getDisplayUtils().getScreenWidth() - LEFT_PADDING * 2;
        mLeftWidth = mTotalWidth;
        mLine = 1;
    }

    @Override
    public void bindData(Object object) {
        if (object == null || ((List<ChannelLiveViewModel.RichText>) object).isEmpty()) {
            return;
        }
        List<ChannelLiveViewModel.RichText> dataList = (List<ChannelLiveViewModel.RichText>) object;
        reset();
        TextView referenceView = null;
        if (mTotalWidth <= 0) {
            MyLog.e(TAG, "bindData  totalWidth is : " + mTotalWidth);
            return;
        }
        removeAllViews();
        setPadding(LEFT_PADDING, 0, LEFT_PADDING, 0);
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(mTotalWidth, MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(2000, MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < dataList.size(); i++) {
            ChannelLiveViewModel.RichText tag = dataList.get(i);
            TextView textView = generateTextView(tag, i);
            if (textView == null) {
                continue;
            }
            Paint paint = textView.getPaint();
            textView.setId(U.getCommonUtils().generateViewId());
            Rect rect = new Rect();
            paint.getTextBounds(tag.getText(), 0, tag.getText().length(), rect);
            int itemWidth = rect.width() + mSearchTagLeftAndRightPadding * 2;
            LayoutParams params = new LayoutParams(itemWidth, mTextViewHeight);
            if (referenceView != null) {
                if (itemWidth <= mLeftWidth) {
                    params.addRule(ALIGN_TOP, referenceView.getId());
                    params.addRule(RIGHT_OF, referenceView.getId());
                    params.leftMargin = MARGIN_BETWEEN_ITEMS;
                } else {
                    params.addRule(BELOW, referenceView.getId());
                    params.addRule(ALIGN_PARENT_LEFT, textView.getId());
                    params.topMargin = MARGIN_BETWEEN_LINES;
                    mLine++;
                    mLeftWidth = mTotalWidth;
                }
            }
            mLeftWidth -= (itemWidth + MARGIN_BETWEEN_ITEMS);
            referenceView = textView;
            addView(textView, params);
            if (mLine == MAX_LINE && mMaxLineHeight == 0) {
                measure(widthMeasureSpec, heightMeasureSpec);
                mMaxLineHeight = getMeasuredHeight();
            }
        }
        measure(widthMeasureSpec, heightMeasureSpec);
        mRealHeight = getMeasuredHeight();
        MyLog.i(TAG, " bindData line: " + mLine + " REAL: " + mRealHeight + " maxLineHeight: " + mMaxLineHeight);
    }

    /**
     * 生成每个元素
     *
     * @param tag
     * @param i
     * @return
     */
    private TextView generateTextView(ChannelLiveViewModel.RichText tag, final int i) {
        if (TextUtils.isEmpty(tag.getText())) {
            return null;
        }
        TextView textView = new TextView(getContext());
        textView.setTextColor(getContext().getResources().getColor(mTextColor));
        textView.setTextSize(mTextSize);
        textView.setGravity(Gravity.CENTER);
        textView.setText(tag.getText());
        textView.setBackground(generateColorfulDrawable(ITEMCOLORS[i % ITEMCOLORS.length]));
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(i);
                }
            }
        });
        return textView;
    }

    /**
     * 生成指定颜色的圆角矩形 背景图
     *
     * @param color
     * @return
     */
    private Drawable generateColorfulDrawable(int color) {
        StateListDrawable listDrawable = new StateListDrawable();
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(ITEM_ROUND_CORNER);
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setStroke(ITEM_STROKE_WIDTH, getContext().getResources().getColor(color));

        GradientDrawable pressedDrawable = new GradientDrawable();
        pressedDrawable.setCornerRadius(ITEM_ROUND_CORNER);
        pressedDrawable.setShape(GradientDrawable.RECTANGLE);
        pressedDrawable.setStroke(ITEM_STROKE_WIDTH, getContext().getResources().getColor(color));
        pressedDrawable.setColor(getContext().getResources().getColor(R.color.black_trans_5));

        listDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        listDrawable.addState(new int[]{android.R.attr.state_enabled}, gradientDrawable);
        listDrawable.addState(new int[]{}, gradientDrawable);
        return listDrawable;
    }

    @Override
    public int getShortHeight() {
        return mMaxLineHeight;
    }

    @Override
    public int getFullHeight() {
        return mRealHeight;
    }

    @Override
    public boolean needFold() {
        return mLine > MAX_LINE;
    }

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }
}
