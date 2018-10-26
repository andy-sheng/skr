package com.wali.live.modulewatch.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Pair;
import android.view.View;

import com.common.utils.U;
import com.wali.live.modulewatch.R;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by lan on 16-3-1.
 */
public class SpanUtils {

    private static final String SCHEME_URL_REX_PATTERN = "walilive://[a-zA-Z0-9.]+(\\/[a-zA-Z0-9.]+)?(\\?)?([a-zA-Z_]+=[0-9a-zA-Z_%,.:/?=]+&)*([a-zA-Z_]+=[0-9a-zA-Z_%,.:/?=]*)?";


    /**
     * 通常用于把只包含一个数字的字符串中的数字变成指定颜色<br>
     * 只会把第一次出现高亮
     *
     * @param text       源字符串
     * @param keyword    需要高亮的字符串
     * @param colorResId
     * @return
     */
    public static CharSequence getHighLightKeywordText(@NonNull String text, @NonNull String keyword, @ColorRes int colorResId) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        if (TextUtils.isEmpty(keyword)) {
            return text;
        }
        int start = text.indexOf(keyword);
        if (start < 0) {
            return text;
        }
        int end = start + keyword.length();
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        ssb.setSpan(new ForegroundColorSpan(U.app().getResources().getColor(colorResId)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    /**
     * 给source加上指定的颜色
     */
    public static SpannableString addColorSpan(String source, final int colorId) {
        SpannableString ss = new SpannableString(source);
        ss.setSpan(new ForegroundColorSpan(U.app().getResources().getColor(colorId)),
                0, source.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return ss;
    }

    /**
     * 在source的基础上给指定的字key加上指定的颜色
     */
    public static SpannableString addColorSpan(String key, String source, final int keyColorId, final int sourceColorId) {
        SpannableString ss = addColorSpan(source, sourceColorId);
        int index = source.indexOf(key);
        if (index < 0) {
            return ss;
        }
        ss.setSpan(new ForegroundColorSpan(U.app().getResources().getColor(keyColorId)),
                index, index + key.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return ss;
    }

    /**
     * 给source的基础上给keyPairs指定的字加上指定的颜色
     */
    public static SpannableString addColorSpan(List<Pair<String, Integer>> keyPairs, String source, final int sourceColorId) {
        SpannableString ss = addColorSpan(source, sourceColorId);
        if (null == keyPairs && keyPairs.size() == 0) {
            return ss;
        }
        int index;
        for (Pair<String, Integer> pair : keyPairs) {
            index = source.indexOf(pair.first);
            if (index < 0) {
                continue;
            }
            ss.setSpan(new ForegroundColorSpan(U.app().getResources().getColor(pair.second)),
                    index, index + pair.first.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    /**
     * 在source的基础上给指定的字key加上图片
     */
    public static SpannableString addImageSpan(String key, String source, int drawableResId) {
        SpannableString ss = new SpannableString(source);
        int index = source.indexOf(key);
        if (index < 0 || drawableResId == 0) {
            return ss;
        }
        ss.setSpan(new ImageSpan(U.app(), drawableResId),
                index, index + key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    /**
     * 给source加上点击事件
     */
    public static SpannableString addClickableSpan(String source, View.OnClickListener onClickListener) {
        SpannableString ss = new SpannableString(source);
        if (onClickListener != null) {
            return ss;
        }
        ss.setSpan(new MyClickableSpan(onClickListener),
                0, source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    /**
     * 给source加上点击事件
     */
    public static SpannableString addClickableSpan(String key, String source, View.OnClickListener onClickListener) {
        SpannableString ss = new SpannableString(source);
        int index = source.indexOf(key);
        if (index < 0 || onClickListener == null) {
            return ss;
        }
        ss.setSpan(new MyClickableSpan(onClickListener),
                0, source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }


    /**
     * 添加schema和Url识别，识别成　连接，并且可以点击
     *
     * @return
     */
    public static SpannableStringBuilder addSechemaAndWebLink(SpannableStringBuilder spannable, final SchemeClickListener webOnClickListener, final SchemeClickListener schemaCliclkOnClickListener) {
//        Pattern pattern = Pattern.compile("walilive:\\/\\/(\\w+\\/)*(\\w+)(\\?(\\w+=[\\da-zA-Z_%,\\.]+&)*\\w+=[\\da-zA-Z_%,\\.]+)?");
        Pattern pattern = Pattern.compile(SCHEME_URL_REX_PATTERN);
        Linkify.addLinks(spannable, Linkify.WEB_URLS);
        Linkify.addLinks(spannable, pattern, "walilive:");
        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        if (spans != null && spans.length > 0) {
            for (URLSpan urlSpan : spans) {
                final String url = urlSpan.getURL();
                int start = spannable.getSpanStart(urlSpan);
                int end = spannable.getSpanEnd(urlSpan);
                spannable.removeSpan(urlSpan);
                if (start < 0 || end <= 0) {
                    continue;
                }
                if (url.startsWith("walilive:")) {

                    String link = U.app().getResources().getString(R.string.sixin_link);
                    spannable.replace(start, end, new SpannableString(link));

                    ClickableSpan span = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            schemaCliclkOnClickListener.onClick(url, widget);
                        }
                    };
                    spannable.setSpan(span, start, start + link.length(), 0);
                } else {
                    ClickableSpan span = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            webOnClickListener.onClick(url, widget);
                        }
                    };
                    spannable.setSpan(span, start, end, 0);
                }
            }
        }
        return spannable;
    }


    /**
     * 添加schema识别，识别成　连接，并且可以点击
     *
     * @return
     */
    public static SpannableStringBuilder addSechemaLink(String source, final SchemeClickListener onClickListener) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(source);
        Pattern pattern = Pattern.compile(SCHEME_URL_REX_PATTERN);
        Linkify.addLinks(spannable, pattern, "walilive:");
        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        if (spans != null && spans.length > 0) {
            for (URLSpan urlSpan : spans) {
                final String url = urlSpan.getURL();
                int start = spannable.getSpanStart(urlSpan);
                int end = spannable.getSpanEnd(urlSpan);
                spannable.removeSpan(urlSpan);
                if (start < 0 || end <= 0) {
                    continue;
                }
                if (url.startsWith("walilive:")) {

                    String link = U.app().getResources().getString(R.string.sixin_link);
                    spannable.replace(start, end, new SpannableString(link));

                    ClickableSpan span = new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            onClickListener.onClick(url, widget);
                        }
                    };
                    spannable.setSpan(span, start, start + link.length(), 0);
                }
            }
        }
        return spannable;
    }


    /**
     * 添加schema识别，识别成　连接，不可点击
     *
     * @return
     */
    public static SpannableStringBuilder converseSechemaAsString(String source) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(source);
        Pattern pattern = Pattern.compile(SCHEME_URL_REX_PATTERN);
        Linkify.addLinks(spannable, pattern, "walilive:");
        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        if (spans != null && spans.length > 0) {
            for (URLSpan urlSpan : spans) {
                String url = urlSpan.getURL();
                int start = spannable.getSpanStart(urlSpan);
                int end = spannable.getSpanEnd(urlSpan);
                spannable.removeSpan(urlSpan);
                if (start < 0 || end <= 0) {
                    continue;
                }
                if (url.startsWith("walilive:")) {
                    String link = "[" + U.app().getResources().getString(R.string.sixin_link) + "]";
                    spannable.replace(start, end, new SpannableString(link));
                }
            }
        }
        return spannable;
    }

    public static class MyClickableSpan extends ClickableSpan {
        private View.OnClickListener mOnClickListener;

        public MyClickableSpan(View.OnClickListener onClickListener) {
            this.mOnClickListener = onClickListener;
        }

        @Override
        public void onClick(View widget) {
            mOnClickListener.onClick(widget);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false);
        }
    }

    public static class MyImageSpan extends ImageSpan {
        private int lineSpace;

        public MyImageSpan(Drawable d, int verticalAlignment) {
            super(d, verticalAlignment);
        }

        /**
         * @param d
         * @param verticalAlignment
         * @param lineSpace         行间距
         */
        public MyImageSpan(Drawable d, int verticalAlignment, int lineSpace) {
            super(d, verticalAlignment);
            this.lineSpace = lineSpace;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            Drawable d = getDrawable();
            Rect rect = d.getBounds();
            if (fm != null) {
                Paint.FontMetricsInt fmPaint = paint.getFontMetricsInt();
                // 获得文字总高度
                int fontHeight = fmPaint.bottom - fmPaint.top;
                // 获得图片总高度
                int drHeight = rect.bottom - rect.top;
                int top = drHeight / 2 - fontHeight / 4;
                int bottom = drHeight / 2 + fontHeight / 4;

                fm.ascent = -bottom;
                fm.top = -bottom;
                fm.bottom = top;
                fm.descent = top;
            }
            return rect.right;
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text,
                         int start, int end, float x,
                         int top, int y, int bottom, @NonNull Paint paint) {
            Drawable b = getDrawable();
            canvas.save();

            int transY = bottom - b.getBounds().bottom;
            // this is the key
            transY -= paint.getFontMetricsInt().descent / 2;

            canvas.translate(x, transY - this.lineSpace);
            b.draw(canvas);
            canvas.restore();
        }
    }

    public static class CenterImageSpan extends ImageSpan {
        public CenterImageSpan(Drawable d, int verticalAlignment) {
            super(d, verticalAlignment);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            Drawable b = getDrawable();
            canvas.save();

            Paint.FontMetricsInt fm = paint.getFontMetricsInt();
            int transY = (y + fm.descent + y + fm.ascent) / 2
                    - b.getBounds().bottom / 2;

            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }
    }

    public abstract static class SchemeClickListener {
        public abstract void onClick(String url, View view);
    }
}
