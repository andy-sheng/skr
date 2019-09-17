

/*
 * Copyright (C) 2017 CoorChice <icechen_@outlook.com>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * <p>
 * Last modified 17-4-20 下午5:32
 */

package com.common.view.ex.stv;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.common.base.R;
import com.common.log.MyLog;

import java.util.ArrayList;
import java.util.List;



public class SuperTextView extends android.support.v7.widget.AppCompatTextView {

    private static final int DEFAULT_TEXT_STROKE_COLOR = Color.BLACK;
    private static final int DEFAULT_TEXT_FILL_COLOR = Color.BLACK;
    private static final float DEFAULT_TEXT_STROKE_WIDTH = 0f;
    private static final int ALLOW_CUSTOM_ADJUSTER_SIZE = 3;
    private int SYSTEM_ADJUSTER_SIZE = 0;


    private Paint paint;
    private boolean autoAdjust;

    private boolean textStroke;
    private int textStrokeColor;
    private int textFillColor;
    private float textStrokeWidth;

    private boolean shaderEnable;
    private int textShaderStartColor;
    private int textShaderEndColor;
    private ShaderMode textShaderMode;
    private boolean textShaderEnable;
    private LinearGradient textShader;

    private List<Adjuster> adjusterList = new ArrayList<>();
    private List<Adjuster> touchAdjusters = new ArrayList<>();
    private boolean superTouchEvent;

    /**
     * 简单的构造函数
     *
     * @param context View运行的Context环境
     */
    public SuperTextView(Context context) {
        super(context);
        init(null);
    }

