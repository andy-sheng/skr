package com.module.playways.room.room.comment;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.grab.room.event.GrabSwitchRoomEvent;
import com.module.playways.room.msg.event.DynamicEmojiMsgEvent;
import com.module.playways.room.room.comment.adapter.CommentAdapter;
import com.module.playways.room.room.comment.listener.CommentItemListener;
import com.module.playways.room.room.comment.model.CommentDynamicModel;
import com.module.playways.room.room.comment.model.CommentModel;
import com.module.playways.room.room.comment.model.CommentTextModel;
import com.module.playways.room.room.event.PretendCommentMsgEvent;
import com.module.playways.room.room.event.RankToVoiceTransformDataEvent;
import com.module.playways.voice.activity.VoiceRoomActivity;
import com.module.rank.R;
import com.module.playways.room.msg.event.CommentMsgEvent;
import com.module.playways.BaseRoomData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class CommentView extends RelativeLayout {
    public final static String TAG = "CommentView";

    RecyclerView mCommentRv;

    int mGameType = 0;

    LinearLayoutManager mLinearLayoutManager;

    CommentAdapter mCommentAdapter;

    CommentItemListener mCommentItemListener;

    int maxHeight = U.getDisplayUtils().dip2px(260);

    private BaseRoomData mRoomData;
    private boolean mOnBottom = true;
    private boolean mDraging = false;
    private boolean mHasDataUpdate = false;
    private long mLastSetCommentListTs = 0;

    public CommentView(Context context) {
        super(context);
        init(null);
    }

    public CommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public void setListener(CommentItemListener listener) {
        this.mCommentItemListener = listener;
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.getLayoutParams().height > maxHeight) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
            layoutParams.topMargin = layoutParams.topMargin + (layoutParams.height - maxHeight);
            layoutParams.height = maxHeight;
            setLayoutParams(layoutParams);
        }
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.gameType);
            mGameType = typedArray.getInt(R.styleable.gameType_type, 0);
            typedArray.recycle();
        }

        inflate(getContext(), R.layout.comment_view_layout, this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mCommentRv = (RecyclerView) this.findViewById(R.id.comment_rv);
        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        mLinearLayoutManager.setStackFromEnd(true);
        mCommentRv.setLayoutManager(mLinearLayoutManager);

        mCommentAdapter = new CommentAdapter(new CommentItemListener() {
            @Override
            public void clickAvatar(int userId) {
                if (mCommentItemListener != null) {
                    if (userId != UserAccountManager.SYSTEM_ID) {
                        mCommentItemListener.clickAvatar(userId);
                    }
                }
            }

            @Override
            public void clickAgreeKick(int userId, boolean isAgree) {
                if (mCommentItemListener != null) {
                    mCommentItemListener.clickAgreeKick(userId, isAgree);
                }
            }
        });
        mCommentAdapter.setGameType(mGameType);
        mCommentRv.setAdapter(mCommentAdapter);
        mCommentRv.addOnScrollListener(mOnScrollListener);

        if (getContext() instanceof VoiceRoomActivity) {
            RankToVoiceTransformDataEvent rankToVoiceTransformDataEvent = EventBus.getDefault().getStickyEvent(RankToVoiceTransformDataEvent.class);
            EventBus.getDefault().removeStickyEvent(RankToVoiceTransformDataEvent.class);
            if (rankToVoiceTransformDataEvent != null) {
                mCommentAdapter.setDataList(rankToVoiceTransformDataEvent.mCommentModelList);
            }
            mCommentRv.scrollToPosition(0);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CommentMsgEvent event) {
        MyLog.d(TAG, "onEvent" + " CommentMsgEvent = " + event.text);
        CommentTextModel commentTextModel = CommentTextModel.parseFromEvent(event, mRoomData);
        processCommentModel(commentTextModel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PretendCommentMsgEvent event) {
        MyLog.d(TAG, "onEvent" + " PresenterEvent =" + event.mCommentModel);
        processCommentModel(event.mCommentModel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DynamicEmojiMsgEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        // TODO: 2019/4/9 特殊图片表情
        CommentDynamicModel commentDynamicModel = CommentDynamicModel.parseFromEvent(event, mRoomData);
        processCommentModel(commentDynamicModel);
    }

    void processCommentModel(CommentModel commentModel) {
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

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(GrabSwitchRoomEvent event) {
        mCommentAdapter.getDataList().clear();
        mCommentAdapter.notifyDataSetChanged();
        mOnBottom = true;
        mDraging = false;
        mHasDataUpdate = false;
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(InputBoardEvent event) {
//        LayoutParams lp lp= (LayoutParams) this.getLayoutParams();
//        if (event.show) {
//            lp.addRule(RelativeLayout.ABOVE, R.id.input_container_view);
//            setOnBottom("InputBoardEvent", true);
//        } else {
//            lp.addRule(RelativeLayout.ABOVE, R.id.bottom_container_view);
//        }
//        setLayoutParams(lp);
//    }

    public void setRoomData(BaseRoomData roomData) {
        this.mRoomData = roomData;
    }

    public BaseRoomData getRoomData() {
        return mRoomData;
    }

    public List<CommentModel> getComments() {
        return mCommentAdapter.getDataList();
    }
}
