package com.module.playways.grab.room.view.pk;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.common.SingCountDownView;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.NormalSelfSingCardView;
import com.module.playways.grab.room.view.pk.view.PKSingCardView;

public class PKSelfSingCardView extends RelativeLayout {

    public final static String TAG = "PKSelfSingCardView";

    NormalSelfSingCardView mNormalSingCardView;
    PKSingCardView mPkSingCardView;
    SingCountDownView mSingCountDownView;

    GrabRoomData mRoomData;

    public PKSelfSingCardView(Context context) {
        super(context);
        init();
    }

    public PKSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PKSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_pk_self_sing_card_layout, this);
        mNormalSingCardView = (NormalSelfSingCardView) findViewById(R.id.normal_sing_card_view);
        mPkSingCardView = (PKSingCardView) findViewById(R.id.pk_sing_card_view);
        mSingCountDownView = (SingCountDownView) findViewById(R.id.sing_count_down_view);
    }

    public void playLyric() {

    }


    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    SelfSingCardView.Listener mListener;

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
    }
}
