package com.module.rankingmode.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.rankingmode.R;
import com.module.rankingmode.room.event.InputBoardEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BottomContainerView extends RelativeLayout {

    Listener mBottomContainerListener;

    ExImageView mEmoji1Btn;
    ExTextView mShowInputContainerBtn;
    ExImageView mEmoji4Btn;
    ExImageView mEmoji3Btn;
    ExImageView mEmoji2Btn;

    public BottomContainerView(Context context) {
        super(context);
        init();
    }

    public BottomContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(),R.layout.bottom_container_view_layout,this);
        mEmoji1Btn = (ExImageView) this.findViewById(R.id.emoji1_btn);
        mShowInputContainerBtn = (ExTextView) this.findViewById(R.id.show_input_container_btn);
        mEmoji4Btn = (ExImageView) this.findViewById(R.id.emoji4_btn);
        mEmoji3Btn = (ExImageView) this.findViewById(R.id.emoji3_btn);
        mEmoji2Btn = (ExImageView) this.findViewById(R.id.emoji2_btn);
        mShowInputContainerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (U.getCommonUtils().isFastDoubleClick()) {
                    return;
                }
                if (mBottomContainerListener != null) {
                    mBottomContainerListener.showInputBtnClick();
                }
            }
        });
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
        if(event.show){
            setVisibility(GONE);
        }else{
            setVisibility(VISIBLE);
        }
    }

    public void setListener(Listener l) {
        mBottomContainerListener = l;
    }

    public interface Listener {
        void showInputBtnClick();
    }

}
