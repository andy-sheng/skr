package com.wali.live.common.gift.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;


/**
 * Created by xzy on 17-6-28.
 */
@SuppressLint("AppCompatCustomView")
public class SmallSendGiftBtn extends TextView {
    public static int rightFace = 1;
    public static int leftFace = 0;
    int horizel = 0;
    int vertical = 0;
    double rotate = 22.5 * Math.PI / 180;

    private SmallSendGiftBtn(Context context) {
        super(context);
        initView();
    }

    private SmallSendGiftBtn(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private SmallSendGiftBtn(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    void initView() {
        setBackground(GlobalData.app().getResources().getDrawable(R.drawable.live_gift_continuity_give_button_small));
        setTextColor(GlobalData.app().getResources().getColor(R.color.white));
        setGravity(Gravity.CENTER);
        setTextSize(DisplayUtils.dip2px(11.33f));
    }

    private void setBuilder(SmallSendGiftBtnBuilder smallSendGiftBuilder) {
        switch (smallSendGiftBuilder.mPosition) {
            case 1:
                horizel = smallSendGiftBuilder.mCircleRadio - (int) (Math.sin(rotate) * smallSendGiftBuilder.mOffRadio);
                vertical = smallSendGiftBuilder.mCircleRadio - (int) (Math.cos(rotate) * smallSendGiftBuilder.mOffRadio);
                break;
            case 2:
                horizel = smallSendGiftBuilder.mCircleRadio + (int) (Math.sin(rotate) * smallSendGiftBuilder.mOffRadio);
                vertical = smallSendGiftBuilder.mCircleRadio - (int) (Math.cos(rotate) * smallSendGiftBuilder.mOffRadio);
                break;
            case 3:
                horizel = smallSendGiftBuilder.mCircleRadio + (int) (Math.cos(rotate) * smallSendGiftBuilder.mOffRadio);
                vertical = smallSendGiftBuilder.mCircleRadio - (int) (Math.sin(rotate) * smallSendGiftBuilder.mOffRadio);
                break;
            case 4:
                horizel = smallSendGiftBuilder.mCircleRadio + (int) (Math.cos(rotate) * smallSendGiftBuilder.mOffRadio);
                vertical = smallSendGiftBuilder.mCircleRadio + (int) (Math.sin(rotate) * smallSendGiftBuilder.mOffRadio);
                break;
        }

        //再得到位置
        if (smallSendGiftBuilder.mFaceTo == leftFace) {
            horizel = horizel + (smallSendGiftBuilder.mCircleRadio - horizel) * 2;
        }

        horizel = horizel - (smallSendGiftBuilder.mSmallCirleRadio);
        vertical = vertical - (smallSendGiftBuilder.mSmallCirleRadio);

        setText(smallSendGiftBuilder.mNum + "");
        setTextSize(DisplayUtils.dip2px(11.33f));
    }

    public static class SmallSendGiftBtnBuilder {
        int mCircleRadio = 0;
        //大圆盘的中心到小圆盘的中心的距离
        int mOffRadio = 0;
        //小圆盘的直径
        int mSmallCirleRadio = 0;
        //位置，把圆盘分成八块，从左上22.5度的位置开始往右
        int mPosition = 0;

        Context mContext;
        //数量
        int mNum = -1;
        //面左面右，1为右,0为左
        int mFaceTo = -1;

        public SmallSendGiftBtnBuilder(Context context) {
            mContext = context;
        }

        public SmallSendGiftBtnBuilder setFace(int face) {
            mFaceTo = face;
            return this;
        }

        public SmallSendGiftBtnBuilder setCircleRadio(int cicleRadio) {
            mCircleRadio = cicleRadio;
            return this;
        }

        public SmallSendGiftBtnBuilder setOffRadio(int offRadio) {
            mOffRadio = offRadio;
            return this;
        }


        public SmallSendGiftBtnBuilder setSmallCirleRadio(int smallCirleRadio) {
            mSmallCirleRadio = smallCirleRadio;
            return this;
        }

        public SmallSendGiftBtnBuilder setPosition(int position) {
            mPosition = position;
            return this;
        }

        public SmallSendGiftBtnBuilder setNum(int num) {
            mNum = num;
            return this;
        }

        public SmallSendGiftBtn build() {
            SmallSendGiftBtn smallSendGiftBtn = new SmallSendGiftBtn(mContext);
            smallSendGiftBtn.setBuilder(this);
            return smallSendGiftBtn;
        }
    }
}