package com.module.playways.rank.room.comment;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rank.R;
import com.module.playways.rank.msg.event.CommentMsgEvent;
import com.module.playways.rank.room.event.InputBoardEvent;
import com.module.playways.rank.room.model.RoomData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class CommentView extends RelativeLayout {
    public final static String TAG = "CommentView";

    RecyclerView mCommentRv;

    LinearLayoutManager mLinearLayoutManager;

    CommentAdapter mCommentAdapter;

    RecyclerOnItemClickListener mClickListener;

    private RoomData mRoomData;
    private boolean mOnBottom = true;
    private boolean mDraging = false;
    private boolean mHasDataUpdate = false;
    private long mLastSetCommentListTs = 0;

    public CommentView(Context context) {
        super(context);
        init();
    }

    public CommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setListener(RecyclerOnItemClickListener listener) {
        this.mClickListener = listener;
    }

    RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            MyLog.d(TAG, "onScrollStateChangd,newState:" + newState + ",mOnBottom:" + mOnBottom);
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                // 闲置状态
                mDraging = false;
                // 停下来判断是否是最后一个,这里忽然有次不能到底了会有bug
                // 如果最后一个可见的元素==列表中最后一个元素，则认为到底了,
                int firstVisiblePosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                MyLog.d(TAG, "onScrollStateChangd firstVisiblePosition :" + firstVisiblePosition);
                if (firstVisiblePosition == 0) {
                    setOnBottom("onScrollStateChanged", true);
                } else {
                    setOnBottom("onScrollStateChanged", false);
                }
            } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                // 手动拖着滑动
                mDraging = true;
            } else {
                // 自动滑动
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            changeAlpha();
        }
    };

    private void setOnBottom(String from, boolean onBottom) {
        MyLog.d(TAG, "onBottom:" + this.mOnBottom + "-->" + onBottom + " from:" + from);
        if (this.mOnBottom != onBottom) {
            this.mOnBottom = onBottom;
            if (mOnBottom) {
                if (mHasDataUpdate) {
                    mCommentAdapter.notifyDataSetChanged();
                }
                mCommentRv.smoothScrollToPosition(0);
//                mMoveToLastItemIv.setVisibility(GONE);
//                mHasMore = 0;
//                if (mRoomChatMsgManager != null) {
//                    mRoomChatMsgManager.updateMaxSize(mRoomChatMsgManager.getInitMaxSize());
//                }
            }
            // 不在底部不需要更新数据
        }
    }

    private void init() {
        inflate(getContext(), R.layout.comment_view_layout, this);
        mCommentRv = (RecyclerView) this.findViewById(R.id.comment_rv);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        mLinearLayoutManager.setStackFromEnd(true);
        mCommentRv.setLayoutManager(mLinearLayoutManager);
        mCommentAdapter = new CommentAdapter(new RecyclerOnItemClickListener<CommentModel>() {
            @Override
            public void onItemClicked(View view, int position, CommentModel model) {
                if (mClickListener != null) {
                    if (model.getUserId() != RoomData.SYSTEM_ID) {
                        mClickListener.onItemClicked(view, position, model);
                    }
                }
            }
        });
        mCommentRv.setAdapter(mCommentAdapter);
        mCommentRv.addOnScrollListener(mOnScrollListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CommentMsgEvent event) {
        CommentModel commentModel = CommentModel.parseFromEvent(event, mRoomData);
        mCommentAdapter.getDataList().add(0, commentModel);
        if (!mOnBottom || mDraging) {
            mHasDataUpdate = true;
//            mHasMore++;
//            mMoveToLastItemIv.setVisibility(VISIBLE);
//            String s = mHasMore > 99 ? "99+" : String.valueOf(mHasMore);
//            mMoveToLastItemIv.setText(getResources().getQuantityString(R.plurals.more_comment_text, mHasMore, s));
//            if (mRoomChatMsgManager != null) {
//                mRoomChatMsgManager.updateMaxSize(Integer.MAX_VALUE);
//            }
        } else {
            // TODO: 2018/12/23 后期可优化，只更新某一部分位置信息 
            mCommentAdapter.notifyDataSetChanged();
            mCommentRv.smoothScrollToPosition(0);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InputBoardEvent event) {
        LayoutParams lp = (LayoutParams) this.getLayoutParams();
        if (event.show) {
            lp.addRule(RelativeLayout.ABOVE, R.id.input_container_view);
            setOnBottom("InputBoardEvent", true);
        } else {
            lp.addRule(RelativeLayout.ABOVE, R.id.bottom_container_view);
        }
        setLayoutParams(lp);
    }

    public void setRoomData(RoomData roomData) {
        this.mRoomData = roomData;
    }

    public RoomData getRoomData() {
        return mRoomData;
    }
}
