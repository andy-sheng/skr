package com.wali.live.watchsdk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhaomin
 * @module 两端对齐的text view
 */
public class AlignTextView extends TextView {
    private float textHeight; // 单行文字高度
    private int width; // textView宽度
    private List<String> lines = new ArrayList<String>(); // 分割后 每一行
    private List<Integer> tailLines = new ArrayList<Integer>(); // 每段最后一行
    private List<ColorSet> colorList = new ArrayList<ColorSet>();                                   //文本里局部显示颜色的列表 onDraw 改变颜色
    private int colorIndex = 0;                                                                     //颜色列表遍历 索引
    private boolean firstCalc = true;  // 初始化计算

    private int mOriginColor;


    public AlignTextView(Context context) {
        super(context);
        setTextIsSelectable(false);
    }

    public AlignTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTextIsSelectable(false);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (firstCalc) {
            width = getMeasuredWidth();
            String text = getText().toString();
            TextPaint paint = getPaint();
            lines.clear();
            tailLines.clear();
            colorIndex = 0;

            // 文本含有换行符时，分割单独处理
            String[] items = text.split("\\n");
            for (String item : items) {
                calc(paint, item);
            }

            //获取行高
            textHeight = 1.0f * getMeasuredHeight() / getLineCount();
            firstCalc = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // canvas.drawText() 不能显示表情 有表情的text 暂时这样处理
        if (containsEmoji(getText().toString())) {
            super.onDraw(canvas);
            colorIndex = 0;
            return;
        }
        int txtIndex = 0;
        TextPaint paint = getPaint();
        paint.setColor(mOriginColor);
        paint.drawableState = getDrawableState();

        width = getMeasuredWidth();

        Paint.FontMetrics fm = paint.getFontMetrics();
        float firstHeight = getTextSize() - (fm.bottom - fm.descent + fm.ascent - fm.top);

        int gravity = getGravity();
        if ((gravity & 0x1000) == 0) { // 是否垂直居中
            firstHeight = firstHeight + (textHeight - firstHeight) / 2;
        }

        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        width = width - paddingLeft - paddingRight;
        for (int i = 0; i < lines.size(); i++) {
            float drawY = i * textHeight + firstHeight;
            String line = lines.get(i);

            // 绘画起始x坐标
            float drawSpacingX = paddingLeft;
            float gap = (width - paint.measureText(line));
            float interval = gap / (line.length() - 1);

            // 绘制最后一行
            if (tailLines.contains(i)) {
                interval = 0;
            }

            for (int j = 0; j < line.length(); j++) {
                float drawX = paint.measureText(line.substring(0, j)) + interval * j;
                setTxtColor(paint, txtIndex + i);//因为用换行切割字符串 所以+行数
                canvas.drawText(line.substring(j, j + 1), drawX + drawSpacingX, drawY +
                        paddingTop, paint);
                txtIndex++;
            }


        }
        colorIndex = 0;
    }


    /**
     * 计算每行应显示的文本数
     *
     * @param text 要计算的文本
     */
    private void calc(Paint paint, String text) {
        if (text.length() == 0) {
            lines.add("\n");
            return;
        }
        int startPosition = 0; // 起始位置
        float oneChineseWidth = paint.measureText("中");
        int ignoreCalcLength = (int) (width / oneChineseWidth); // 忽略计算的长度
        StringBuilder sb = new StringBuilder(text.substring(0, Math.min(ignoreCalcLength + 1,
                text.length())));

        for (int i = ignoreCalcLength + 1; i < text.length(); i++) {
            String s = text.substring(startPosition, i + 1);
            if (paint.measureText(s) > width) {
                startPosition = i;
                if (isPunctuation(text.charAt(i))) {
                    startPosition -= 1;
                    sb.deleteCharAt(sb.length() - 1);
                }
                //将之前的字符串 作为一行
                lines.add(sb.toString());
                sb = new StringBuilder();

                //添加开始忽略的字符串，长度不足的话直接结束,否则继续
                if ((text.length() - startPosition) > ignoreCalcLength) {
                    sb.append(text.substring(startPosition, startPosition + ignoreCalcLength));
                } else {
                    lines.add(text.substring(startPosition));
                    break;
                }

                i = i + ignoreCalcLength - 1;
            } else {
                sb.append(text.charAt(i));
            }
        }
        if (sb.length() > 0) {
            lines.add(sb.toString());
        }

        tailLines.add(lines.size() - 1);
    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        firstCalc = true;
        mOriginColor = getCurrentTextColor();
        super.setText(text, type);
        requestLayout();//有的时候不调用一下显示不出来
    }

    // 判别是否包含Emoji表情
    private static boolean containsEmoji(String str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (isEmojiCharacter(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmojiCharacter(char codePoint) {
        return !((codePoint == 0x0) ||
                (codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF)));

    }

    private static boolean isPunctuation(char c) {
        Pattern pattern = Pattern.compile("\\p{P}");
        Matcher matcher = pattern.matcher(String.valueOf(c));
        return matcher.matches();
    }

    //-----颜色相关代码-------------------------------------------------------------------------------
    public static class ColorSet {
        int l, r, c;

        ColorSet(int l, int r, int c) {
            this.l = l;
            this.r = r;
            this.c = c;
        }
    }

    //添加局部颜色 自动排序
    public void addColorSpan(int start, int end, int color) {
        List<ColorSet> colorListCopy = new ArrayList<ColorSet>(colorList);
        int index = 0;
        for (int i = 0; i < colorListCopy.size(); i++) {
            ColorSet set = colorListCopy.get(i);
            if (start <= set.l) {
                if (start == set.l && end == set.r) {
                    set.c = color;
                    return;
                }
                break;
            }
            index++;
        }
        colorListCopy.add(index, new ColorSet(start, end, color));
        colorList = colorListCopy;
    }

    public void removeColorSpan(int start, int end) {
        List<ColorSet> colorListCopy = new ArrayList<ColorSet>(colorList);
        for (ColorSet set : colorList) {
            if (start == set.l && end == set.r) {
                colorListCopy.remove(set);
            }
        }
        colorList = colorListCopy;
    }

    public void clearColorList() {
        colorList.clear();
        colorIndex = 0;
    }

    //遍历时候
    private void setTxtColor(TextPaint paint, int charAt) {
        while (colorIndex >= 0 && colorIndex < colorList.size()) {
            ColorSet set = colorList.get(colorIndex);

            if (charAt < set.l) {
                break;
            }
            if (charAt >= set.r) {
                colorIndex++;
                continue;
            }
            paint.setColor(set.c);
            return;
        }
        paint.setColor(mOriginColor);
    }
}