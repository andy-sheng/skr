package com.module.playways.rank.room.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.rank.room.RankRoomData;
import com.module.playways.rank.room.model.RankPlayerInfoModel;
import com.module.playways.rank.room.model.RankRoundInfoModel;
import com.module.rank.R;
import com.module.playways.BaseRoomData;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.live.proto.Common.ESex;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;

public class TurnChangeCardView extends RelativeLayout {

    public final static String TAG = "TurnChangeCardView";

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
        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse(new URL(BaseRoomData.RANK_BATTLE_START_SVGA), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamicItem(info));
                    mFirstSvga.setImageDrawable(drawable);
                    mFirstSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }

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
        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse("rank_battle_next.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamicItem(info));
                    mNextSvga.setImageDrawable(drawable);
                    mNextSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }

        mNextSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mNextSvga != null) {
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
        Bitmap bitmap = Bitmap.createBitmap(U.getDisplayUtils().dip2px(70), U.getDisplayUtils().dip2px(70), Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(info.getUserInfo().getSex() == ESex.SX_MALE.getValue() ? U.getColor(com.common.core.R.color.color_man_stroke_color) : U.getColor(com.common.core.R.color.color_woman_stroke_color));
        dynamicEntity.setDynamicImage(bitmap, "border");

        HttpImage image = ImageFactory.newHttpImage(info.getUserInfo().getAvatar())
                .addOssProcessors(OssImgFactory.newResizeBuilder()
                                .setW(ImageUtils.SIZE.SIZE_160.getW())
                                .build()
                        , OssImgFactory.newCircleBuilder()
                                .setR(500)
                                .build()
                )
                .build();
        File file = FrescoWorker.getCacheFileFromFrescoDiskCache(image.getUrl());
        if (file != null && file.exists()) {
            dynamicEntity.setDynamicImage(BitmapFactory.decodeFile(file.getPath()), "avatar128");
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
            dynamicEntity.setDynamicText("演唱：" + info.getUserInfo().getNickname(), textPaint2, "text2");
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
                mFirstSvga.stopAnimation(false);
            }
            if (mNextSvga != null) {
                mNextSvga.setCallback(null);
                mNextSvga.stopAnimation(false);
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
