package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.config.GetConfigManager;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IOrientationListener;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.common.barrage.event.CommentRefreshEvent;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.common.barrage.view.adapter.LiveCommentRecyclerAdapter;
import com.wali.live.common.barrage.view.utils.CommentLevelOrLikeCache;
import com.wali.live.common.barrage.view.utils.CommentVfansLevelCache;
import com.wali.live.common.barrage.view.utils.CommentVipLevelIconCache;
import com.wali.live.common.model.CommentModel;
import com.wali.live.event.JumpSchemeEvent;
import com.wali.live.event.UserActionEvent;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * @module 直播间弹幕评论
 */
public class WatchGameLiveCommentView extends RelativeLayout implements IBindActivityLIfeCycle, IComponentView<WatchGameLiveCommentView.IPresenter, WatchGameLiveCommentView.IView> {
    private static final String TAG = WatchGameLiveCommentView.class.getSimpleName();
    private static final int IDLE_STATUS_MAX_TIME = 30 * 1000;

    // this value should be adjust when ui design is changed
    private static final int COMMENT_WIDTH_LANDSCAPE = DisplayUtils.getPhoneHeight() / 2;
    private static final int COMMENT_HEIGHT_PORTRAIT = DisplayUtils.dip2px(130.33f); // 弹幕区域高度
    private static final int COMMENT_HEIGHT_LANDSCAPE = DisplayUtils.dip2px(60.33f);
    private static final int COMMENT_MARGIN_PORTRAIT = DisplayUtils.dip2px(75f); // 弹幕区域右边距

    private static final int COMMENT_PADDING_LEFT = DisplayUtils.dip2px(6);
    private static final int COMMENT_PADDING_RIGHT = DisplayUtils.dip2px(6.67f);
    private static final int LANDSPACE_COMMENT_PADDING_BOTTOM = DisplayUtils.dip2px(43.33f);
    private static final int PORTRAIT_COMMENT_PADDING_BOTTOM = DisplayUtils.dip2px(6.66f);

    private boolean mIsLandscape = false;
    private int mExtraRightMargin = 0;

    private int mHasMore = 0;

    private LiveCommentRecyclerAdapter mAdapter;
    private boolean mOnBottom = true;
    private boolean mHasDataUpdate = false; // 时候有数据可更新

    private String mToken;

    private int[] mLocation = new int[2];
    private int[] mItemLocation = new int[2];


    public TextView mMoveToLastItemIv;  //点击回到最底部

    @Nullable
    protected IPresenter mPresenter;

    public RecyclerView mCommentRv;

//    @Bind(R.id.barrage_comment_txt_vip_join)
//    public EnterRoomView mEnterRoom;

    private LiveCommentRecyclerAdapter.LiveCommentNameClickListener mNameViewClickListener = null;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean mDraging = false;

    public WatchGameLiveCommentView(Context context) {
        super(context);
        initContentView(context);
    }

    public WatchGameLiveCommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initContentView(context);
    }

    public WatchGameLiveCommentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initContentView(context);
    }

    private LinearLayoutManager mLayoutManager;

    private RecyclerView.OnScrollListener mOnScrollListener;


    private LiveRoomChatMsgManager mRoomChatMsgManager;

    /**
     * 捕获一些 Recycler View Inconsistency Detected error 的异常
     */
    public static class WrapContentLinearLayoutManager extends LinearLayoutManager {

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                MyLog.e("Error", "IndexOutOfBoundsException in RecyclerView happens");
            }
        }

        @Override
        public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
            int result = 0;
            try {
                result = super.scrollVerticallyBy(dy, recycler, state);
            } catch (Exception e) {
                MyLog.w(TAG + e);
            }
            return result;
        }
    }

    private void initContentView(Context context) {
        inflate(context, R.layout.livecomment_recycler_layout, this);
        mMoveToLastItemIv = (TextView) this.findViewById(R.id.moveTolastIv);
        mCommentRv = (RecyclerView) this.findViewById(R.id.barrage_comment_list_view);
        mLayoutManager = new WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);
        mCommentRv.setLayoutManager(mLayoutManager);
        mCommentRv.setItemAnimator(null);
        mCommentRv.setVerticalFadingEdgeEnabled(true);
        mCommentRv.setFadingEdgeLength(25);
