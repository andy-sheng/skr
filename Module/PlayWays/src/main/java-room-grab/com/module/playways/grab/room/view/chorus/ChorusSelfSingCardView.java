package com.module.playways.grab.room.view.chorus;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabChorusUserStatusChangeEvent;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.normal.view.SingCountDownView;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.R;
import com.zq.lyrics.LyricsManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * 合唱的歌唱者看到的板子
 */
public class ChorusSelfSingCardView extends RelativeLayout {

    public final static String TAG = "ChorusSelfSingCardView";

    RecyclerView mLyricRecycleView;
    ChorusSelfLyricAdapter mChorusSelfLyricAdapter;
    SingCountDownView mSingCountDownView;

    GrabRoomData mRoomData;
    SongModel mSongModel;

    Disposable mDisposable;

    public static class DH {
        UserInfoModel mUserInfoModel;
        ChorusRoundInfoModel mChorusRoundInfoModel;

        public void reset() {
            mUserInfoModel =null;
            mChorusRoundInfoModel = null;
        }
    }

    DH mLeft = new DH();
    DH mRight = new DH();

    SelfSingCardView.Listener mListener;

    public ChorusSelfSingCardView(Context context) {
        super(context);
        init();
    }

    public ChorusSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChorusSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_chorus_self_sing_card_layout, this);
        mLyricRecycleView =  findViewById(R.id.lyric_recycle_view);
        mSingCountDownView = findViewById(R.id.sing_count_down_view);
        mLyricRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mChorusSelfLyricAdapter = new ChorusSelfLyricAdapter(mLeft, mRight);
        mLyricRecycleView.setAdapter(mChorusSelfLyricAdapter);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    public void playLyric() {
        if (mRoomData == null) {
            return;
        }
        mLeft.reset();
        mRight.reset();
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel != null) {
            List<ChorusRoundInfoModel> chorusRoundInfoModelList = infoModel.getChorusRoundInfoModels();
            if (chorusRoundInfoModelList != null && chorusRoundInfoModelList.size() >= 2) {
                int uid1 = chorusRoundInfoModelList.get(0).getUserID();
                int uid2 = chorusRoundInfoModelList.get(1).getUserID();
                mLeft.mUserInfoModel = mRoomData.getUserInfo(uid1);
                mLeft.mChorusRoundInfoModel = chorusRoundInfoModelList.get(0);
                mRight.mUserInfoModel = mRoomData.getUserInfo(uid2);
                mRight.mChorusRoundInfoModel = chorusRoundInfoModelList.get(1);
            }
            mSongModel = infoModel.getMusic();
            playWithNoAcc();
            mSingCountDownView.startPlay(0,infoModel.getSingTotalMs(),true);
        }
    }

    private void playWithNoAcc() {
        if (mSongModel == null) {
            return;
        }
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mDisposable = LyricsManager.getLyricsManager(U.app())
                .loadGrabPlainLyric(mSongModel.getStandLrc())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String result) throws Exception {
                        List<String> lyrics = new ArrayList<>();
                        if (!TextUtils.isEmpty(result)) {
                            String[] strings = result.split("\n");
                            for (int i = 0; i < strings.length; i = i + 2) {
                                if ((i + 1) < strings.length) {
                                    lyrics.add(strings[i] + "\n" + strings[i + 1]);
                                } else {
                                    lyrics.add(strings[i]);
                                }
                            }
                        }
                        mChorusSelfLyricAdapter.computeFlag();
                        mChorusSelfLyricAdapter.setDataList(lyrics);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        MyLog.d(TAG, "accept" + " throwable=" + throwable);
                    }
                });
    }

    public void setListener(SelfSingCardView.Listener listener) {
        mListener = listener;
        mSingCountDownView.setListener(listener);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            mSingCountDownView.reset();
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabChorusUserStatusChangeEvent event) {
        if (getVisibility() == GONE) {
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
