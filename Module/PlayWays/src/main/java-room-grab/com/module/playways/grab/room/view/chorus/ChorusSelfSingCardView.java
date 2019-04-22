package com.module.playways.grab.room.view.chorus;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.countdown.CircleCountDownView;
import com.component.busilib.view.BitmapTextView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.song.model.SongModel;
import com.module.rank.R;
import com.zq.lyrics.LyricsManager;

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

    ImageView mIvTag;
    CircleCountDownView mCircleCountDownView;
    BitmapTextView mCountDownTv;

    GrabRoomData mRoomData;
    SongModel mSongModel;

    Disposable mDisposable;

    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;

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
        mLyricRecycleView = (RecyclerView) findViewById(R.id.lyric_recycle_view);
        mIvTag = (ImageView) this.findViewById(R.id.iv_tag);
        mCircleCountDownView = (CircleCountDownView) this.findViewById(R.id.circle_count_down_view);
        mCountDownTv = (BitmapTextView) this.findViewById(R.id.count_down_tv);

        mLyricRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mChorusSelfLyricAdapter = new ChorusSelfLyricAdapter();
        mLyricRecycleView.setAdapter(mChorusSelfLyricAdapter);
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    public void playLyric() {
        if (mRoomData == null) {
            return;
        }
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel != null) {
            List<ChorusRoundInfoModel> chorusRoundInfoModelList = infoModel.getChorusRoundInfoModels();
            if (chorusRoundInfoModelList != null && chorusRoundInfoModelList.size() >= 2) {
                int uid1 = chorusRoundInfoModelList.get(0).getUserID();
                int uid2 = chorusRoundInfoModelList.get(1).getUserID();
                mLeftUserInfoModel = mRoomData.getUserInfo(uid1);
                mRightUserInfoModel = mRoomData.getUserInfo(uid2);
            }
        }

        mChorusSelfLyricAdapter.setUserInfos(mLeftUserInfoModel, mRightUserInfoModel);
        playLyric(infoModel);
    }

    public void playLyric(GrabRoundInfoModel infoModel) {
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的");
            return;
        }

        mSongModel = infoModel.getMusic();
        playWithNoAcc();
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
                        mChorusSelfLyricAdapter.setDataList(lyrics);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        MyLog.d(TAG, "accept" + " throwable=" + throwable);
                    }
                });
    }

}
