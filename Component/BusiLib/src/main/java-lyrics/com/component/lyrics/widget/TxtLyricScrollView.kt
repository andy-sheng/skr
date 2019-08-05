package com.component.lyrics.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.common.log.MyLog
import com.common.utils.dp
import com.component.busilib.R
import com.component.lyrics.LyricsReader

/**
 * 纯文本歌词view，支持手动拖动和自动滚动
 */
class TxtLyricScrollView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val lyrics = ArrayList<String>() // 所有歌词
    private var progress = 200f // 当前所处的进度位置 100为播放完毕
    private var maxProgress = progress // 进度的最大值
    private val highlightNum: Int // 中间高亮几行
    private val fadingNum: Int // 上下渐隐的行数
    private val lineSpace: Float // 歌词行间距
    private val lyricAudo: Boolean
    private var lyricFontSize = 15.dp().toFloat() //歌词字体大小
    private var ht = 0f
    private var hb = 0f

    private var touchDown = false
    private var ly = 0f

    private val paint1: Paint

    private var duration = 0
    private var postion = 0
    private var playing = false

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TxtLyricScrollView)
        val lyricFontSize = typedArray.getDimension(R.styleable.TxtLyricScrollView_lyricFontSize, 15.dp().toFloat())
        val lyricColor = typedArray.getColor(R.styleable.TxtLyricScrollView_lyricColor, Color.WHITE)
        lineSpace = typedArray.getDimension(R.styleable.TxtLyricScrollView_lyricLineSpace, 2.dp().toFloat())
        highlightNum = typedArray.getInt(R.styleable.TxtLyricScrollView_lyricHighlightNum, 4)
        fadingNum = typedArray.getInt(R.styleable.TxtLyricScrollView_lyricFadeNum, 2)
        lyricAudo = typedArray.getBoolean(R.styleable.TxtLyricScrollView_lyricAuto, false)
        typedArray.recycle()

        paint1 = Paint().apply {
            textSize = lyricFontSize
            color = lyricColor
            style = Paint.Style.FILL
            //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
            textAlign = Paint.Align.CENTER
            isDither = true
        }
    }

    fun setLyrics(l: ArrayList<String>) {
        lyrics.clear()
        lyrics.addAll(l)
        progress = 0f
        invalidate()
    }

    fun setLyrics(l: String) {
        lyrics.clear()
        val a = l.split("\n")
        for (s in a) {
            lyrics.add(s)
        }
        progress = 0f
        invalidate()
    }

    fun setLyrics(lyricsReader: LyricsReader?) {
        lyrics.clear()
        lyricsReader?.let {
            val it = it.lrcLineInfos.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                val s = entry.value.lineLyrics
                lyrics.add(s)
            }
        }
        progress = 0f
        invalidate()
    }

    fun setDuration(pos: Int) {
        duration = pos
    }

    fun pause() {
        playing = false
    }

    fun play(pos: Int) {
        playing = true
        play(pos, 0)
    }

    private fun play(pos: Int, delay: Int) {
        this.postion = pos
        if(duration<=0){
            MyLog.e("未设置duration")
        }
        progress = (this.postion * maxProgress) / duration
        postInvalidateDelayed(delay.toLong())
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (lyricAudo) {
            return false
        }
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDown = true
                ly = event?.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchDown) {
                    var ty = (event?.y - ly)
                    var np = progress - ty
                    MyLog.d("TxtLyricScrollView", "move y:${event?.y} ty:$ty np:${np} maxProgress:$maxProgress progress:$progress")
                    if (np <= 0) {
                        np = 0f
                    } else if (np >= maxProgress) {
                        np = maxProgress
                    }
                    if (np != progress) {
                        progress = np
                        postInvalidate()
                    }
                    ly = event?.y
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                touchDown = false
            }
        }
        return true
    }

    /**
     * 关于drawtext 坐标的解释
     * https://blog.csdn.net/zly921112/article/details/50401976
     */
    override fun onDraw(canvas: Canvas?) {
        MyLog.d("TxtLyricScrollView", "progress=${progress}")
        super.onDraw(canvas)
        if (ht == 0f && lyrics.size>0) {
            // 计算一些基本参数
            val fontMetrics = paint1.fontMetrics
            // 字体的大小
            lyricFontSize = Math.abs(fontMetrics.bottom) + Math.abs(fontMetrics.top)
            // 由高亮的行数 上下渐隐的行数 行间距 可以算出
            val hh = lyricFontSize * highlightNum + lineSpace * highlightNum
            ht = height / 2 - hh / 2
            hb = ht + hh
            maxProgress = lyricFontSize + (lyrics.size - 1) * (lyricFontSize + lineSpace) + ht - hb
        }

        canvas?.apply {
            // 将坐标原点移到控件中心
            //drawLine(width / 2f, 0f, width / 2f, height.toFloat(), paint1)
            //drawLine(0f, height / 2f, width.toFloat(), height / 2f, paint1)
            for (i in 0 until lyrics.size) {
                var ty = lyricFontSize + i * (lyricFontSize + lineSpace) + ht - progress
                if (ty < ht) {
                    val at = Math.abs(ty - ht)
                    val yuzhi = (lyricFontSize + lineSpace) * fadingNum // 这个距离还有透明度
                    var alpha = 255 - at * 255f / yuzhi
                    if (alpha < 0) {
                        alpha = 0f
                    }
                    paint1.alpha = alpha.toInt()
                } else if (ty > hb) {
                    val at = Math.abs(ty - hb)
                    val yuzhi = (lyricFontSize + lineSpace) * fadingNum // 这个距离还有透明度
                    var alpha = 255 - at * 255f / yuzhi
                    if (alpha < 0) {
                        alpha = 0f
                    }
                    paint1.alpha = alpha.toInt()
                } else {
                    paint1.alpha = 255
                }
                if (paint1.alpha > 0) {
                    drawText(lyrics[i], width / 2f, ty, paint1)
                }
            }
        }
        if (lyricAudo) {
            if (playing) {
                play(postion + 16, 16)
            }
        }
    }


}