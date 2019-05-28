package com.module.playways.room.prepare.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.U;

public class MatchSucessBottomView extends View {

    int[] mCenterPosition = new int[]{U.getDisplayUtils().getPhoneWidth() / 2, U.getDisplayUtils().getPhoneHeight() / 2};

    public MatchSucessBottomView(Context context) {
        this(context, null);
    }

    public MatchSucessBottomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatchSucessBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint p = new Paint();

        p.setColor(0xFFFFED61);
        //实例化路径
        Path path = new Path();
        //第一个点
        path.moveTo(mCenterPosition[0], mCenterPosition[1]);// 此点为多边形的起点
        int[] point2 = new int[]{0, mCenterPosition[1] + U.getDisplayUtils().getPhoneWidth() / 2};
        path.lineTo(point2[0], point2[1]);
        int[] point3 = new int[]{0, U.getDisplayUtils().getPhoneHeight()};
        path.lineTo(point3[0], point3[1]);
        int[] point4 = new int[]{U.getDisplayUtils().getPhoneWidth(), U.getDisplayUtils().getPhoneHeight()};
        path.lineTo(point4[0], point4[1]);
        int[] point5 = new int[]{U.getDisplayUtils().getPhoneWidth(), mCenterPosition[1] + U.getDisplayUtils().getPhoneWidth() / 2};
        path.lineTo(point5[0], point5[1]);
//        int[] point6 = new int[]{U.getDisplayUtils().getPhoneWidth(), U.getDisplayUtils().getScreenHeight() - U.getDisplayUtils().getScreenWidth() / 2};
//        path.lineTo(point5[0], point5[1]);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, p);
    }
}
