package com.module.playways.grab.room.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.anim.ObjectPlayControlTemplate;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.share.SharePanel;
import com.common.core.share.SharePlatform;
import com.common.core.share.ShareType;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.log.MyLog;
import com.common.player.IPlayer;
import com.common.player.VideoPlayerAdapter;
import com.common.player.mediaplayer.AndroidMediaPlayer;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;

import com.component.busilib.constans.GameModeType;
import com.component.busilib.view.BitmapTextView;
import com.module.RouterConstants;
import com.module.playways.R;

import com.module.playways.grab.room.GrabResultData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.playways.grab.room.model.WorksUploadModel;
import com.module.playways.grab.room.production.ResultProducationAdapter;
import com.module.playways.room.prepare.model.PrepareData;
import com.module.playways.room.room.model.score.ScoreResultModel;
import com.module.playways.room.room.model.score.ScoreStateModel;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMusic;
import com.zq.dialog.ShareWorksDialog;
import com.zq.level.view.LevelStarProgressBar;
import com.zq.level.view.NormalLevelView2;
import com.zq.person.model.ProducationModel;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 一唱到底结果页面 (有作品)
 */
public class GrabProductionFragment extends BaseFragment {

    public final static String TAG = "GrabProductionFragment";

    GrabRoomData mRoomData;
    GrabResultData mGrabResultData;
    ScoreStateModel mScoreStateModel;

    RelativeLayout mSingEndRecord;
    ExRelativeLayout mResultArea;
    ExTextView mLevelDescTv;
    LevelStarProgressBar mLevelProgress;
    BitmapTextView mSongNum;
    BitmapTextView mSongEndPer;
    BitmapTextView mBaodengNum;
    NormalLevelView2 mLevelView;
    LinearLayout mLlBottomArea;
    RecyclerView mProductionView;
    ExTextView mTvBack;
    ExTextView mTvAgain;
    ExTextView mTvShare;

    Handler mUiHandler = new Handler();
    UserInfoServerApi mUserInfoServerApi;

    ResultProducationAdapter mAdapter;
    IPlayer mIPlayer;

    ShareWorksDialog mShareWorksDialog;

    boolean mSaving = false;

    /**
     * 使用一个队列负责上传 作品保存成功 移除队列
     */
    ObjectPlayControlTemplate<WorksUploadModel, GrabProductionFragment> mObjectPlayControlTemplate = new ObjectPlayControlTemplate<WorksUploadModel, GrabProductionFragment>() {
        @Override
        protected GrabProductionFragment accept(WorksUploadModel cur) {
            if (!mSaving) {
                mSaving = true;
                return GrabProductionFragment.this;
            }
            return null;
        }

        @Override
        public void onStart(WorksUploadModel worksUploadModel, GrabProductionFragment grabProductionFragment) {
            if (TextUtils.isEmpty(worksUploadModel.getUrl())) {
                saveWorksStep1(worksUploadModel);
            } else {
                if (worksUploadModel.getWorksID() > 0) {
                    mSaving = false;
                    mObjectPlayControlTemplate.endCurrent(worksUploadModel);
                } else {
                    saveWorksStep2(worksUploadModel);
                }
            }
        }

        @Override
        protected void onEnd(WorksUploadModel worksUploadModel) {

        }
    };

    @Override
    public int initView() {
        return R.layout.grab_production_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mSingEndRecord = (RelativeLayout) mRootView.findViewById(R.id.sing_end_record);
        mResultArea = (ExRelativeLayout) mRootView.findViewById(R.id.result_area);
        mLevelDescTv = (ExTextView) mRootView.findViewById(R.id.level_desc_tv);
        mLevelProgress = (LevelStarProgressBar) mRootView.findViewById(R.id.level_progress);
        mSongNum = (BitmapTextView) mRootView.findViewById(R.id.song_num);
        mSongEndPer = (BitmapTextView) mRootView.findViewById(R.id.song_end_per);
        mBaodengNum = (BitmapTextView) mRootView.findViewById(R.id.baodeng_num);
        mLevelView = (NormalLevelView2) mRootView.findViewById(R.id.level_view);
        mLlBottomArea = (LinearLayout) mRootView.findViewById(R.id.ll_bottom_area);

        mProductionView = (RecyclerView) mRootView.findViewById(R.id.production_view);

        mTvBack = (ExTextView) mRootView.findViewById(R.id.tv_back);
        mTvAgain = (ExTextView) mRootView.findViewById(R.id.tv_again);
        mTvShare = (ExTextView) mRootView.findViewById(R.id.tv_share);

        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);

