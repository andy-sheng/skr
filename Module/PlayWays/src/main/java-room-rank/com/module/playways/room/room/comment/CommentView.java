package com.module.playways.room.room.comment;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.player.IPlayer;
import com.common.player.IPlayerCallback;
import com.common.player.MyMediaPlayer;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabSwitchRoomEvent;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.msg.event.AudioMsgEvent;
import com.module.playways.room.msg.event.DynamicEmojiMsgEvent;
import com.module.playways.room.room.comment.adapter.CommentAdapter;
import com.module.playways.room.room.comment.listener.CommentViewItemListener;
import com.module.playways.room.room.comment.model.CommentAudioModel;
import com.module.playways.room.room.comment.model.CommentDynamicModel;
import com.module.playways.room.room.comment.model.CommentModel;
import com.module.playways.room.room.comment.model.CommentTextModel;
import com.module.playways.room.room.event.PretendCommentMsgEvent;
import com.module.playways.room.room.event.RankToVoiceTransformDataEvent;
import com.module.playways.songmanager.event.MuteAllVoiceEvent;
import com.module.playways.voice.activity.VoiceRoomActivity;
import com.module.playways.R;
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

    CommentViewItemListener mCommentItemListener;

    IPlayer mMediaPlayer = null;

    int maxHeight = U.getDisplayUtils().dip2px(260);

    private BaseRoomData mRoomData;
    private boolean mOnBottom = true;
    private boolean mDraging = false;
    private boolean mHasDataUpdate = false;
    private long mLastSetCommentListTs = 0;

    public static final int MSG_ENSURE_AUTO_SCROLL_BOTTOM = 1;  //自动滚动到底部

    Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ENSURE_AUTO_SCROLL_BOTTOM:
                    setOnBottom("AUTO_SCROLL_BOTTOM", true);
                    break;
            }
        }
    };

    public CommentView(Context context) {
        super(context);
        init(null);
    }

    public CommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public void setListener(CommentViewItemListener listener) {
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
                mUIHandler.removeCallbacksAndMessages(null);
            } else {
                // 自动滑动
                mUIHandler.removeCallbacksAndMessages(null);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            changeAlpha();
        }
    };

    private void setOnBottom(String from, boolean onBottom) {
        MyLog.d(TAG, "onBottom:" + this.mOnBottom + "-->" + onBottom + " from:" + from);
        if (!onBottom) {
            mUIHandler.sendEmptyMessageDelayed(MSG_ENSURE_AUTO_SCROLL_BOTTOM, 5000);
        } else {
            mUIHandler.removeCallbacksAndMessages(null);
        }
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
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
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
        // TODO: 2019/4/28 必须提前注册 
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mCommentRv = this.findViewById(R.id.comment_rv);
        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        mLinearLayoutManager.setStackFromEnd(true);
        mCommentRv.setLayoutManager(mLinearLayoutManager);

        mCommentAdapter = new CommentAdapter(new CommentAdapter.CommentAdapterListener() {
            @Override
            public void clickAvatar(int userId) {
                if (mCommentItemListener != null) {
                    if (userId != UserAccountManager.SYSTEM_ID) {
                        mCommentItemListener.clickAvatar(userId);
                    }
                }
            }

            @Override
            public void clickAudio(boolean isPlaying, CommentAudioModel commentAudioModel) {
                MyLog.d(TAG, "clickAudio" + " isPlaying=" + isPlaying + " commentAudioModel=" + commentAudioModel);
                if (commentAudioModel == null) {
                    return;
                }
                if (isPlaying) {
                    // 暂停播放
                    if (mMediaPlayer != null) {
                        mMediaPlayer.reset();
                    }
                    mCommentAdapter.setCurrentPlayAudioModel(null);
                    EventBus.getDefault().post(new MuteAllVoiceEvent(false));
                } else {
                    // 重新开始播放
                    GrabRoundInfoModel now = ((GrabRoomData) mRoomData).getRealRoundInfo();
                    if (now != null && now.isSingStatus() && now.singBySelf()) {
                        U.getToastUtil().showShort("演唱中无法收听语音");
                    } else {
                        // 直接在此处播放，有需要在放到外面去
                        mCommentAdapter.setCurrentPlayAudioModel(commentAudioModel);
                        if (mMediaPlayer == null) {
                            mMediaPlayer = new MyMediaPlayer();
                            mMediaPlayer.setCallback(new IPlayerCallback() {
                                @Override
                                public void onPrepared() {

                                }

                                @Override
                                public void onCompletion() {
                                    mCommentAdapter.setCurrentPlayAudioModel(null);
                                    EventBus.getDefault().post(new MuteAllVoiceEvent(false));
                                }

                                @Override
                                public void onSeekComplete() {

                                }

                                @Override
                                public void onVideoSizeChanged(int width, int height) {

                                }

                                @Override
                                public void onError(int what, int extra) {

                                }

                                @Override
                                public void onInfo(int what, int extra) {

                                }
                            });
                        }
                        if (!TextUtils.isEmpty(commentAudioModel.getLocalPath())) {
                            // 播放本地
                            mMediaPlayer.startPlay(commentAudioModel.getLocalPath());
                        } else {
                            // 播放url
                            mMediaPlayer.startPlay(commentAudioModel.getMsgUrl());
                        }
                        EventBus.getDefault().post(new MuteAllVoiceEvent(true));
                    }
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
        mUIHandler.removeCallbacksAndMessages(null);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    public void tryStopPlay(){
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mCommentAdapter.setCurrentPlayAudioModel(null);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CommentMsgEvent event) {
        MyLog.d(TAG, "onEvent" + " CommentMsgEvent = " + event.text);
        if (event.type == CommentMsgEvent.MSG_TYPE_SEND) {
            setOnBottom("CommentMsgEvent", true);
        }
        CommentTextModel commentTextModel = CommentTextModel.parseFromEvent(event, mRoomData);
        processCommentModel(commentTextModel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AudioMsgEvent event) {
        MyLog.d(TAG, "onEvent" + " AudioMsgEvent=" + event);
        if (event.type == AudioMsgEvent.MSG_TYPE_SEND) {
            setOnBottom("AudioMsgEvent", true);
        }
        CommentAudioModel commentAudioModel = CommentAudioModel.Companion.parseFromEvent(event, mRoomData);
        processCommentModel(commentAudioModel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PretendCommentMsgEvent event) {
        MyLog.d(TAG, "onEvent" + " PresenterEvent =" + event.mCommentModel);
        processCommentModel(event.mCommentModel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(DynamicEmojiMsgEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        if (event.type == DynamicEmojiMsgEvent.MSG_TYPE_SEND) {
            setOnBottom("DynamicEmojiMsgEvent", true);
        }
        // TODO: 2019/4/9 特殊图片表情
        CommentDynamicModel commentDynamicModel = CommentDynamicModel.parseFromEvent(event, mRoomData);
        processCommentModel(commentDynamicModel);
    }

    void processCommentModel(CommentModel commentModel) {
        mCommentAdapter.addToHead(commentModel);
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
