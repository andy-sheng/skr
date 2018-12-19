package com.module.rankingmode.room.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.OnLineInfoModel;
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
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.FloatLyricsView;
import com.zq.lyrics.widget.ManyLyricsView;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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

    Handler mUiHanlder = new Handler();

    Disposable prepareLyricTask;

    ScrollView scrollView;

    TurnChangeCardView mTurnChangeView;

    @Override
    public int initView() {
        return R.layout.ranking_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        // 请保证从下面的view往上面的view开始初始化
        scrollView = mRootView.findViewById(R.id.scrollview);
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
        mTestTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        mRootView.findViewById(R.id.tv_control).setOnClickListener(v -> {
            needScroll = !needScroll;
        });
        presenter = new RankingCorePresenter(this, mRoomData);
        addPresent(presenter);

        showMsg("gameid 是 " + mRoomData.getGameId() + " userid 是 " + MyUserInfoManager.getInstance().getUid());
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
        return false;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 0) {
            mRoomData = (RoomData) data;
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
        mUiHanlder.post(new Runnable() {
            @Override
            public void run() {
                mTurnChangeView.setData(mRoomData);
                playShowTurnCardAnimator();
            }
        });

        HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(3)
                .start(new HandlerTaskTimer.ObserverW() {
                           @Override
                           public void onNext(Integer integer) {
                               showMsg("你的演唱要开始了，倒计时" + (4 - integer));
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
        showMsg("用户" + uid + "的演唱开始了");
        mTurnChangeView.setData(mRoomData);
        playShowTurnCardAnimator();
    }

    @Override
    public void userExit() {

    }

    @Override
    public void gameFinish() {
        showMsg("游戏结束了");
        mManyLyricsView.initLrcData();
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), EvaluationFragment.class)
                .setAddToBackStack(true)
                .addDataBeforeAdd(0, mRoomData)
                .build()
        );

        mFloatLyricsView.setVisibility(View.GONE);
        mManyLyricsView.setVisibility(View.GONE);
    }

    @Override
    public void updateUserState(List<OnLineInfoModel> jsonOnLineInfoList) {
        if (jsonOnLineInfoList == null) {
            return;
        }
        for (OnLineInfoModel onLineInfoModel : jsonOnLineInfoList) {
            if (!onLineInfoModel.isIsOnline()) {
                showMsg("用户" + onLineInfoModel.getUserID() + "处于离线状态");
            }
        }
    }

    SongModel playingSongModel;

    @Override
    public void playLyric(SongModel songModel, boolean play) {
        showMsg("开始播放歌词 songId=" + songModel.getItemID());
        playingSongModel = songModel;

        if (prepareLyricTask != null && !prepareLyricTask.isDisposed()) {
            prepareLyricTask.dispose();
        }

        File file = SongResUtils.getZRCELyricFileByUrl(songModel.getLyric());

        if (file == null) {
            MyLog.d(TAG, "playLyric is not in local file");

            fetchLyricTask(songModel, play);
        } else {
            MyLog.d(TAG, "playLyric is exist");

            final String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
            parseLyrics(fileName, play);
        }
    }


    private void parseLyrics(String fileName, boolean play){
        MyLog.d(TAG, "parseLyrics" + " fileName=" + fileName);
        prepareLyricTask = LyricsManager.getLyricsManager(getActivity()).loadLyricsObserable(fileName, "沙漠骆驼", fileName.hashCode() + "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lyricsReader -> {
                    drawLyric(fileName.hashCode()+ "", lyricsReader, play);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }


    private void drawLyric(String fileNameHash, LyricsReader lyricsReader, boolean play){
        MyLog.d(TAG, "drawLyric" + " fileNameHash=" + fileNameHash + " lyricsReader=" + lyricsReader);
        if (lyricsReader != null) {
            lyricsReader.setHash(fileNameHash);

            //自己
            if (presenter.getRoomData().getRealRoundInfo().getUserID()
                    == MyUserInfoManager.getInstance().getUid()) {
                mFloatLyricsView.setVisibility(View.GONE);
                mManyLyricsView.setVisibility(View.VISIBLE);
                mManyLyricsView.initLrcData();
                mManyLyricsView.setLyricsReader(lyricsReader);
                if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY && play) {
                    MyLog.d(TAG, "onEventMainThread " + "play");
                    mManyLyricsView.play(0);
                }
            } else {
                mManyLyricsView.setVisibility(View.GONE);
                mFloatLyricsView.setVisibility(View.VISIBLE);
                mFloatLyricsView.initLrcData();
                mFloatLyricsView.setLyricsReader(lyricsReader);
                if (mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mFloatLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY && play) {
                    MyLog.d(TAG, "onEventMainThread " + "play");
                    mFloatLyricsView.play(0);
                }
            }
        }
    }


    private void fetchLyricTask(SongModel songModel, boolean play){
        MyLog.d(TAG, "fetchLyricTask" + " songModel=" + songModel);
        prepareLyricTask = Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) {
                File tempFile = new File(SongResUtils.createTempLyricFileName(songModel.getLyric()));

                boolean isSuccess = U.getHttpUtils().downloadFileSync(songModel.getLyric(), tempFile, null);

                File oldName = new File(SongResUtils.createTempLyricFileName(songModel.getLyric()));
                File newName = new File(SongResUtils.createLyricFileName(songModel.getLyric()));

                if (isSuccess) {
                    if (oldName.renameTo(newName)) {
                        System.out.println("已重命名");
                    } else {
                        System.out.println("Error");
                        emitter.onError(new Throwable("重命名错误"));
                    }
                } else {
                    emitter.onError(new Throwable("下载失败"));
                }

                emitter.onNext(newName);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> {
            final String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
            parseLyrics(fileName, play);
        }, throwable -> {
            MyLog.e(TAG, throwable);
        });
    }

    @Override
    public void showMsg(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            addText(msg);
        } else {
            addText("收到一个空信息");
        }
    }

    boolean needScroll = true;

    void addText(String te) {
        mUiHanlder.post(() -> {
            mTestTv.append(U.getDateTimeUtils().formatTimeStringForDate(System.currentTimeMillis(), "HH:mm:ss:SSS") + ":" + te + "\n");
            if (needScroll) {
                scrollView.smoothScrollTo(0, mTestTv.getBottom());
            }
        });
    }
}
