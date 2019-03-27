package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.ObjectPlayControlTemplate;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class GrabDengBigAnimationViewGroup extends RelativeLayout {

    public final static String TAG = GrabDengBigAnimationViewGroup.class.getSimpleName();

    static final int MAX_NUM = 1;   // 同时播放动画个数

    private List<GrabDengBigAnimationView> mFeedGrabDengBigViews = new ArrayList<>(MAX_NUM);


//    ObjectPlayControlTemplate<, GrabDengBigAnimationView> mControlTemplate =

    public GrabDengBigAnimationViewGroup(Context context) {
        super(context);
        init(context);
    }

    public GrabDengBigAnimationViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GrabDengBigAnimationViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.grab_deng_big_animation_view_group, this);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
//        mControlTemplate.destroy();
    }


}
