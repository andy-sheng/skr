package com.module.playways.grab.room.view.normal.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.common.log.MyLog;
import com.module.playways.R;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.zq.live.proto.Room.EWantSingType;

/**
 * 自己唱的歌词板(正常和pk都可以用)
 */
public class SelfSingLyricView extends BaseSelfSingLyricView {

    public final static String TAG = "SelfSingLyricView";

    ImageView mIvChallengeIcon;

    public SelfSingLyricView(Context context) {
        super(context);
    }

    public SelfSingLyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelfSingLyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init() {
        inflate(getContext(), R.layout.grab_self_sing_lyric_layout, this);

        mTvLyric = findViewById(R.id.tv_lyric);
        mManyLyricsView = findViewById(R.id.many_lyrics_view);
        mVoiceScaleView = findViewById(R.id.voice_scale_view);
        mIvChallengeIcon = findViewById(R.id.iv_challenge_icon);
    }

    public void initLyric() {
        super.initLyric();

        if (mRoomData == null) {
            MyLog.w(TAG, "playLyric mRoomData = null");
            return;
        }

        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的");
            return;
        }

        if (infoModel.getWantSingType() == EWantSingType.EWST_COMMON_OVER_TIME.getValue()
                || infoModel.getWantSingType() == EWantSingType.EWST_ACCOMPANY_OVER_TIME.getValue()) {
            mIvChallengeIcon.setVisibility(VISIBLE);
        } else {
            mIvChallengeIcon.setVisibility(GONE);
        }

        mVoiceScaleView.setVisibility(View.GONE);
    }
}
