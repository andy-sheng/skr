package com.module.playways.rank.prepare.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.ActivityUtils;
import com.common.utils.FragmentUtils;
import com.common.utils.HttpUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.component.busilib.manager.BgMusicManager;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.rank.R;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.prepare.presenter.PrepareSongPresenter;
import com.module.playways.rank.prepare.view.IPrepareResView;
import com.module.playways.rank.song.fragment.SongSelectFragment;
import com.module.playways.rank.song.model.SongModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 准备资源界面
 */
public class PrepareResFragment extends BaseFragment implements IPrepareResView {

    public final static String TAG = "PrepareResFragment";

    RelativeLayout mMainActContainer;

    ExImageView mIvTop;
    SimpleDraweeView mSongIcon;
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
        mSongIcon = (SimpleDraweeView) mRootView.findViewById(R.id.song_icon);
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

        if (!TextUtils.isEmpty(mPrepareData.getSongModel().getCover())) {
            FrescoWorker.loadImage(mSongIcon,
                    ImageFactory.newHttpImage(mPrepareData.getSongModel().getCover())
                            .setCornerRadius(U.getDisplayUtils().dip2px(6))
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(Color.parseColor("#0C2275")).build());
        } else {
            FrescoWorker.loadImage(mSongIcon,
                    ImageFactory.newResImage(R.drawable.xuanzegequ_wufengmian)
                            .setCornerRadius(U.getDisplayUtils().dip2px(6))
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(Color.parseColor("#0C2275")).build());
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
                    mIvStartMatch.setEnabled(true);
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
                U.getSoundUtils().play(TAG, R.raw.song_pairbutton);
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                        .withSerializable("prepare_data", mPrepareData)
                        .navigation();
                HashMap map = new HashMap();
                map.put("songId", mPrepareData.getSongModel().getItemID());
                StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK),
                        StatConstants.KEY_MATCH_START, map);
            }
        });

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getSoundUtils().play(TAG, R.raw.general_back);
                onBackPressed();
            }
        });

        mIvStartMatch.setEnabled(false);

        mPrepareSongPresenter = new PrepareSongPresenter(mOnDownloadProgress, this, mPrepareData.getSongModel());
        addPresent(mPrepareSongPresenter);
        mPrepareSongPresenter.prepareRes();

        U.getSoundUtils().preLoad(TAG, R.raw.general_back, R.raw.song_pairbutton);
        playBackgroundMusic();
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPrepareSongPresenter.cancelTask();
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
