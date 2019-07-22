package com.module.playways.grab.room.view.freemic;

import android.view.View;
import android.view.ViewStub;
import android.widget.ScrollView;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ExViewStub;
import com.common.view.ex.ExImageView;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.zq.live.proto.Room.EQRoundStatus;
import com.zq.lyrics.LyricsManager;
import com.zq.mediaengine.kit.ZqEngineKit;

import io.reactivex.functions.Consumer;

/**
 * 自由麦自己视角的卡片
 */
public class FreeMicSelfSingCardView extends ExViewStub {

    public final String TAG = "MiniGameSelfSingCardView";

    ExImageView mIvBg;
    ScrollView mSvLyric;
    TextView mTvLyric;
    SingCountDownView2 mSingCountDownView;
    ExImageView mmMicControlBtn;
    GrabRoomData mRoomData;
    SelfSingCardView.Listener mListener;

    public FreeMicSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mIvBg = parentView.findViewById(R.id.iv_bg);
        mSvLyric = parentView.findViewById(R.id.sv_lyric);
        mTvLyric = parentView.findViewById(R.id.tv_lyric);
        mSingCountDownView = parentView.findViewById(R.id.sing_count_down_view);
        mSingCountDownView.setListener(mListener);
        mmMicControlBtn = parentView.findViewById(R.id.mic_control_btn);
        mmMicControlBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (ZqEngineKit.getInstance().getParams().isAnchor()) {
                    ZqEngineKit.getInstance().setClientRole(false);
                    mmMicControlBtn.setImageResource(R.drawable.free_mic_close_mic);
                } else {
                    ZqEngineKit.getInstance().setClientRole(true);
                    ZqEngineKit.getInstance().muteLocalAudioStream(false);
                    mmMicControlBtn.setImageResource(R.drawable.free_mic_open_mic);
                }
            }
        });
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_free_mic_self_sing_card_stub_layout;
    }

    public boolean playLyric() {
        tryInflate();
        setVisibility(View.VISIBLE);
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel != null) {
            int totalMs = infoModel.getMusic().getStandIntroEndT() - infoModel.getMusic().getStandIntroBeginT();
            int progress;  //当前进度条
            int leaveTime; //剩余时间
            MyLog.d(TAG, "countDown isParticipant:" + infoModel.isParticipant() + " enterStatus=" + infoModel.getEnterStatus());
            if (!infoModel.isParticipant() && infoModel.getEnterStatus()==EQRoundStatus.QRS_INTRO.getValue()){
                MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多");
                progress = infoModel.getElapsedTimeMs() * 100 / totalMs;
                leaveTime = totalMs - infoModel.getElapsedTimeMs();
            } else {
                progress = 1;
                leaveTime = totalMs;
            }
            mSingCountDownView.startPlay(progress, leaveTime, true);
            String url = infoModel.getMusic().getStandLrc();

            LyricsManager.getLyricsManager(U.app())
                    .loadGrabPlainLyric(url)
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            mTvLyric.setText(s);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            MyLog.e(throwable);
                        }
                    });
        }
        if(mRoomData.isInPlayerList()){
            mmMicControlBtn.setVisibility(View.VISIBLE);
            if (ZqEngineKit.getInstance().getParams().isAnchor()) {
                mmMicControlBtn.setImageResource(R.drawable.free_mic_open_mic);
            } else {
                mmMicControlBtn.setImageResource(R.drawable.free_mic_close_mic);
            }
        }else{
            mmMicControlBtn.setVisibility(View.GONE);
        }
        return true;
    }

    public void setListener(SelfSingCardView.Listener listener) {
        mListener = listener;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(visibility==View.GONE){
            if(mSingCountDownView!=null){
                mSingCountDownView.reset();
            }
        }
    }

    public void destroy() {

    }

}
