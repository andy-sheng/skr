package com.component.lyrics.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.component.lyrics.LyricsReader;
import com.component.lyrics.model.LyricsInfo;
import com.component.lyrics.model.LyricsLineInfo;
import com.component.lyrics.utils.LyricsUtils;

import java.util.List;
import java.util.TreeMap;

/**
 * @Description: 双行歌词，支持翻译（该歌词在这里只以动感歌词的形式显示）和音译歌词（注：不支持lrc歌词的显示）
 * @author: zhangliangming
 * @date: 2018-04-21 11:43
 **/
public class FloatLyricsView extends AbstractLrcView {

    /**
     * 歌词居左
     */
    public static final int ORIENTATION_LEFT = 0;
    /**
     * 歌词居中
     */
    public static final int ORIENTATION_CENTER = 1;
    /**
     *
     */
    private int mOrientation = ORIENTATION_LEFT;

    public FloatLyricsView(Context context) {
        super(context);
        init(context);
    }

    public FloatLyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * @throws
     * @Description: 初始
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-04-21 9:08
     */
    private void init(Context context) {

        //获取屏幕宽度
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int screensWidth = displayMetrics.widthPixels;

        //设置歌词的最大宽度
        int textMaxWidth = screensWidth / 3 * 2;
        mTextMaxWidth = textMaxWidth;

    }

    @Override
    protected void onDrawLrcView(Canvas canvas) {
        drawFloatLrcView(canvas);
    }

    @Override
    protected void updateView(long playProgress) {
        updateFloatLrcView(playProgress);
    }