        mAdapter = new ResultProducationAdapter(new ResultProducationAdapter.Listener() {
            @Override
            public void onClickPlay(int position, WorksUploadModel model) {
                mAdapter.setSelectPosition(position);
                if (mIPlayer == null) {
                    mIPlayer = new AndroidMediaPlayer();
                    // 播放完毕
                    mIPlayer.setCallback(new VideoPlayerAdapter.PlayerCallbackAdapter() {
                        @Override
                        public void onCompletion() {
                            super.onCompletion();
                            //mIPlayer.stop();
                            stopPlay();
                        }
                    });
                }
                mIPlayer.reset();
                mIPlayer.startPlay(model.getLocalPath());
            }

            @Override
            public void onClickPause(int position, WorksUploadModel model) {
                stopPlay();
            }

            @Override
            public void onClickSaveAndShare(int position, WorksUploadModel model) {
                MyLog.d(TAG, "onClickSaveAndShare" + " position=" + position + " model=" + model);
                if (position == mAdapter.getSelectPosition()) {
                    stopPlay();
                }
                if (model.getWorksID() > 0) {
                    showShareDialog(model);
                } else {
                    mObjectPlayControlTemplate.add(model, true);
                }
            }
        });
        mProductionView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mProductionView.setAdapter(mAdapter);

