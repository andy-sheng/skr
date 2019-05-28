package com.common.emoji;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;

/**
 * CSDN_LQR
 * 表情底部tab
 */
public class EmotionTab extends RelativeLayout {

    private BaseImageView mIvIcon;
    private String mStickerCoverImgPath;
    private int mIconSrc = R.drawable.ic_tab_add;

    public EmotionTab(Context context, int iconSrc) {
        super(context);
        mIconSrc = iconSrc;
        init(context);
    }

    public EmotionTab(Context context, String stickerCoverImgPath) {
        super(context);
        mStickerCoverImgPath = stickerCoverImgPath;
        init(context);
    }


    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.emotion_tab, this);

        mIvIcon = (BaseImageView) findViewById(R.id.ivIcon);

        if (TextUtils.isEmpty(mStickerCoverImgPath)) {
            mIvIcon.setImageResource(mIconSrc);
        } else {
            FrescoWorker.loadImage(mIvIcon,ImageFactory.newPathImage(mStickerCoverImgPath).build());
        }
    }

}
