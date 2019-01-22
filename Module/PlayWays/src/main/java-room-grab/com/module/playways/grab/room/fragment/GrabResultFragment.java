package com.module.playways.grab.room.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.playways.RoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.rank.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 一唱到底结果页面
 */
public class GrabResultFragment extends BaseFragment {

    RoomData mRoomData;
    GrabResultInfoModel mGrabResultInfoModel;

    RelativeLayout mSingEndRecord;
    ExRelativeLayout mResultArea;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ImageView mSingNumBg;
    ImageView mSingNum;
    TextView mSingWantNum;
    TextView mSingSelfNum;
    TextView mSingSucessTv;
    TextView mSingAboveTv;
    LinearLayout mLlBottomArea;
    ExTextView mTvBack;
    ExTextView mTvAgain;

    @Override
    public int initView() {
        return R.layout.grab_result_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mSingEndRecord = (RelativeLayout) mRootView.findViewById(R.id.sing_end_record);
        mResultArea = (ExRelativeLayout) mRootView.findViewById(R.id.result_area);
        mAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mSingNumBg = (ImageView) mRootView.findViewById(R.id.sing_num_bg);
        mSingNum = (ImageView) mRootView.findViewById(R.id.sing_num);
        mSingWantNum = (TextView) mRootView.findViewById(R.id.sing_want_num);
        mSingSelfNum = (TextView) mRootView.findViewById(R.id.sing_self_num);
        mSingSucessTv = (TextView) mRootView.findViewById(R.id.sing_sucess_tv);
        mSingAboveTv = (TextView) mRootView.findViewById(R.id.sing_above_tv);
        mLlBottomArea = (LinearLayout) mRootView.findViewById(R.id.ll_bottom_area);
        mTvBack = (ExTextView) mRootView.findViewById(R.id.tv_back);
        mTvAgain = (ExTextView) mRootView.findViewById(R.id.tv_again);

        List<GrabResultInfoModel> list = mRoomData.getResultList();
        if (list == null || list.size() <= 0) {
            /**
             * 游戏结束会由sync或者push触发
             * push触发的话带着结果数据
             */
            syncFromServer();
        } else {
            bindData(list);
        }


        RxView.clicks(mTvBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    getActivity().finish();
                });

        RxView.clicks(mTvAgain)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                            .withInt("key_game_type", mRoomData.getGameType())
                            .withBoolean("selectSong", true)
                            .navigation();
                });
    }

    private void bindData(List<GrabResultInfoModel> list) {
        if (list == null || list.size() <= 0) {
            MyLog.w(TAG, "bindData" + " list = null");
            return;
        }
        for (GrabResultInfoModel resultInfoModel : list) {
            if (resultInfoModel != null && resultInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                this.mGrabResultInfoModel = resultInfoModel;
            }
        }

        if (mGrabResultInfoModel != null) {
            AvatarUtils.loadAvatarByUrl(mAvatarIv,
                    AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                            .setCircle(true)
                            .setBorderWidth(U.getDisplayUtils().dip2px(3))
                            .setBorderColor(Color.WHITE)
                            .build());
            mNameTv.setText(MyUserInfoManager.getInstance().getNickName());
            Drawable drawable = getNumDrawable(mGrabResultInfoModel.getWholeTimeSingCnt());
            mSingNum.setImageDrawable(drawable);
            mSingWantNum.setText(String.valueOf(mGrabResultInfoModel.getWantSingChanceCnt()));
            mSingSelfNum.setText(String.valueOf(mGrabResultInfoModel.getGetSingChanceCnt()));
            mSingSucessTv.setText(String.valueOf(mGrabResultInfoModel.getWholeTimeSingRatio() * 100) + "%");
            mSingAboveTv.setText(String.valueOf(mGrabResultInfoModel.getBeyondSkrerRatio() * 100) + "%");
        } else {
            MyLog.d(TAG, "还去同步了一次");
            syncFromServer();
        }
    }

    private void syncFromServer() {
        GrabRoomServerApi getStandResult = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        ApiMethods.subscribe(getStandResult.getStandResult(mRoomData.getGameId()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    int status = result.getData().getIntValue("status");
                    List<GrabResultInfoModel> resultInfoModels = JSON.parseArray(result.getData().getString("resultInfo"), GrabResultInfoModel.class);
                    if (resultInfoModels != null) {
                        bindData(resultInfoModels);
                    }
                }
            }
        }, this);
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (RoomData) data;
        }
    }

    @Override
    protected boolean onBackPressed() {
        getActivity().finish();
        return super.onBackPressed();
    }

    private Drawable getNumDrawable(int num) {
        Drawable drawable = null;
        switch (num) {
            case 0:
                drawable = U.getDrawable(R.drawable.zhanji_0);
                break;
            case 1:
                drawable = U.getDrawable(R.drawable.zhanji_1);
                break;
            case 2:
                drawable = U.getDrawable(R.drawable.zhanji_2);
                break;
            case 3:
                drawable = U.getDrawable(R.drawable.zhanji_3);
                break;
            case 4:
                drawable = U.getDrawable(R.drawable.zhanji_4);
                break;
            case 5:
                drawable = U.getDrawable(R.drawable.zhanji_5);
                break;
            case 6:
                drawable = U.getDrawable(R.drawable.zhanji_6);
                break;
            case 7:
                drawable = U.getDrawable(R.drawable.zhanji_7);
                break;
            case 8:
                drawable = U.getDrawable(R.drawable.zhanji_8);
                break;
        }

        return drawable;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
