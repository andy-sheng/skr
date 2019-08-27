package com.module.playways.room.room.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.HttpImage;
import com.common.utils.U;
import com.glidebitmappool.BitmapFactoryAdapter;
import com.glidebitmappool.BitmapPoolAdapter;
import com.module.playways.listener.SVGAListener;
import com.module.playways.room.room.RankRoomData;
import com.module.playways.room.room.model.RankPlayerInfoModel;
import com.module.playways.room.room.model.RankRoundInfoModel;
import com.module.playways.R;
import com.module.playways.BaseRoomData;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.live.proto.Common.ESex;


import java.io.File;

public class TurnChangeCardView extends RelativeLayout {

    public final String TAG = "TurnChangeCardView";

    RankRoomData mRoomData;

    SVGAImageView mFirstSvga;
    SVGAImageView mNextSvga;

    SVGAListener mSVGAListener;

    public TurnChangeCardView(Context context) {
        super(context);
        init();
    }

    public TurnChangeCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TurnChangeCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        inflate(getContext(), R.layout.room_turn_change_view_layout, this);
        mFirstSvga = (SVGAImageView) findViewById(R.id.first_svga);
        mNextSvga = (SVGAImageView) findViewById(R.id.next_svga);
    }

    public boolean setData(RankRoomData data, SVGAListener listener) {
        this.mRoomData = data;
        this.mSVGAListener = listener;

        RankRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel == null) {
            return false;
        }

        int curUid = infoModel.getUserID();
        int seq = infoModel.getRoundSeq();

        RankPlayerInfoModel curInfo = mRoomData.getPlayerInfoModel(curUid);

        if (curInfo == null) {
            if (mSVGAListener != null) {
                mSVGAListener.onFinished();
            }
            return false;
        }

        bindData(curInfo, seq);
        return true;
    }


    public void bindData(RankPlayerInfoModel cur, int seq) {
        if (seq == 1) {
            firstTurnCard(cur);
        } else {
            nextTurnCard(cur);
        }
    }

    private void firstTurnCard(RankPlayerInfoModel info) {
        setVisibility(VISIBLE);
        mFirstSvga.clearAnimation();
        mFirstSvga.setCallback(null);

        mFirstSvga.setVisibility(VISIBLE);
        mFirstSvga.setLoops(1);
        SvgaParserAdapter.parse( BaseRoomData.RANK_BATTLE_START_SVGA, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete( SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamicItem(info));
                mFirstSvga.setImageDrawable(drawable);
                mFirstSvga.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        mFirstSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mFirstSvga != null) {
                    mFirstSvga.stopAnimation(true);
                    mFirstSvga.setVisibility(GONE);
                }
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mFirstSvga != null && mFirstSvga.isAnimating()) {
                    mFirstSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });

    }

    private void nextTurnCard(RankPlayerInfoModel info) {
        setVisibility(VISIBLE);
        mNextSvga.clearAnimation();
        mNextSvga.setCallback(null);

        mNextSvga.setVisibility(VISIBLE);
        mNextSvga.setLoops(1);
        SvgaParserAdapter.parse( "rank_battle_next.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete( SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamicItem(info));
                mNextSvga.setImageDrawable(drawable);
                mNextSvga.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        mNextSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mNextSvga != null) {
                    mNextSvga.setCallback(null);
                    mNextSvga.stopAnimation(true);
                    mNextSvga.setVisibility(GONE);
                }
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mNextSvga != null && mNextSvga.isAnimating()) {
                    mNextSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });

    }

    private SVGADynamicEntity requestDynamicItem(RankPlayerInfoModel info) {
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        Bitmap bitmap = BitmapPoolAdapter.getBitmap(U.getDisplayUtils().dip2px(70), U.getDisplayUtils().dip2px(70), Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(info.getUserInfo().getSex() == ESex.SX_MALE.getValue() ? U.getColor(com.common.core.R.color.color_man_stroke_color) : U.getColor(com.common.core.R.color.color_woman_stroke_color));
        dynamicEntity.setDynamicImage(bitmap, "border");

        HttpImage image = AvatarUtils.getAvatarUrl(AvatarUtils.newParamsBuilder(info.getUserInfo().getAvatar())
                .setCircle(true)
                .build());
        File file = FrescoWorker.getCacheFileFromFrescoDiskCache(image.getUrl());
        if (file != null && file.exists()) {
            Bitmap bitmap2 = BitmapFactoryAdapter.decodeFile(file.getPath());
            //防止用户不给sd权限导致 bitmap为null
            if(bitmap2!=null){
                dynamicEntity.setDynamicImage(bitmap2, "avatar128");
            }else{
                dynamicEntity.setDynamicImage(image.getUrl(), "avatar128");
            }
        } else {
            dynamicEntity.setDynamicImage(image.getUrl(), "avatar128");
        }

        TextPaint textPaint1 = new TextPaint();
        textPaint1.setColor(Color.parseColor("#0C2275"));
        textPaint1.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint1.setTextAlign(Paint.Align.LEFT);
        textPaint1.setTextSize(U.getDisplayUtils().dip2px(18));

        TextPaint textPaint2 = new TextPaint();
        textPaint2.setColor(Color.parseColor("#0C2275"));
        textPaint2.setTextAlign(Paint.Align.LEFT);
        textPaint2.setTextSize(U.getDisplayUtils().dip2px(12));

        String songName = "";
        if (info != null && info.getSongList() != null && info.getSongList().size() > 0) {
            songName = info.getSongList().get(0).getItemName();
        }
        if (info.getUserInfo().getUserId() == MyUserInfoManager.getInstance().getUid()) {
            if (songName.length() > 14) {
                songName = songName.substring(0, 11) + "...";
            }
            dynamicEntity.setDynamicText("轮到你唱啦！", textPaint1, "text1");
            dynamicEntity.setDynamicText("《" + songName + "》", textPaint2, "text2");
        } else {
            if (songName.length() > 10) {
                songName = songName.substring(0, 7) + "...";
            }
            dynamicEntity.setDynamicText("《" + songName + "》", textPaint1, "text1");
            dynamicEntity.setDynamicText("演唱：" + info.getUserInfo().getNicknameRemark(), textPaint2, "text2");
        }
        return dynamicEntity;
    }


    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            this.mSVGAListener = null;
            if (mFirstSvga != null) {
                mFirstSvga.setCallback(null);
                mFirstSvga.stopAnimation(true);
            }
            if (mNextSvga != null) {
                mNextSvga.setCallback(null);
                mNextSvga.stopAnimation(true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mSVGAListener = null;
        if (mFirstSvga != null) {
            mFirstSvga.setCallback(null);
            mFirstSvga.stopAnimation(true);
        }
        if (mNextSvga != null) {
            mNextSvga.setCallback(null);
            mNextSvga.stopAnimation(true);
        }
    }
}
