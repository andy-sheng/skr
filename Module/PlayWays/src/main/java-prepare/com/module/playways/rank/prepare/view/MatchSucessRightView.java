package com.module.playways.rank.prepare.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.U;

public class MatchSucessRightView extends View {
    int[] mCenterPosition = new int[]{U.getDisplayUtils().getPhoneWidth() / 2, U.getDisplayUtils().getPhoneHeight() / 2};

    public MatchSucessRightView(Context context) {
        this(context, null);
    }

    public MatchSucessRightView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatchSucessRightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint p = new Paint();

        p.setColor(0xff0288D0);
        //实例化路径
        Path path = new Path();
        //第一个点
        path.moveTo(mCenterPosition[0], mCenterPosition[1]);// 此点为多边形的起点
        int[] point2 = new int[]{U.getDisplayUtils().getPhoneWidth() / 2, 0};
        path.lineTo(point2[0], point2[1]);
        int[] point3 = new int[]{U.getDisplayUtils().getPhoneWidth(), 0};
        path.lineTo(point3[0], point3[1]);
        int[] point4 = new int[]{U.getDisplayUtils().getPhoneWidth(), U.getDisplayUtils().getPhoneHeight() / 2 + U.getDisplayUtils().getPhoneWidth() / 2};
        path.lineTo(point4[0], point4[1]);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, p);
    }
}
