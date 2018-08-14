package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.widget.RelativeLayout;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.wali.live.watchsdk.R;

public class GamePreviewImageView extends RelativeLayout {

    BaseImageView mMainIv;

    public GamePreviewImageView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.game_preview_item_view, this);
        mMainIv = (BaseImageView) this.findViewById(R.id.main_iv);
    }

    public void setImageUrl(String imageUrl) {
        BaseImage baseImage = ImageFactory.newHttpImage(imageUrl)
                .build();
        FrescoWorker.loadImage(mMainIv, baseImage);
    }
}
