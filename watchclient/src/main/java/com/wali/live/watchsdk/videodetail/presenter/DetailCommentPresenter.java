package com.wali.live.watchsdk.videodetail.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.rx.RxRetryAssist;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.proto.Feeds;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.feeds.FeedsCommentUtils;
import com.wali.live.watchsdk.videodetail.adapter.DetailCommentAdapter;
import com.wali.live.watchsdk.videodetail.view.DetailCommentView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.ComponentController.MSG_COMMENT_TOTAL_CNT;
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

    private long mCommentTs = 0;
    private int mTotalCnt = 0;
    private boolean mHasMore = true;
    private Subscription mPullSubscription;

    private List<DetailCommentAdapter.CommentItem> mHotList = new ArrayList<>();
    private List<DetailCommentAdapter.CommentItem> mAllList = new ArrayList<>();

    public DetailCommentPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel roomData) {
        super(componentController);
        mMyRoomData = roomData;
        registerAction(MSG_SEND_COMMENT);
    }

    @Override
    public void pullFeedsComment() {
        if (mPullSubscription != null && !mPullSubscription.isUnsubscribed()) {
            return;
        }
        mView.onShowLoadingView(true);
        mPullSubscription = Observable.just(0)
                .map(new Func1<Integer, FeedsCommentList>() {
                    @Override
                    public FeedsCommentList call(Integer integer) {
                        Feeds.QueryFeedCommentsResponse rsp = FeedsCommentUtils.fetchFeedsCommentFromServer(
                                mMyRoomData.getRoomId(), mCommentTs, PULL_COMMENT_LIMIT, false, true,
                                FEEDS_COMMENT_PULL_TYPE_ALL_HYBIRD, true);
                        if (rsp == null && rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
                            return null;
                        }
                        FeedsCommentList feedsCommentList = new FeedsCommentList(rsp.getLastTs(), rsp.getHasMore());
                        Feeds.FeedComment feedComment = rsp.getFeedComment();
                        if (feedComment == null) {
                            return feedsCommentList;
                        }
                        feedsCommentList.totalCnt = feedComment.getTotal();
                        List<Feeds.CommentInfo> commentInfoList = feedComment.getCommentInfosList();
                        if (commentInfoList == null) {
                            return feedsCommentList;
                        }
                        List<DetailCommentAdapter.CommentItem> outList;
                        for (Feeds.CommentInfo commentInfo : commentInfoList) {
                            outList = commentInfo.getIsGood() ? feedsCommentList.hotList : feedsCommentList.allList;
                            outList.add(new DetailCommentAdapter.CommentItem(
                                    commentInfo.getCommentId(),
                                    commentInfo.getFromUserLevel(),
                                    commentInfo.getFromUid(),
                                    commentInfo.getFromNickname(),
                                    commentInfo.getToUid(),
                                    commentInfo.getToNickname(),
                                    commentInfo.getContent()));
                        }
                        return feedsCommentList;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<FeedsCommentList>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<FeedsCommentList>() {
                    @Override
                    public void call(FeedsCommentList feedsCommentList) {
                        if (mView == null) {
                            return;
                        }
                        mView.onShowLoadingView(false);
                        if (feedsCommentList != null) {
                            if (mTotalCnt != feedsCommentList.totalCnt) {
                                mTotalCnt = feedsCommentList.totalCnt;
                                mComponentController.onEvent(MSG_COMMENT_TOTAL_CNT, new Params().putItem(mTotalCnt));
                            }
                            mCommentTs = feedsCommentList.lastTs;
                            mHasMore = feedsCommentList.hasMore;
                            mHotList.addAll(feedsCommentList.hotList);
                            mAllList.addAll(feedsCommentList.allList);
                            mView.onPullCommentDone(mHotList, mAllList);
                            if (feedsCommentList.isEmpty() && !mHasMore) {
                                ToastUtils.showToast(mView.getRealView().getContext(), R.string.feeds_comment_no_more);
                            }
                        } else {
                            mView.onPullCommentFailed();
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
                        return FeedsCommentUtils.sendComment(commentItem, ownerId, feedsId, 0, 0);
                    }
                })
                .retryWhen(new RxRetryAssist()) // 增加一次重试
                .subscribeOn(Schedulers.io())
                .compose(this.<DetailCommentAdapter.CommentItem>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DetailCommentAdapter.CommentItem>() {
                    @Override
                    public void call(DetailCommentAdapter.CommentItem commentItem) {
                        if (commentItem != null) {
                            if (!mHasMore) {
                                ++mTotalCnt;
                                mComponentController.onEvent(MSG_COMMENT_TOTAL_CNT, new Params().putItem(mTotalCnt));
                            } else {
                                pullFeedsComment();
                            }
                        } else {
                            ToastUtils.showToast(mView.getRealView().getContext(), R.string.commend_failed);
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
                    sendComment((String) params.getItem(0), (DetailCommentAdapter.CommentItem) params.getItem(1));
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    public static class FeedsCommentList {
        private long lastTs;
        private int totalCnt;
        private boolean hasMore;
        private List<DetailCommentAdapter.CommentItem> hotList = new ArrayList<>();
        private List<DetailCommentAdapter.CommentItem> allList = new ArrayList<>();

        public boolean isEmpty() {
            return hotList.isEmpty() && allList.isEmpty();
        }

        public FeedsCommentList(long lastTs, boolean hasMore) {
            this.lastTs = lastTs;
            this.hasMore = hasMore;
        }
    }
}
