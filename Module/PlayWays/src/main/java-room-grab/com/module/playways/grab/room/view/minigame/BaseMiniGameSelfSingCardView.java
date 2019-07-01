package com.module.playways.grab.room.view.minigame;

import android.graphics.Color;
import android.view.View;
import android.view.ViewStub;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ExViewStub;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.NewChorusLyricModel;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.room.song.model.MiniGameInfoModel;
import com.zq.live.proto.Common.EMiniGamePlayType;
import com.zq.lyrics.LyricsManager;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

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

    public BaseMiniGameSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mGrabRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mSvLyric = mParentView.findViewById(R.id.sv_lyric);
        mAvatarIv = mParentView.findViewById(R.id.avatar_iv);
        mFirstTipsTv = mParentView.findViewById(R.id.first_tips_tv);
        mTvLyric = mParentView.findViewById(R.id.tv_lyric);
    }

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
    }

    public boolean playLyric() {
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        if (infoModel == null) {
            MyLog.w(TAG, "infoModel 是空的");
            return false;
        }

        if (infoModel.getMusic() == null) {
            MyLog.w(TAG, "songModel 是空的");
            return false;
        }
//        mCharmsView.bindData(mGrabRoomData, (int) MyUserInfoManager.getInstance().getUid());
        mMiniGameInfoModel = infoModel.getMusic().getMiniGame();
        if (mMiniGameInfoModel == null) {
            MyLog.w(TAG, "MiniGame 是空的");
            return false;
        }

        tryInflate();
        mSvLyric.scrollTo(0, 0);
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
            setLyric(mTvLyric, mMiniGameSongUrl);
        } else {
            // TODO: 2019-05-29 不带歌词的,待补充
            mTvLyric.setText(mMiniGameInfoModel.getDisplayGameRule());
        }
        return true;
    }

    protected void setLyric(TextView lyricTv, String lyricUrl) {
        LyricsManager.getLyricsManager(U.app())
                .loadGrabPlainLyric(lyricUrl)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String o) throws Exception {
                        lyricTv.setText("");
                        if (U.getStringUtils().isJSON(o)) {
                            NewChorusLyricModel newChorusLyricModel = JSON.parseObject(o, NewChorusLyricModel.class);
                            lyricTv.append(mMiniGameInfoModel.getDisplayGameRule());
                            lyricTv.append("\n");
                            for (int i = 0; i < newChorusLyricModel.getItems().size() && i < 2; i++) {
                                lyricTv.append(newChorusLyricModel.getItems().get(i).getWords());
                                if (i == 0) {
                                    lyricTv.append("\n");
                                }
                            }
                        } else {
                            lyricTv.append(mMiniGameInfoModel.getDisplayGameRule());
                            lyricTv.append("\n");
                            lyricTv.append(o);
                        }
                    }
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
