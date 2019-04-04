package com.module.playways.voice.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.module.playways.PlayWaysActivity;
import com.module.playways.rank.room.event.InputBoardEvent;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class VoiceTopContainerView extends RelativeLayout {
    public final static String TAG = "VoiceTopContainerView";
    ExTextView mBackTv;


    public VoiceTopContainerView(Context context) {
        super(context);
        init();
    }

    public VoiceTopContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.voice_top_container_view_layout, this);
        mBackTv = (ExTextView) this.findViewById(R.id.back_tv);
        mBackTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                for(Activity activity : U.getActivityUtils().getActivityList()){
                    if(activity instanceof PlayWaysActivity){
                        activity.finish();
                        break;
                    }
                }

                if (getContext() instanceof Activity) {
                    Activity activity = (Activity) getContext();
                    if (activity != null) {
                        activity.finish();
                    }
                }
            }
        });
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
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InputBoardEvent event) {
//        if (event.show) {
//            setVisibility(GONE);
//        } else {
//            setVisibility(VISIBLE);
//        }
    }

}
