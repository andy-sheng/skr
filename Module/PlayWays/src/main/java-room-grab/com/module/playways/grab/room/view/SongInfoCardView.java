package com.module.playways.grab.room.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.utils.ImageUtils;
import com.common.view.ex.ExTextView;

import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;


/**
 * 转场时的歌曲信息页
 */
public class SongInfoCardView extends RelativeLayout {


    SVGAImageView mSongCover;
    ExTextView mSongNameTv;
    ExTextView mSongOwnerTv;

    public SongInfoCardView(Context context) {
        super(context);
        init();
    }

    public SongInfoCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SongInfoCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_song_info_card_layout, this);
        mSongCover = (SVGAImageView) findViewById(R.id.song_cover);
        mSongNameTv = (ExTextView) findViewById(R.id.song_name_tv);
        mSongOwnerTv = (ExTextView) findViewById(R.id.song_owner_tv);

        // TODO: 2019/1/20 just for test
        SongModel songModel = new SongModel();
        songModel.setItemID(839);
        songModel.setItemName("卡路里");
        songModel.setOwner("火箭少女101");

        HttpImage httpImage = ImageFactory.newHttpImage("http://online-sound-bja.oss-cn-beijing.aliyuncs.com/cover/fdb383b9eca4cc747dec665a9f97117b.jpg")
                .addOssProcessors(OssImgFactory.newResizeBuilder()
                        .setW(160)
                        .build())
                .build();
        songModel.setCover(httpImage.getUrl());
        bindSongModel(songModel);
    }

    // 该动画需要循环播放
    public void bindSongModel(SongModel songModel) {
        if (songModel == null || TextUtils.isEmpty(songModel.getCover())) {
            return;
        }

        mSongNameTv.setText(songModel.getItemName());
        mSongOwnerTv.setText(songModel.getOwner());
        mSongCover.setVisibility(VISIBLE);
        mSongCover.setLoops(0);
        SVGAParser parser = new SVGAParser(getContext());
        try {
            parser.parse("record_player.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity, requestDynamicBitmapItem(songModel.getCover()));
                    mSongCover.setImageDrawable(drawable);
                    mSongCover.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }
    }

    private SVGADynamicEntity requestDynamicBitmapItem(String cover) {
        if (TextUtils.isEmpty(cover)) {
            return null;
        }
        HttpImage httpImage = ImageFactory.newHttpImage(cover)
                .addOssProcessors(OssImgFactory.newResizeBuilder()
                        .setW(ImageUtils.SIZE.SIZE_160.getW())
                        .build())
                .build();
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        if (!TextUtils.isEmpty(httpImage.getUrl())) {
            dynamicEntity.setDynamicImage(httpImage.getUrl(), "cover");
        }
        return dynamicEntity;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            if (mSongCover != null) {
                mSongCover.stopAnimation(false);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mSongCover != null) {
            mSongCover.stopAnimation(true);
        }
    }
}

