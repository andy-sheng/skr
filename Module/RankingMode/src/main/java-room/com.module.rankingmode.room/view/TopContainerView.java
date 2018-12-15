package com.module.rankingmode.room.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.view.ex.ExImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rankingmode.R;
import com.module.rankingmode.room.viewer.RoomViewerRvAdapter;
import com.module.rankingmode.room.event.InputBoardEvent;
import com.module.rankingmode.room.viewer.RoomViewerModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class TopContainerView extends RelativeLayout {
    public final static String TAG = "TopContainerView";
    RecyclerView mViewerRv;
    ExImageView mCloseBtn;
    LinearLayoutManager mLinearLayoutManger;
    RoomViewerRvAdapter mRoomViewerRvAdapter;

    Listener mListener;

    public TopContainerView(Context context) {
        super(context);
        init();
    }

    public TopContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
         MyLog.d(TAG,"init" );

        inflate(getContext(), R.layout.top_container_view_layout, this);
        MyLog.d(TAG,"init 1" );
        mViewerRv = (RecyclerView) this.findViewById(R.id.viewer_rv);
        mCloseBtn = (ExImageView) this.findViewById(R.id.close_btn);
        mLinearLayoutManger = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mViewerRv.setLayoutManager(mLinearLayoutManger);
        mRoomViewerRvAdapter = new RoomViewerRvAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {

            }
        });
        mViewerRv.setAdapter(mRoomViewerRvAdapter);

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.closeBtnClick();
                }
            }
        });
        MyLog.d(TAG,"init 3" );

        //TODO TEST
        List<RoomViewerModel> l = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            RoomViewerModel viewerModel = new RoomViewerModel("" + i, "");
            l.add(viewerModel);
        }
        mRoomViewerRvAdapter.setDataList(l);
        MyLog.d(TAG,"init 4" );
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

    public interface Listener {
        void closeBtnClick();
    }
}
