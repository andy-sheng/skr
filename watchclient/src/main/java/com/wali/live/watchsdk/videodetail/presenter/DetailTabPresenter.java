package com.wali.live.watchsdk.videodetail.presenter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.videodetail.view.DetailCommentView;
import com.wali.live.watchsdk.videodetail.view.DetailIntroduceView;
import com.wali.live.watchsdk.videodetail.view.DetailReplayView;
import com.wali.live.watchsdk.videodetail.view.DetailTabView;

import java.util.ArrayList;
import java.util.List;

import static com.wali.live.component.BaseSdkController.MSG_FOLD_INFO_AREA;
import static com.wali.live.component.BaseSdkController.MSG_REPLAY_TOTAL_CNT;
import static com.wali.live.component.BaseSdkController.MSG_UPDATE_COMMENT_CNT;
import static com.wali.live.component.BaseSdkController.MSG_UPDATE_TAB_TYPE;

/**
 * Created by yangli on 2017/06/02.
 *
 * @module 详情TAB表现
 */
public class DetailTabPresenter extends ComponentPresenter<DetailTabView.IView>
        implements DetailTabView.IPresenter {
    private static final String TAG = "DetailTabPresenter";

    private RoomBaseDataModel mMyRoomData;

    private DetailCommentPresenter mCommentPresenter;
    private DetailReplayPresenter mReplayPresenter;

    private DetailCommentView mCommentView;
    private DetailReplayView mReplayView;
    private DetailIntroduceView mDetailIntroduceView; //详情页
    private boolean mIsReplay = true;
    private int mCommentCnt = 0;
    private int mReplayCnt = 0;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public DetailTabPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel roomData) {
        super(controller);
        mMyRoomData = roomData;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_UPDATE_COMMENT_CNT);
        registerAction(MSG_REPLAY_TOTAL_CNT);
        registerAction(MSG_FOLD_INFO_AREA);
        registerAction(MSG_UPDATE_TAB_TYPE);
        if (mCommentPresenter != null) {
            mCommentPresenter.startPresenter();
        }
        if (mReplayPresenter != null) {
            mReplayPresenter.startPresenter();
        }
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        if (mCommentPresenter != null) {
            mCommentPresenter.stopPresenter();
        }
        if (mReplayPresenter != null) {
            mReplayPresenter.stopPresenter();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mCommentPresenter != null) {
            mCommentPresenter.destroy();
        }
        if (mReplayPresenter != null) {
            mReplayPresenter.destroy();
        }
    }

    private void setupCommentView(Context context) {
        if (mCommentView == null) {
            mCommentView = new DetailCommentView(context);
            mCommentPresenter = new DetailCommentPresenter(mController);
            mCommentPresenter.setView(mCommentView.getViewProxy());
            mCommentView.setPresenter(mCommentPresenter);
            mCommentPresenter.startPresenter();
            mCommentPresenter.onNewFeedId(mMyRoomData.getRoomId(), mMyRoomData.getUid());
        }
    }

    private void setupReplayView(Context context) {
        if (mReplayView == null) {
            mReplayView = new DetailReplayView(context);
            mReplayPresenter = new DetailReplayPresenter(mController, mMyRoomData);
            mReplayView.setMyRoomData(mMyRoomData);
            mReplayPresenter.setView(mReplayView.getViewProxy());
            mReplayView.setPresenter(mReplayPresenter);
            mReplayPresenter.startPresenter();
            mReplayPresenter.pullReplayList();
        }
    }

    private void syncTabPageList(Context context) {
        MyLog.w(TAG, "synTabPageList");
        List<Pair<String, ? extends View>> tabPageList = new ArrayList<>();
        if (mIsReplay) {
            setupCommentView(context);
            tabPageList.add(Pair.create(String.format(context.getResources().getString(
                    R.string.feeds_detail_label_comment), String.valueOf(mCommentCnt)), mCommentView));
            setupReplayView(context);
            tabPageList.add(Pair.create(String.format(context.getResources().getString(
                    R.string.feeds_detail_label_replay), String.valueOf(mReplayCnt)), mReplayView));
        } else {
            if (mDetailIntroduceView == null) {
                mDetailIntroduceView = new DetailIntroduceView(context);
            }
            tabPageList.add(Pair.create(context.getResources().getString(
                    R.string.feeds_detail_label_detail), mDetailIntroduceView));
            setupCommentView(context);
            tabPageList.add(Pair.create(String.format(context.getResources().getString(
                    R.string.feeds_detail_label_comment), String.valueOf(mCommentCnt)), mCommentView));
        }
        mView.onTabPageList(tabPageList);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_UPDATE_COMMENT_CNT: {
                mCommentCnt = params.getItem(0);
                mView.onUpdateCommentTotalCnt(mCommentCnt, mIsReplay);
                return true;
            }
            case MSG_REPLAY_TOTAL_CNT: {
                if (!mIsReplay) {
                    return false;
                }
                mReplayCnt = params.getItem(0);
                mView.onUpdateReplayTotalCnt(mReplayCnt);
                return true;
            }
            case MSG_FOLD_INFO_AREA: {
                mView.onFoldInfoArea();
                return true;
            }
            case MSG_UPDATE_TAB_TYPE: {
                DetailInfoPresenter.FeedsInfo feedsInfo = params.getItem(0);
                if (feedsInfo == null) {
                    return false;
                }
                if (!feedsInfo.isReplay) {
                    mIsReplay = feedsInfo.isReplay;
                    syncTabPageList(GlobalData.app());
                    mView.onUpdateCommentTotalCnt(mCommentCnt, mIsReplay); // 防止详情评论拉取失败时，Tab上错误显示回放及回放数目
                    mDetailIntroduceView.setData(feedsInfo.title, feedsInfo.description);
                } else {
                    syncTabPageList(GlobalData.app());
                }
                return true;
            }
            default:
                break;
        }
        return false;
    }
}
