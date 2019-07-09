package com.module.playways.audition.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.log.MyLog;
import com.common.player.AndroidMediaPlayer;
import com.common.player.ExoPlayer;
import com.common.player.IPlayer;
import com.common.player.VideoPlayerAdapter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.ActivityUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.SkrConfig;
import com.engine.Params;
import com.module.playways.R;
import com.module.playways.room.song.model.SongModel;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.zq.dialog.ShareWorksDialog;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.utils.SongResUtils;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;
import com.zq.mediaengine.kit.ZqEngineKit;
import com.zq.person.model.ProducationModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class PlayRecordFragment extends BaseFragment {

    TextView mTvName;
    LinearLayout mBottomContainer;
    RelativeLayout mBackArea;
    RelativeLayout mOptArea;
    ExTextView mOptTv;
    RelativeLayout mResetArea;
    RelativeLayout mSaveShareArea;
    ExTextView mSaveShareTv;
    ManyLyricsView mManyLyricsView;

    SongModel mSongModel;

    Handler mUiHanlder;

    IPlayer mPlayer;

    boolean mIsPlay = false;

    String mPath;  // 文件路径
    String mUrl;   // 文件上传Url
    int mDuration;  // 作品时长
    int mWorksId;   // 作品id

    ShareWorksDialog mShareWorksDialog;

    @Override
    public int initView() {
        return R.layout.play_record_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTvName = (TextView) mRootView.findViewById(R.id.tv_name);
        mBottomContainer = (LinearLayout) mRootView.findViewById(R.id.bottom_container);
        mBackArea = (RelativeLayout) mRootView.findViewById(R.id.back_area);
        mOptArea = (RelativeLayout) mRootView.findViewById(R.id.opt_area);
        mOptTv = (ExTextView) mRootView.findViewById(R.id.opt_tv);
        mResetArea = (RelativeLayout) mRootView.findViewById(R.id.reset_area);
        mSaveShareArea = (RelativeLayout) mRootView.findViewById(R.id.save_share_area);
        mSaveShareTv = (ExTextView) mRootView.findViewById(R.id.save_share_tv);
        mManyLyricsView = (ManyLyricsView) mRootView.findViewById(R.id.many_lyrics_view);

        mTvName.setText("《" + mSongModel.getItemName() + "》");
        mUiHanlder = new Handler();

        playLyrics(mSongModel);
        playRecord();
        mManyLyricsView.setAuthorName(mSongModel.getUploaderName());

        mBackArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 返回选歌页面
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mResetArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mFragmentDataListener != null) {
                    mFragmentDataListener.onFragmentResult(0, 0, null, null);
                }
                U.getFragmentUtils().popFragment(PlayRecordFragment.this);
            }
        });

        mOptArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIsPlay) {
                    // 暂停
                    if (mPlayer != null) {
                        mPlayer.pause();
                        mIsPlay = false;
                    }
                    mManyLyricsView.pause();
                    mOptTv.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.audition_bofang), null, null);
                    mOptTv.setText("播放");
                } else {
                    // 播放
                    mManyLyricsView.resume();
                    mPlayer.resume();
                    mIsPlay = true;
                    mOptTv.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.audition_zanting), null, null);
                    mOptTv.setText("暂停");
                }
            }
        });

        mSaveShareArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (!TextUtils.isEmpty(mUrl) && mWorksId > 0) {
                    if (SkrConfig.getInstance().worksShareOpen()) {
                        showShareDialog(false);
                    } else {

                    }
                } else {
                    saveWorksStep1();
                }
            }
        });

        if (SkrConfig.getInstance().worksShareOpen()) {

        } else {
            mSaveShareTv.setText("保存");
        }

    }

    /**
     * 播放录音
     */
    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mSongModel = (SongModel) data;
        }
    }

    LyricsReader mLyricsReader;

    private void playLyrics(SongModel songModel) {
        final String lyricFile = SongResUtils.getFileNameWithMD5(songModel.getLyric());

        if (lyricFile != null) {
            LyricsManager.getLyricsManager(U.app())
                    .loadLyricsObserable(lyricFile, lyricFile.hashCode() + "")
                    .subscribeOn(Schedulers.io())
                    .retry(10)
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindUntilEvent(FragmentEvent.DESTROY))
                    .subscribe(lyricsReader -> {
                        MyLog.d(TAG, "playMusic, start play lyric");
                        mManyLyricsView.resetData();
                        mManyLyricsView.initLrcData();
                        lyricsReader.cut(songModel.getRankLrcBeginT(), songModel.getRankLrcEndT());
                        MyLog.d(TAG, "getRankLrcBeginT : " + songModel.getRankLrcBeginT());
                        mManyLyricsView.setLyricsReader(lyricsReader);
                        mLyricsReader = lyricsReader;
                        if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY) {
                            mManyLyricsView.play(songModel.getBeginMs());
                            MyLog.d(TAG, "songModel.getBeginMs() : " + songModel.getBeginMs());
                        }
                    }, throwable -> MyLog.e(throwable));
        } else {
            MyLog.e(TAG, "没有歌词文件，不应该，进界面前已经下载好了");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        MyLog.w(TAG, event.foreground ? "切换到前台" : "切换到后台");
        if (!event.foreground && mIsPlay) {
            // 暂停
            if (mPlayer != null) {
                mPlayer.pause();
                mIsPlay = false;
            }
            mManyLyricsView.pause();
            mOptTv.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.audition_bofang), null, null);
            mOptTv.setText("播放");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void destroy() {
        super.destroy();
        mManyLyricsView.release();
        if (mPlayer != null) {
            mPlayer.release();
        }
        mUiHanlder.removeCallbacksAndMessages(null);
        if (mShareWorksDialog != null) {
            mShareWorksDialog.dismiss(false);
        }
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    /**
     * 播放录音
     */
    private void playRecord() {
        if (mPlayer != null) {
            mPlayer.reset();
        }
        if (mPlayer == null) {
            if (AuditionFragment.RECORD_BY_CALLBACK) {
                mPlayer = new AndroidMediaPlayer();
            } else {
                mPlayer = new ExoPlayer();
            }

            mPlayer.setCallback(new VideoPlayerAdapter.PlayerCallbackAdapter() {
                @Override
                public void onCompletion() {
                    super.onCompletion();
                    mManyLyricsView.seekto(mSongModel.getBeginMs());
                    mUiHanlder.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mManyLyricsView.pause();
                            mPlayer.pause();
                        }
                    }, 30);

                    mIsPlay = false;
                    mPlayer.seekTo(0);
                    mOptTv.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.audition_bofang), null, null);
                    mOptTv.setText("播放");
                }
            });
        }

        mIsPlay = true;
        if (AuditionFragment.RECORD_BY_CALLBACK) {
            Params params = ZqEngineKit.getInstance().getParams();
            mPlayer.startPlayPcm(AuditionFragment.PCM_SAVE_PATH, params.getAudioChannels(),
                    params.getAudioSampleRate(), params.getAudioSampleRate() * params.getAudioChannels());
        } else {
            mPlayer.startPlay(AuditionFragment.AAC_SAVE_PATH);
        }
    }

    private void saveWorksStep1() {
        if (AuditionFragment.RECORD_BY_CALLBACK) {
            mPath = AuditionFragment.PCM_SAVE_PATH;
        } else {
            mPath = AuditionFragment.AAC_SAVE_PATH;
        }
        UploadTask uploadTask = UploadParams.newBuilder(mPath)
                .setFileType(UploadParams.FileType.audioAi)
                .startUploadAsync(new UploadCallback() {

                    @Override
                    public void onProgressNotInUiThread(long currentSize, long totalSize) {

                    }

                    @Override
                    public void onSuccessNotInUiThread(String url) {
                        MyLog.d(TAG, "onSuccess" + " url=" + url);
                        mUrl = url;
                        saveWorksStep2();
                    }

                    @Override
                    public void onFailureNotInUiThread(String msg) {
                        U.getToastUtil().showShort("保存失败");
                        mUrl = "";
                    }
                });
    }

    private void saveWorksStep2() {
        // TODO: 2019/5/22 上传服务器
        HashMap<String, Object> map = new HashMap<>();
        map.put("category", ProducationModel.TYPE_PRACTICE);
        if (mDuration <= 0) {
            // 这是个耗时操作
            mDuration = U.getMediaUtils().getDuration(mPath);
        }
        // 单位毫秒
        map.put("duration", mDuration);
        map.put("songID", mSongModel.getItemID());
        map.put("worksURL", mUrl);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        UserInfoServerApi mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(mUserInfoServerApi.addWorks(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mWorksId = result.getData().getIntValue("worksID");
                    if (SkrConfig.getInstance().worksShareOpen()) {
                        mSaveShareTv.setText("分享");
                        mSaveShareTv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.audition_share, 0, 0);
                        showShareDialog(true);
                    } else {
                        mSaveShareTv.setText("已保存");
                        mSaveShareTv.setClickable(false);
                    }
                } else {
                    mWorksId = 0;
                    U.getToastUtil().showShort("保存失败");
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                mWorksId = 0;
                U.getToastUtil().showShort("保存失败");
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                mWorksId = 0;
                U.getToastUtil().showShort("保存失败");

            }
        }, this);
    }

    private void showShareDialog(boolean containSaveTips) {
        if (mShareWorksDialog != null) {
            mShareWorksDialog.dismiss(false);
        }
        mShareWorksDialog = new ShareWorksDialog(PlayRecordFragment.this, mSongModel.getItemName(), containSaveTips);
        mShareWorksDialog.setData((int) MyUserInfoManager.getInstance().getUid(), MyUserInfoManager.getInstance().getNickName(), MyUserInfoManager.getInstance().getAvatar()
                , mSongModel.getItemName(), mUrl, mWorksId);
        mShareWorksDialog.show();
    }

    @Override
    protected boolean onBackPressed() {
        return true;
    }
}
