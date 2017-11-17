package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IOrientationListener;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.common.barrage.event.CommentRefreshEvent;
import com.wali.live.common.barrage.view.MyListView;
import com.wali.live.common.barrage.view.adapter.LiveCommentRecyclerAdapter;
import com.wali.live.common.model.CommentModel;
import com.wali.live.event.UserActionEvent;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static com.wali.live.component.view.Utils.$click;

/**
 * Created by yangli on 2017/03/02.
 *
 * @module 弹幕区视图
 */
public class LiveCommentView extends RelativeLayout implements View.OnClickListener,
        IComponentView<LiveCommentView.IPresenter, LiveCommentView.IView>, IOrientationListener {
    private static final String TAG = "LiveCommentView1";

    // this value should be adjust when ui design is changed
    private static final int WIDTH_LANDSCAPE = GlobalData.screenHeight >> 1; // 弹幕区域尺寸
    private static final int HEIGHT_PORTRAIT = DisplayUtils.dip2px(160.33f);
    private static final int HEIGHT_LANDSCAPE = DisplayUtils.dip2px(115f);

    public static final int LARGE_MARGIN_PORTRAIT = DisplayUtils.dip2px(110.67f);  // 连麦小窗存在时，弹幕区域右边距
    public static final int NORMAL_MARGIN_PORTRAIT = DisplayUtils.dip2px(70f);     // 连麦小窗不存在时，弹幕区域右边距

    @Nullable
    protected IPresenter mPresenter;
    private LiveCommentRecyclerAdapter.NameClickListener mNameViewClickListener = null;

    private LiveCommentRecyclerAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView.OnScrollListener mOnScrollListener;

    private String mToken;
    private boolean mDragging = false;
    private boolean mOnBottom = true;
    private boolean mHasDataUpdate = false; // 时候有数据可更新
    private boolean mIsLandscape = false;

    private ImageView mMoveToLastItemIv;     //点击回到最底部
    private MyListView mCommentRv;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ctrl_btn) {
            setOnBottom("mMoveToLastItemIv", true);
        }
    }

    public void setIsGameLive(boolean isGameLive) {
        mAdapter.setIsGameLive(isGameLive);
    }

    public void setNameViewClickListener(
            LiveCommentRecyclerAdapter.NameClickListener nameViewClickListener) {
        mNameViewClickListener = nameViewClickListener;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public LiveCommentView(Context context) {
        this(context, null);
    }

    public LiveCommentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveCommentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.live_comment_view, this);

        mMoveToLastItemIv = $(R.id.ctrl_btn);
        mCommentRv = $(R.id.my_list_view);

        $click(mMoveToLastItemIv, this);

        setupCommentView();
    }

    private void setupCommentView() {
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        mCommentRv.setLayoutManager(mLayoutManager);
        mCommentRv.setItemAnimator(null);
        mCommentRv.setVerticalFadingEdgeEnabled(true);
        mCommentRv.setFadingEdgeLength(25);
        mCommentRv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        UserActionEvent.post(UserActionEvent.EVENT_TYPE_TOUCH_DOWN_COMMENT_RC, 0, 0);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                MyLog.d(TAG, "onScrollStateChanged, newState:" + newState + ", mOnBottom:" + mOnBottom);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    mDragging = false;
                    // 停下来判断是否是最后一个
                    int firstVisiblePosition = mLayoutManager.findFirstVisibleItemPosition();
                    MyLog.d(TAG, "onScrollStateChanged firstVisiblePosition :" + firstVisiblePosition);
                    if (firstVisiblePosition == 0) {
                        setOnBottom("onScrollStateChanged", true);
                    } else {
                        setOnBottom("onScrollStateChanged", false);
                    }
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) { //用户滑动的话则动态跟新maxSize
                    mDragging = true;
                }
            }
        };
        mCommentRv.addOnScrollListener(mOnScrollListener);
        mCommentRv.setHasFixedSize(true);
        mAdapter = new LiveCommentRecyclerAdapter(getContext());
        mCommentRv.setAdapter(mAdapter);
        mAdapter.setLiveCommentNameClickListener(new LiveCommentRecyclerAdapter.NameClickListener() {
            @Override
            public void onClickName(long uid) {
                if (mNameViewClickListener != null) {
                    mNameViewClickListener.onClickName(uid);
                } else {
                    if (CommonUtils.isFastDoubleClick(200)) {
                        return;
                    }
                    UserActionEvent.post(UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, uid, uid);
                }
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
                EventBus.getDefault().post(new LiveCommentView.LiveCommentStateEvent(
                        LiveCommentView.LiveCommentStateEvent.TYPE_UPDATE_TO_DEFAULT_SIZE));
            } else {
                // 不在底部不需要更新数据
                mMoveToLastItemIv.setVisibility(VISIBLE);
            }
        }
    }

    // 上一次设定列表数据的时间
    private long mLastSetCommentListTs = 0;
    private List<CommentModel> mDataList;

    // 如果是之前是到底了，则我们自动也滚到底
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

    private void refreshComment(boolean force) {
        if (mAdapter != null && mDataList != null) {
            MyLog.d(TAG, "setLiveCommentList, dataList.size:" + mDataList.size() + ",force:" + force);
            mLastSetCommentListTs = System.currentTimeMillis();
            if (force) {
                mAdapter.setCommentList(mDataList);
                mHasDataUpdate = false;
                mCommentRv.scrollToPosition(0);
            } else if (mOnBottom && this.getVisibility() == VISIBLE && !mDragging) {
                mAdapter.setCommentList(mDataList);
                mHasDataUpdate = false;
                mCommentRv.scrollToPosition(0);
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
    public void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
        if (mIsLandscape) {
            layoutParams.width = WIDTH_LANDSCAPE;
            layoutParams.height = HEIGHT_LANDSCAPE;
        } else {
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.height = HEIGHT_PORTRAIT;
        }
        setLayoutParams(layoutParams);
    }

    public void reset() {
        mLastSetCommentListTs = 0;
        mOnBottom = true;
        mHasDataUpdate = false;
        if (mMoveToLastItemIv != null && mMoveToLastItemIv.getVisibility() == View.VISIBLE) {
            mMoveToLastItemIv.setVisibility(View.INVISIBLE);
        }
        if (mDataList != null) {
            mDataList.clear();
        }
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
                return (T) LiveCommentView.this;
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                LiveCommentView.this.onOrientation(isLandscape);
            }

            @Override
            public void onCommentRefreshEvent(CommentRefreshEvent event) {
                if (event != null && event.barrageMsgs != null && event.token.equals(mToken)) {
                    setDataSourceOnMainThread(event.barrageMsgs);
                    if (event.needManualMoveToLast) {
                        setOnBottom("onEventMainThread", true);
                    }
                }
            }

            @Override
            public void setRightMargin(int rightMargin) {
                RelativeLayout.LayoutParams layoutParams =
                        (RelativeLayout.LayoutParams) LiveCommentView.this.getLayoutParams();
                if (layoutParams.rightMargin != rightMargin) {
                    if (!mIsLandscape && layoutParams.rightMargin > rightMargin) {
                        mAdapter.notifyDataSetChanged();
                    }
                    layoutParams.rightMargin = rightMargin;
                    LiveCommentView.this.setLayoutParams(layoutParams);
                }
            }

            @Override
            public boolean isLandscape() {
                return LiveCommentView.this.mIsLandscape;
            }

            @Override
            public void destroy() {
                mToken = "";
                mCommentRv.removeOnScrollListener(mOnScrollListener);
            }
        }
        return new ComponentView();
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
    }

    public static class LiveCommentStateEvent {
        public static final int TYPE_UPDATE_TO_MAX_SIZE = 1;
        public static final int TYPE_UPDATE_TO_DEFAULT_SIZE = 2;
        public int type;

        public LiveCommentStateEvent(int type) {
            this.type = type;
        }
    }
}