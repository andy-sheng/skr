package com.module.playways.room.prepare.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.permission.SkrAudioPermission;
import com.common.log.MyLog;
import com.common.utils.ActivityUtils;
import com.common.utils.FragmentUtils;
import com.common.utils.HttpUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.manager.BgMusicManager;
import com.module.RouterConstants;
import com.module.playways.R;
import com.module.playways.room.prepare.model.PrepareData;
import com.module.playways.room.prepare.presenter.PrepareSongPresenter;
import com.module.playways.room.prepare.view.IPrepareResView;
import com.module.playways.room.song.fragment.SongSelectFragment;
import com.module.playways.room.song.model.SongModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 准备资源界面
 */
public class PrepareResFragment extends BaseFragment implements IPrepareResView {

    public final static String TAG = "PrepareResFragment";

    RelativeLayout mMainActContainer;

    ExImageView mIvTop;
    //    SimpleDraweeView mSongIcon;
    ExTextView mSongName;
    ExTextView mTvDuration;
    ExTextView mTvLyric;

    ExImageView mIvBack;
    ProgressBar songResProgressbar;
    ExTextView mIvStartMatch;

    private PrepareData mPrepareData = new PrepareData();

    HttpUtils.OnDownloadProgress mOnDownloadProgress;

    PrepareSongPresenter mPrepareSongPresenter;

    Handler mUiHandler;

    boolean mSetBackGround = false;

    boolean mIsLyricPrepared = false;
    boolean mIsResPrepared = false;

    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    @Override
    public int initView() {
        return R.layout.prepare_res_fragment_layout;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mIvTop = (ExImageView) mRootView.findViewById(R.id.iv_top);
//        mSongIcon = (SimpleDraweeView) mRootView.findViewById(R.id.song_icon);
        mSongName = (ExTextView) mRootView.findViewById(R.id.song_name);
        mTvDuration = (ExTextView) mRootView.findViewById(R.id.tv_duration);
        mTvLyric = (ExTextView) mRootView.findViewById(R.id.tv_lyric);
        mIvStartMatch = mRootView.findViewById(R.id.iv_start_match);
        songResProgressbar = (ProgressBar) mRootView.findViewById(R.id.song_res_progressbar);
        songResProgressbar.setMax(100);
        mSongName.setText(mPrepareData.getSongModel().getItemName());

        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);

        if (mSetBackGround) {
            mMainActContainer.setBackgroundResource(R.drawable.dabeijing);
        }

        mTvDuration.setText(U.getDateTimeUtils().formatTimeStringForDate(mPrepareData.getSongModel().getTotalMs(), "mm:ss"));

        mUiHandler = new Handler();

        mOnDownloadProgress = new HttpUtils.OnDownloadProgress() {
            @Override
            public void onDownloaded(long downloaded, long totalLength) {
//                MyLog.d(TAG, "onDownloaded" + " downloaded=" + downloaded + " totalLength=" + totalLength);
                mUiHandler.post(() -> {
                    int progress = (int) ((((float) downloaded / (float) totalLength)) * 100);
                    songResProgressbar.setProgress(progress);
                });
            }

            @Override
            public void onCompleted(String localPath) {
                MyLog.w(TAG, "onCompleted" + " localPath=" + localPath);
                mUiHandler.post(() -> {
//                    U.getToastUtil().showShort("歌曲资源已经准备好了");
                    songResProgressbar.setProgress(100);
                    mIsResPrepared = true;
                    tryEnableStartMatchBtn();
                });
            }

            @Override
            public void onCanceled() {
                MyLog.d(TAG, "onCanceled");
            }

            @Override
            public void onFailed() {
                U.getToastUtil().showShort("下载资源失败，请退出重试");
                MyLog.w(TAG, "download song res failed");
            }
        };

        mIvStartMatch.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getSoundUtils().play(TAG, R.raw.rank_startmatching, 500);
                mSkrAudioPermission.ensurePermission(new Runnable() {
                    @Override
                    public void run() {
                        ARouter.getInstance()
                                .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                                .withSerializable("prepare_data", mPrepareData)
                                .navigation();
                    }
                }, true);
            }
        });

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                onBackPressed();
            }
        });

        mIvStartMatch.setEnabled(false);

        mPrepareSongPresenter = new PrepareSongPresenter(mOnDownloadProgress, this, mPrepareData.getSongModel());
        addPresent(mPrepareSongPresenter);
        mPrepareSongPresenter.prepareRes();

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back, R.raw.rank_startmatching);
        playBackgroundMusic();
    }

    private void tryEnableStartMatchBtn() {
        if (mIsLyricPrepared && mIsResPrepared) {
            mIvStartMatch.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrAudioPermission.onBackFromPermisionManagerMaybe(getActivity());
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
        BgMusicManager.getInstance().destory();
    }

    @Override
    public void onLyricReady(String lyrics) {
        MyLog.d(TAG, "onLyricReady" + " lyrics=" + lyrics);
        mTvLyric.setText(lyrics);
        mIsLyricPrepared = true;
        tryEnableStartMatchBtn();
    }

    @Override
    public void lyricReadyFailed() {
        U.getToastUtil().showShort("下载资源失败，请退出重试");
        MyLog.w(TAG, "lyricReadyFailed");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mPrepareSongPresenter != null) {
            mPrepareSongPresenter.cancelTask();
        }
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        playBackgroundMusic();
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mPrepareData.setSongModel((SongModel) data);
            mPrepareData.setBgMusic(((SongModel) data).getRankUserVoice());
        }
        if (type == 1) {
            mPrepareData.setGameType((int) data);
        }
        if (type == 2) {
            mSetBackGround = (boolean) data;
        }
    }

    @Override
    protected boolean onBackPressed() {
        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                .setPopFragment(PrepareResFragment.this)
                .setNotifyShowFragment(SongSelectFragment.class)
                .setHasAnimation(true)
//                .setPopAbove(false)
                .build());
        return true;
    }

    @Override
    public void notifyToShow() {
        MyLog.d(TAG, "toStaskTop");
        playBackgroundMusic();
        mRootView.setVisibility(View.VISIBLE);
    }

    private void playBackgroundMusic() {
        if (!BgMusicManager.getInstance().isPlaying() && mPrepareData != null && PrepareResFragment.this.fragmentVisible) {
            if (!TextUtils.isEmpty(mPrepareData.getBgMusic())) {
                BgMusicManager.getInstance().starPlay(mPrepareData.getBgMusic(), 0, "PrepareResFragment");
            }
        }
    }

    @Override
    public void notifyToHide() {
        MyLog.d(TAG, "pushIntoStash");
        mRootView.setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        MyLog.w(TAG, event.foreground ? "切换到前台" : "切换到后台");
        if (event.foreground) {
            playBackgroundMusic();
        } else {
            BgMusicManager.getInstance().destory();
        }
    }
}
