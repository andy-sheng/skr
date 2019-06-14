package com.module.playways.grab.room.view.minigame;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.avatar.AvatarUtils;
import com.common.core.crash.IgnoreException;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.utils.U;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.NewChorusLyricModel;
import com.module.playways.grab.room.view.ExViewStub;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.room.song.model.MiniGameInfoModel;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.zq.live.proto.Common.EMiniGamePlayType;
import com.zq.lyrics.utils.SongResUtils;

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

public abstract class BaseMiniGameSelfSingCardView extends ExViewStub {
    public final static String TAG = "BaseMiniGameSelfSingCardView";

    GrabRoomData mGrabRoomData;
    MiniGameInfoModel mMiniGameInfoModel;
    String mMiniGameSongUrl;
    SelfSingCardView.Listener mListener;

    //    CharmsView mCharmsView;
    ScrollView mSvLyric;
    SimpleDraweeView mAvatarIv;
    TextView mFirstTipsTv;
    TextView mTvLyric;    //用来显示游戏内容
//    SingCountDownView mSingCountDownView;

    Disposable mDisposable;

    public BaseMiniGameSelfSingCardView(ViewStub viewStub,GrabRoomData roomData) {
        super(viewStub);
        mGrabRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mAvatarIv = mParentView.findViewById(R.id.avatar_iv);
        mFirstTipsTv = mParentView.findViewById(R.id.first_tips_tv);
        mSvLyric = mParentView.findViewById(R.id.sv_lyric);
        mTvLyric = mParentView.findViewById(R.id.tv_lyric);
    }

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
    }

    public void playLyric() {
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        if (infoModel == null) {
            MyLog.w(TAG, "infoModel 是空的");
            return;
        }

        if (infoModel.getMusic() == null) {
            MyLog.w(TAG, "songModel 是空的");
            return;
        }
        tryInflate();
        mSvLyric.scrollTo(0, 0);
//        mCharmsView.bindData(mGrabRoomData, (int) MyUserInfoManager.getInstance().getUid());
        mMiniGameInfoModel = infoModel.getMusic().getMiniGame();
        if (mMiniGameInfoModel == null) {
            MyLog.w(TAG, "MiniGame 是空的");
            return;
        }

//        int totalTs = infoModel.getSingTotalMs();
//        mSingCountDownView.setTagTvText(mMiniGameInfoModel.getGameName());
//        mSingCountDownView.startPlay(0, totalTs, true);

        if (infoModel.getMINIGameRoundInfoModels() != null && infoModel.getMINIGameRoundInfoModels().size() > 0) {
            UserInfoModel userInfoModel = mGrabRoomData.getUserInfo(infoModel.getMINIGameRoundInfoModels().get(0).getUserID());
            if (userInfoModel != null) {
                AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(Color.WHITE)
                        .build());
                String name = UserInfoManager.getInstance().getRemarkName(userInfoModel.getUserId(), userInfoModel.getNickname());
                if (name.length() > 7) {
                    name = name.substring(0, 7);
                }
                mFirstTipsTv.setText("【" + name + "】" + "先开始");
            } else {
                MyLog.w(TAG, "playLyric userInfoModel = null");
            }
        } else {
            MyLog.w(TAG, "playLyric getMINIGameRoundInfoModels = null");
        }


        if (mMiniGameInfoModel.getGamePlayType() == EMiniGamePlayType.EMGP_SONG_DETAIL.getValue()) {
            // TODO: 2019-05-29 带歌词的
            mMiniGameSongUrl = mMiniGameInfoModel.getSongInfo().getSongURL();
            File file = SongResUtils.getGrabLyricFileByUrl(mMiniGameSongUrl);

            if (file == null || !file.exists()) {
                MyLog.w(TAG, "playLyric is not in local file");
                fetchLyricTask();
            } else {
                MyLog.w(TAG, "playLyric is exist");
                final File fileName = SongResUtils.getGrabLyricFileByUrl(mMiniGameSongUrl);
                drawLyric(fileName);
            }
        } else {
            // TODO: 2019-05-29 不带歌词的,待补充
            mTvLyric.setText(mMiniGameInfoModel.getDisplayGameRule());
        }

    }

    protected void drawLyric(final File file) {
        MyLog.w(TAG, "file is " + file);
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                if (file != null && file.exists() && file.isFile()) {
                    try (BufferedSource source = Okio.buffer(Okio.source(file))) {
                        String lyric = source.readUtf8();
                        emitter.onNext(lyric);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                emitter.onComplete();
            }
        })
                .compose(((BaseActivity) mParentView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String o) {
                mTvLyric.setText("");
                if (isJSON2(o)) {
                    NewChorusLyricModel newChorusLyricModel = JSON.parseObject(o, NewChorusLyricModel.class);
                    mTvLyric.append(mMiniGameInfoModel.getDisplayGameRule());
                    mTvLyric.append("\n");
                    for (int i = 0; i < newChorusLyricModel.getItems().size() && i < 2; i++) {
                        mTvLyric.append(newChorusLyricModel.getItems().get(i).getWords());
                        if (i == 0) {
                            mTvLyric.append("\n");
                        }
                    }
                } else {
                    mTvLyric.append(mMiniGameInfoModel.getDisplayGameRule());
                    mTvLyric.append("\n");
                    mTvLyric.append(o);
                }
            }
        }, throwable -> MyLog.e(TAG, throwable));
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

    protected void fetchLyricTask() {
        if (TextUtils.isEmpty(mMiniGameSongUrl)) {
            MyLog.w(TAG, "fetchLyricTask mMiniGameSongUrl = null");
            return;
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }

        mDisposable = Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) {
                File newName = new File(SongResUtils.createStandLyricFileName(mMiniGameSongUrl));
                boolean isSuccess = U.getHttpUtils().downloadFileSync(mMiniGameSongUrl, newName, true, null);

                if (isSuccess) {
                    emitter.onNext(newName);
                    emitter.onComplete();
                } else {
                    emitter.onError(new IgnoreException("下载失败" + TAG));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RxRetryAssist(5, 1, false))
                .subscribe(file -> {
                    final File fileName = SongResUtils.getGrabLyricFileByUrl(mMiniGameSongUrl);
                    drawLyric(fileName);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    public void destroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }
}
