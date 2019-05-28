package com.common.view.ex;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.common.base.R;

import java.util.ArrayList;
import java.util.List;


// TODO: 2019/2/13 引入此类的原因是为了解决maxLine和ellipsize属性冲突导致不断刷warn日志的问题  推荐单行加上singline=true  多行使用该类

/**
 *  具体解决可查看 https://www.codetd.com/article/2990243
 */
public class EllipsizingTextView extends AppCompatTextView {
    private static final String ELLIPSIS = "...";

    public interface EllipsizeListener {
        /**
         * 另外这个类还提供了一个监听器EllipsizeListener，这个监听器原文作者说的是开始在Ellipsize（这个单词不知道怎么读，
         * 网上也查不到，估计是Google自创的）的时候调用的，大概意思应该就是说在开始省略（也就是开始 ”...“ ）的时候调用，
         * 这个暂时还没有试过，等以后需要的时候再去试一试
         *
         * @param ellipsized
         */
        void ellipsizeStateChanged(boolean ellipsized);
    }

    private final List<EllipsizeListener> ellipsizeListeners = new ArrayList<EllipsizeListener>();
    private boolean isEllipsized;
    private boolean isStale;
    private boolean programmaticChange;
    private String fullText;
    private int maxLines = -1;
    private float lineSpacingMultiplier = 1.0f;
    private float lineAdditionalVerticalPadding = 0.0f;

    public EllipsizingTextView(Context context) {
        super(context);
        init(context, null);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ellipisize);
        this.maxLines = typedArray.getInt(R.styleable.ellipisize_max_lines, 2);
        setMaxLines(maxLines);
    }

    public void addEllipsizeListener(EllipsizeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        ellipsizeListeners.add(listener);
    }

    public void removeEllipsizeListener(EllipsizeListener listener) {
        ellipsizeListeners.remove(listener);
    }

    public boolean isEllipsized() {
        return isEllipsized;
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        this.maxLines = maxLines;
        isStale = true;
    }

    @Override
    public int getMaxLines() {
        return maxLines;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        this.lineAdditionalVerticalPadding = add;
        this.lineSpacingMultiplier = mult;
        super.setLineSpacing(add, mult);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        if (!programmaticChange) {
            fullText = text.toString();
            isStale = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isStale) {
            super.setEllipsize(null);
            resetText();
        }
        super.onDraw(canvas);
    }

    private void resetText() {
        int maxLines = getMaxLines();
        String workingText = fullText;
        boolean ellipsized = false;
        if (maxLines != -1) {
            Layout layout = createWorkingLayout(workingText);
            if (layout.getLineCount() > maxLines) {
                workingText = fullText.substring(0, layout.getLineEnd(maxLines - 1)).trim();
                while (createWorkingLayout(workingText + ELLIPSIS).getLineCount() > maxLines) {
                    int lastSpace = workingText.lastIndexOf(' ');
                    if (lastSpace == -1) {
                        break;
                    }
                    workingText = workingText.substring(0, lastSpace);
                }
                workingText = workingText + ELLIPSIS;
                ellipsized = true;
            }
        }
        if (!workingText.equals(getText())) {
            programmaticChange = true;
            try {
                setText(workingText);
            } finally {
                programmaticChange = false;
            }
        }
        isStale = false;
        if (ellipsized != isEllipsized) {
            isEllipsized = ellipsized;
            for (EllipsizeListener listener : ellipsizeListeners) {
                listener.ellipsizeStateChanged(ellipsized);
            }
        }
    }

    private Layout createWorkingLayout(String workingText) {
        return new StaticLayout(workingText, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight(),
                Layout.Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineAdditionalVerticalPadding, false);
    }

    @Override
    public void setEllipsize(TextUtils.TruncateAt where) {
        // Ellipsize settings are not respected
    }
}