    /**
     * inflate Xml布局文件时会被调用
     *
     * @param context View运行的Context环境
     * @param attrs   View在xml布局文件中配置的属性集合对象
     */
    public SuperTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    /**
     * 略
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public SuperTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        initAttrs(attrs);
        paint = new Paint();
        initPaint();
    }

    private void initAttrs(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray =
                    getContext().obtainStyledAttributes(attrs, R.styleable.SuperTextView);
            textStroke = typedArray.getBoolean(R.styleable.SuperTextView_stv_text_stroke, false);
            textStrokeColor = typedArray.getColor(R.styleable.SuperTextView_stv_text_stroke_color,
                    DEFAULT_TEXT_STROKE_COLOR);
            textFillColor = typedArray.getColor(R.styleable.SuperTextView_stv_text_fill_color,
                    DEFAULT_TEXT_FILL_COLOR);
            textStrokeWidth = typedArray.getDimension(R.styleable.SuperTextView_stv_text_stroke_width,
                    DEFAULT_TEXT_STROKE_WIDTH);
            autoAdjust = typedArray.getBoolean(R.styleable.SuperTextView_stv_autoAdjust, true);

            textShaderStartColor =
                    typedArray.getColor(R.styleable.SuperTextView_stv_textShaderStartColor, 0);
            textShaderEndColor =
                    typedArray.getColor(R.styleable.SuperTextView_stv_textShaderEndColor, 0);
            textShaderMode =
                    ShaderMode.valueOf(typedArray.getInteger(R.styleable.SuperTextView_stv_textShaderMode,
                            ShaderMode.TOP_TO_BOTTOM.code));
            textShaderEnable = typedArray.getBoolean(R.styleable.SuperTextView_stv_textShaderEnable, false);

            typedArray.recycle();
        }
    }

    private void initPaint() {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //MyLog.d("SuperTextView","onSizeChanged" + " w=" + w + " h=" + h + " oldw=" + oldw + " oldh=" + oldh);

    }

//    @Override
//    public void invalidateDrawable(Drawable drawable) {
//        invalidate();
//        MyLog.d("SuperTextView","invalidateDrawable" + " drawable=" + drawable);
//    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //MyLog.d("SuperTextView","onLayout" + " changed=" + changed + " left=" + left + " top=" + top + " right=" + right + " bottom=" + bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        MyLog.d("SuperTextView","onDraw" + " canvas=" + canvas);
        if (getVisibility() != VISIBLE || !isAttachedToWindow() || getWidth() < 0 || getHeight() < 0)
            return;
        boolean needScroll = getScrollX() != 0 || getScrollY() != 0;
        if (needScroll) {
            canvas.translate(getScrollX(), getScrollY());
        }
        isNeedToAdjust(canvas, Adjuster.Opportunity.BEFORE_DRAWABLE);
        isNeedToAdjust(canvas, Adjuster.Opportunity.BEFORE_TEXT);
        if (needScroll) {
            canvas.translate(-getScrollX(), -getScrollY());
        }
        if (textStroke) {
            drawTextStroke(canvas);
        }
        if (textShaderEnable) {
            drawShaderText(canvas);
        } else {
            sdkOnDraw(canvas);
        }
        isNeedToAdjust(canvas, Adjuster.Opportunity.AT_LAST);
    }

    private LinearGradient createShader(int startColor, int endColor, ShaderMode shaderMode,
                                        float x0, float y0, float x1, float y1) {
        if (startColor != 0 && endColor != 0) {
            int temp = 0;
            switch (shaderMode) {
                case TOP_TO_BOTTOM:
                    x1 = x0;
                    break;
                case BOTTOM_TO_TOP:
                    x1 = x0;
                    temp = startColor;
                    startColor = endColor;
                    endColor = temp;
                    break;
                case LEFT_TO_RIGHT:
                    y1 = y0;
                    break;
                case RIGHT_TO_LEFT:
                    y1 = y0;
                    temp = startColor;
                    startColor = endColor;
                    endColor = temp;
                    break;
            }
            return new LinearGradient(x0, y0, x1, y1, startColor, endColor, Shader.TileMode.CLAMP);
        } else {
            return null;
        }
    }

    private void isNeedToAdjust(Canvas canvas, Adjuster.Opportunity currentOpportunity) {
        for (int i = 0; i < adjusterList.size(); i++) {
            Adjuster adjuster = adjusterList.get(i);
            if (currentOpportunity == adjuster.getOpportunity()) {
                long startDrawAdjustersTime = System.currentTimeMillis();
                if (adjuster.getType() == Adjuster.TYPE_SYSTEM) {
                    adjuster.adjust(this, canvas);
                } else if (autoAdjust) {
                    adjuster.adjust(this, canvas);
                }
            }
        }
    }

    private void drawTextStroke(Canvas canvas) {
        getPaint().setStyle(Paint.Style.STROKE);
        setTextColor(textStrokeColor);
        getPaint().setFakeBoldText(true);
        getPaint().setStrokeWidth(textStrokeWidth);
        sdkOnDraw(canvas);
        getPaint().setStyle(Paint.Style.FILL);
        getPaint().setFakeBoldText(false);
        /**
         * 下面这句不注释掉会导致 onDraw 不断被调用
         * 注释掉效果也一样
         */
        //setTextColor(textFillColor);
    }

    private void drawShaderText(Canvas canvas) {
        Shader tempShader = getPaint().getShader();
        if (getLayout() != null && getLayout().getLineCount() > 0) {
            float x0 = getLayout().getLineLeft(0);
            int y0 = getLayout().getLineTop(0);
            float x1 = x0 + getLayout().getLineWidth(0);
            float y1 = y0 + getLayout().getHeight();
            if (getLayout().getLineCount() > 1) {
                for (int i = 1; i < getLayout().getLineCount(); i++) {
                    if (x0 > getLayout().getLineLeft(i)) {
                        x0 = getLayout().getLineLeft(i);
                    }
                    if (x1 < x0 + getLayout().getLineWidth(i)) {
                        x1 = x0 + getLayout().getLineWidth(i);
                    }
                }
            }
            if (textShader == null) {
                textShader = createShader(textShaderStartColor, textShaderEndColor, textShaderMode,
                        x0, y0, x1, y1);
            }
            getPaint().setShader(textShader);
            sdkOnDraw(canvas);
        }
        getPaint().setShader(tempShader);
    }

    @SuppressLint("WrongCall")
    private void sdkOnDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /**
     * 获取最后一个 {@link Adjuster}
     *
     * @return 获得最后一个 {@link Adjuster}，如果存在的话。
     */
    public Adjuster getAdjuster() {
        if (adjusterList.size() > SYSTEM_ADJUSTER_SIZE) {
            return adjusterList.get(adjusterList.size() - 1);
        }
        return null;
    }

    /**
     * 获得index对应的 {@link Adjuster}。
     *
     * @param index 期望获得的Adjuster的index。
     * @return index对应的Adjuster，如果参数错误返回null。
     */
    public Adjuster getAdjuster(int index) {
        int realIndex = SYSTEM_ADJUSTER_SIZE + index;
        if (realIndex > SYSTEM_ADJUSTER_SIZE - 1 && realIndex < adjusterList.size()) {
            return adjusterList.get(realIndex);
        }
        return null;
    }

    /**
     * 获得SuperTextView中的所有Adjuster，如果没有返回null
     *
     * @return 如果SuperTextView有Adjuster，返回List<Adjuster>；否则返回null
     */
    public List<Adjuster> getAdjusterList() {
        if (adjusterList.size() > SYSTEM_ADJUSTER_SIZE) {
            ArrayList<Adjuster> r = new ArrayList<>();
            r.addAll(SYSTEM_ADJUSTER_SIZE, adjusterList);
            return r;
        }
        return null;
    }

    /**
     * 添加一个Adjuster。
     * 注意，最多支持添加3个Adjuster，否则新的Adjuster总是会覆盖最后一个Adjuster。
     *
     * @param adjuster {@link Adjuster}。会触发一次重绘。
     * @return SuperTextView
     */
    public SuperTextView addAdjuster(Adjuster adjuster) {
        if (adjusterList.size() < SYSTEM_ADJUSTER_SIZE + ALLOW_CUSTOM_ADJUSTER_SIZE) {
            innerAddAdjuster(adjuster);
        } else {
            removeAdjuster(adjusterList.size() - 1);
            innerAddAdjuster(adjuster);
        }
        return this;
    }

    private void addSysAdjuster(Adjuster adjuster) {
        if (adjuster != null) {
            adjuster.setType(Adjuster.TYPE_SYSTEM);
            adjusterList.add(SYSTEM_ADJUSTER_SIZE, adjuster);
            SYSTEM_ADJUSTER_SIZE++;
        }
    }

    private void innerAddAdjuster(Adjuster adjuster) {
        adjusterList.add(adjuster);
        adjuster.attach(this);
        postInvalidate();
    }

    /**
     * 移除指定位置的Adjuster。
     *
     * @param index 期望移除的Adjuster的位置。
     * @return 被移除的Adjuster，如果参数错误返回null。
     */
    public Adjuster removeAdjuster(int index) {
        int realIndex = SYSTEM_ADJUSTER_SIZE + index;
        if (realIndex > SYSTEM_ADJUSTER_SIZE - 1 && realIndex < adjusterList.size()) {
            Adjuster remove = adjusterList.remove(realIndex);
            remove.detach(this);
            postInvalidate();
            return remove;
        }
        return null;
    }

    /**
     * 移除指定的Adjuster，如果包含的话。
     *
     * @param adjuster 需要被移除的Adjuster
     * @return 被移除Adjuster在移除前在Adjuster列表中的位置。如果没有包含，返回-1。
     */
    public int removeAdjuster(Adjuster adjuster) {
        if (adjuster.type != Adjuster.TYPE_SYSTEM && adjusterList.contains(adjuster)) {
            int index = adjusterList.indexOf(adjuster);
            adjusterList.remove(adjuster);
            adjuster.detach(this);
            postInvalidate();
            return index;
        }
        return -1;
    }

    /**
     * 检查是否开启了文字描边
     *
     * @return true 表示开启了文字描边，否则表示没开启。
     */
    public boolean isTextStroke() {
        return textStroke;
    }

    /**
     * 设置是否开启文字描边。
     * 注意，开启文字描边后，文字颜色需要通过 {@link #setTextFillColor(int)} 设置。
     *
     * @param textStroke true表示开启文字描边。默认为false。会触发一次重绘。
     * @return SuperTextView
     */
    public SuperTextView setTextStroke(boolean textStroke) {
        this.textStroke = textStroke;
        postInvalidate();

        return this;
    }

    /**
     * 获取文字描边的颜色
     *
     * @return 文字描边的颜色。
     */
    public int getTextStrokeColor() {
        return textStrokeColor;
    }

    /**
     * 设置文字描边的颜色
     *
     * @param textStrokeColor 设置文字描边的颜色。默认为{@link Color#BLACK}。会触发一次重绘。
     * @return SuperTextView
     */
    public SuperTextView setTextStrokeColor(int textStrokeColor) {
        this.textStrokeColor = textStrokeColor;
        postInvalidate();

        return this;
    }

    /**
     * 获取文字的填充颜色，在开启文字描边时 {@link #setTextStroke(boolean)} 默认为BLACK。
     *
     * @return 文字填充颜色。
     */
    public int getTextFillColor() {
        return textFillColor;
    }

    /**
     * 设置文字的填充颜色，需要开启文字描边 {@link #setTextStroke(boolean)} 才能生效。默认为BLACK。
     *
     * @param textFillColor 设置文字填充颜色。默认为{@link Color#BLACK}。会触发一次重绘。
     * @return SuperTextView
     */
    public SuperTextView setTextFillColor(int textFillColor) {
        this.textFillColor = textFillColor;
        postInvalidate();

        return this;
    }

    /**
     * 获取文字描边的宽度
     *
     * @return 文字描边宽度。
     */
    public float getTextStrokeWidth() {
        return textStrokeWidth;
    }

    /**
     * 设置文字描边的宽度，需要开启文字描边 {@link #setTextStroke(boolean)} 才能生效。
     *
     * @param textStrokeWidth 设置文字描边宽度。会触发一次重绘。
     * @return SuperTextView
     */
    public SuperTextView setTextStrokeWidth(float textStrokeWidth) {
        this.textStrokeWidth = textStrokeWidth;
        postInvalidate();

        return this;
    }

    /**
     * 检查是否开启 {@link Adjuster} 功能。
     *
     * @return true表示开启了Adjuster功能。
     */
    public boolean isAutoAdjust() {
        return autoAdjust;
    }

    /**
     * 设置是否开启 {@link Adjuster} 功能。
     *
     * @param autoAdjust true开启Adjuster功能。反之，关闭。会触发一次重绘。
     * @return SuperTextView
     */
    public SuperTextView setAutoAdjust(boolean autoAdjust) {
        this.autoAdjust = autoAdjust;
        postInvalidate();

        return this;
    }


    /**
     * 检查是否启用了渐变功能。
     *
     * @return 返回true，如果启用了渐变功能。
     */
    public boolean isShaderEnable() {
        return shaderEnable;
    }

    /**
     * 设置是否启用渐变色功能。
     *
     * @param shaderEnable true启用渐变功能。反之，停用。
     * @return SuperTextView
     */
    public SuperTextView setShaderEnable(boolean shaderEnable) {
        this.shaderEnable = shaderEnable;
        postInvalidate();
        return this;
    }

    /**
     * 获取文字渐变色的起始颜色。
     *
     * @return 文字渐变起始色。
     */
    public int getTextShaderStartColor() {
        return textShaderStartColor;
    }

    /**
     * 设置文字渐变起始色。需要调用{@link SuperTextView#setTextShaderEnable(boolean)}后才能生效。会触发一次重绘。
     *
     * @param shaderStartColor 文字渐变起始色
     * @return SuperTextView
     */
    public SuperTextView setTextShaderStartColor(int shaderStartColor) {
        this.textShaderStartColor = shaderStartColor;
        textShader = null;
        postInvalidate();
        return this;
    }

    /**
     * 获取文字渐变色的结束颜色。
     *
     * @return 文字渐变结束色。
     */
    public int getTextShaderEndColor() {
        return textShaderEndColor;
    }

    /**
     * 设置文字渐变结束色。需要调用{@link SuperTextView#setShaderEnable(boolean)}后才能生效。会触发一次重绘。
     *
     * @param shaderEndColor 文字渐变结束色
     * @return SuperTextView
     */
    public SuperTextView setTextShaderEndColor(int shaderEndColor) {
        this.textShaderEndColor = shaderEndColor;
        textShader = null;
        postInvalidate();
        return this;
    }

    /**
     * 获取文字渐变色模式。在{@link ShaderMode}中可以查看所有支持的模式。
     * 需要调用{@link SuperTextView#setTextShaderEnable(boolean)}后才能生效。
     *
     * @return 渐变模式。
     */
    public ShaderMode getTextShaderMode() {
        return textShaderMode;
    }

    /**
     * 设置文字渐变模式。在{@link ShaderMode}中可以查看所有支持的模式。
     * 需要调用{@link SuperTextView#setTextShaderEnable(boolean)}后才能生效。
     *
     * @param shaderMode 文字渐变模式
     * @return SuperTextView
     */
    public SuperTextView setTextShaderMode(ShaderMode shaderMode) {
        this.textShaderMode = shaderMode;
        textShader = null;
        postInvalidate();
        return this;
    }

    /**
     * 检查是否启用了文字渐变功能。
     *
     * @return 返回true，如果启用了文字渐变功能。
     */
    public boolean isTextShaderEnable() {
        return textShaderEnable;
    }

    /**
     * 设置是否启用文字渐变色功能。
     *
     * @param shaderEnable true启用文字渐变功能。反之，停用。
     * @return SuperTextView
     */
    public SuperTextView setTextShaderEnable(boolean shaderEnable) {
        this.textShaderEnable = shaderEnable;
        postInvalidate();
        return this;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean hasConsume = false;
        int action = event.getAction();
        int actionMasked = action & MotionEvent.ACTION_MASK;
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < adjusterList.size(); i++) {
                Adjuster adjuster = adjusterList.get(i);
                if (adjuster.onTouch(this, event)) {
                    if (adjuster.type == Adjuster.TYPE_SYSTEM || isAutoAdjust()) {
                        hasConsume = true;
                        touchAdjusters.add(adjuster);
                    }
                }
            }
                /**
                 * 触发 Drawable 事件，就禁止控件处理事件了
                 */
                superTouchEvent = super.onTouchEvent(event);
        } else {
            for (int i = 0; i < touchAdjusters.size(); i++) {
                Adjuster adjuster = touchAdjusters.get(i);
                adjuster.onTouch(this, event);
                hasConsume = true;
            }
            if (superTouchEvent) {
                super.onTouchEvent(event);
            }
            if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
                touchAdjusters.clear();
                superTouchEvent = false;
            }
        }
        return hasConsume || superTouchEvent;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    /**
     * Adjuster被设计用来在SuperTextView的绘制过程中插入一些操作。
     * 这具有非常重要的意义。你可以用它来实现各种各样的效果。比如插入动画、修改状态。
     * 你可以指定Adjuster的作用层级，通过调用{@link Adjuster#setOpportunity(Opportunity)}，
     * {@link Opportunity}。默认为{@link Opportunity#BEFORE_TEXT}。
     * 在Adjuster中，可以获取到控件的触摸事件，这对于实现一些复杂的交互效果很有帮助。
     */
    public static abstract class Adjuster {
        private static final int TYPE_SYSTEM = 0x001;
        private static final int TYPE_CUSTOM = 0x002;

        private Opportunity opportunity = Opportunity.BEFORE_TEXT;
        private int type = TYPE_CUSTOM;

        /**
         * 当前Adjuster被设置到的SuperTextView
         */
        public SuperTextView host;

        /**
         * 在Canvas上绘制的东西将能够呈现在SuperTextView上。
         * 提示：你需要注意图层的使用。
         *
         * @param v      SuperTextView
         * @param canvas 用于绘制的Canvas。注意对Canvas的变换最好使用图层，否则会影响后续的绘制。
         */
        protected abstract void adjust(SuperTextView v, Canvas canvas);

        /**
         * 在这个方法中，你能够捕获到SuperTextView中发生的触摸事件。
         * 需要注意，如果你在该方法中返回了true来处理SuperTextView的触摸事件的话，你将在
         * SuperTextView的setOnTouchListener中设置的OnTouchListener中同样能够捕获到这些触摸事件，即使你在OnTouchListener中返回了false。
         * 但是，如果你在OnTouchListener中返回了true，这个方法将会失效，因为事件在OnTouchListener中被拦截了。
         *
         * @param v     SuperTextView
         * @param event 控件件接收到的触摸事件。
         * @return 默认返回false。如果想持续的处理控件的触摸事件就返回true。否则，只能接收到{@link MotionEvent#ACTION_DOWN}事件。
         */
        public boolean onTouch(SuperTextView v, MotionEvent event) {
            return false;
        }

        /**
         * 当Adjuster被通过 {@link SuperTextView#addAdjuster(Adjuster)} 设置到一个SuperTextView中时，
         * 会被调用。用于建立Adjuster与宿主SuperTextView之间的关系。
         *
         * @param stv 当前被设置到的SuperTextView对象
         * @return
         */
        private void attach(SuperTextView stv) {
            this.host = stv;
            onAttach(this.host);
        }

        /**
         * 当Adjuster被通过 {@link SuperTextView#addAdjuster(Adjuster)} 设置到一个SuperTextView中时， 会被调用。
         * <p>
         * 在这个方法中，开发者可以根据当前所处的SuperTextView环境，进行一些初始化的配置。
         *
         * @param stv 当前被设置到的SuperTextView对象
         */
        public void onAttach(SuperTextView stv) {

        }

        /**
         * 当Adjuster被从一个SuperTextView中移除时会被调用，用于解除Adjuster与宿主SuperTextView之间的关系。
         *
         * @param stv 当前被从那个SuperTextView中移除
         * @return
         */
        private void detach(SuperTextView stv) {
            this.host = null;
            onDetach(stv);
        }

        /**
         * 当Adjuster被从一个SuperTextView中移除时会被调用，用于解除Adjuster与宿主SuperTextView之间的关系。
         * <p>
         * 需要注意，在这个方法中，成员变量 {@link Adjuster#host} 已经被释放，不要直接使用该成员变量，而是使用 参数 stv。
         *
         * @param stv 当前被从那个SuperTextView中移除
         * @return
         */
        public void onDetach(SuperTextView stv) {

        }

        /**
         * 获取当前Adjuster的层级。
         *
         * @return Adjuster的作用层级。
         */
        public Opportunity getOpportunity() {
            return opportunity;
        }

        /**
         * 设置Adjuster的作用层级。在 {@link Opportunity} 中可以查看所有支持的层级。
         *
         * @param opportunity Adjuster的作用层级
         * @return 返回Adjuster本身，方便调用。
         */
        public Adjuster setOpportunity(Opportunity opportunity) {
            this.opportunity = opportunity;
            return this;
        }

        /**
         * @hide
         */
        private Adjuster setType(int type) {
            this.type = type;
            return this;
        }

        private int getType() {
            return type;
        }

        /**
         * Adjuster贴心的设计了控制作用层级的功能。
         * 你可以通过{@link Adjuster#setOpportunity(Opportunity)}来指定Adjuster的绘制层级。
         * 在SuperTextView中，绘制层级被从下到上分为：背景层、Drawable层、文字层3个层级。
         * 通过Opportunity来指定你的Adjuster想要插入到那个层级间。
         */
        public static enum Opportunity {
            /**
             * 背景层和Drawable层之间
             */
            BEFORE_DRAWABLE,
            /**
             * Drawable层和文字层之间
             */
            BEFORE_TEXT,
            /**
             * 最顶层
             */
            AT_LAST
        }
    }


    /**
     * SuperTextView的渐变模式。
     */
    public static enum ShaderMode {
        /**
         * 从上到下
         */
        TOP_TO_BOTTOM(0),
        /**
         * 从下到上
         */
        BOTTOM_TO_TOP(1),
        /**
         * 从左到右
         */
        LEFT_TO_RIGHT(2),
        /**
         * 从右到左
         */
        RIGHT_TO_LEFT(3);

        public int code;

        ShaderMode(int code) {
            this.code = code;
        }

        public static ShaderMode valueOf(int code) {
            for (ShaderMode mode : ShaderMode.values()) {
                if (mode.code == code) {
                    return mode;
                }
            }
            return TOP_TO_BOTTOM;
        }
    }


}
