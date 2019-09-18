package com.common.view.ninegrid

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.common.base.R

import java.util.ArrayList

/**
 * 描述:
 * 作者：HMY
 * 时间：2016/5/10
 */
abstract class NineGridLayout : ViewGroup {

    private var mSpacing = DEFUALT_SPACING
    private var mColumns: Int = 0
    private var mRows: Int = 0
    private var mTotalWidth: Int = 0
    private var mSingleWidth: Int = 0

    private var mIsShowAll = false
    private var mIsFirst = true
    private val mUrlList = ArrayList<String>()

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NineGridLayout)

        mSpacing = typedArray.getDimension(R.styleable.NineGridLayout_sapcing, DEFUALT_SPACING)
        typedArray.recycle()
        init(context)
    }

    private fun init(context: Context) {
        if (mUrlList.isNullOrEmpty()) {
            visibility = View.GONE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        mTotalWidth = right - left
        mSingleWidth = ((mTotalWidth - mSpacing * (3 - 1)) / 3).toInt()
        if (mIsFirst) {
            notifyDataSetChanged()
            mIsFirst = false
        }
    }

    /**
     * 设置间隔
     *
     * @param spacing
     */
    fun setSpacing(spacing: Float) {
        mSpacing = spacing
    }

    /**
     * 设置是否显示所有图片（超过最大数时）
     *
     * @param isShowAll
     */
    fun setIsShowAll(isShowAll: Boolean) {
        mIsShowAll = isShowAll
    }

    fun setUrlList(urlList: List<String>) {
        if (urlList.isNullOrEmpty()) {
            visibility = View.GONE
            return
        }
        visibility = View.VISIBLE

        mUrlList.clear()
        mUrlList.addAll(urlList)

        if (!mIsFirst) {
            notifyDataSetChanged()
        }
    }

    fun notifyDataSetChanged() {
        post { refresh() }
    }

    private fun refresh() {
        removeAllViews()
        val size = mUrlList.size
        if (size > 0) {
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }

        if (size == 1) {
            val url = mUrlList[0]
            val imageView = createImageView(0, url)

            //避免在ListView中一张图未加载成功时，布局高度受其他item影响
            val params = layoutParams
            params.height = mSingleWidth
            layoutParams = params
            imageView.layout(0, 0, mSingleWidth, mSingleWidth)

            val isShowDefualt = displayOneImage(imageView, url, mTotalWidth)
            if (isShowDefualt) {
                layoutImageView(imageView, 0, url, false)
            } else {
                addView(imageView)
            }
            return
        }

        generateChildrenLayout(size)
        layoutParams()

        for (i in 0 until size) {
            val url = mUrlList[i]
            val imageView: RatioImageView
            if (!mIsShowAll) {
                if (i < MAX_COUNT - 1) {
                    imageView = createImageView(i, url)
                    layoutImageView(imageView, i, url, false)
                } else { //第9张时
                    if (size <= MAX_COUNT) {//刚好第9张
                        imageView = createImageView(i, url)
                        layoutImageView(imageView, i, url, false)
                    } else {//超过9张
                        imageView = createImageView(i, url)
                        layoutImageView(imageView, i, url, true)
                        break
                    }
                }
            } else {
                imageView = createImageView(i, url)
                layoutImageView(imageView, i, url, false)
            }
        }
    }

    private fun layoutParams() {
        val singleHeight = mSingleWidth

        //根据子view数量确定高度
        val params = layoutParams
        params.height = (singleHeight * mRows + mSpacing * (mRows - 1)).toInt()
        layoutParams = params
    }

    private fun createImageView(i: Int, url: String): RatioImageView {
        val imageView = RatioImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setOnClickListener { onClickImage(i, url, mUrlList) }
        return imageView
    }

    /**
     * @param imageView
     * @param url
     * @param showNumFlag 是否在最大值的图片上显示还有未显示的图片张数
     */
    private fun layoutImageView(imageView: RatioImageView, i: Int, url: String, showNumFlag: Boolean) {
        val singleWidth = ((mTotalWidth - mSpacing * (3 - 1)) / 3).toInt()

        val position = findPosition(i)
        val left = ((singleWidth + mSpacing) * position[1]).toInt()
        val top = ((singleWidth + mSpacing) * position[0]).toInt()
        val right = left + singleWidth
        val bottom = top + singleWidth

        imageView.layout(left, top, right, bottom)

        addView(imageView)
        if (showNumFlag) {//添加超过最大显示数量的文本
            val overCount = mUrlList.size - MAX_COUNT
            if (overCount > 0) {
                val textSize = 30f
                val textView = TextView(context)
                textView.text = "+$overCount"
                textView.setTextColor(Color.WHITE)
                textView.setPadding(0, singleWidth / 2 - getFontHeight(textSize), 0, 0)
                textView.textSize = textSize
                textView.gravity = Gravity.CENTER
                textView.setBackgroundColor(Color.BLACK)
                textView.background.alpha = 120

                textView.layout(left, top, right, bottom)
                addView(textView)
            }
        }
        displayImage(imageView, url)
    }

    private fun findPosition(childNum: Int): IntArray {
        val position = IntArray(2)
        for (i in 0 until mRows) {
            for (j in 0 until mColumns) {
                if (i * mColumns + j == childNum) {
                    position[0] = i//行
                    position[1] = j//列
                    break
                }
            }
        }
        return position
    }

    /**
     * 根据图片个数确定行列数量
     *
     * @param length
     */
    private fun generateChildrenLayout(length: Int) {
        if (length <= 3) {
            mRows = 1
            mColumns = length
        } else if (length <= 6) {
            mRows = 2
            mColumns = 3
            if (length == 4) {
                mColumns = 2
            }
        } else {
            mColumns = 3
            if (mIsShowAll) {
                mRows = length / 3
                val b = length % 3
                if (b > 0) {
                    mRows++
                }
            } else {
                mRows = 3
            }
        }
    }

    protected fun setOneImageLayoutParams(imageView: RatioImageView, width: Int, height: Int) {
        imageView.layoutParams = ViewGroup.LayoutParams(width, height)
        imageView.layout(0, 0, width, height)

        val params = layoutParams
        //        params.width = width;
        params.height = height
        layoutParams = params
    }

    private fun getFontHeight(fontSize: Float): Int {
        val paint = Paint()
        paint.textSize = fontSize
        val fm = paint.fontMetrics
        return Math.ceil((fm.descent - fm.ascent).toDouble()).toInt()
    }

    /**
     * @param imageView
     * @param url
     * @param parentWidth 父控件宽度
     * @return true 代表按照九宫格默认大小显示，false 代表按照自定义宽高显示
     */
    protected abstract fun displayOneImage(imageView: RatioImageView, url: String, parentWidth: Int): Boolean

    protected abstract fun displayImage(imageView: RatioImageView, url: String)

    protected abstract fun onClickImage(position: Int, url: String, urlList: List<String>)

    companion object {

        private val DEFUALT_SPACING = 3f
        private val MAX_COUNT = 9
    }
}