        mTvBack.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mTvShare.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                SharePanel sharePanel = new SharePanel(getActivity());
                sharePanel.setShareContent("http://res-static.inframe.mobi/common/skr-share.png");
                sharePanel.show(ShareType.IMAGE_RUL);
            }
        });

        mTvAgain.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                PrepareData prepareData = new PrepareData();
                prepareData.setGameType(GameModeType.GAME_MODE_GRAB);
                prepareData.setTagId(mRoomData.getTagId());
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                        .withSerializable("prepare_data", prepareData)
                        .navigation();

                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });


        if (mRoomData != null) {
            mGrabResultData = mRoomData.getGrabResultData();
        }

        if (mGrabResultData == null) {
            /**
             * 游戏结束会由sync或者push触发
             * push触发的话带着结果数据
             */
            syncFromServer();
        } else {
            bindData();
        }

        U.getSoundUtils().preLoad(TAG, R.raw.grab_gameover);

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                U.getSoundUtils().play(GrabProductionFragment.TAG, R.raw.grab_gameover, 500);
            }
        }, 500);
    }

    public void stopPlay() {
        mAdapter.setSelectPosition(-1);
        if (mIPlayer != null) {
            mIPlayer.setCallback(null);
            mIPlayer.stop();
        }
    }

    private void saveWorksStep1(WorksUploadModel model) {
        UploadTask uploadTask = UploadParams.newBuilder(model.getLocalPath())
                .setFileType(UploadParams.FileType.audioAi)
                .startUploadAsync(new UploadCallback() {

                    @Override
                    public void onProgress(long currentSize, long totalSize) {

                    }

                    @Override
                    public void onSuccess(String url) {
                        MyLog.d(TAG, "onSuccess" + " url=" + url);
                        model.setUrl(url);
                        saveWorksStep2(model);

                    }

                    @Override
                    public void onFailure(String msg) {
                        U.getToastUtil().showShort("保存失败");
                        mSaving = false;
                        mObjectPlayControlTemplate.endCurrent(model);
                    }
                });
    }

    private void saveWorksStep2(WorksUploadModel model) {
        // TODO: 2019/5/22 上传服务器
        HashMap<String, Object> map = new HashMap<>();
        if (model.isBlight()) {
            // 一唱到底高光时刻
            map.put("category", ProducationModel.TYPE_STAND_HIGHLIGHT);
        } else {
            // 一唱到底
            map.put("category", ProducationModel.TYPE_STAND_NORMAL);
        }
        map.put("duration", String.valueOf(model.getSongModel().getTotalMs()));
        map.put("songID", model.getSongModel().getItemID());
        map.put("worksURL", model.getUrl());
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mUserInfoServerApi.addWorks(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    int worksID = result.getData().getIntValue("worksID");
                    model.setWorksID(worksID);
                    mAdapter.update(model);
                    showShareDialog(model);
                    U.getToastUtil().showShort("保存成功");
                    mSaving = false;
                    mObjectPlayControlTemplate.endCurrent(model);
                } else {
                    U.getToastUtil().showShort("保存失败");
                    mSaving = false;
                    mObjectPlayControlTemplate.endCurrent(model);
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                U.getToastUtil().showShort("保存失败");
                mSaving = false;
                mObjectPlayControlTemplate.endCurrent(model);
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                U.getToastUtil().showShort("保存失败");
                mSaving = false;
                mObjectPlayControlTemplate.endCurrent(model);
            }
        }, GrabProductionFragment.this);
    }

    private void showShareDialog(WorksUploadModel momentModel) {
        if (mShareWorksDialog != null) {
            mShareWorksDialog.dismiss(false);
        }
        mShareWorksDialog = new ShareWorksDialog(getContext(), momentModel.getSongModel().getDisplaySongName(), new ShareWorksDialog.ShareListener() {
            @Override
            public void onClickQQShare() {
                shareUrl(SharePlatform.QQ, momentModel);
            }

            @Override
            public void onClickQZoneShare() {
                shareUrl(SharePlatform.QZONE, momentModel);
            }

            @Override
            public void onClickWeixinShare() {
                shareUrl(SharePlatform.WEIXIN, momentModel);
            }

            @Override
            public void onClickQuanShare() {
                shareUrl(SharePlatform.WEIXIN_CIRCLE, momentModel);
            }
        });
        mShareWorksDialog.show();
    }

    private void shareUrl(SharePlatform sharePlatform, WorksUploadModel model) {
        if (model != null && model.getWorksID() != 0 && !TextUtils.isEmpty(model.getUrl())) {
            UMusic music = new UMusic(model.getUrl());
            music.setTitle("" + model.getSongModel().getItemName());
            music.setDescription(MyUserInfoManager.getInstance().getNickName() + "的撕歌精彩时刻");
            music.setThumb(new UMImage(getActivity(), MyUserInfoManager.getInstance().getAvatar()));

            StringBuilder sb = new StringBuilder();
            sb.append("http://dev.app.inframe.mobi/user/work")
                    .append("?skerId=").append(MyUserInfoManager.getInstance().getUid())
                    .append("&workId=").append(model.getWorksID());

            String mUrl = ApiManager.getInstance().findRealUrlByChannel(sb.toString());
            music.setmTargetUrl(mUrl);

            switch (sharePlatform) {
                case QQ:
                    new ShareAction(getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.QQ)
                            .share();
                    break;
                case QZONE:
                    new ShareAction(getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.QZONE)
                            .share();
                    break;
                case WEIXIN:
                    new ShareAction(getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.WEIXIN)
                            .share();
                    break;

                case WEIXIN_CIRCLE:
                    new ShareAction(getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                            .share();
                    break;
            }
        } else {
            MyLog.w(TAG, "shareUrl" + " sharePlatform=" + sharePlatform + " model=" + model);
        }

    }

    private void bindData() {
        if (mGrabResultData == null) {
            MyLog.w(TAG, "bindData" + " grabResultData = null");
            return;
        }

        if (mGrabResultData != null) {
            for (ScoreResultModel scoreResultModel : mGrabResultData.getScoreResultModels()) {
                if (scoreResultModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                    mScoreStateModel = scoreResultModel.getSeq(3);
                }
            }

            if (mScoreStateModel != null && mGrabResultData.getGrabResultInfoModel() != null) {
                mLevelView.bindData(mScoreStateModel.getMainRanking(), mScoreStateModel.getSubRanking());
                mLevelDescTv.setText("" + mScoreStateModel.getSubRanking() + "段");
                int progress = 0;
                if (mScoreStateModel.getMaxExp() != 0) {
                    progress = mScoreStateModel.getCurrExp() * 100 / mScoreStateModel.getMaxExp();
                }
                mLevelProgress.setCurProgress(progress);
                mSongNum.setText(String.valueOf(mGrabResultData.getGrabResultInfoModel().getWholeTimeSingCnt()) + "");
                mSongEndPer.setText(String.valueOf(mGrabResultData.getGrabResultInfoModel().getWholeTimeSingRatio()) + "");
                mBaodengNum.setText(String.valueOf(mGrabResultData.getGrabResultInfoModel().getOtherBlightCntTotal()) + "");
            }

            if (mRoomData.getWorksUploadModel() != null) {
                mAdapter.setDataList(mRoomData.getWorksUploadModel());
            }
        } else {
            MyLog.w(TAG, "bindData 数据为空了");
        }
    }

    private void syncFromServer() {
        if (mRoomData != null) {
            GrabRoomServerApi getStandResult = ApiManager.getInstance().createService(GrabRoomServerApi.class);
            ApiMethods.subscribe(getStandResult.getStandResult(mRoomData.getGameId()), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        GrabResultInfoModel resultInfoModel = JSON.parseObject(result.getData().getString("resultInfo"), GrabResultInfoModel.class);
                        List<ScoreResultModel> scoreResultModels = JSON.parseArray(result.getData().getString("userScoreResult"), ScoreResultModel.class);
                        if (resultInfoModel != null && scoreResultModels != null) {
                            mGrabResultData = new GrabResultData(resultInfoModel, scoreResultModels);
                            mRoomData.setGrabResultData(mGrabResultData);
                            bindData();
                        } else {
                            MyLog.d(TAG, "syncFromServer" + " info=null");
                        }

                    }
                }
            }, this);
        } else {
            MyLog.d(TAG, "syncFromServer" + " mRoomData == null Why?");
        }
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (GrabRoomData) data;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(GrabProductionFragment.TAG);
        mUiHandler.removeCallbacksAndMessages(null);
        if (mIPlayer != null) {
            mIPlayer.stop();
            mIPlayer.release();
        }
        if (mShareWorksDialog != null) {
            mShareWorksDialog.dismiss(false);
        }
        if (mObjectPlayControlTemplate != null) {
            mObjectPlayControlTemplate.destroy();
        }
    }

    @Override
    protected boolean onBackPressed() {
        if (getActivity() != null) {
            getActivity().finish();
        }
        return true;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
