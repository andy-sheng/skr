package com.wali.live.watchsdk.videodetail.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
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

    public DetailCommentPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel roomData) {
        super(componentController);
        mMyRoomData = roomData;
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
                        for (Feeds.CommentInfo commentInfo : commentInfoList) {
                            feedsCommentList.commentItemList.add(
                                    new DetailCommentAdapter.CommentItem(
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
                            mView.onPullCommentDone(feedsCommentList.commentItemList);
                            if (feedsCommentList.commentItemList.isEmpty() && !mHasMore) {
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
        private List<DetailCommentAdapter.CommentItem> commentItemList = new ArrayList<>();

        public FeedsCommentList(long lastTs, boolean hasMore) {
            this.lastTs = lastTs;
            this.hasMore = hasMore;
        }
    }
}
