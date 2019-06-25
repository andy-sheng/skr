package com.module.playways.grab.room.view.chorus;

import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.grab.room.model.NewChorusLyricModel;
import com.module.playways.room.song.model.SongModel;
import com.zq.lyrics.LyricsManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;


/**
 * 合唱的歌唱者看到的板子
 */
public class DoubleChorusSelfSingCardView extends BaseChorusSelfCardView {
    UserInfoModel mFirstModel;
    UserInfoModel mSecondModel;
    DoubleRoomData mRoomData;
    ChorusSelfLyricAdapter mChorusSelfLyricAdapter;

    public final static String TAG = "DoubleChorusSelfSingCardView";

    public DoubleChorusSelfSingCardView(ViewStub viewStub, DoubleRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mLyricRecycleView = mParentView.findViewById(R.id.lyric_recycle_view);
        mLyricRecycleView.setLayoutManager(new LinearLayoutManager(mParentView.getContext(), LinearLayoutManager.VERTICAL, false));
        mChorusSelfLyricAdapter = new ChorusSelfLyricAdapter(mLeft, mRight, isForVideo(), mRoomData);
        mLyricRecycleView.setAdapter(mChorusSelfLyricAdapter);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        int statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(U.app());
        {
            RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) parentView.getLayoutParams();
            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
        }
    }

    public void updateLockState() {
        mChorusSelfLyricAdapter.notifyDataSetChanged();
    }

    public boolean playLyric(SongModel songModel, UserInfoModel firstModel, UserInfoModel secondModel) {
        mSongModel = songModel;
        mFirstModel = firstModel;
        mSecondModel = secondModel;

        return playLyric();
    }

    protected boolean playLyric() {
        if (mSongModel == null || mFirstModel == null || mSecondModel == null) {
            MyLog.e(TAG, "playLyric model is null");
            return false;
        }

        mLeft.reset();
        mRight.reset();

        mLeft.mUserInfoModel = mFirstModel;
        mLeft.mChorusRoundInfoModel = null;
        mRight.mUserInfoModel = mSecondModel;
        mRight.mChorusRoundInfoModel = null;

        tryInflate();
        setVisibility(View.VISIBLE);
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }

        mDisposable = LyricsManager.getLyricsManager(U.app())
                .loadGrabPlainLyric(mSongModel.getStandLrc())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) throws Exception {
                        List<ChorusSelfLyricAdapter.ChorusLineLyricModel> lyrics = new ArrayList<>();

                        if (isJSON2(result)) {
                            NewChorusLyricModel newChorusLyricModel = JSON.parseObject(result, NewChorusLyricModel.class);
                            for (NewChorusLyricModel.ItemsBean itemsBean : newChorusLyricModel.getItems()) {
                                UserInfoModel owner = (itemsBean.getTurn() == 1 ? mLeft.mUserInfoModel : mRight.mUserInfoModel);

                                if (lyrics.size() > 0) {
                                    ChorusSelfLyricAdapter.ChorusLineLyricModel bean = lyrics.get(lyrics.size() - 1);
                                    if (bean.getUserInfoModel().getUserId() == owner.getUserId()) {
                                        bean.lyrics += "\n" + itemsBean.getWords();
                                    } else {
                                        lyrics.add(new ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, itemsBean.getWords()));
                                    }
                                } else {
                                    lyrics.add(new ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, itemsBean.getWords()));
                                }
                            }
                        } else {
                            if (!TextUtils.isEmpty(result)) {
                                String[] strings = result.split("\n");
                                boolean turnLeft = true;
                                for (int i = 0; i < strings.length; i = i + 2) {
                                    UserInfoModel owner = turnLeft ? mLeft.mUserInfoModel : mRight.mUserInfoModel;
                                    turnLeft = !turnLeft;
                                    if ((i + 1) < strings.length) {
                                        lyrics.add(new ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, strings[i] + "\n" + strings[i + 1]));
                                    } else {
                                        lyrics.add(new ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, strings[i]));
                                    }
                                }
                            }
                        }
                        mChorusSelfLyricAdapter.computeFlag();
                        mChorusSelfLyricAdapter.setDataList(lyrics);
                        // 移到顶部
                        mLyricRecycleView.scrollToPosition(0);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        MyLog.e(TAG, "accept" + " throwable=" + throwable);
                    }
                });
        return true;
    }

    @Override
    protected boolean isForVideo() {
        return false;
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_video_chorus_self_sing_card_stub_layout;
    }
}
