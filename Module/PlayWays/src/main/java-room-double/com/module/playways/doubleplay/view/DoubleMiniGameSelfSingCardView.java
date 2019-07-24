package com.module.playways.doubleplay.view;

import android.graphics.Color;
import android.view.View;
import android.view.ViewStub;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ExViewStub;
import com.facebook.drawee.view.SimpleDraweeView;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.playways.R;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.doubleplay.loadsir.LyricLoadErrorCallBack;
import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic;
import com.module.playways.doubleplay.pbLocalModel.LocalGameItemInfo;
import com.module.playways.grab.room.model.NewChorusLyricModel;
import com.module.playways.room.song.model.MiniGameInfoModel;
import com.zq.live.proto.Common.EGameType;
import com.zq.live.proto.Common.EMiniGamePlayType;
import com.component.lyrics.LyricsManager;

import io.reactivex.functions.Consumer;

public class DoubleMiniGameSelfSingCardView extends ExViewStub {
    public final String TAG = "DoubleMiniGameSelfSingCardView";
    LocalCombineRoomMusic mMusic;
    //是不是这个人点的歌儿
    boolean mIsOwner = false;
    UserInfoModel mOwnerInfo;
    DoubleRoomData mDoubleRoomData;

    public MiniGameInfoModel mMiniGameInfoModel;
    public String mMiniGameSongUrl;
    public ScrollView mSvLyric;
    public SimpleDraweeView mAvatarIv;
    public TextView mFirstTipsTv;
    public TextView mTvLyric;    //用来显示游戏内容
    LoadService mLoadService;

    public DoubleMiniGameSelfSingCardView(ViewStub viewStub) {
        super(viewStub);
    }

    @Override
    protected void init(View parentView) {
        mSvLyric = mParentView.findViewById(R.id.sv_lyric);
        mAvatarIv = mParentView.findViewById(R.id.avatar_iv);
        mFirstTipsTv = mParentView.findViewById(R.id.first_tips_tv);
        mTvLyric = mParentView.findViewById(R.id.tv_lyric);
        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new LyricLoadErrorCallBack())
                .build();
        mLoadService = mLoadSir.register(mSvLyric, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                playLyric();
            }
        });
    }

    public void updateLockState() {
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mDoubleRoomData.getAvatarById(mMusic.getUserID()))
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColor(Color.WHITE)
                .build());

        String mOwnerName = mOwnerInfo.getNickname();
        if (mOwnerInfo.getNickname().length() > 7) {
            mOwnerName = mOwnerName.substring(0, 7);
        }
        mFirstTipsTv.setTextColor(U.getColor(R.color.black_trans_60));
        mFirstTipsTv.setText("【" + mOwnerName + "】" + "先开始");
    }

    public boolean playLyric(LocalGameItemInfo localGameItemInfo) {
        if (localGameItemInfo == null) {
            MyLog.w(TAG, "playLyric mCur 是空的");
            return false;
        }

        tryInflate();
        mSvLyric.scrollTo(0, 0);

        mTvLyric.setTextColor(U.getColor(R.color.black_trans_60));

        if (localGameItemInfo.getGameType() == EGameType.GT_Music.getValue()) {
            mTvLyric.setText(localGameItemInfo.getMusic().getContent() + "\n\n" + localGameItemInfo.getMusic().getExample());
        } else if (localGameItemInfo.getGameType() == EGameType.GT_Question.getValue()) {
            mTvLyric.setText(localGameItemInfo.getQuestion().getContent());
        }

        mLoadService.showSuccess();
        return true;
    }

    public boolean playLyric(LocalCombineRoomMusic music, DoubleRoomData roomData) {
        mMusic = music;
        mDoubleRoomData = roomData;
        if (music.getUserID() == MyUserInfoManager.getInstance().getUid()) {
            mIsOwner = true;
            mOwnerInfo = roomData.getMyUser();
        } else {
            mIsOwner = false;
            mOwnerInfo = roomData.getAntherUser();
        }
        return playLyric();
    }

    public boolean playLyric() {
        if (mMusic == null) {
            MyLog.w(TAG, "infoModel 是空的");
            return false;
        }

        mMiniGameInfoModel = mMusic.getMusic().getMiniGame();
        if (mMiniGameInfoModel == null) {
            MyLog.w(TAG, "MiniGame 是空的");
            return false;
        }

        tryInflate();
        mSvLyric.scrollTo(0, 0);
//        int totalTs = infoModel.getSingTotalMs();
//        mSingCountDownView.setTagTvText(mMiniGameInfoModel.getGameName());
//        mSingCountDownView.startPlay(0, totalTs, true);

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(mDoubleRoomData.getAvatarById(mMusic.getUserID()))
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2))
                .setBorderColor(Color.WHITE)
                .build());

        if (mDoubleRoomData.getEnableNoLimitDuration()) {
            String mOwnerName = mOwnerInfo.getNicknameRemark();
            if (mOwnerInfo.getNickname().length() > 7) {
                mOwnerName = mOwnerName.substring(0, 7);
            }
            mFirstTipsTv.setTextColor(U.getColor(R.color.black_trans_60));
            mFirstTipsTv.setText("【" + mOwnerName + "】" + "先开始");
        } else {
            mFirstTipsTv.setTextColor(U.getColor(R.color.black_trans_60));
            mFirstTipsTv.setText("【 他 】" + "先开始");
        }


        MiniGameInfoModel model = mMusic.getMusic().getMiniGame();
        if (model.getGamePlayType() == EMiniGamePlayType.EMGP_SONG_DETAIL.getValue()) {
            // TODO: 2019-05-29 带歌词的
            mMiniGameSongUrl = model.getSongInfo().getSongURL();
            setLyric(mTvLyric, mMiniGameSongUrl);
        } else {
            // TODO: 2019-05-29 不带歌词的
            mTvLyric.setTextColor(U.getColor(R.color.black_trans_60));
            mTvLyric.setText(model.getDisplayGameRule());
            mLoadService.showSuccess();
        }

        return true;
    }

    protected void setLyric(TextView lyricTv, String lyricUrl) {
        lyricTv.setText("");
        LyricsManager.getLyricsManager(U.app())
                .loadGrabPlainLyric(lyricUrl)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String o) throws Exception {
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

                        mLoadService.showSuccess();
                    }
                }, throwable -> {
                    mLoadService.showCallback(LyricLoadErrorCallBack.class);
                    MyLog.e(TAG, "setLyric" + " throwable=" + throwable);
                });
    }

    public void destroy() {

    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_video_mini_game_self_sing_card_stub_layout;
    }
}
