package com.module.rankingmode.room.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.HttpUtils;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.OnLineInfoModel;
import com.module.rankingmode.prepare.model.RoundInfoModel;
import com.module.rankingmode.room.comment.CommentView;
import com.module.rankingmode.room.model.RoomData;
import com.module.rankingmode.room.presenter.RankingCorePresenter;
import com.module.rankingmode.room.view.BottomContainerView;
import com.module.rankingmode.room.view.IGameRuleView;
import com.module.rankingmode.room.view.InputContainerView;
import com.module.rankingmode.room.view.TopContainerView;
import com.module.rankingmode.room.view.TurnChangeCardView;
import com.module.rankingmode.song.model.SongModel;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.model.UrlRes;
import com.zq.lyrics.utils.ZipUrlResourceManager;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.FloatLyricsView;
import com.zq.lyrics.widget.ManyLyricsView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class RankingRoomFragment extends BaseFragment implements IGameRuleView {

    RoomData mRoomData;

    InputContainerView mInputContainerView;

    BottomContainerView mBottomContainerView;

    CommentView mCommentView;

    TopContainerView mTopContainerView;

    RankingCorePresenter presenter;

    ExTextView mTestTv;

    ManyLyricsView mManyLyricsView;

    FloatLyricsView mFloatLyricsView;

    ZipUrlResourceManager zipUrlResourceManager;

    TurnChangeCardView mTurnChangeView;

    @Override
    public int initView() {
        return R.layout.ranking_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        // 请保证从下面的view往上面的view开始初始化
        initInputView();
        initBottomView();
        initCommentView();
        initTopView();

        mManyLyricsView = mRootView.findViewById(R.id.many_lyrics_view);
        mManyLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
        mFloatLyricsView = mRootView.findViewById(R.id.float_lyrics_view);
        mFloatLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);

        mTurnChangeView = mRootView.findViewById(R.id.turn_change_view);

        mTestTv = mRootView.findViewById(R.id.test_tv);
        presenter = new RankingCorePresenter(this, mRoomData);
        addPresent(presenter);

    }


    ObjectAnimator mTurnChangeCardShowAnimator;

    public void playShowTurnCardAnimator() {
        if (mTurnChangeCardShowAnimator == null) {
            mTurnChangeCardShowAnimator = ObjectAnimator.ofFloat(mTurnChangeView, "translationX", -U.getDisplayUtils().getScreenWidth(), 0);
            mTurnChangeCardShowAnimator.setDuration(1000);

            mTurnChangeCardShowAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    HandlerTaskTimer.newBuilder()
                            .delay(2000)
                            .start(new HandlerTaskTimer.ObserverW() {
                                @Override
                                public void onNext(Integer integer) {
                                    playHideTurnCardAnimator();
                                }
                            });
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mTurnChangeView.setVisibility(View.VISIBLE);
                }
            });
        }

        mTurnChangeCardShowAnimator.start();
    }

    ObjectAnimator mTurnChangeCardHideAnimator;

    public void playHideTurnCardAnimator() {
        if (mTurnChangeCardHideAnimator == null) {
            mTurnChangeCardHideAnimator = ObjectAnimator.ofFloat(mTurnChangeView, "translationX", 0, -U.getDisplayUtils().getScreenWidth());
            mTurnChangeCardHideAnimator.setDuration(1000);

            mTurnChangeCardHideAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mTurnChangeView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }
            });
        }

        mTurnChangeCardHideAnimator.start();

    }

    HttpUtils.OnDownloadProgress onDownloadProgress = new HttpUtils.OnDownloadProgress() {
        @Override
        public void onDownloaded(long downloaded, long totalLength) {

        }

        @Override
        public void onCompleted(String localPath) {
            MyLog.d(TAG, "onCompleted" + " localPath=" + localPath);
            if (playingSongModel != null) {
                playLyric(playingSongModel.getItemID());
            }
        }

        @Override
        public void onCanceled() {

        }

        @Override
        public void onFailed() {

        }
    };

    private void initInputView() {
        mInputContainerView = mRootView.findViewById(R.id.input_container_view);
        mInputContainerView.setRoomData(mRoomData);
    }

    private void initBottomView() {
        mBottomContainerView = (BottomContainerView) mRootView.findViewById(R.id.bottom_container_view);
        mBottomContainerView.setListener(new BottomContainerView.Listener() {
            @Override
            public void showInputBtnClick() {
                mInputContainerView.showSoftInput();
            }
        });
        mBottomContainerView.setRoomData(mRoomData);
    }

    private void initCommentView() {
        mCommentView = mRootView.findViewById(R.id.comment_view);
        mCommentView.setRoomData(mRoomData);
    }

    private void initTopView() {
        mTopContainerView = mRootView.findViewById(R.id.top_container_view);
        // 加上状态栏的高度
        int statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(getContext());
        RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) mTopContainerView.getLayoutParams();
        topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
        mTopContainerView.setListener(new TopContainerView.Listener() {
            @Override
            public void closeBtnClick() {
                getActivity().finish();
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 0) {
            mRoomData = (RoomData) data;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LrcEvent.FinishLoadLrcEvent finishLoadLrcEvent) {
        MyLog.d(TAG, "onEventMainThread" + " finishLoadLrcEvent hash is =" + finishLoadLrcEvent.hash);
        LyricsReader lyricsReader = LyricsManager.getLyricsManager(getContext()).getLyricsUtil(finishLoadLrcEvent.hash);
        if (lyricsReader != null) {
            lyricsReader.setHash(finishLoadLrcEvent.hash);

            //自己
            if (presenter.getRoomData().getRealRoundInfo().getUserID()
                    == MyUserInfoManager.getInstance().getUid()) {
                mFloatLyricsView.setVisibility(View.GONE);
                mManyLyricsView.setVisibility(View.VISIBLE);
                mManyLyricsView.initLrcData();
                mManyLyricsView.setLyricsReader(lyricsReader);
                if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY) {
                    MyLog.d(TAG, "onEventMainThread " + "play");
                    RoundInfoModel roundInfo = presenter.getRoomData().getRealRoundInfo();
                    mManyLyricsView.play(roundInfo.getSingBeginMs());
                }
            } else {
                mManyLyricsView.setVisibility(View.GONE);
                mFloatLyricsView.setVisibility(View.VISIBLE);
                mFloatLyricsView.initLrcData();
                mFloatLyricsView.setLyricsReader(lyricsReader);
                if (mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mFloatLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY) {
                    MyLog.d(TAG, "onEventMainThread " + "play");
                    RoundInfoModel roundInfo = presenter.getRoomData().getRealRoundInfo();
                    mFloatLyricsView.play(roundInfo.getSingBeginMs());
                }
            }

        }
    }


    @Override
    protected boolean onBackPressed() {
        if (mInputContainerView.onBackPressed()) {
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void startSelfCountdown(Runnable countDownOver) {
        mTurnChangeView.setData(mRoomData);
        playShowTurnCardAnimator();
        HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(3)
                .start(new HandlerTaskTimer.ObserverW() {
                           @Override
                           public void onNext(Integer integer) {
                               addText("你的演唱要开始了，倒计时" + (4 - integer));
                           }

                           @Override
                           public void onComplete() {
                               super.onComplete();
                               countDownOver.run();
                           }
                       }
                );
    }

    @Override
    public void startRivalCountdown(int uid) {
        addText("用户" + uid + "的演唱开始了");
        mTurnChangeView.setData(mRoomData);
        playShowTurnCardAnimator();
    }

    @Override
    public void userExit() {

    }

    @Override
    public void gameFinish() {
        addText("游戏结束了");
//        mManyLyricsView.initLrcData();
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), EvaluationFragment.class)
                .setAddToBackStack(true)
                .addDataBeforeAdd(0, mRoomData)
                .build()
        );
    }

    @Override
    public void updateUserState(List<OnLineInfoModel> jsonOnLineInfoList) {
        if (jsonOnLineInfoList == null) {
            return;
        }
        for (OnLineInfoModel onLineInfoModel : jsonOnLineInfoList) {
            if (!onLineInfoModel.isIsOnline()) {
                addText("用户" + onLineInfoModel.getUserID() + "处于离线状态");
            }
        }
    }

    SongModel playingSongModel;

    @Override
    public void playLyric(int songId) {
        addText("开始播放歌词 songId=" + songId);
        Observable.fromIterable(mRoomData.getSongModelList())
                .filter(songModel -> songModel.getItemID() == songId)
                .subscribe(songModel -> {
                    playingSongModel = songModel;
                    File file = SongResUtils.getZRCELyricFileByUrl(songModel.getLyric());
                    MyLog.d(TAG, "playLyric songModel:" + songModel);

                    if (file == null) {
                        MyLog.d(TAG, "playLyric 1");
                        ArrayList<UrlRes> urlResArrayList = new ArrayList<>();
                        UrlRes lyric = new UrlRes(songModel.getLyric(), SongResUtils.getLyricDir(), SongResUtils.SUFF_ZRCE);
                        urlResArrayList.add(lyric);
                        zipUrlResourceManager = new ZipUrlResourceManager(urlResArrayList, onDownloadProgress);
                        zipUrlResourceManager.go();
                    } else {
                        MyLog.d(TAG, "playLyric 2");

                        String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
                        LyricsManager.getLyricsManager(getActivity()).loadLyricsUtil(fileName, "沙漠骆驼", fileName.hashCode() + "");

                    }
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }

    void addText(String te) {
        String a = mTestTv.getText() + "\n" + te;
        mTestTv.setText(a);
    }
}
