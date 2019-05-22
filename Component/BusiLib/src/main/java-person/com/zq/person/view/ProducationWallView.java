package com.zq.person.view;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
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
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.dialog.view.TipsDialogView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.media.UMusic;
import com.zq.dialog.ShareWorksDialog;
import com.zq.person.adapter.ProducationAdapter;
import com.zq.person.model.ProducationModel;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 作品墙view
 */
public class ProducationWallView extends RelativeLayout {

    public final static String TAG = "ProducationWallView";

    BaseFragment mFragment;
    UserInfoServerApi mUserInfoServerApi;
    int mUserId;

    SmartRefreshLayout mSmartRefresh;
    RecyclerView mProducationView;
    ProducationAdapter mProducationAdapter;

    IPlayer mIPlayer;

    int DEFAUAT_CNT = 20;       // 默认拉取一页的数量
    int offset;  // 拉照片偏移量

    DialogPlus mConfirmDialog;
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
        mSmartRefresh = (SmartRefreshLayout) findViewById(R.id.smart_refresh);

        mProducationView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        boolean hasDeleted = false;
        if (mUserId == MyUserInfoManager.getInstance().getUid()) {
            hasDeleted = true;
        }
        mProducationAdapter = new ProducationAdapter(new ProducationAdapter.Listener() {
            @Override
            public void onClickDele(int position, ProducationModel model) {
                if (position == mProducationAdapter.getSelectPlayPosition()) {
                    // 先停止播放
                    stopPlay();
                }
                // TODO: 2019/5/22 弹出删除确认框
                showConfirmDialog(model);
            }

            @Override
            public void onClickShare(int position, ProducationModel model) {
                // TODO: 2019/5/22 弹出分享框 需不需要先停止音乐
                if (position == mProducationAdapter.getSelectPlayPosition()) {
                    // 先停止播放
                    stopPlay();
                }
                showShareDialog(model);
            }

            @Override
            public void onClickPlay(int position, ProducationModel model) {
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

                playProducation(model);
            }

            @Override
            public void onClickPause(int position, ProducationModel model) {
                stopPlay();
            }
        }, hasDeleted);
        mProducationView.setAdapter(mProducationAdapter);

        mSmartRefresh.setEnableRefresh(false);
        mSmartRefresh.setEnableLoadMore(true);
        mSmartRefresh.setEnableLoadMoreWhenContentNotFull(false);
        mSmartRefresh.setEnableOverScrollDrag(true);
        mSmartRefresh.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getProducations(offset);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });
    }

    private void showConfirmDialog(final ProducationModel model) {
        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append("确定删除改作品吗？")
                .create();
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                .setMessageTip(stringBuilder)
                .setConfirmTip("确认")
                .setCancelTip("我再想想")
                .setConfirmBtnClickListener(new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        if (mConfirmDialog != null) {
                            mConfirmDialog.dismiss();
                        }
                        deleteProducation(model);
                    }
                })
                .setCancelBtnClickListener(new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        if (mConfirmDialog != null) {
                            mConfirmDialog.dismiss();
                        }
                    }
                })
                .build();

        mConfirmDialog = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .create();
        mConfirmDialog.show();

    }

    private void showShareDialog(final ProducationModel model) {
        if (mShareWorksDialog != null) {
            mShareWorksDialog.dismiss(false);
        }
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
            // TODO: 2019/5/22 微信分享不成功的原因可能是mUrl未上线，微信会检测这个
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


    private void deleteProducation(final ProducationModel model) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("worksID", model.getWorksID());
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mUserInfoServerApi.deleWorks(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mProducationAdapter.delete(model);
                }
            }
        }, mFragment);
    }

    public void playProducation(final ProducationModel model) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("toUserID", mUserId);
        map.put("worksID", model.getWorksID());
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mUserInfoServerApi.playWorks(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // TODO: 2019/5/22 播放次数客户端自己加一
                    model.setPlayCnt(model.getPlayCnt() + 1);
                    mProducationAdapter.update(model);
                }
            }
        }, mFragment);
    }

    private void addProducation(List<ProducationModel> list, int newOffset, int totalCnt, boolean isClear) {
        offset = newOffset;
        mSmartRefresh.finishLoadMore();
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
        mSmartRefresh.finishLoadMore();
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
        if (mConfirmDialog != null) {
            mConfirmDialog.dismiss(false);
        }
    }
}