    /**
     * 绘画歌词
     *
     * @param canvas
     */
    private void drawFloatLrcView(Canvas canvas) {
        int extraLrcStatus = getExtraLrcStatus();
        //绘画歌词
        if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_NOSHOWEXTRALRC) {
            //只显示默认歌词
            drawDynamicLyrics(canvas);
        } else {
            //显示翻译歌词 OR 音译歌词
            drawDynamiAndExtraLyrics(canvas);
        }

    }

    /**
     * 绘画歌词
     *
     * @param canvas
     */
    private void drawDynamicLyrics(Canvas canvas) {
        //获取数据
        int[] subPaintHLColors = new int[]{getSubPaintHLColor(), getSubPaintHLColor()};


        // 先设置当前歌词，之后再根据索引判断是否放在左边还是右边
        List<LyricsLineInfo> splitLyricsLineInfos = mLrcLineInfos.get(mLyricsLineNum).getSplitLyricsLineInfos();
        LyricsLineInfo lyricsLineInfo = splitLyricsLineInfos.get(mSplitLyricsLineNum);
        //获取行歌词高亮宽度
        float lineLyricsHLWidth = LyricsUtils.getLineLyricsHLWidth(mLyricsReader.getLyricsType(), mPaintHL, lyricsLineInfo, mSplitLyricsWordIndex, mLyricsWordHLTime);
        // 当行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        //获取当前高亮歌词的宽度
        float curLrcTextWidth = LyricsUtils.getTextWidth(mPaintHL, curLyrics);
        // 当前歌词行的x坐标
        float textX = 0;
        // 当前歌词行的y坐标
        float textY = 0;
        int splitLyricsRealLineNum = LyricsUtils.getSplitLyricsRealLineNum(mLrcLineInfos, mLyricsLineNum, mSplitLyricsLineNum);
        float topPadding = (getHeight() - mSpaceLineHeight - 2 * LyricsUtils.getTextHeight(mPaint)) / 2;
        if (splitLyricsRealLineNum % 2 == 0) {
            if (mOrientation == ORIENTATION_LEFT) {
                textX = mPaddingLeftOrRight;
            } else {
                textX = (getWidth() - curLrcTextWidth) / 2;
            }
            textY = topPadding + LyricsUtils.getTextHeight(mPaint);
            float nextLrcTextY = textY + mSpaceLineHeight + LyricsUtils.getTextHeight(mPaint);

            // 画下一句的歌词，该下一句还在该行的分割集合里面
            if (mSplitLyricsLineNum + 1 < splitLyricsLineInfos.size()) {
                String lrcRightText = splitLyricsLineInfos.get(
                        mSplitLyricsLineNum + 1).getLineLyrics();
                float lrcRightTextWidth = LyricsUtils.getTextWidth(mPaint, lrcRightText);
                float textRightX = 0;

                if (mOrientation == ORIENTATION_LEFT) {
                    textRightX = getWidth() - lrcRightTextWidth - mPaddingLeftOrRight;
                } else {
                    textRightX = (getWidth() - lrcRightTextWidth) / 2;
                }

                LyricsUtils.drawOutline(canvas, mPaintOutline, lrcRightText, textRightX, nextLrcTextY);

                LyricsUtils.drawText(canvas, mPaint, mPaintColors, lrcRightText, textRightX,
                        nextLrcTextY);

            } else if (mLyricsLineNum + 1 < mLrcLineInfos.size()) {
                // 画下一句的歌词，该下一句不在该行分割歌词里面，需要从原始下一行的歌词里面找
                List<LyricsLineInfo> nextSplitLyricsLineInfos = mLrcLineInfos.get(mLyricsLineNum + 1).getSplitLyricsLineInfos();
                String lrcRightText = nextSplitLyricsLineInfos.get(0).getLineLyrics();
                float lrcRightTextWidth = LyricsUtils.getTextWidth(mPaint, lrcRightText);
                float textRightX = 0;

                if (mOrientation == ORIENTATION_LEFT) {
                    textRightX = getWidth() - lrcRightTextWidth - mPaddingLeftOrRight;
                } else {
                    textRightX = (getWidth() - lrcRightTextWidth) / 2;
                }

                LyricsUtils.drawOutline(canvas, mPaintOutline, lrcRightText, textRightX,
                        nextLrcTextY);

                LyricsUtils.drawText(canvas, mPaint, mPaintColors, lrcRightText, textRightX, nextLrcTextY);
            }

        } else {
            if (mOrientation == ORIENTATION_LEFT) {
                textX = getWidth() - curLrcTextWidth - mPaddingLeftOrRight;
            } else {
                textX = (getWidth() - curLrcTextWidth) / 2;
            }
            float preLrcTextY = topPadding + LyricsUtils.getTextHeight(mPaint);
            textY = preLrcTextY + mSpaceLineHeight + LyricsUtils.getTextHeight(mPaint);

            // 画下一句的歌词，该下一句还在该行的分割集合里面
            if (mSplitLyricsLineNum + 1 < splitLyricsLineInfos.size()) {
                String lrcLeftText = splitLyricsLineInfos.get(
                        mSplitLyricsLineNum + 1).getLineLyrics();
                float lrcLeftTextWidth = LyricsUtils.getTextWidth(mPaint, lrcLeftText);

                float textLeftX = 0;
                if (mOrientation == ORIENTATION_LEFT) {
                    textLeftX = mPaddingLeftOrRight;
                } else {
                    textLeftX = (getWidth() - lrcLeftTextWidth) / 2;
                }

                LyricsUtils.drawOutline(canvas, mPaintOutline, lrcLeftText, textLeftX,
                        preLrcTextY);
                LyricsUtils.drawText(canvas, mPaint, mPaintColors, lrcLeftText, textLeftX,
                        preLrcTextY);

            } else if (mLyricsLineNum + 1 < mLrcLineInfos.size()) {
                // 画下一句的歌词，该下一句不在该行分割歌词里面，需要从原始下一行的歌词里面找
                List<LyricsLineInfo> nextSplitLyricsLineInfos = mLrcLineInfos.get(mLyricsLineNum + 1).getSplitLyricsLineInfos();
                String lrcLeftText = nextSplitLyricsLineInfos.get(0).getLineLyrics();
                float lrcLeftTextWidth = LyricsUtils.getTextWidth(mPaint, lrcLeftText);

                float textLeftX = 0;
                if (mOrientation == ORIENTATION_LEFT) {
                    textLeftX = mPaddingLeftOrRight;
                } else {
                    textLeftX = (getWidth() - lrcLeftTextWidth) / 2;
                }

                LyricsUtils.drawOutline(canvas, mPaintOutline, lrcLeftText, textLeftX,
                        preLrcTextY);
                LyricsUtils.drawText(canvas, mPaint, mPaintColors, lrcLeftText, textLeftX,
                        preLrcTextY);
            }
        }

        //画歌词,在画高亮和普通歌词的时候轮廓大小不一样
        float outLineHLSize = mPaintOutline.getTextSize();
        mPaintOutline.setTextSize(mPaintHL.getTextSize());
        LyricsUtils.drawOutline(canvas, mPaintOutline, curLyrics, textX, textY);
        mPaintOutline.setTextSize(outLineHLSize);

        if (mEnableVerbatim) {
            LyricsUtils.drawDynamicText(canvas, mPaint, mPaintHL, subPaintHLColors, mPaintHLColors, curLyrics, lineLyricsHLWidth, textX, textY);
        }else {
            LyricsUtils.drawText(canvas, mPaintHL, mPaintHLColors, curLyrics, textX, textY);
        }
    }


    /**
     * 绘画歌词和额外歌词
     *
     * @param canvas
     */
    private void drawDynamiAndExtraLyrics(Canvas canvas) {
        //获取数据

        //
        float topPadding = (getHeight() - mExtraLrcSpaceLineHeight - LyricsUtils.getTextHeight(mPaint) - LyricsUtils.getTextHeight(mExtraLrcPaint)) / 2;
        // 当前歌词行的y坐标
        float lrcTextY = topPadding + LyricsUtils.getTextHeight(mPaint);
        //额外歌词行的y坐标
        float extraLrcTextY = lrcTextY + mExtraLrcSpaceLineHeight + LyricsUtils.getTextHeight(mExtraLrcPaint);

        LyricsLineInfo lyricsLineInfo = mLrcLineInfos.get(mLyricsLineNum);
        //获取行歌词高亮宽度
        float lineLyricsHLWidth = LyricsUtils.getLineLyricsHLWidth(mLyricsReader.getLyricsType(), mPaint, lyricsLineInfo, mLyricsWordIndex, mLyricsWordHLTime);
        //画默认歌词
        LyricsUtils.drawDynamiLyrics(canvas, mLyricsReader.getLyricsType(), mPaint, mPaintHL, mPaintOutline, lyricsLineInfo, lineLyricsHLWidth, getWidth(), mLyricsWordIndex, mLyricsWordHLTime, lrcTextY, mPaddingLeftOrRight, mPaintColors, mPaintHLColors);

        //显示翻译歌词
        if (mLyricsReader.getLyricsType() == LyricsInfo.DYNAMIC && mExtraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC && mTranslateDrawType == AbstractLrcView.TRANSLATE_DRAW_TYPE_DYNAMIC) {

            LyricsLineInfo translateLyricsLineInfo = mTranslateLrcLineInfos.get(mLyricsLineNum);
            float extraLyricsLineHLWidth = LyricsUtils.getLineLyricsHLWidth(mLyricsReader.getLyricsType(), mExtraLrcPaint, translateLyricsLineInfo, mExtraLyricsWordIndex, mTranslateLyricsWordHLTime);
            //画翻译歌词
            LyricsUtils.drawDynamiLyrics(canvas, mLyricsReader.getLyricsType(), mExtraLrcPaint, mExtraLrcPaintHL, mExtraLrcPaintOutline, translateLyricsLineInfo, extraLyricsLineHLWidth, getWidth(), mExtraLyricsWordIndex, mTranslateLyricsWordHLTime, extraLrcTextY, mPaddingLeftOrRight, mPaintColors, mPaintHLColors);

        } else {
            LyricsLineInfo transliterationLineInfo = mTransliterationLrcLineInfos.get(mLyricsLineNum);
            float extraLyricsLineHLWidth = LyricsUtils.getLineLyricsHLWidth(mLyricsReader.getLyricsType(), mExtraLrcPaint, transliterationLineInfo, mExtraLyricsWordIndex, mLyricsWordHLTime);
            //画音译歌词
            LyricsUtils.drawDynamiLyrics(canvas, mLyricsReader.getLyricsType(), mExtraLrcPaint, mExtraLrcPaintHL, mExtraLrcPaintOutline, transliterationLineInfo, extraLyricsLineHLWidth, getWidth(), mExtraLyricsWordIndex, mLyricsWordHLTime, extraLrcTextY, mPaddingLeftOrRight, mPaintColors, mPaintHLColors);

        }

    }


    /**
     * 更新歌词视图
     *
     * @param playProgress
     */
    private void updateFloatLrcView(long playProgress) {

        int lyricsLineNum = LyricsUtils.getLineNumber(mLyricsReader.getLyricsType(), mLrcLineInfos, playProgress, mLyricsReader.getPlayOffset());
        mLyricsLineNum = lyricsLineNum;
        updateSplitData(playProgress);
    }


    /**
     * 设置默认颜色
     *
     * @param paintColor
     */
    public void setPaintColor(int[] paintColor) {
        setPaintColor(paintColor, false);
    }


    /**
     * 设置高亮颜色
     *
     * @param paintHLColor
     */
    public void setPaintHLColor(int[] paintHLColor) {
        setPaintHLColor(paintHLColor, false);
    }


    /**
     * 设置字体文件
     *
     * @param typeFace
     */
    public void setTypeFace(Typeface typeFace) {
        setTypeFace(typeFace, false);
    }

    /**
     * 设置空行高度
     *
     * @param spaceLineHeight
     */
    public void setSpaceLineHeight(float spaceLineHeight) {
        setSpaceLineHeight(spaceLineHeight, false);
    }


    /**
     * 设置额外空行高度
     *
     * @param extraLrcSpaceLineHeight
     */
    public void setExtraLrcSpaceLineHeight(float extraLrcSpaceLineHeight) {
        setExtraLrcSpaceLineHeight(extraLrcSpaceLineHeight, false);
    }


    /**
     * 设置歌词解析器
     *
     * @param lyricsReader
     */
    public void setLyricsReader(LyricsReader lyricsReader) {
        super.setLyricsReader(lyricsReader);
        if (lyricsReader != null && lyricsReader.getLyricsType() == LyricsInfo.DYNAMIC) {
            int extraLrcType = getExtraLrcType();
            //翻译歌词以动感歌词形式显示
            if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_BOTH || extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLATELRC) {
                setTranslateDrawType(AbstractLrcView.TRANSLATE_DRAW_TYPE_DYNAMIC);
            }
        } else {
            setLrcStatus(AbstractLrcView.LRCSTATUS_NONSUPPORT);
        }
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     * @param extraFontSize 额外歌词字体
     */
    public void setSize(int fontSize, int extraFontSize) {
        setSize(fontSize, extraFontSize, false);
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     */
    public void setFontSize(float fontSize) {
        setFontSize(fontSize, false);
    }


    /**
     * 设置额外字体大小
     *
     * @param extraLrcFontSize
     */
    public void setExtraLrcFontSize(float extraLrcFontSize) {
        setExtraLrcFontSize(extraLrcFontSize, false);
    }

    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

}
