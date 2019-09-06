package com.module.playways.doubleplay.view;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ExViewStub;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.playways.R;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.doubleplay.loadsir.LyricLoadErrorCallBack;
import com.module.playways.grab.room.model.NewChorusLyricModel;
import com.module.playways.grab.room.view.chorus.BaseChorusSelfCardView;
import com.module.playways.grab.room.view.chorus.ChorusSelfLyricAdapter;
import com.module.playways.room.song.model.SongModel;
import com.component.lyrics.LyricsManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static com.module.playways.grab.room.view.chorus.ChorusSelfLyricAdapter.ChorusLineLyricModel.DOUBLE_TYPE;

/**
 * 合唱的歌唱者看到的板子
 */
public class DoubleChorusSelfSingCardView extends ExViewStub {
    public final String TAG = "DoubleChorusSelfSingCardView";
    UserInfoModel mFirstModel;
    UserInfoModel mSecondModel;
    DoubleRoomData mRoomData;
    ChorusSelfLyricAdapter mChorusSelfLyricAdapter;
    RecyclerView mLyricRecycleView;
    SongModel mSongModel;
    Disposable mDisposable;
    LoadService mLoadService;

    BaseChorusSelfCardView.DH mLeft = new BaseChorusSelfCardView.DH();
    BaseChorusSelfCardView.DH mRight = new BaseChorusSelfCardView.DH();

    public DoubleChorusSelfSingCardView(ViewStub viewStub, DoubleRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mLyricRecycleView = getMParentView().findViewById(R.id.lyric_recycle_view);
        mLyricRecycleView.setLayoutManager(new LinearLayoutManager(getMParentView().getContext(), LinearLayoutManager.VERTICAL, false));
        mChorusSelfLyricAdapter = new ChorusSelfLyricAdapter(mLeft, mRight, false, mRoomData);
        mLyricRecycleView.setAdapter(mChorusSelfLyricAdapter);
        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new LyricLoadErrorCallBack())
                .build();
        mLoadService = mLoadSir.register(mLyricRecycleView, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                playLyric();
            }
        });
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

        mChorusSelfLyricAdapter.getDataList().clear();
        mChorusSelfLyricAdapter.notifyDataSetChanged();

        mDisposable = LyricsManager.INSTANCE
                .loadGrabPlainLyric(mSongModel.getStandLrc())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) throws Exception {
                        List<ChorusSelfLyricAdapter.ChorusLineLyricModel> lyrics = new ArrayList<>();

                        if (U.getStringUtils().isJSON(result)) {
                            NewChorusLyricModel newChorusLyricModel = JSON.parseObject(result, NewChorusLyricModel.class);
                            for (NewChorusLyricModel.ItemsBean itemsBean : newChorusLyricModel.getItems()) {
                                UserInfoModel owner = (itemsBean.getTurn() == 1 ? mLeft.mUserInfoModel : mRight.mUserInfoModel);

                                if (lyrics.size() > 0) {
                                    ChorusSelfLyricAdapter.ChorusLineLyricModel bean = lyrics.get(lyrics.size() - 1);
                                    if (bean.getUserInfoModel().getUserId() == owner.getUserId()) {
                                        bean.lyrics += "\n" + itemsBean.getWords();
                                    } else {
                                        lyrics.add(new ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, itemsBean.getWords(), DOUBLE_TYPE));
                                    }
                                } else {
                                    lyrics.add(new ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, itemsBean.getWords(), DOUBLE_TYPE));
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
                                        lyrics.add(new ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, strings[i] + "\n" + strings[i + 1], DOUBLE_TYPE));
                                    } else {
                                        lyrics.add(new ChorusSelfLyricAdapter.ChorusLineLyricModel(owner, strings[i], DOUBLE_TYPE));
                                    }
                                }
                            }
                        }
                        mChorusSelfLyricAdapter.setSongModel(mSongModel);
                        mChorusSelfLyricAdapter.computeFlag();
                        mChorusSelfLyricAdapter.setDataList(lyrics);
                        // 移到顶部
                        mLyricRecycleView.scrollToPosition(0);
                        mLoadService.showSuccess();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        MyLog.e(TAG, "accept" + " throwable=" + throwable);
                        mLoadService.showCallback(LyricLoadErrorCallBack.class);
                    }
                });
        return true;
    }

    public void destroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_video_chorus_self_sing_card_stub_layout;
    }
}
