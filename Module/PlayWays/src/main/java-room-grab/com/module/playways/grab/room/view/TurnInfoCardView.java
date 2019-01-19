package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.rank.R;

public class TurnInfoCardView extends RelativeLayout {

    public static final int MODE_SONG_SEQ = 1;
    public static final int MODE_BATTLE_BEGIN = 2;

    ExImageView mInfoIv;

    int mMode = MODE_BATTLE_BEGIN;

    public TurnInfoCardView(Context context) {
        super(context);
        init();
    }

    public TurnInfoCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TurnInfoCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_turn_info_card_layout, this);
        mInfoIv = (ExImageView) this.findViewById(R.id.info_iv);
        setBackgroundResource(R.drawable.kaishiduizhan_bj);
    }

    public void setModeSongSeq(boolean first) {
        if (mMode != MODE_SONG_SEQ) {
            setBackgroundResource(R.drawable.dijishou_bj);
            mMode = MODE_SONG_SEQ;
        }
        if (first) {
            mInfoIv.setImageResource(R.drawable.diyishou);
        } else {
            mInfoIv.setImageResource(R.drawable.xiayishou);
        }
    }

    public void setModeBattleBegin() {
        if (mMode != MODE_BATTLE_BEGIN) {
            setBackgroundResource(R.drawable.kaishiduizhan_bj);
            mMode = MODE_BATTLE_BEGIN;
        }
        mInfoIv.setImageResource(R.drawable.kaishiduizhan);
    }
}
