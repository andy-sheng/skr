package com.wali.live.moduletest.fragment;

import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.wali.live.moduletest.R;

public class ShowTextViewFragment extends BaseFragment {


    @Override
    public int initView() {
        return R.layout.test_fragment_show_textview_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        {
            View mTvTest = getRootView().findViewById(R.id.tv_test1);
            Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                    .setGradientAngle(0)
                    .setGradientColor(Color.parseColor("#63B8FF"), Color.parseColor("#4F94CD"))
                    .build();
            mTvTest.setClickable(true);
            mTvTest.setBackground(drawable);
        }

        {
            View mTvTest = getRootView().findViewById(R.id.tv_test2);
            Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                    .setShape(DrawableCreator.Shape.Rectangle)
                    .setPressedDrawable(new ColorDrawable(Color.parseColor("#7CCD7C")))
                    .setUnPressedDrawable(new ColorDrawable(Color.parseColor("#7CFC00")))
                    .build();
            mTvTest.setClickable(true);
            mTvTest.setBackground(drawable);
        }

        {
            View mTvTest = getRootView().findViewById(R.id.tv_test3);
            Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                    .setShape(DrawableCreator.Shape.Rectangle)
                    .setPressedStrokeColor(Color.parseColor("#000000"), Color.parseColor("#ffcc0000"))
                    .setPressedTextColor(Color.parseColor("#0000ff"))
                    .setUnPressedTextColor(Color.parseColor("#ffffff"))
                    .setRipple(true, Color.parseColor("#71C671"))
                    .setSolidColor(Color.parseColor("#7CFC00"))
                    .setStrokeWidth(U.getDisplayUtils().dip2px(2))
                    .build();
            mTvTest.setClickable(true);
            mTvTest.setBackground(drawable);
        }

        {
            View mTvTest = getRootView().findViewById(R.id.tv_test4);
            Drawable drawable = new DrawableCreator.Builder()
                    .setPressedDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circle_like_pressed))
                    .setUnPressedDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circle_like_normal))
                    .build();
            mTvTest.setClickable(true);
            mTvTest.setBackground(drawable);
        }


        {
            View v = getRootView().findViewById(R.id.image_view_btn);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        {
            View v = getRootView().findViewById(R.id.image_button_btn);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        {

            ExTextView v = getRootView().findViewById(R.id.span_test);
            int lineHeight = v.getLineHeight();
            float textSize = v.getTextSize();
            SpannableStringBuilder ssb = new SpanUtils()
                    .appendImage(R.drawable.shape_spannable_block_high, SpanUtils.ALIGN_TOP)
                    .append("大图").setBackgroundColor(Color.LTGRAY)
                    .appendImage(R.drawable.shape_spannable_block_high, SpanUtils.ALIGN_TOP)
                    .append("顶部").setBackgroundColor(Color.LTGRAY)
                    .appendImage(R.drawable.shape_spannable_block_high, SpanUtils.ALIGN_TOP)
                    .appendLine("对齐").setBackgroundColor(Color.LTGRAY)

                    .appendImage(R.drawable.shape_spannable_block_high, SpanUtils.ALIGN_CENTER)
                    .append("大图").setBackgroundColor(Color.GREEN)
                    .appendImage(R.drawable.shape_spannable_block_high, SpanUtils.ALIGN_CENTER)
                    .append("居中").setBackgroundColor(Color.GREEN)
                    .appendImage(R.drawable.shape_spannable_block_high, SpanUtils.ALIGN_CENTER)
                    .appendLine("对齐").setBackgroundColor(Color.GREEN)

                    .appendImage(R.drawable.shape_spannable_block_high, SpanUtils.ALIGN_BOTTOM)
                    .append("大图").setBackgroundColor(Color.LTGRAY)
                    .appendImage(R.drawable.shape_spannable_block_high, SpanUtils.ALIGN_BOTTOM)
                    .append("底部").setBackgroundColor(Color.LTGRAY)
                    .appendImage(R.drawable.shape_spannable_block_high, SpanUtils.ALIGN_BOTTOM)
                    .appendLine("对齐").setBackgroundColor(Color.LTGRAY)

                    .appendLine("粗体").setBackgroundColor(Color.LTGRAY).setBold().setForegroundColor(Color.YELLOW).setVerticalAlign(SpanUtils.ALIGN_CENTER)
                    .appendLine("前景色").setForegroundColor(Color.GREEN)
                    .appendLine("背景色").setBackgroundColor(Color.LTGRAY)
                    .append("行高顶部对齐").setLineHeight(3 * lineHeight, SpanUtils.ALIGN_TOP).setFontSize(20).setBackgroundColor(Color.GREEN)
                    .append("行高").setLineHeight(3 * lineHeight, SpanUtils.ALIGN_CENTER).setFontSize(40).setBackgroundColor(Color.LTGRAY)
                    .appendLine("行高顶部").setLineHeight(3 * lineHeight, SpanUtils.ALIGN_BOTTOM).setFontSize(60).setBackgroundColor(Color.LTGRAY)
                    .append("行高").setFontSize(100).setBackgroundColor(Color.GREEN)
                    .append("行高").setFontSize(20).setBackgroundColor(Color.LTGRAY).setUnderline().setVerticalAlign(SpanUtils.ALIGN_CENTER)
                    .appendLine("行高居中对齐").setLineHeight(2 * lineHeight, SpanUtils.ALIGN_CENTER).setBackgroundColor(Color.LTGRAY)
                    .appendLine("行高底部对齐").setLineHeight(2 * lineHeight, SpanUtils.ALIGN_BOTTOM).setBackgroundColor(Color.GREEN)
                    .appendLine("测试段落缩，首行缩进两字，其他行不缩进").setLeadingMargin((int) textSize * 2, 10).setBackgroundColor(Color.GREEN)
                    .appendLine("测试引用，后面的字是为了凑到两行的效果").setQuoteColor(Color.GREEN, 10, 10).setBackgroundColor(Color.LTGRAY)
                    .appendLine("测试列表项，后面的字是为了凑到两行的效果").setBullet(Color.GREEN, 20, 10).setBackgroundColor(Color.LTGRAY).setBackgroundColor(Color.GREEN)
                    .appendLine("32dp 字体").setFontSize(32, true)
                    .appendLine("2 倍字体").setFontProportion(2)
                    .appendLine("横向 2 倍字体").setFontXProportion(1.5f)
                    .appendLine("删除线").setStrikethrough()
                    .appendLine("下划线").setUnderline()
                    .append("测试").appendLine("上标").setSuperscript()
                    .append("测试").appendLine("下标").setSubscript()
                    .appendLine("粗体").setBold()
                    .appendLine("斜体").setItalic()
                    .appendLine("粗斜体").setBoldItalic()
                    .appendLine("monospace 字体").setFontFamily("monospace")
//                .appendLine("自定义字体").setTypeface(Typeface.createFromAsset(getAssets(), "fonts/dnmbhs.ttf"))
                    .appendLine("相反对齐").setHorizontalAlign(Layout.Alignment.ALIGN_OPPOSITE)
                    .appendLine("居中对齐").setHorizontalAlign(Layout.Alignment.ALIGN_CENTER)
                    .appendLine("正常对齐").setHorizontalAlign(Layout.Alignment.ALIGN_NORMAL)
                    .append("测试").appendLine("点击事件").setClickSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            U.getToastUtil().showShort("事件触发了");
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            ds.setColor(Color.BLUE);
                            ds.setUnderlineText(false);
                        }
                    })
                    .append("测试").appendLine("Url").setUrl("https://github.com/Blankj/AndroidUtilCode")
                    .append("测试").appendLine("模糊").setBlur(3, BlurMaskFilter.Blur.NORMAL)
                    .appendLine("颜色渐变").setShader(new LinearGradient(0, 0,
                            64 * U.getDisplayUtils().getDensity() * 4, 0,
                            getResources().getIntArray(R.array.rainbow),
                            null,
                            Shader.TileMode.REPEAT)).setFontSize(64, true)
                    .appendLine("图片着色").setFontSize(64, true).setShader(
                            new BitmapShader(BitmapFactory.decodeResource(getResources(), R.drawable.span_cheetah),
                                    Shader.TileMode.REPEAT,
                                    Shader.TileMode.REPEAT))
                    .appendLine("阴影效果").setFontSize(64, true).setBackgroundColor(Color.BLACK).setShadow(24, 8, 8, Color.WHITE)

                    .append("小图").setBackgroundColor(Color.GREEN)
                    .appendImage(R.drawable.shape_spannable_block_low, SpanUtils.ALIGN_TOP)
                    .append("顶部").setBackgroundColor(Color.GREEN)
                    .appendImage(R.drawable.shape_spannable_block_low, SpanUtils.ALIGN_CENTER)
                    .append("居中").setBackgroundColor(Color.GREEN)
                    .appendImage(R.drawable.shape_spannable_block_low, SpanUtils.ALIGN_BASELINE)
                    .append("底部").setBackgroundColor(Color.GREEN)
                    .appendImage(R.drawable.shape_spannable_block_low, SpanUtils.ALIGN_BOTTOM)
                    .appendLine("对齐").setBackgroundColor(Color.GREEN)

                    .append("测试空格").appendSpace(30, Color.LTGRAY).appendSpace(50, Color.GREEN).appendSpace(100).appendSpace(30, Color.LTGRAY).appendSpace(50, Color.GREEN)
                    .create();

            v.setText(ssb);
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
