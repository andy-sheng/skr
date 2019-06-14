package com.module.playways.grab.room.view.chorus;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;

import com.alibaba.fastjson.JSON;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabChorusUserStatusChangeEvent;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.NewChorusLyricModel;
import com.module.playways.grab.room.view.ExViewStub;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.room.song.model.SongModel;
import com.zq.lyrics.LyricsManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public abstract class BaseChorusSelfCardView extends ExViewStub {
    public final static String TAG = "ChorusSelfSingCardView";

    RecyclerView mLyricRecycleView;
    ChorusSelfLyricAdapter mChorusSelfLyricAdapter;
    GrabRoomData mRoomData;
    SongModel mSongModel;

    Disposable mDisposable;

    public static class DH {
        UserInfoModel mUserInfoModel;
        ChorusRoundInfoModel mChorusRoundInfoModel;

        public void reset() {
            mUserInfoModel = null;
            mChorusRoundInfoModel = null;
        }
    }

    DH mLeft = new DH();
    DH mRight = new DH();

    SelfSingCardView.Listener mListener;


    public BaseChorusSelfCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mLyricRecycleView = mParentView.findViewById(R.id.lyric_recycle_view);
        mLyricRecycleView.setLayoutManager(new LinearLayoutManager(mParentView.getContext(), LinearLayoutManager.VERTICAL, false));
        mChorusSelfLyricAdapter = new ChorusSelfLyricAdapter(mLeft, mRight);
        mLyricRecycleView.setAdapter(mChorusSelfLyricAdapter);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public abstract void playLyric();

    protected void playWithNoAcc() {
        if (mSongModel == null) {
            return;
        }
        tryInflate();
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
    }

    public boolean isJSON2(String str) {
        boolean result = false;
        try {
            Object obj = JSON.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    public void setListener(SelfSingCardView.Listener listener) {
        mListener = listener;
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        super.onViewAttachedToWindow(v);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabChorusUserStatusChangeEvent event) {
        if (mParentView == null || mParentView.getVisibility() == View.GONE) {
            return;
        }
        if (mLeft.mUserInfoModel != null) {
            if (event.mChorusRoundInfoModel.getUserID() == mLeft.mUserInfoModel.getUserId()) {
                mLeft.mChorusRoundInfoModel = event.mChorusRoundInfoModel;
            }
        }
        if (mRight.mUserInfoModel != null) {
            if (event.mChorusRoundInfoModel.getUserID() == mRight.mUserInfoModel.getUserId()) {
                mRight.mChorusRoundInfoModel = event.mChorusRoundInfoModel;
            }
        }
        mChorusSelfLyricAdapter.computeFlag();
        mChorusSelfLyricAdapter.notifyDataSetChanged();
    }

    public void destroy() {

    }
}
