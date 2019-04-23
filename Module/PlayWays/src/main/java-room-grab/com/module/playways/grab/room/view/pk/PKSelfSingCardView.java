package com.module.playways.grab.room.view.pk;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.rank.R;

public class PKSelfSingCardView extends RelativeLayout {

    public final static String TAG = "PKSelfSingCardView";

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
