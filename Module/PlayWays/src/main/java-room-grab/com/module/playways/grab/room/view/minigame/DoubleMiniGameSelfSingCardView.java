package com.module.playways.grab.room.view.minigame;

import android.graphics.Color;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.avatar.AvatarUtils;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.NewChorusLyricModel;
import com.module.playways.room.song.model.SongModel;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.zq.lyrics.utils.SongResUtils;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSource;
import okio.Okio;

public class DoubleMiniGameSelfSingCardView extends BaseMiniGameSelfSingCardView {
    public final static String TAG = "DoubleMiniGameSelfSingCardView";
    SongModel mSongModel;
    //是不是这个人点的歌儿
    boolean mIsOwner = false;

    String mOwnerName;

    String mAvatar;

    public DoubleMiniGameSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub, roomData);
    }

    @Override
    protected void init(View parentView) {
        super.init(parentView);
        int statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(U.app());
        {
            RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) parentView.getLayoutParams();
            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
        }
    }

    public boolean playLyric(SongModel songModel, boolean isOwner, String ownerName, String avatar) {
        mIsOwner = isOwner;
        mOwnerName = ownerName;
        mSongModel = songModel;
        mAvatar = avatar;

        return playLyric();
    }

    public boolean playLyric() {
        if (mSongModel == null) {
            MyLog.w(TAG, "infoModel 是空的");
            return false;
        }

        mMiniGameInfoModel = mSongModel.getMiniGame();
        if (mMiniGameInfoModel == null) {
            MyLog.w(TAG, "MiniGame 是空的");
            return false;
        }

        tryInflate();
        mSvLyric.scrollTo(0, 0);
//        int totalTs = infoModel.getSingTotalMs();
//        mSingCountDownView.setTagTvText(mMiniGameInfoModel.getGameName());
//        mSingCountDownView.startPlay(0, totalTs, true);

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mAvatar)
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColor(Color.WHITE)
                .build());

        if (mOwnerName.length() > 7) {
            mOwnerName = mOwnerName.substring(0, 7);
        }

        mFirstTipsTv.setText("【" + mOwnerName + "】" + "先开始");

        // TODO: 2019-05-29 带歌词的
        mMiniGameSongUrl = mSongModel.getMiniGame().getSongInfo().getSongURL();
        File file = SongResUtils.getGrabLyricFileByUrl(mMiniGameSongUrl);

        if (file == null || !file.exists()) {
            MyLog.w(TAG, "playLyric is not in local file");
            fetchLyricTask();
        } else {
            MyLog.w(TAG, "playLyric is exist");
            final File fileName = SongResUtils.getGrabLyricFileByUrl(mMiniGameSongUrl);
            drawLyric(fileName);
        }
        return true;
    }

//    protected void drawLyric(final File file) {
//        MyLog.w(TAG, "file is " + file);
//        Observable.create(new ObservableOnSubscribe<String>() {
//            @Override
//            public void subscribe(ObservableEmitter<String> emitter) {
//                if (file != null && file.exists() && file.isFile()) {
//                    try (BufferedSource source = Okio.buffer(Okio.source(file))) {
//                        String lyric = source.readUtf8();
//                        emitter.onNext(lyric);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                emitter.onComplete();
//            }
//        })
//                .compose(((BaseActivity) mParentView.getContext()).bindUntilEvent(ActivityEvent.DESTROY))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io()).subscribe(new Consumer<String>() {
//            @Override
//            public void accept(String o) {
//                mTvLyric.setText("");
//                if (isJSON2(o)) {
//                    NewChorusLyricModel newChorusLyricModel = JSON.parseObject(o, NewChorusLyricModel.class);
//                    mTvLyric.append(mMiniGameInfoModel.getDisplayGameRule());
//                    mTvLyric.append("\n");
//                    for (int i = 0; i < newChorusLyricModel.getItems().size() && i < 2; i++) {
//                        mTvLyric.append(newChorusLyricModel.getItems().get(i).getWords());
//                        if (i == 0) {
//                            mTvLyric.append("\n");
//                        }
//                    }
//                } else {
//                    mTvLyric.append(mMiniGameInfoModel.getDisplayGameRule());
//                    mTvLyric.append("\n");
//                    mTvLyric.append(o);
//                }
//            }
//        }, throwable -> MyLog.e(TAG, throwable));
//    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_video_mini_game_self_sing_card_stub_layout;
    }
}
