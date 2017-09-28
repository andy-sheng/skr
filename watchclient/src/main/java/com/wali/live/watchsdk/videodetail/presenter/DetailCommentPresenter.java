package com.wali.live.watchsdk.videodetail.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.SparseArray;

import com.base.dialog.MyAlertDialog;
import com.base.log.MyLog;
import com.base.utils.rx.RxRetryAssist;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.Params;
import com.wali.live.component.presenter.BaseSdkRxPresenter;
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

import static com.wali.live.component.BaseSdkController.MSG_FOLD_INFO_AREA;
import static com.wali.live.component.BaseSdkController.MSG_NEW_FEED_ID;
import static com.wali.live.component.BaseSdkController.MSG_SEND_COMMENT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_COMMENT_INPUT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_PERSONAL_INFO;
import static com.wali.live.component.BaseSdkController.MSG_UPDATE_COMMENT_CNT;
import static com.wali.live.watchsdk.feeds.FeedsCommentUtils.PULL_TYPE_ALL_HYBRID;
import static com.wali.live.watchsdk.feeds.FeedsInfoUtils.FEED_TYPE_DEFAULT;

/**
 * Created by yangli on 2017/06/02.
 *
 * @module 评论列表页表现
 */
public class DetailCommentPresenter extends BaseSdkRxPresenter<DetailCommentView.IView>
        implements DetailCommentView.IPresenter {
    private static final String TAG = "DetailCommentPresenter";

    private final static int PULL_COMMENT_LIMIT = 12; // 单次拉取评论的条数

    private String mFeedId;
    private long mOwnerId;

    private Subscription mPullSubscription;
    private int mTotalCnt = 0;
    private volatile boolean mIsReverse = false;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final PullCommentHelper mNewerPuller = new PullCommentHelper(true);
    private final PullCommentHelper mOlderPuller = new PullCommentHelper(false);

    @Override
    protected final String getTAG() {
        return TAG;
    }

    public DetailCommentPresenter(@NonNull IEventController controller) {
        super(controller);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_SEND_COMMENT);
        registerAction(MSG_NEW_FEED_ID);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
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
                postEvent(MSG_UPDATE_COMMENT_CNT, new Params().putItem(mTotalCnt));
            }
            mView.onUpdateCommentList(helper.mHotList, helper.mAllList, !helper.mIsAsc);
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
            if (mNewerPuller.mCanShowNoMore) {
                mNewerPuller.mCanShowNoMore = false;
                ToastUtils.showToast(mView.getRealView().getContext(), R.string.feeds_comment_no_more);
            }
            return;
        }
        MyLog.w(TAG, "pullNewerComments");
        mView.onShowLoadingView(true);
        final String feedId = mFeedId;
        mPullSubscription = Observable.just(0)
                .map(new Func1<Integer, PullCommentHelper>() {
                    @Override
                    public PullCommentHelper call(Integer integer) {
                        return mNewerPuller.pullMore(feedId, PULL_COMMENT_LIMIT);
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
                        onPullMoreDone(null);
                    }
                });
    }

    @Override
    public void pullOlderComments() {
        if (mPullSubscription != null && !mPullSubscription.isUnsubscribed()) {
            return;
        }
        if (!mOlderPuller.mHasMore) {
            if (mOlderPuller.mCanShowNoMore) {
                mOlderPuller.mCanShowNoMore = false;
                ToastUtils.showToast(mView.getRealView().getContext(), R.string.feeds_comment_no_more);
            }
            return;
        }
        MyLog.w(TAG, "pullOlderComments");
        mView.onShowLoadingView(true);
        final String feedId = mFeedId;
        mPullSubscription = Observable.just(0)
                .map(new Func1<Integer, PullCommentHelper>() {
                    @Override
                    public PullCommentHelper call(Integer integer) {
                        return mOlderPuller.pullMore(feedId, PULL_COMMENT_LIMIT);
                    }
                })
                .map(new Func1<PullCommentHelper, PullCommentHelper>() {
                    @Override
                    public PullCommentHelper call(PullCommentHelper helper) {
                        if (helper == null) {
                            return null;
                        }
                        // 如果反向拉取与之前正向拉取的数据重合了，则表明评论全部拉取完成，合并两个列表
                        if (mNewerPuller.mCommentTs != 0 && mOlderPuller.mCommentTs != 0 &&
                                mNewerPuller.mCommentTs > mOlderPuller.mCommentTs) {
                            while (!mNewerPuller.mHotList.isEmpty()) {
                                DetailCommentAdapter.CommentItem item = mNewerPuller.mHotList.removeLast();
                                if (!mOlderPuller.mHotList.contains(item)) {
                                    mOlderPuller.mHotList.addLast(item);
                                }
                            }
                            while (!mNewerPuller.mAllList.isEmpty()) {
                                DetailCommentAdapter.CommentItem item = mNewerPuller.mAllList.removeLast();
                                if (!mOlderPuller.mAllList.contains(item)) {
                                    mOlderPuller.mAllList.addLast(item);
                                }
                            }
                            mOlderPuller.mHasMore = false;
                        }
                        return mOlderPuller;
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
                        onPullMoreDone(null);
                    }
                });
    }

    public void onNewFeedId(final String feedId, final long ownerId) {
        mOwnerId = ownerId;
        mFeedId = feedId;
        if (mIsReverse) {
            mIsReverse = false;
            mView.setReverseLayout(mIsReverse);
        }
        mNewerPuller.reset();
        mOlderPuller.reset();
        mView.onUpdateCommentList(mNewerPuller.mHotList, mNewerPuller.mAllList, !mNewerPuller.mIsAsc);
        if (mPullSubscription != null && !mPullSubscription.isUnsubscribed()) {
            mPullSubscription.unsubscribe();
            mPullSubscription = null;
        }
        pullNewerComments();
    }

    @Override
    public void showCommentInput(DetailCommentAdapter.CommentItem commentItem) {
        if (commentItem == null || commentItem.fromUid == MyUserInfoManager.getInstance().getUuid()) {
            // 不能回复自己的评论
            return;
        }
        postEvent(MSG_SHOW_COMMENT_INPUT, new Params().putItem(mFeedId).putItem(commentItem));
    }

    private void sendComment(final String feedId, final DetailCommentAdapter.CommentItem commentItem) {
        if (TextUtils.isEmpty(mFeedId) || TextUtils.isEmpty(feedId) ||
                !mFeedId.equals(feedId)) {
            return;
        }
        final long ownerId = mOwnerId;
        Observable.just(0)
                .map(new Func1<Integer, DetailCommentAdapter.CommentItem>() {
                    @Override
                    public DetailCommentAdapter.CommentItem call(Integer integer) {
                        DetailCommentAdapter.CommentItem result = FeedsCommentUtils.sendComment(
                                commentItem, feedId, ownerId, FEED_TYPE_DEFAULT, 0);
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
                        postEvent(MSG_UPDATE_COMMENT_CNT, new Params().putItem(mTotalCnt));
                        if (!mIsReverse) {
                            if (mPullSubscription != null && !mPullSubscription.isUnsubscribed()) {
                                mPullSubscription.unsubscribe();
                                mPullSubscription = null;
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

    private void deleteComment(final DetailCommentAdapter.CommentItem commentItem) {
        if (TextUtils.isEmpty(mFeedId)) {
            return;
        }
        final long ownerId = mOwnerId;
        final String feedId = mFeedId;
        Observable.just(0)
                .map(new Func1<Integer, PullCommentHelper>() {
                    @Override
                    public PullCommentHelper call(Integer integer) {
                        boolean result = FeedsCommentUtils.deleteComment(
                                commentItem, feedId, ownerId, FEED_TYPE_DEFAULT);
                        if (result) {
                            PullCommentHelper helper = mIsReverse ? mOlderPuller : mNewerPuller;
                            helper.removeItem(commentItem);
                            return helper;
                        }
                        return null;
                    }
                })
                .retryWhen(new RxRetryAssist()) // 增加一次重试
                .subscribeOn(Schedulers.from(mExecutor))
                .compose(this.<PullCommentHelper>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PullCommentHelper>() {
                    @Override
                    public void call(PullCommentHelper helper) {
                        if (mView == null) {
                            return;
                        }
                        if (helper == null) {
                            ToastUtils.showToast(mView.getRealView().getContext(), R.string.feeds_comment_delete_failed);
                            return;
                        }
                        --mTotalCnt;
                        postEvent(MSG_UPDATE_COMMENT_CNT, new Params().putItem(mTotalCnt));
                        mView.onUpdateCommentList(helper.mHotList, helper.mAllList, !helper.mIsAsc);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "sendComment failed, exception=" + throwable);
                        ToastUtils.showToast(mView.getRealView().getContext(), R.string.feeds_comment_delete_failed);
                    }
                });
    }

    @Override
    public final void foldInfoArea() {
        postEvent(MSG_FOLD_INFO_AREA);
    }

    @Override
    public void showCommentPopup(final Context context, final DetailCommentAdapter.CommentItem commentItem) {
        if (commentItem == null) {
            MyLog.w(TAG, "onLongClickComment commentItem is null");
            return;
        }

        final int MENU_COPY = 0, MENU_DELETE = 1, MENU_CANCEL = 2;

        String[] items = context.getResources().getStringArray(R.array.feeds_long_click_onther_comment);
        final SparseArray<Integer> itemsMap = new SparseArray<>();
        if (commentItem.fromUid == MyUserInfoManager.getInstance().getUuid() ||
                mOwnerId == MyUserInfoManager.getInstance().getUuid()) { // 评论作者和Feeds作者 可以删除评论
            items = context.getResources().getStringArray(R.array.feeds_long_click_my_comment);
            itemsMap.put(0, MENU_COPY);
            itemsMap.put(1, MENU_DELETE);
            itemsMap.put(2, MENU_CANCEL);
        } else {
            itemsMap.put(1, MENU_COPY);
            itemsMap.put(2, MENU_CANCEL);
        }
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(context);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int item = itemsMap.get(which);

                switch (item) {
                    case MENU_COPY: // 复制
                        FeedsCommentUtils.copyToClipboard(commentItem.content, true);
                        ToastUtils.showToast(context, R.string.sixin_message_item_content_menu_copy_success);
                        break;
                    case MENU_DELETE: // 刪除
                        showDeleteConfirmDialog(context, commentItem);
                        break;
                    case MENU_CANCEL: // 取消
                    default:
                        break;
                }
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public final void showPersonalInfo(long uid) {
        if (uid > 0) {
            postEvent(MSG_SHOW_PERSONAL_INFO, new Params().putItem(uid));
        }
    }

    private void showDeleteConfirmDialog(final Context context, final DetailCommentAdapter.CommentItem commentItem) {
        final MyAlertDialog.Builder builder = new MyAlertDialog.Builder(context);
        builder.setMessage(R.string.feeds_comment_delete_dialog_title);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { // 确认删除
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteComment(commentItem);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // 取消
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        MyAlertDialog dialog = builder.setAutoDismiss(false).setCancelable(true).create();
        dialog.show();
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_SEND_COMMENT:
                sendComment((String) params.getItem(0), (DetailCommentAdapter.CommentItem) params.getItem(1));
                break;
            case MSG_NEW_FEED_ID:
                onNewFeedId((String) params.getItem(0), (long) params.getItem(1));
                break;
            default:
                break;
        }
        return false;
    }

    public static class PullCommentHelper {
        private boolean mIsAsc;
        private int mTotalCnt = 0;
        private long mCommentTs = 0;
        private volatile boolean mHasMore = true;
        private volatile boolean mCanShowNoMore = true;

        private Deque<DetailCommentAdapter.CommentItem> mHotList = new ArrayDeque<>();
        private Deque<DetailCommentAdapter.CommentItem> mAllList = new ArrayDeque<>();

        public PullCommentHelper(boolean isAsc) {
            mIsAsc = isAsc;
        }

        private void reset() {
            mTotalCnt = 0;
            mCommentTs = 0;
            mHasMore = true;
            mCanShowNoMore = true;
            mHotList.clear();
            mAllList.clear();
        }

        private void addSendItem(DetailCommentAdapter.CommentItem commentItem) {
            if (commentItem != null) {
                mAllList.addFirst(commentItem);
            }
        }

        private void removeItem(DetailCommentAdapter.CommentItem commentItem) {
            mHotList.remove(commentItem);
            mAllList.remove(commentItem);
        }

        private PullCommentHelper pullMore(final String feedId, final int limit) {
            Feeds.QueryFeedCommentsResponse rsp = FeedsCommentUtils.fetchFeedsComment(
                    feedId, mCommentTs, limit, false, mIsAsc, PULL_TYPE_ALL_HYBRID, true);
            if (rsp == null || rsp.getErrCode() != ErrorCode.CODE_SUCCESS) {
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
                if (!outList.contains(commentItem)) {
                    outList.addLast(commentItem);
                }
            }
            return this;
        }
    }
}