//        resetHeight();
        mCommentRv.setOnTouchListener(
                new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        //            MyLog.d(TAG, "setOnTouchListener,event:" + event.getAction());
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                UserActionEvent userActionEvent = new UserActionEvent(UserActionEvent.EVENT_TYPE_TOUCH_DOWN_COMMENT_RC, 0, 0);
                                EventBus.getDefault().post(userActionEvent);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                break;
                            case MotionEvent.ACTION_UP:
                                break;
                        }
                        return false;
                    }
                });
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                MyLog.d(TAG, "onScrollStateChangd,newState:" + newState + ",mOnBottom:" + mOnBottom);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    // 闲置状态
                    mDraging = false;
                    // 停下来判断是否是最后一个,这里忽然有次不能到底了会有bug
                    // 如果最后一个可见的元素==列表中最后一个元素，则认为到底了,
                    int firstVisiblePosition = mLayoutManager.findFirstVisibleItemPosition();
                    MyLog.d(TAG, "onScrollStateChangd firstVisiblePosition :" + firstVisiblePosition);
                    if (firstVisiblePosition == 0) {
                        setOnBottom("onScrollStateChanged", true);
                    } else {
                        setOnBottom("onScrollStateChanged", false);
                    }
//                    if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
//                        setOnBottom("onScrollStateChanged", true);
//                    } else {
//                        mOnBottom = false;
//                    }
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    // 手动拖着滑动
                    mDraging = true;
                } else {
                    // 自动滑动
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                changeAlpha();
            }
        };
        mCommentRv.addOnScrollListener(mOnScrollListener);
        mCommentRv.setHasFixedSize(true);
        mAdapter = new LiveCommentRecyclerAdapter(context);
        mCommentRv.setAdapter(mAdapter);
        mAdapter.setLiveCommentNameClickListener(new LiveCommentRecyclerAdapter.LiveCommentNameClickListener() {
            @Override
            public void onClickName(long uid) {
                if (null != mNameViewClickListener) {
                    mNameViewClickListener.onClickName(uid);
                } else {
                    if (CommonUtils.isFastDoubleClick(200)) {
                        return;
                    }
                    UserActionEvent userActionEvent = new UserActionEvent(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, uid, uid);
                    EventBus.getDefault().post(userActionEvent);
                }
            }

            @Override
            public void onClickComment(String schemaUrl) {
                EventBus.getDefault().post(new JumpSchemeEvent(schemaUrl));
            }
        });

        mMoveToLastItemIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setOnBottom("mMoveToLastItemIv", true);
            }
        });
    }

    private void setOnBottom(String from, boolean onBottom) {
        MyLog.d(TAG, "onBottom:" + this.mOnBottom + "-->" + onBottom + " from:" + from);
        if (this.mOnBottom != onBottom) {
            this.mOnBottom = onBottom;
            if (mOnBottom) {
                if (mHasDataUpdate) {
                    refreshComment(true);
                }
                mCommentRv.smoothScrollToPosition(0);
                mMoveToLastItemIv.setVisibility(GONE);
                mHasMore = 0;
                if (mRoomChatMsgManager != null) {
                    mRoomChatMsgManager.updateMaxSize(mRoomChatMsgManager.getInitMaxSize());
                }
            }
            // 不在底部不需要更新数据
        }
    }

    // 上一次设定列表数据的时间
    private long mLastSetCommentListTs = 0;

    private List<CommentModel> mDataList;

    //如果是之前是到底了，则我们自动也滚到底
    public void setDataSourceOnMainThread(List<CommentModel> dataList) {
        if (mAdapter != null && dataList != null) {
            // 优化一下，不要每次都remove message
            mDataList = dataList;
            mHasDataUpdate = true;
            long now = System.currentTimeMillis();
            if (now - mLastSetCommentListTs > 200) {
                refreshComment();
            }
        }
    }

    private void refreshComment() {
        refreshComment(false);
    }

    private Runnable afterRefresh = new Runnable() {
        @Override
        public void run() {
            mHasDataUpdate = false;
            mCommentRv.smoothScrollToPosition(0);
        }
    };


    private void refreshComment(boolean force) {
        if (mAdapter != null && mDataList != null) {
            MyLog.d(TAG, "setLiveCommentList, dataList.size:" + mDataList.size() + ",force:" + force);
            mLastSetCommentListTs = System.currentTimeMillis();
            if (!mCommentRv.isComputingLayout()) {
                if (force) {
                    for (CommentModel model : mDataList) {
                        if (model.getCommentColor() == R.color.white) {
                            model.setCommentColor(R.color.color_black_trans_80);
                            model.setNameColor(R.color.color_black_trans_40);
                        }
                    }
                    mAdapter.setLiveCommentList(mDataList, afterRefresh);
                } else if (mOnBottom && this.getVisibility() == VISIBLE && !mDraging) {
                    for (CommentModel model : mDataList) {
                        if (model.getCommentColor() == R.color.white) {
                            model.setCommentColor(R.color.color_black_trans_80);
                            model.setNameColor(R.color.color_black_trans_40);
                        }
                    }
                    mAdapter.setLiveCommentList(mDataList, afterRefresh);
                } else {

                }
            }
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            if (mHasDataUpdate) {
                refreshComment();
            }
        }
    }

    @Override
    public void onActivityDestroy() {
        this.mToken = "";
        mHandler.removeCallbacksAndMessages(null);
//        EventBus.getDefault().unregister(this);
        mCommentRv.removeOnScrollListener(mOnScrollListener);
        GetConfigManager.getInstance().clearMedalMap();
        CommentLevelOrLikeCache.clear();
        CommentVipLevelIconCache.clear();
        CommentVfansLevelCache.clear();
    }

    @Override
    public void onActivityCreate() {
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this);
//        }
    }

    public LiveRoomChatMsgManager getmRoomChatMsgManager() {
        return mRoomChatMsgManager;
    }

    public void setRoomChatMsgManager(LiveRoomChatMsgManager mRoomChatMsgManager) {
        this.mRoomChatMsgManager = mRoomChatMsgManager;
    }

    public void setIsGameLive(boolean isGameLive) {
        mAdapter.setIsGameLive(isGameLive);
//        mEnterRoom.setIsGameLive(isGameLive);
    }

    public void setNameViewClickListener(LiveCommentRecyclerAdapter.LiveCommentNameClickListener nameViewClickListener) {
        mNameViewClickListener = nameViewClickListener;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    private void adjustLayoutForOrient() {
//        LayoutParams layoutParams = (LayoutParams) getLayoutParams();
//        if (mIsLandscape) {
//            layoutParams.width = COMMENT_WIDTH_LANDSCAPE;
//            layoutParams.height = COMMENT_HEIGHT_LANDSCAPE;
//            layoutParams.rightMargin = 0;
//            setPadding(COMMENT_PADDING_LEFT, 0, COMMENT_PADDING_RIGHT, PORTRAIT_COMMENT_PADDING_BOTTOM);
//        } else {
//            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//            layoutParams.height = COMMENT_HEIGHT_PORTRAIT;
//            layoutParams.rightMargin = Math.max(mExtraRightMargin, COMMENT_MARGIN_PORTRAIT);
//            setPadding(COMMENT_PADDING_LEFT, 0, COMMENT_PADDING_RIGHT, PORTRAIT_COMMENT_PADDING_BOTTOM);
//        }
//        setLayoutParams(layoutParams);
    }

    public void onOrientation(boolean isLandscape) {
        mIsLandscape = isLandscape;
        adjustLayoutForOrient();
    }

    public void reset() {
        mLastSetCommentListTs = 0;
        mOnBottom = true;
        mHasDataUpdate = false;
        if (mMoveToLastItemIv != null && mMoveToLastItemIv.getVisibility() != View.GONE) {
            mMoveToLastItemIv.setVisibility(View.GONE);
            mHasMore = 0;
        }
        mDataList.clear();
        CommentVfansLevelCache.clear();
        refreshComment();
    }


    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @NonNull
            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameLiveCommentView.this;
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                WatchGameLiveCommentView.this.onOrientation(isLandscape);
            }

            @Override
            public void onCommentRefreshEvent(CommentRefreshEvent event) {
                if (event != null && event.barrageMsgs != null && event.token.equals(mToken)) {
                    setDataSourceOnMainThread(event.barrageMsgs);
                    if (event.needManualMoveToLast) {
                        setOnBottom("onEventMainThread", true);
                    }
                    if (!mOnBottom) {
                        mHasMore++;
                        mMoveToLastItemIv.setVisibility(VISIBLE);
                        String s = mHasMore > 99 ? "99+" : String.valueOf(mHasMore);
                        mMoveToLastItemIv.setText(getResources().getQuantityString(R.plurals.more_comment_text, mHasMore, s));
                        if (mRoomChatMsgManager != null) {
                            mRoomChatMsgManager.updateMaxSize(Integer.MAX_VALUE);
                        }
                    }
                }
            }

            @Override
            public void setRightMargin(int rightMargin) {
                LayoutParams layoutParams =
                        (LayoutParams) WatchGameLiveCommentView.this.getLayoutParams();
                if (layoutParams.rightMargin != rightMargin) {
                    if (!mIsLandscape && layoutParams.rightMargin > rightMargin) {
                        mAdapter.notifyDataSetChanged();
                    }
                    layoutParams.rightMargin = rightMargin;
                    WatchGameLiveCommentView.this.setLayoutParams(layoutParams);
                }
            }

            @Override
            public boolean isLandscape() {
                return WatchGameLiveCommentView.this.mIsLandscape;
            }

            @Override
            public void destroy() {
                mToken = "";
                mCommentRv.removeOnScrollListener(mOnScrollListener);
            }

            @Override
            public void dropHeight(boolean isDrop) {
                WatchGameLiveCommentView.this.dropHeight(isDrop);
            }
        }
        return new ComponentView();
    }

    private void dropHeight(boolean isDrop) {
        LayoutParams layoutParams = (LayoutParams) getLayoutParams();
        if (isDrop) {
            layoutParams.width = COMMENT_WIDTH_LANDSCAPE;
            layoutParams.height = COMMENT_HEIGHT_LANDSCAPE;
            layoutParams.rightMargin = 0;
            setPadding(COMMENT_PADDING_LEFT, 0, COMMENT_PADDING_RIGHT, PORTRAIT_COMMENT_PADDING_BOTTOM);
        } else {
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = COMMENT_HEIGHT_PORTRAIT;
            layoutParams.rightMargin = Math.max(mExtraRightMargin, COMMENT_MARGIN_PORTRAIT);
            setPadding(COMMENT_PADDING_LEFT, 0, COMMENT_PADDING_RIGHT, PORTRAIT_COMMENT_PADDING_BOTTOM);
        }
        setLayoutParams(layoutParams);
    }


    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy, IOrientationListener {
        /**
         * 新的消息到来
         */
        void onCommentRefreshEvent(CommentRefreshEvent event);

        /**
         * 右边距
         */
        void setRightMargin(int rightMargin);

        /**
         * 查询横竖屏状态
         */
        boolean isLandscape();

        /**
         * 销毁对象
         */
        void destroy();

        /**
         * 高度是否调小
         */
        void dropHeight(boolean isDrop);
    }

}