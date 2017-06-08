package com.wali.live.watchsdk.videodetail.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.rx.RxRetryAssist;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.proto.Feeds;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.feeds.FeedsCommentUtils;
import com.wali.live.watchsdk.videodetail.adapter.DetailCommentAdapter;
import com.wali.live.watchsdk.videodetail.view.DetailCommentView;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.ComponentController.MSG_COMMENT_TOTAL_CNT;
import static com.wali.live.component.ComponentController.MSG_FOLD_INFO_AREA;
import static com.wali.live.component.ComponentController.MSG_SEND_COMMENT;
import static com.wali.live.component.ComponentController.MSG_SHOW_COMMENT_INPUT;
import static com.wali.live.watchsdk.feeds.FeedsCommentUtils.FEEDS_COMMENT_PULL_TYPE_ALL_HYBIRD;

/**
 * Created by yangli on 2017/06/02.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 评论列表页表现
 */
public class DetailCommentPresenter extends ComponentPresenter<DetailCommentView.IView>
        implements DetailCommentView.IPresenter {
    private static final String TAG = "DetailCommentPresenter";

    private final static int PULL_COMMENT_LIMIT = 30; // 单次拉取评论的条数

    private RoomBaseDataModel mMyRoomData;

    private Subscription mPullSubscription;
    private int mTotalCnt = 0;
    private boolean mIsReverse = false;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final PullCommentHelper mNewerPuller = new PullCommentHelper(true);
    private final PullCommentHelper mOlderPuller = new PullCommentHelper(false);

    public DetailCommentPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel roomData) {
        super(componentController);
        mMyRoomData = roomData;
        registerAction(MSG_SEND_COMMENT);
    }

    @Override
    public void destroy() {
        super.destroy();
        mExecutor.shutdownNow();
    }

    private void onPullMoreDone(PullCommentHelper helper) {
        mView.onShowLoadingView(false);
        if (helper != null) {
            if (mTotalCnt != helper.mTotalCnt) {
                mTotalCnt = helper.mTotalCnt;
                mComponentController.onEvent(MSG_COMMENT_TOTAL_CNT, new Params().putItem(mTotalCnt));
            }
            mView.onPullCommentDone(helper.mHotList, helper.mAllList, !helper.mIsAsc);
            if (!helper.mHasMore && helper.mCanShowNoMore) {
                helper.mCanShowNoMore = false;
                ToastUtils.showToast(mView.getRealView().getContext(), R.string.feeds_comment_no_more);
            }
        } else {
            mView.onPullCommentFailed();
        }
    }

    @Override
    public void pullNewerComments() {
        if (mPullSubscription != null && !mPullSubscription.isUnsubscribed()) {
            return;
        }
        if (!mNewerPuller.mHasMore) {
            return;
        }
        MyLog.w(TAG, "pullNewerComments");
        mView.onShowLoadingView(true);
        mPullSubscription = Observable.just(0)
                .map(new Func1<Integer, PullCommentHelper>() {
                    @Override
                    public PullCommentHelper call(Integer integer) {
                        return mNewerPuller.pullMore(PULL_COMMENT_LIMIT);
                    }
                })
                .subscribeOn(Schedulers.from(mExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<PullCommentHelper>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<PullCommentHelper>() {
                    @Override
                    public void call(PullCommentHelper helper) {
                        if (mView == null || mIsReverse) { // 当前列表为反向显示模式则不处理
                            return;
                        }
                        onPullMoreDone(helper);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncFeedsInfo failed, exception=" + throwable);
                    }
                });
    }

    @Override
    public void pullOlderComments() {
        if (mPullSubscription != null && !mPullSubscription.isUnsubscribed()) {
            return;
        }
        if (!mOlderPuller.mHasMore) {
            return;
        }
        MyLog.w(TAG, "pullOlderComments");
        mView.onShowLoadingView(true);
        mPullSubscription = Observable.just(0)
                .map(new Func1<Integer, PullCommentHelper>() {
                    @Override
                    public PullCommentHelper call(Integer integer) {
                        return mOlderPuller.pullMore(PULL_COMMENT_LIMIT);
                    }
                })
                .subscribeOn(Schedulers.from(mExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<PullCommentHelper>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<PullCommentHelper>() {
                    @Override
                    public void call(PullCommentHelper helper) {
                        if (mView == null) { // 当前列表为正向显示模式则不处理
                            return;
                        }
                        onPullMoreDone(helper);
                        if (helper != null && !mIsReverse) {
                            mIsReverse = true;
                            mView.setReverseLayout(true);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "syncFeedsInfo failed, exception=" + throwable);
                    }
                });
    }

    @Override
    public void showCommentInput(DetailCommentAdapter.CommentItem commentItem) {
        if (commentItem == null || commentItem.fromUid == MyUserInfoManager.getInstance().getUuid()) {
            // 不能回复自己的评论
            return;
        }
        mComponentController.onEvent(MSG_SHOW_COMMENT_INPUT, new Params()
                .putItem(mMyRoomData.getRoomId()).putItem(commentItem));
    }

    @Override
    public void sendComment(final String feedsId, final DetailCommentAdapter.CommentItem commentItem) {
        if (TextUtils.isEmpty(mMyRoomData.getRoomId()) || TextUtils.isEmpty(feedsId) ||
                !mMyRoomData.getRoomId().equals(feedsId)) {
            return;
        }
        final long ownerId = mMyRoomData.getUid();
        Observable.just(0)
                .map(new Func1<Integer, DetailCommentAdapter.CommentItem>() {
                    @Override
                    public DetailCommentAdapter.CommentItem call(Integer integer) {
                        DetailCommentAdapter.CommentItem result = FeedsCommentUtils.sendComment(
                                commentItem, ownerId, feedsId, 0, 0);
                        mOlderPuller.addSendItem(result);
                        return result;
                    }
                })
                .retryWhen(new RxRetryAssist()) // 增加一次重试
                .subscribeOn(Schedulers.from(mExecutor))
                .compose(this.<DetailCommentAdapter.CommentItem>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DetailCommentAdapter.CommentItem>() {
                    @Override
                    public void call(DetailCommentAdapter.CommentItem commentItem) {
                        if (mView == null) {
                            return;
                        }
                        if (commentItem == null) {
                            ToastUtils.showToast(mView.getRealView().getContext(), R.string.commend_failed);
                            return;
                        }
                        ++mTotalCnt;
                        mComponentController.onEvent(MSG_COMMENT_TOTAL_CNT, new Params().putItem(mTotalCnt));
                        if (!mIsReverse) {
                            if (mPullSubscription != null && !mPullSubscription.isUnsubscribed()) {
                                mPullSubscription.unsubscribe();
                            }
                            pullOlderComments();
                        } else {
                            mView.addSendComment(commentItem);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "sendComment failed, exception=" + throwable);
                        ToastUtils.showToast(mView.getRealView().getContext(), R.string.commend_failed);
                    }
                });
    }

    @Override
    public void foldInfoArea() {
        mComponentController.onEvent(MSG_FOLD_INFO_AREA);
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                case MSG_SEND_COMMENT:
                    sendComment((String) params.getItem(0), (DetailCommentAdapter.CommentItem)
                            params.getItem(1));
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    public class PullCommentHelper {
        private long mCommentTs;
        private boolean mIsAsc;
        private int mTotalCnt;
        private volatile boolean mHasMore = true;
        private volatile boolean mCanShowNoMore;

        private Deque<DetailCommentAdapter.CommentItem> mHotList = new ArrayDeque<>();
        private Deque<DetailCommentAdapter.CommentItem> mAllList = new ArrayDeque<>();

        public PullCommentHelper(boolean isAsc) {
            mIsAsc = isAsc;
        }

        private void addSendItem(DetailCommentAdapter.CommentItem commentItem) {
            if (commentItem != null) {
                if (mCommentTs == 0) {
                    mCommentTs = commentItem.createTime;
                }
                mAllList.addFirst(commentItem);
            }
        }

        private PullCommentHelper pullMore(final int limit) {
            Feeds.QueryFeedCommentsResponse rsp = FeedsCommentUtils.fetchFeedsCommentFromServer(
                    mMyRoomData.getRoomId(), mCommentTs, limit, false, mIsAsc,
                    FEEDS_COMMENT_PULL_TYPE_ALL_HYBIRD, true);
            if (rsp == null && rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                return null;
            }
            mCommentTs = rsp.getLastTs();
            mHasMore = rsp.getHasMore();
            Feeds.FeedComment feedComment = rsp.getFeedComment();
            if (feedComment == null) {
                return this;
            }
            mTotalCnt = feedComment.getTotal();
            List<Feeds.CommentInfo> commentInfoList = feedComment.getCommentInfosList();
            if (commentInfoList == null) {
                return this;
            }
            mCanShowNoMore = !commentInfoList.isEmpty();
            Deque<DetailCommentAdapter.CommentItem> outList;
            for (Feeds.CommentInfo commentInfo : commentInfoList) {
                DetailCommentAdapter.CommentItem commentItem = new DetailCommentAdapter.CommentItem(
                        commentInfo.getCommentId(),
                        commentInfo.getFromUserLevel(),
                        commentInfo.getFromUid(),
                        commentInfo.getFromNickname(),
                        commentInfo.getToUid(),
                        commentInfo.getToNickname(),
                        commentInfo.getContent());
                outList = commentInfo.getIsGood() ? mHotList : mAllList;
                outList.addLast(commentItem);
            }
            return this;
        }
    }
}
