package com.module.rankingmode.prepare.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.avatar.AvatarUtils;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.HttpUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.presenter.PrepareSongPresenter;
import com.module.rankingmode.song.event.SongSelectEventClass;
import com.module.rankingmode.song.fragment.SongSelectFragment;
import com.module.rankingmode.song.model.SongModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

/**
 * 准备资源界面
 */
public class PrepareResFragment extends BaseFragment {
    ExImageView mIvTop;
    SimpleDraweeView mSongIcon;
    ExTextView mSongName;
    ExTextView mTvDuration;
    ExTextView mTvLyric;

    ExImageView ivBack;

    ExImageView ivStartMatch;

    private PrepareData mPrepareData = new PrepareData();

    HttpUtils.OnDownloadProgress onDownloadProgress;

    PrepareSongPresenter prepareSongPresenter;

    Handler handler;

    @Override
    public int initView() {
        return R.layout.prepare_res_fragment_layout;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SongSelectEventClass.PopSelectSongFragment event) {
        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                .setPopAbove(false)
                .setShowFragment(SongSelectFragment.class)
                .setPopFragment(PrepareResFragment.this).build());
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvTop = (ExImageView)mRootView.findViewById(R.id.iv_top);
        mSongIcon = (SimpleDraweeView)mRootView.findViewById(R.id.song_icon);
        mSongName = (ExTextView)mRootView.findViewById(R.id.song_name);
        mTvDuration = (ExTextView)mRootView.findViewById(R.id.tv_duration);
        mTvLyric = (ExTextView)mRootView.findViewById(R.id.tv_lyric);
        ivStartMatch = (ExImageView)mRootView.findViewById(R.id.iv_start_match);

        mSongName.setText(mPrepareData.getSongModel().getItemName());

        ivBack = (ExImageView)mRootView.findViewById(R.id.iv_back);


        AvatarUtils.loadAvatarByUrl(mSongIcon,
                AvatarUtils.newParamsBuilder(mPrepareData.getSongModel().getCover())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .setBorderColor(Color.WHITE)
                        .build());

        mTvDuration.setText(U.getDateTimeUtils().formatTimeStringForDate(mPrepareData.getSongModel().getTotalMs(), "mm:ss"));

        handler = new Handler();

        onDownloadProgress = new HttpUtils.OnDownloadProgress() {
            @Override
            public void onDownloaded(long downloaded, long totalLength) {
//                MyLog.d(TAG, "onDownloaded" + " downloaded=" + downloaded + " totalLength=" + totalLength);
            }

            @Override
            public void onCompleted(String localPath) {
                MyLog.d(TAG, "onCompleted" + " localPath=" + localPath);
                handler.post(() -> {
                    U.getToastUtil().showShort("歌曲资源已经准备好了");
                    ivStartMatch.setEnabled(true);
                });
            }

            @Override
            public void onCanceled() {
                MyLog.d(TAG, "onCanceled");
                handler.post(() -> {

                });
            }

            @Override
            public void onFailed() {
                MyLog.d(TAG, "onFailed");
                handler.post(() -> {

                });
            }
        };

        RxView.clicks(ivStartMatch)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((FragmentActivity) PrepareResFragment.this.getContext(), MatchFragment.class)
                            .setHideFragment(PrepareResFragment.class)
                            .setAddToBackStack(false)
                            .setHasAnimation(false)
                            .addDataBeforeAdd(0, mPrepareData)
                            .setFragmentDataListener(new FragmentDataListener() {
                                @Override
                                public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                                }
                            })
                            .build());
                });

        RxView.clicks(ivBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                            .setPopAbove(false)
                            .setShowFragment(SongSelectFragment.class)
                            .setPopFragment(PrepareResFragment.this).build());
                });

        ivStartMatch.setEnabled(false);

        prepareSongPresenter = new PrepareSongPresenter(onDownloadProgress, mPrepareData.getSongModel());
        prepareSongPresenter.prepareRes();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        prepareSongPresenter.cancelTask();
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if(type == 0){
            mPrepareData.setSongModel((SongModel) data);
        }
    }

    @Override
    protected boolean onBackPressed() {
        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                .setPopAbove(false)
                .setShowFragment(SongSelectFragment.class)
                .setPopFragment(PrepareResFragment.this).build());

        return true;
    }

    @Override
    public void toStaskTop() {
        MyLog.d(TAG, "toStaskTop" );
        mRootView.setVisibility(View.VISIBLE);
    }

    @Override
    public void pushIntoStash() {
        MyLog.d(TAG, "pushIntoStash" );
        mRootView.setVisibility(View.GONE);
    }
}
