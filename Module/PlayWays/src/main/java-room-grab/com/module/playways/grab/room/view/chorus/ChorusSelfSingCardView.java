package com.module.playways.grab.room.view.chorus;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.countdown.CircleCountDownView;
import com.component.busilib.view.BitmapTextView;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.room.song.model.SongModel;
import com.module.rank.R;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSource;
import okio.Okio;


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
        // TODO: 2019/4/21 导唱的小文件即歌词
        File file = SongResUtils.getGrabLyricFileByUrl(mSongModel.getStandLrc());
        if (file == null || !file.exists()) {
            MyLog.w(TAG, "playLyric is not in local file");
            fetchLyricTask(mSongModel);
        } else {
            MyLog.w(TAG, "playLyric is exist");
            drawLyric(file);
        }
    }

    /**
     * 拉取一唱到底的歌词字符串
     *
     * @param songModel
     */
    private void fetchLyricTask(SongModel songModel) {
        MyLog.w(TAG, "fetchLyricTask" + " songModel=" + songModel);
        mDisposable = Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) {
                File tempFile = new File(SongResUtils.createStandLyricTempFileName(songModel.getStandLrc()));
                boolean isSuccess = U.getHttpUtils().downloadFileSync(songModel.getStandLrc(), tempFile, null);
                File oldName = new File(SongResUtils.createStandLyricTempFileName(songModel.getStandLrc()));
                File newName = new File(SongResUtils.createStandLyricFileName(songModel.getStandLrc()));
                if (isSuccess) {
                    if (oldName != null && oldName.renameTo(newName)) {
                        MyLog.w(TAG, "已重命名");
                        emitter.onNext(newName);
                        emitter.onComplete();
                    } else {
                        MyLog.w(TAG, "Error");
                        emitter.onError(new Throwable("重命名错误"));
                    }
                } else {
                    emitter.onError(new Throwable("下载失败, 文件地址是" + songModel.getStandLrc()));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RxRetryAssist(5, 1, false))
//                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(file -> {
                    final File fileName = SongResUtils.getGrabLyricFileByUrl(songModel.getStandLrc());
                    drawLyric(fileName);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }

    /**
     * 画出一唱到底歌词字符串
     *
     * @param file
     */
    private void drawLyric(final File file) {
        MyLog.w(TAG, "file is " + file);
        Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> emitter) {
                if (file != null && file.exists() && file.isFile()) {
                    try (BufferedSource source = Okio.buffer(Okio.source(file))) {
                        String result = source.readUtf8();
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
                        emitter.onNext(lyrics);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                emitter.onComplete();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new Consumer<List<String>>() {
            @Override
            public void accept(List<String> o) {
                mChorusSelfLyricAdapter.setDataList(o);
            }
        }, throwable -> MyLog.e(TAG, throwable));
    }
}
