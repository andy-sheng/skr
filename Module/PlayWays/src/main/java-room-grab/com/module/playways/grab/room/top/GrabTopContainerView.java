package com.module.playways.grab.room.top;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.SomeOneGrabEvent;
import com.module.playways.grab.room.event.SomeOneLightBurstEvent;
import com.module.playways.grab.room.event.SomeOneLightOffEvent;
import com.module.playways.grab.room.event.SomeOneOnlineChangeEvent;
import com.module.playways.rank.room.event.InputBoardEvent;
import com.module.playways.rank.room.view.MoreOpView;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class GrabTopContainerView extends RelativeLayout {
    public final static String TAG = "GrapTopContainerView";
    public static final int MSG_SHOW = 0;
    public static final int MSG_HIDE = 1;
    RelativeLayout mRelativeLayoutIconContainer;
    GrabPlayerRv2 mTopContentRv;
    ExImageView mMoreBtn;
    ExTextView mSongIndexTv;
    MoreOpView mMoreOpView;
    GrabTopView mGrabTopView;// 切房间按钮，金币
    Listener mListener;
    GrabRoomData mRoomData;
    GrabAudienceView mGrabAudienceView;

    Handler mUiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SHOW:
                    setVisibility(VISIBLE);
                    break;
                case MSG_HIDE:
                    setVisibility(GONE);
                    break;
            }
        }
    };

//    GrabTopAdapter mGrabTopAdapter;

    public GrabTopContainerView(Context context) {
        super(context);
        init();
    }

    public GrabTopContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_top_container_view_layout, this);
        mRelativeLayoutIconContainer = (RelativeLayout) this.findViewById(R.id.relativeLayout_icon_container);
        mTopContentRv = this.findViewById(R.id.top_content_rv);
        mGrabTopView = (GrabTopView) findViewById(R.id.grab_top_view);
        mMoreBtn = (ExImageView) this.findViewById(R.id.more_btn);
        mSongIndexTv = (ExTextView) this.findViewById(R.id.song_index_tv);
        mGrabAudienceView = (GrabAudienceView) this.findViewById(R.id.grab_audience_view);

//        mTopContentRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//        mGrabTopAdapter = new GrabTopAdapter();
//        mTopContentRv.setAdapter(mGrabTopAdapter);

        mMoreBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMoreOpView == null) {
                    mMoreOpView = new MoreOpView(getContext());
                    mMoreOpView.setListener(new MoreOpView.Listener() {
                        @Override
                        public void onClostBtnClick() {
                            if (mListener != null) {
                                mListener.closeBtnClick();
                            }
                        }

                        @Override
                        public void onVoiceChange(boolean voiceOpen) {
                            // 打开或者关闭声音 只是不听别人的声音
                            if (mListener != null) {
                                mListener.onVoiceChange(voiceOpen);
                            }
                        }
                    });
                    mMoreOpView.setRoomData(mRoomData);
                }
                mMoreOpView.showAt(mMoreBtn);
            }
        });
    }

    public void setSeqIndex(int seq, int size) {
        String text = String.format("%s/%s",seq,size);
        mSongIndexTv.setText(text);
    }

    public void setModeGrab() {
        // 抢唱模式
        mTopContentRv.setModeGrab();
    }

    public void setModeSing(long singUid) {
        // 演唱模式
        mTopContentRv.setModeSing((int) singUid);
    }

    public void onGameFinish() {
        if (mMoreOpView != null) {
            mMoreOpView.dismiss();
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneGrabEvent event) {
        mTopContentRv.grap(event.uid);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneLightOffEvent event) {
        mTopContentRv.lightOff(event.uid);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneLightBurstEvent event) {
        mTopContentRv.toBurstState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneOnlineChangeEvent event) {
        mTopContentRv.onlineChange(event.playerInfoModel);
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    @Override
    public void setVisibility(int visibility) {
        mUiHandler.removeCallbacksAndMessages(null);
        super.setVisibility(visibility);
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
        mTopContentRv.setRoomData(roomData);
        mGrabTopView.setRoomData(roomData);
        mGrabAudienceView.setRoomData(roomData);
    }

    public void hideWithDelay(long delay){
        mUiHandler.removeCallbacksAndMessages(null);
        mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(MSG_HIDE), delay);
    }

    public GrabTopView getGrabTopView(){
        return mGrabTopView;
    }

    void reset() {
    }

    public interface Listener {
        void closeBtnClick();

        void onVoiceChange(boolean voiceOpen);
    }
}
