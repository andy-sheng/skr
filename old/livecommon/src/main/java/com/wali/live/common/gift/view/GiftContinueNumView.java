package com.wali.live.common.gift.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.live.module.common.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangjiawei on 2017/7/19.
 */
// max number:9999
public class GiftContinueNumView extends RelativeLayout {

    int[] nums = new int[]{R.drawable.lianfa_number_0_s, R.drawable.lianfa_number_1_s, R.drawable.lianfa_number_2_s,
            R.drawable.lianfa_number_3_s, R.drawable.lianfa_number_4_s, R.drawable.lianfa_number_5_s,
            R.drawable.lianfa_number_6_s, R.drawable.lianfa_number_7_s, R.drawable.lianfa_number_8_s,
            R.drawable.lianfa_number_9_s};

    private int number=0;
    private Context mContext;

    private ImageView xIv;
    private ImageView num0Iv;
    private ImageView num1Iv;
    private ImageView num2Iv;
    private ImageView num3Iv;

    private List<ImageView> numList=new ArrayList<>();

    public GiftContinueNumView(Context context) {
        this(context,null);
    }

    public GiftContinueNumView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GiftContinueNumView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
        init();
    }

    private void init() {
        inflate(mContext, R.layout.gift_continue_numer_layout,this);
        xIv= (ImageView) findViewById(R.id.x_iv);
        num0Iv= (ImageView) findViewById(R.id.num0_iv);
        num1Iv= (ImageView) findViewById(R.id.num1_iv);
        num2Iv= (ImageView) findViewById(R.id.num2_iv);
        num3Iv= (ImageView) findViewById(R.id.num3_iv);
        numList.add(num3Iv);
        numList.add(num2Iv);
        numList.add(num1Iv);
        numList.add(num0Iv);
        numList.add(xIv);
        dismiss();
        disableClipOnParents(this);
    }



    public void showNum(int num){
        dismiss();
        number=num;
        xIv.setVisibility(VISIBLE);
        int index=0;
        while (number>0){
            ImageView view=numList.get(index);
            view.setVisibility(VISIBLE);
            view.setImageResource(nums[number-number/10*10]);
            number/=10;
            index++;
        }

//        Observable.timer(1000, TimeUnit.SECONDS)
//                .compose(((BaseActivity)mContext).bindToLifecycle())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(aLong -> dismiss());
    }

    public void dismiss(){
        for(ImageView view:numList){
            view.setVisibility(View.GONE);
        }
    }

    public void disableClipOnParents(View v) {
        if (v.getParent() == null) {
            return;
        }

        if (v instanceof ViewGroup) {
            ((ViewGroup) v).setClipChildren(false);
        }

        if (v.getParent() instanceof View) {
            disableClipOnParents((View) v.getParent());
        }
    }
}
