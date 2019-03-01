package com.module.playways.grab.room.top;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.playways.BaseRoomData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.SomeOneGrabEvent;
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

    RelativeLayout mRelativeLayoutIconContainer;
    GrabPlayerRv2 mTopContentRv;
    ExImageView mMoreBtn;
    ExTextView mSongIndexTv;
    MoreOpView mMoreOpView;
    GrabTopView mGrabTopView;
    Listener mListener;
    BaseRoomData mRoomData;

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
    public void onEvent(SomeOneOnlineChangeEvent event) {
        mTopContentRv.onlineChange(event.playerInfoModel);
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public void setRoomData(BaseRoomData roomData) {
        mRoomData = roomData;
        mTopContentRv.setRoomData(roomData);
        mGrabTopView.setRoomData((GrabRoomData) roomData);
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
