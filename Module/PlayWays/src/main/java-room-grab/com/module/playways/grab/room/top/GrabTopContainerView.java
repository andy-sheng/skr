package com.module.playways.grab.room.top;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.playways.RoomData;
import com.module.playways.rank.room.event.InputBoardEvent;
import com.module.playways.rank.room.score.bar.ScoreTipsView;
import com.module.playways.rank.room.view.MoreOpView;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class GrabTopContainerView extends RelativeLayout {
    public final static String TAG = "GrapTopContainerView";

    RelativeLayout mRelativeLayoutIconContainer;
    RecyclerView mTopContentRv;
    ExImageView mMoreBtn;
    ExTextView mSongIndexTv;

    MoreOpView mMoreOpView;

    Listener mListener;
    RoomData mRoomData;

    ScoreTipsView.Item mLastItem;

    GrabTopAdapter mGrabTopAdapter;

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
        mRelativeLayoutIconContainer = (RelativeLayout)this.findViewById(R.id.relativeLayout_icon_container);
        mTopContentRv = (RecyclerView)this.findViewById(R.id.top_content_rv);
        mMoreBtn = (ExImageView)this.findViewById(R.id.more_btn);
        mSongIndexTv = (ExTextView)this.findViewById(R.id.song_index_tv);

        mTopContentRv.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        mGrabTopAdapter = new GrabTopAdapter();
        mTopContentRv.setAdapter(mGrabTopAdapter);

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
        if (event.show) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
    }


    void reset(){
        mLastItem = null;
    }


    public interface Listener {
        void closeBtnClick();

        void onVoiceChange(boolean voiceOpen);
    }
}
