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
import com.wali.live.watchsdk.videodetail.view.DetailIntroduceView;
import com.wali.live.watchsdk.videodetail.view.DetailCommentView;
import com.wali.live.watchsdk.videodetail.view.DetailReplayView;
import com.wali.live.watchsdk.videodetail.view.DetailTabView;

import java.util.ArrayList;
import java.util.List;

import static com.wali.live.component.BaseSdkController.MSG_COMMENT_TOTAL_CNT;
import static com.wali.live.component.BaseSdkController.MSG_FOLD_INFO_AREA;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_FEEDS_DETAIL;
import static com.wali.live.component.BaseSdkController.MSG_REPLAY_TOTAL_CNT;

/**
 * Created by yangli on 2017/06/02.
 *
 * @module 详情TAB表现
 */
public class DetailTabPresenter extends ComponentPresenter<DetailTabView.IView>
        implements DetailTabView.IPresenter {
    private static final String TAG = "DetailTabPresenter";

    private RoomBaseDataModel mMyRoomData;

    private final DetailCommentPresenter mCommentPresenter;
    private final DetailReplayPresenter mReplayPresenter;

    private DetailCommentView mCommentView;
    private DetailReplayView mReplayView;
    private DetailIntroduceView mDetailIntroduceView; //详情页

    private boolean mIsReplay = true;
    private int mCommentCnt = 0;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public DetailTabPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel roomData) {
        super(controller);
        mMyRoomData = roomData;
        mCommentPresenter = new DetailCommentPresenter(mController, mMyRoomData);
        mReplayPresenter = new DetailReplayPresenter(mController, mMyRoomData);
    }

    @Override
    public void startPresenter() {
        registerAction(MSG_COMMENT_TOTAL_CNT);
        registerAction(MSG_REPLAY_TOTAL_CNT);
        registerAction(MSG_FOLD_INFO_AREA);
        registerAction(MSG_PLAYER_FEEDS_DETAIL);
        mCommentPresenter.startPresenter();
        mReplayPresenter.startPresenter();
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        mCommentPresenter.stopPresenter();
        mReplayPresenter.stopPresenter();
    }

    @Override
    public void destroy() {
        super.destroy();
        mCommentPresenter.destroy();
        mReplayPresenter.destroy();
    }

    @Override
    public void syncTabPageList(Context context) {
        MyLog.w(TAG, "synTabPageList");
        List<Pair<String, ? extends View>> tabPageList = new ArrayList<>();
        if (mIsReplay) {
            if (mCommentView == null) {
                mCommentView = new DetailCommentView(context);
                mCommentPresenter.setView(mCommentView.getViewProxy());
                mCommentView.setPresenter(mCommentPresenter);
            }
            tabPageList.add(Pair.create(String.format(context.getResources().getString(
                    R.string.feeds_detail_label_comment), String.valueOf(mCommentCnt)), mCommentView));

            if (mReplayView == null) {
                mReplayView = new DetailReplayView(context);
                mReplayView.setMyRoomData(mMyRoomData);
                mReplayPresenter.setView(mReplayView.getViewProxy());
                mReplayView.setPresenter(mReplayPresenter);
            }
            tabPageList.add(Pair.create(String.format(context.getResources().getString(
                    R.string.feeds_detail_label_replay), "0"), mReplayView));
        } else {
            if (mDetailIntroduceView == null) {
                mDetailIntroduceView = new DetailIntroduceView(context);
            }
            tabPageList.add(Pair.create(context.getResources().getString(
                    R.string.feeds_detail_label_detail), mDetailIntroduceView));

            if (mCommentView == null) {
                mCommentView = new DetailCommentView(context);
                mCommentPresenter.setView(mCommentView.getViewProxy());
                mCommentView.setPresenter(mCommentPresenter);
            }
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
            case MSG_COMMENT_TOTAL_CNT:
                mCommentCnt = (int) params.getItem(0);
                mView.updateCommentTotalCnt(mCommentCnt, mIsReplay);
                break;
            case MSG_REPLAY_TOTAL_CNT:
                mView.updateReplayTotalCnt((int) params.getItem(0));
                break;
            case MSG_FOLD_INFO_AREA:
                mView.onFoldInfoArea();
                break;
            case MSG_PLAYER_FEEDS_DETAIL:
                DetailInfoPresenter.FeedsInfo feedsInfo = params.getItem(0);
                if (feedsInfo != null && !feedsInfo.isReplay) {
                    mIsReplay = feedsInfo.isReplay;
                    syncTabPageList(GlobalData.app());
                    mDetailIntroduceView.setData(feedsInfo.title, feedsInfo.description);
                }
                break;
            default:
                break;
        }
        return false;
    }
}
