package com.zq.person.view;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.share.SharePlatform;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.log.MyLog;
import com.common.player.IPlayer;
import com.common.player.VideoPlayerAdapter;
import com.common.player.exoplayer.ExoPlayer;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.component.busilib.R;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMusic;
import com.zq.dialog.ShareWorksDialog;
import com.zq.person.adapter.ProducationAdapter;
import com.zq.person.model.ProducationModel;

import java.util.List;

/**
 * 作品墙view
 */
public class ProducationWallView extends RelativeLayout {

    public final static String TAG = "ProducationWallView";

    BaseFragment mFragment;
    UserInfoServerApi mUserInfoServerApi;
    int mUserId;

    RecyclerView mProducationView;
    ProducationAdapter mProducationAdapter;

    IPlayer mIPlayer;

    int DEFAUAT_CNT = 20;       // 默认拉取一页的数量
    int offset;  // 拉照片偏移量
    int mSelectPlayPosition = -1;  //选中播放的id

    ShareWorksDialog mShareWorksDialog;

    public ProducationWallView(BaseFragment fragment, int userId) {
        super(fragment.getContext());
        this.mFragment = fragment;
        this.mUserId = userId;
        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.producation_wall_view_layout, this);

        mProducationView = (RecyclerView) findViewById(R.id.producation_view);
        mProducationView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        boolean hasDeleted = false;
        if (mUserId == MyUserInfoManager.getInstance().getUid()) {
            hasDeleted = true;
        }
        mProducationAdapter = new ProducationAdapter(new ProducationAdapter.Listener() {
            @Override
            public void onClickDele(int position, ProducationModel model) {
                if (position == mSelectPlayPosition) {
                    // 先停止播放
                    stopPlay();
                }
                // TODO: 2019/5/22 弹出删除确认框
            }

            @Override
            public void onClickShare(int position, ProducationModel model) {
                // TODO: 2019/5/22 弹出分享框 需不需要先停止音乐
                if (position == mSelectPlayPosition) {
                    // 先停止播放
                    stopPlay();
                }
                showShareDialog(model);
            }

            @Override
            public void onClickPlay(int position, ProducationModel model) {
                mSelectPlayPosition = position;
                mProducationAdapter.setSelectPlayPosition(position);
                if (mIPlayer == null) {
                    mIPlayer = new ExoPlayer();
                    // 播放完毕
                    mIPlayer.setCallback(new VideoPlayerAdapter.PlayerCallbackAdapter() {
                        @Override
                        public void onCompletion() {
                            super.onCompletion();
                            stopPlay();
                        }
                    });
                }
                mIPlayer.reset();
                mIPlayer.startPlay(model.getWorksURL());

                // TODO: 2019/5/22  加上播放的接口
            }

            @Override
            public void onClickPause(int position, ProducationModel model) {
                stopPlay();
            }
        }, hasDeleted);
        mProducationView.setAdapter(mProducationAdapter);
    }

    private void showShareDialog(final ProducationModel model) {
        if (mShareWorksDialog == null) {
            mShareWorksDialog = new ShareWorksDialog(getContext(), model.getName(), new ShareWorksDialog.ShareListener() {
                @Override
                public void onClickQQShare() {
                    shareUrl(SharePlatform.QQ, model);
                }

                @Override
                public void onClickQZoneShare() {
                    shareUrl(SharePlatform.QZONE, model);
                }

                @Override
                public void onClickWeixinShare() {
                    shareUrl(SharePlatform.WEIXIN, model);
                }

                @Override
                public void onClickQuanShare() {
                    shareUrl(SharePlatform.WEIXIN_CIRCLE, model);
                }
            });
        }
        mShareWorksDialog.show();
    }

    private void shareUrl(SharePlatform sharePlatform, ProducationModel model) {
        if (model != null && model.getWorksID() != 0 && !TextUtils.isEmpty(model.getWorksURL())) {
            UMusic music = new UMusic(model.getWorksURL());
            music.setTitle("" + model.getName());
            music.setDescription(MyUserInfoManager.getInstance().getNickName() + "的撕歌精彩时刻");
            music.setThumb(new UMImage(mFragment.getActivity(), MyUserInfoManager.getInstance().getAvatar()));

            StringBuilder sb = new StringBuilder();
            sb.append("http://dev.app.inframe.mobi/user/work")
                    .append("?skerId=").append(String.valueOf(MyUserInfoManager.getInstance().getUid()))
                    .append("&workId=").append(String.valueOf(model.getWorksID()));
            String mUrl = ApiManager.getInstance().findRealUrlByChannel(sb.toString());
            music.setmTargetUrl(mUrl);

            switch (sharePlatform) {
                case QQ:
                    new ShareAction(mFragment.getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.QQ)
                            .share();
                    break;
                case QZONE:
                    new ShareAction(mFragment.getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.QZONE)
                            .share();
                    break;
                case WEIXIN:
                    new ShareAction(mFragment.getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.WEIXIN)
                            .share();
                    break;

                case WEIXIN_CIRCLE:
                    new ShareAction(mFragment.getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                            .share();
                    break;
            }
        } else {
            MyLog.w(TAG, "shareUrl" + " sharePlatform=" + sharePlatform + " model=" + model);
        }
    }

    public void stopPlay() {
        mSelectPlayPosition = -1;
        mProducationAdapter.setSelectPlayPosition(-1);
        if (mIPlayer != null) {
            mIPlayer.setCallback(null);
            mIPlayer.stop();
        }
    }

    public void getProducations() {
        getProducations(0);
    }

    public void getProducations(final int offset) {
        ApiMethods.subscribe(mUserInfoServerApi.getWorks(mUserId, offset, DEFAUAT_CNT), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    int totalCnt = result.getData().getIntValue("totalCnt");
                    int newOffset = result.getData().getIntValue("offset");
                    List<ProducationModel> list = JSON.parseArray(result.getData().getString("works"), ProducationModel.class);
                    if (offset == 0) {
                        addProducation(list, newOffset, totalCnt, true);
                    } else {
                        addProducation(list, newOffset, totalCnt, false);
                    }
                } else {
                    loadProducationsFailed();
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                loadProducationsFailed();
            }
        }, mFragment);
    }

    private void addProducation(List<ProducationModel> list, int newOffset, int totalCnt, boolean isClear) {
        offset = newOffset;
        if (isClear) {
            mProducationAdapter.getDataList().clear();
        }

        if (list != null && list.size() > 0) {
            mProducationAdapter.getDataList().addAll(list);
            mProducationAdapter.notifyDataSetChanged();
        } else {
            if (mProducationAdapter.getDataList() != null && mProducationAdapter.getDataList().size() > 0) {
                // 没有更多了
            } else {
                // 没有数据
            }
        }
    }

    private void loadProducationsFailed() {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mIPlayer != null) {
            mIPlayer.setCallback(null);
            mIPlayer.stop();
            mIPlayer.release();
        }
        if (mShareWorksDialog != null) {
            mShareWorksDialog.dismiss(false);
        }
    }
}
