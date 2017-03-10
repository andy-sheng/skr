package com.wali.live.common.barrage.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.wali.live.base.BaseEvent;
import com.wali.live.common.barrage.event.CommentRefreshEvent;
import com.wali.live.common.barrage.view.adapter.LiveCommentRecyclerAdapter;
import com.wali.live.common.model.CommentModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * @module 直播间弹幕评论
 */
@Deprecated
public class LiveCommentView extends RelativeLayout implements IBindActivityLIfeCycle {
    private static final String TAG = LiveCommentView.class.getSimpleName();
    private static final int IDLE_STATUS_MAX_TIME = 30 * 1000;

    private LiveCommentRecyclerAdapter mAdapter;
    private boolean mOnBottom = true;
    private boolean mHasDataUpdate = false; // 时候有数据可更新

    private String mToken;

    public ImageView mMoveToLastItemIv;     //点击回到最底部

    public MyListView mCommentRv;

    private LiveCommentRecyclerAdapter.LiveCommentNameClickListener mNameViewClickListener = null;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean mDraging = false;

    private boolean mHasAdjust = false;

    public LiveCommentView(Context context) {
        super(context);
        initContentView(context);
    }

    public LiveCommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initContentView(context);
    }

    public LiveCommentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initContentView(context);
    }

    private RecyclerView.OnScrollListener mOnScrollListener;

    private LinearLayoutManager mLayoutManager;

    private void initContentView(Context context) {
        inflate(context, R.layout.livecomment_recycler_layout, this);

        mMoveToLastItemIv = (ImageView) findViewById(R.id.moveTolastIv);

        mCommentRv = (MyListView) findViewById(R.id.barrage_comment_list_view);
        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);
        mCommentRv.setLayoutManager(mLayoutManager);
        mCommentRv.setItemAnimator(null);
        mCommentRv.setVerticalFadingEdgeEnabled(true);
        mCommentRv.setFadingEdgeLength(25);
        mCommentRv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //            MyLog.d(TAG, "setOnTouchListener,event:" + event.getAction());
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        BaseEvent.UserActionEvent userActionEvent = new BaseEvent.UserActionEvent(BaseEvent.UserActionEvent.EVENT_TYPE_TOUCH_DOWN_COMMENT_RC, 0, 0);
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
                    mDraging = false;

                    // 个数小于等于0就直接返回
                    if (mAdapter.getItemCount() <= 0) {
                        return;
                    }

                    // 停下来判断是否是最后一个
                    int firstVisiblePosition = mLayoutManager.findFirstVisibleItemPosition();
                    MyLog.d(TAG, "onScrollStateChangd firstVisiblePosition :" + firstVisiblePosition);
                    if (firstVisiblePosition == 0) {
                        setOnBottom("onScrollStateChanged", true);
                    } else {
                        setOnBottom("onScrollStateChanged", false);
                    }
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) { //用户滑动的话则动态跟新maxSize
                    mDraging = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
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
                    BaseEvent.UserActionEvent userActionEvent = new BaseEvent.UserActionEvent(BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, uid, uid);
                    EventBus.getDefault().post(userActionEvent);
                }
            }
        });

        mMoveToLastItemIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
                mCommentRv.scrollToPosition(0);
                // INVISIBLE占位
                mMoveToLastItemIv.setVisibility(INVISIBLE);
                EventBus.getDefault().post(new LiveCommentStateEvent(LiveCommentStateEvent.TYPE_UPDATE_TO_DEFAULT_SIZE));
            } else {
                // 不在底部不需要更新数据
                mMoveToLastItemIv.setVisibility(VISIBLE);
            }
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
            mCommentRv.scrollToPosition(0);
        }
    };

    private void refreshComment(boolean force) {
        if (mAdapter != null && mDataList != null) {
            MyLog.d(TAG, "setLiveCommentList, dataList.size:" + mDataList.size() + ",force:" + force);
            mLastSetCommentListTs = System.currentTimeMillis();
            if (force) {
                mAdapter.setLiveCommentList(mDataList, afterRefresh);
            } else if (mOnBottom && this.getVisibility() == VISIBLE && !mDraging) {
                mAdapter.setLiveCommentList(mDataList, afterRefresh);
            } else {

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
        EventBus.getDefault().unregister(this);
        mCommentRv.removeOnScrollListener(mOnScrollListener);
    }

    @Override
    public void onActivityCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    // this value should be adjust when ui design is changed
    private static final int COMMENT_VIEW_HEIGHT_IN_PORTRAIT = DisplayUtils.dip2px(160.33f);        // 弹幕区域高度
    private static final int COMMENT_VIEW_HEIGHT_IN_LANDSCAPE = DisplayUtils.dip2px(115f);

    private static final int COMMENT_VIEW_LARGE_MARGIN_IN_PORTRAIT = DisplayUtils.dip2px(110.67f);  // 连麦小窗存在时，弹幕区域右边距
    private static final int COMMENT_VIEW_LARGE_MARGIN_IN_LANDSCAPE = DisplayUtils.dip2px(240f);

    private static final int COMMENT_VIEW_NORMAL_MARGIN_IN_PORTRAIT = DisplayUtils.dip2px(70f);     // 连麦小窗不存在时，弹幕区域右边距
    private static final int COMMENT_VIEW_NORMAL_MARGIN_IN_LANDSCAPE = DisplayUtils.dip2px(140f);

    private static final int MOVE_BUTTON_LARGE_MARGIN = DisplayUtils.dip2px(23.33f);                // 弹幕置底按钮距离弹幕区域右侧的右边距
    private static final int MOVE_BUTTON_NORMAL_MARGIN = DisplayUtils.dip2px(3.33f);

    private boolean mIsLandscape = false;
    private boolean mIsNormalRightMargin = true;

    private void adjustLayoutForOrient(int height) {
        RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams) getLayoutParams();
        RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) mMoveToLastItemIv.getLayoutParams();
        lp1.height = height > 0 ? height : lp1.height; // none-positive height means not need to adjust height
        if (mIsNormalRightMargin) {
            lp1.rightMargin = mIsLandscape ? COMMENT_VIEW_NORMAL_MARGIN_IN_LANDSCAPE : COMMENT_VIEW_NORMAL_MARGIN_IN_PORTRAIT;
            lp2.rightMargin = MOVE_BUTTON_NORMAL_MARGIN;
        } else {
            lp1.rightMargin = mIsLandscape ? COMMENT_VIEW_LARGE_MARGIN_IN_LANDSCAPE : COMMENT_VIEW_LARGE_MARGIN_IN_PORTRAIT;
            lp2.rightMargin = MOVE_BUTTON_LARGE_MARGIN;
        }
        mMoveToLastItemIv.setLayoutParams(lp2);
        setLayoutParams(lp1);
    }

    public void adjustToLargeRightMargin() {
        MyLog.d(TAG, "adjustToLargeRightMargin");
        mIsNormalRightMargin = false;
        adjustLayoutForOrient(-1);
    }

    public void adjustToNormalRightMargin() {
        MyLog.d(TAG, "adjustToNormalRightMargin");
        mIsNormalRightMargin = true;
        adjustLayoutForOrient(-1);
    }

    public void setIsGameLive(boolean isGameLive) {
        mAdapter.setIsGameLive(isGameLive);
    }

    public void setNameViewClickListener(LiveCommentRecyclerAdapter.LiveCommentNameClickListener nameViewClickListener) {
        mNameViewClickListener = nameViewClickListener;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CommentRefreshEvent event) {
        if (event != null && event.barrageMsgs != null && event.token.equals(mToken)) {
            if (!mHasAdjust) {
                try {
                    orientInner();
                    mHasAdjust = true;
                } catch (Exception e) {
                }
            }
            setDataSourceOnMainThread(event.barrageMsgs);
            if (event.needManualMoveToLast) {
                setOnBottom("onEventMainThread", true);
            }
        }
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public static class LiveCommentStateEvent {
        public static final int TYPE_UPDATE_TO_MAX_SIZE = 1;
        public static final int TYPE_UPDATE_TO_DEFAULT_SIZE = 2;
        public int type;

        public LiveCommentStateEvent(int type) {
            this.type = type;
        }
    }

    private static final int LANDSCAPE_WIDTH = GlobalData.screenHeight >> 1;

    public void orientComment(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        orientInner();
    }

    private void orientInner() {
        adjustLayoutForOrient(mIsLandscape ? COMMENT_VIEW_HEIGHT_IN_LANDSCAPE : COMMENT_VIEW_HEIGHT_IN_PORTRAIT);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
        layoutParams.width = mIsLandscape ? LANDSCAPE_WIDTH : LayoutParams.MATCH_PARENT;
        setLayoutParams(layoutParams);
    }

    public void reset() {
        mLastSetCommentListTs = 0;
        mOnBottom = true;
        mHasDataUpdate = false;
        if (mMoveToLastItemIv != null && mMoveToLastItemIv.getVisibility() == View.VISIBLE) {
            mMoveToLastItemIv.setVisibility(View.INVISIBLE);
        }
    }
}
