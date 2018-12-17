//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.emoticon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ReplacementSpan;
import android.util.DisplayMetrics;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroidEmoji {
    private static float density;
    private static Context mContext;
    private static final int MAX_DISPLAY_EMOJI = 600;
    private static Map<Integer, io.rong.imkit.emoticon.AndroidEmoji.EmojiInfo> sEmojiMap;
    private static List<io.rong.imkit.emoticon.AndroidEmoji.EmojiInfo> sEmojiList;

    public AndroidEmoji() {
    }

    public static void init(Context context) {
        sEmojiMap = new HashMap();
        sEmojiList = new ArrayList();
        mContext = context.getApplicationContext();
        int[] codes = context.getResources().getIntArray(context.getResources().getIdentifier("rc_emoji_code", "array", context.getPackageName()));
        TypedArray array = context.getResources().obtainTypedArray(context.getResources().getIdentifier("rc_emoji_res", "array", context.getPackageName()));
        TypedArray strArray = context.getResources().obtainTypedArray(context.getResources().getIdentifier("rc_emoji_description", "array", context.getPackageName()));
        if (codes.length != array.length()) {
            throw new RuntimeException("Emoji resource init fail.");
        } else {
            int i = -1;

            while (true) {
                ++i;
                if (i >= codes.length) {
                    DisplayMetrics dm = context.getResources().getDisplayMetrics();
                    density = dm.density;
                    array.recycle();
                    return;
                }

                io.rong.imkit.emoticon.AndroidEmoji.EmojiInfo emoji = new io.rong.imkit.emoticon.AndroidEmoji.EmojiInfo(codes[i], array.getResourceId(i, -1), strArray.getResourceId(i, -1));
                sEmojiMap.put(codes[i], emoji);
                sEmojiList.add(emoji);
            }
        }
    }

    public static List<io.rong.imkit.emoticon.AndroidEmoji.EmojiInfo> getEmojiList() {
        return sEmojiList;
    }

    public static int getEmojiCount(String input) {
        if (input == null) {
            return 0;
        } else {
            int count = 0;
            char[] chars = input.toCharArray();
            new SpannableStringBuilder(input);

            for (int i = 0; i < chars.length; ++i) {
                if (!Character.isHighSurrogate(chars[i])) {
                    int codePoint;
                    boolean var5;
                    if (Character.isLowSurrogate(chars[i])) {
                        if (i <= 0 || !Character.isSurrogatePair(chars[i - 1], chars[i])) {
                            continue;
                        }

                        codePoint = Character.toCodePoint(chars[i - 1], chars[i]);
                        var5 = true;
                    } else {
                        codePoint = chars[i];
                        var5 = false;
                    }

                    if (sEmojiMap.containsKey(codePoint)) {
                        ++count;
                    }
                }
            }

            return count;
        }
    }

    public static CharSequence ensure(String input) {
        if (input == null) {
            return input;
        } else {
            char[] chars = input.toCharArray();
            SpannableStringBuilder ssb = new SpannableStringBuilder(input);

            for (int i = 0; i < chars.length; ++i) {
                if (!Character.isHighSurrogate(chars[i])) {
                    int codePoint;
                    boolean isSurrogatePair;
                    if (Character.isLowSurrogate(chars[i])) {
                        if (i <= 0 || !Character.isSurrogatePair(chars[i - 1], chars[i])) {
                            continue;
                        }

                        codePoint = Character.toCodePoint(chars[i - 1], chars[i]);
                        isSurrogatePair = true;
                    } else {
                        codePoint = chars[i];
                        isSurrogatePair = false;
                    }

                    if (sEmojiMap.containsKey(codePoint)) {
                        ssb.setSpan(new io.rong.imkit.emoticon.AndroidEmoji.EmojiImageSpan(codePoint), isSurrogatePair ? i - 1 : i, i + 1, 33);
                    }
                }
            }

            return ssb;
        }
    }

    public static boolean isEmoji(String input) {
        if (input == null) {
            return false;
        } else {
            char[] chars = input.toCharArray();
            int length = chars.length;

            for (int i = 0; i < length; ++i) {
                if (!Character.isHighSurrogate(chars[i])) {
                    int codePoint;
                    if (Character.isLowSurrogate(chars[i])) {
                        if (i <= 0 || !Character.isSurrogatePair(chars[i - 1], chars[i])) {
                            continue;
                        }

                        codePoint = Character.toCodePoint(chars[i - 1], chars[i]);
                    } else {
                        codePoint = chars[i];
                    }

                    if (sEmojiMap.containsKey(codePoint)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static void ensure(Spannable spannable) {
        char[] chars = spannable.toString().toCharArray();

        for (int i = 0; i < chars.length; ++i) {
            if (!Character.isHighSurrogate(chars[i])) {
                int codePoint;
                boolean isSurrogatePair;
                if (Character.isLowSurrogate(chars[i])) {
                    if (i <= 0 || !Character.isSurrogatePair(chars[i - 1], chars[i])) {
                        continue;
                    }

                    codePoint = Character.toCodePoint(chars[i - 1], chars[i]);
                    isSurrogatePair = true;
                } else {
                    codePoint = chars[i];
                    isSurrogatePair = false;
                }

                if (sEmojiMap.containsKey(codePoint)) {
                    spannable.setSpan(new io.rong.imkit.emoticon.AndroidEmoji.EmojiImageSpan(codePoint), isSurrogatePair ? i - 1 : i, i + 1, 34);
                }
            }
        }

    }

    public static SpannableStringBuilder replaceEmojiWithText(Spannable spannable) {
        if (spannable == null) {
            return null;
        } else {
            char[] chars = spannable.toString().toCharArray();
            String resultSpanStr = getReplaceEmojiText(chars, spannable.toString());
            return new SpannableStringBuilder(resultSpanStr);
        }
    }

    public static String replaceEmojiWithText(String input) {
        if (input == null) {
            return null;
        } else {
            char[] chars = input.toCharArray();
            String resultSpanStr = getReplaceEmojiText(chars, input);
            return resultSpanStr == null ? null : resultSpanStr.toString();
        }
    }

    private static String getReplaceEmojiText(char[] chars, String srcString) {
        int emojiCount = 0;
        StringBuilder resultSpanStr = new StringBuilder("");

        for (int i = 0; i < chars.length; ++i) {
            if (!Character.isHighSurrogate(chars[i])) {
                int codePoint;
                boolean isSurrogatePair;
                if (Character.isLowSurrogate(chars[i])) {
                    if (i <= 0 || !Character.isSurrogatePair(chars[i - 1], chars[i])) {
                        continue;
                    }

                    codePoint = Character.toCodePoint(chars[i - 1], chars[i]);
                    isSurrogatePair = true;
                } else {
                    codePoint = chars[i];
                    isSurrogatePair = false;
                }

                if (sEmojiMap.containsKey(codePoint)) {
                    ++emojiCount;
                    char[] spanchars = srcString.toCharArray();
                    if (spanchars != null && spanchars.length > 0) {
                        if (emojiCount > 600) {
                            resultSpanStr.append("[");
                            resultSpanStr.append(mContext.getResources().getString(((io.rong.imkit.emoticon.AndroidEmoji.EmojiInfo) sEmojiMap.get(codePoint)).strId));
                            resultSpanStr.append("]");
                        } else {
                            resultSpanStr = appendSpanStr(isSurrogatePair, resultSpanStr, chars, i);
                        }
                    } else {
                        resultSpanStr = appendSpanStr(isSurrogatePair, resultSpanStr, chars, i);
                    }
                } else {
                    resultSpanStr = appendSpanStr(isSurrogatePair, resultSpanStr, chars, i);
                }
            }
        }

        return resultSpanStr == null ? null : resultSpanStr.toString();
    }

    private static StringBuilder appendSpanStr(boolean isSurrogatePair, StringBuilder resultSpanStr, char[] chars, int index) {
        if (resultSpanStr == null) {
            return null;
        } else {
            if (isSurrogatePair) {
                if (index - 1 >= 0) {
                    resultSpanStr.append(chars[index - 1]);
                    resultSpanStr.append(chars[index]);
                }
            } else if (index >= 0) {
                resultSpanStr.append(chars[index]);
            }

            return resultSpanStr;
        }
    }

    public static int getEmojiSize() {
        return sEmojiMap.size();
    }

    public static int getEmojiCode(int index) {
        io.rong.imkit.emoticon.AndroidEmoji.EmojiInfo info = (io.rong.imkit.emoticon.AndroidEmoji.EmojiInfo) sEmojiList.get(index);
        return info.code;
    }

    public static Drawable getEmojiDrawable(Context context, int index) {
        Drawable drawable = null;
        if (index >= 0 && index < sEmojiList.size()) {
            io.rong.imkit.emoticon.AndroidEmoji.EmojiInfo emoji = (io.rong.imkit.emoticon.AndroidEmoji.EmojiInfo) sEmojiList.get(index);
            drawable = context.getResources().getDrawable(emoji.resId);
        }

        return drawable;
    }

    private static class EmojiInfo {
        int code;
        int resId;
        int strId;

        public EmojiInfo(int code, int resId) {
            this.code = code;
            this.resId = resId;
        }

        public EmojiInfo(int code, int resId, int strId) {
            this.code = code;
            this.resId = resId;
            this.strId = strId;
        }
    }

    public static class EmojiImageSpan extends ReplacementSpan {
        Drawable mDrawable;
        private static final String TAG = "DynamicDrawableSpan";
        public static final int ALIGN_BOTTOM = 0;
        private WeakReference<Drawable> mDrawableRef;

        private EmojiImageSpan(int codePoint) {
            if (io.rong.imkit.emoticon.AndroidEmoji.sEmojiMap.containsKey(codePoint)) {
                this.mDrawable = io.rong.imkit.emoticon.AndroidEmoji.mContext.getResources().getDrawable(((io.rong.imkit.emoticon.AndroidEmoji.EmojiInfo) io.rong.imkit.emoticon.AndroidEmoji.sEmojiMap.get(codePoint)).resId);
                int width = this.mDrawable.getIntrinsicWidth() - (int) (4.0F * io.rong.imkit.emoticon.AndroidEmoji.density);
                int height = this.mDrawable.getIntrinsicHeight() - (int) (4.0F * io.rong.imkit.emoticon.AndroidEmoji.density);
                this.mDrawable.setBounds(0, 0, width > 0 ? width : 0, height > 0 ? height : 0);
            }

        }

        public Drawable getDrawable() {
            return this.mDrawable;
        }

        public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
            Drawable d = this.getCachedDrawable();
            Rect rect = d.getBounds();
            if (fm != null) {
                fm.ascent = -rect.bottom;
                fm.descent = 0;
                fm.top = fm.ascent;
                fm.bottom = 0;
            }

            return rect.right;
        }

        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            Drawable b = this.getCachedDrawable();
            canvas.save();
            int transY = bottom - b.getBounds().bottom;
            transY = (int) ((float) transY - io.rong.imkit.emoticon.AndroidEmoji.density);
            canvas.translate(x, (float) transY);
            b.draw(canvas);
            canvas.restore();
        }

        private Drawable getCachedDrawable() {
            WeakReference<Drawable> wr = this.mDrawableRef;
            Drawable d = null;
            if (wr != null) {
                d = (Drawable) wr.get();
            }

            if (d == null) {
                d = this.getDrawable();
                this.mDrawableRef = new WeakReference(d);
            }

            return d;
        }
    }
}
