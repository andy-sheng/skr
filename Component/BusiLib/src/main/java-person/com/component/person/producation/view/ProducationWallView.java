package com.component.person.producation.view;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.player.IPlayer;
import com.common.player.MyMediaPlayer;
import com.common.player.VideoPlayerAdapter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.SpanUtils;
import com.common.view.DebounceViewClickListener;
import com.component.busilib.R;
import com.component.person.producation.adapter.ProducationAdapter;
import com.component.person.producation.model.ProducationModel;
import com.component.person.view.RequestCallBack;
import com.dialog.view.TipsDialogView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.component.dialog.ShareWorksDialog;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 作品墙view
 */
public class ProducationWallView extends RelativeLayout {

    public final String TAG = "ProducationWallView";

    BaseFragment mFragment;
    RequestCallBack mCallBack;
    UserInfoServerApi mUserInfoServerApi;
    UserInfoModel mUserInfoModel;

    RecyclerView mProducationView;
    ProducationAdapter mAdapter;

    IPlayer mIPlayer;

    int DEFAUAT_CNT = 20;       // 默认拉取一页的数量
    int offset;  // 拉照片偏移量
    long mLastUpdateInfo = 0;    //上次更新成功时间

    DialogPlus mConfirmDialog;
    ShareWorksDialog mShareWorksDialog;

    public UserInfoModel getUserInfoModel() {
        return mUserInfoModel;
    }

    public void setUserInfoModel(UserInfoModel userInfoModel) {
        mUserInfoModel = userInfoModel;
    }

    public ProducationWallView(BaseFragment fragment, UserInfoModel userInfoModel, RequestCallBack requestCallBack) {
        super(fragment.getContext());
        this.mFragment = fragment;
        this.mUserInfoModel = userInfoModel;
        this.mCallBack = requestCallBack;
        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.producation_wall_view_layout, this);

        mProducationView = findViewById(R.id.producation_view);
        boolean isSelf = false;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mProducationView.setLayoutManager(linearLayoutManager);
        if (mUserInfoModel != null && mUserInfoModel.getUserId() == MyUserInfoManager.getInstance().getUid()) {
            isSelf = true;
        }
        mAdapter = new ProducationAdapter(new ProducationAdapter.Listener() {
            @Override
            public void onClickDele(int position, ProducationModel model) {
                if (model.getWorksID() == mAdapter.getPlayingWorksIdPosition()) {
                    // 先停止播放
                    stopPlay();
                }
                // TODO: 2019/5/22 弹出删除确认框
                showConfirmDialog(model);
            }

            @Override
            public void onClickShare(int position, ProducationModel model) {
                // TODO: 2019/5/22 弹出分享框 需不需要先停止音乐
                if (model.getWorksID() == mAdapter.getPlayingWorksIdPosition()) {
                    // 先停止播放
                    stopPlay();
                }
                showShareDialog(model);
            }

            @Override
            public void onClickPlayBtn(View view, boolean play, int position, ProducationModel model) {
                if (play) {
                    if (mIPlayer == null) {
                        mIPlayer = new MyMediaPlayer();
                        mIPlayer.setDecreaseVolumeEnd(true);
                        // 播放完毕
                        mIPlayer.setCallback(new VideoPlayerAdapter.PlayerCallbackAdapter() {
                            @Override
                            public void onCompletion() {
                                super.onCompletion();
                                mAdapter.setPlayPosition(-1);
                            }
                        });
                    }
                    mIPlayer.reset();
                    mIPlayer.startPlay(model.getWorksURL());
                    playProducation(model, position);
                    // 开始播放当前postion，
                    // 清楚上一个
                    mAdapter.setPlayPosition(model.getWorksID());
                } else {
                    if (mIPlayer != null) {
                        //mIPlayer.setCallback(null);
                        mIPlayer.pause();
                    }
                    // 不用刷新，优化下，防止闪动， icon 在 click 事件内部已经设置过了
                    mAdapter.setPlayPosition(-1);
                }
            }
        }, isSelf, linearLayoutManager);
        mProducationView.setAdapter(mAdapter);
    }

    private void showConfirmDialog(final ProducationModel model) {
        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append("确定删除该作品吗？")
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
        mShareWorksDialog = new ShareWorksDialog(mFragment, model.getName(), false);
        mShareWorksDialog.setData(model.getUserID(), model.getNickName(), model.getCover(), model.getName(), model.getWorksURL(), model.getWorksID());
        mShareWorksDialog.show();
    }

    public void stopPlay() {
        mAdapter.setPlayPosition(-1);
        if (mIPlayer != null) {
            mIPlayer.stop();
        }
    }

    public void getProducations(boolean isFlag) {
        long now = System.currentTimeMillis();
        if (!isFlag) {
            // 10分钟更新一次吧
            if ((now - mLastUpdateInfo) < 10 * 60 * 1000) {
                return;
            }
        }

        getProducations(0);
    }

    public void getMoreProducations() {
        getProducations(offset);
    }

    private void getProducations(final int offset) {
        ApiMethods.subscribe(mUserInfoServerApi.getWorks(mUserInfoModel.getUserId(), offset, DEFAUAT_CNT), new ApiObserver<ApiResult>() {
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
                    mAdapter.delete(model);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }, mFragment);
    }

    public void playProducation(final ProducationModel model, final int position) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("toUserID", mUserInfoModel.getUserId());
        map.put("worksID", model.getWorksID());
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mUserInfoServerApi.playWorks(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // TODO: 2019/5/22 播放次数客户端自己加一
                    model.setPlayCnt(model.getPlayCnt() + 1);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }, mFragment, new ApiMethods.RequestControl("playWorks", ApiMethods.ControlType.CancelThis));
    }

    private void addProducation(List<ProducationModel> list, int newOffset, int totalCnt, boolean isClear) {
        offset = newOffset;
        mLastUpdateInfo = System.currentTimeMillis();

        if (mCallBack != null) {
            mCallBack.onRequestSucess();
        }
        if (isClear) {
            mAdapter.getDataList().clear();
        }

        if (list != null && list.size() > 0) {
            mAdapter.getDataList().addAll(list);
            mAdapter.notifyDataSetChanged();
        } else {
            if (mAdapter.getDataList() != null && mAdapter.getDataList().size() > 0) {
                // 没有更多了
            } else {
                // 没有数据
            }
        }
    }

    private void loadProducationsFailed() {
        if (mCallBack != null) {
            mCallBack.onRequestSucess();
        }
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
