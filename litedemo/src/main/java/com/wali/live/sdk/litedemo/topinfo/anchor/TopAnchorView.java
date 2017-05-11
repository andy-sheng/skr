package com.wali.live.sdk.litedemo.topinfo.anchor;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.liveassistant.avatar.AvatarUtils;
import com.mi.liveassistant.data.model.User;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.fresco.FrescoWorker;
import com.wali.live.sdk.litedemo.fresco.image.ImageFactory;

import static android.content.ContentValues.TAG;

/**
 * Created by lan on 17/5/4.
 */
public class TopAnchorView extends RelativeLayout {
    private SimpleDraweeView mAnchorDv;
    private TextView mAnchorTv;

    public TopAnchorView(Context context) {
        super(context);
        init();
    }

    public TopAnchorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TopAnchorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected <V extends View> V $(int id) {
        return (V) findViewById(id);
    }

    private void init() {
        inflate(getContext(), R.layout.top_anchor_view, this);

        mAnchorDv = $(R.id.anchor_dv);
        mAnchorTv = $(R.id.anchor_tv);
    }

    public void updateAnchor(User anchor) {
        mAnchorTv.setText(anchor.getNickname());

        String avatarUrl = AvatarUtils.getAvatarUrlByUid(anchor.getUid(), anchor.getAvatar());
        Log.d(TAG, "updateAnchorView avatarUrl=" + avatarUrl);
        FrescoWorker.loadImage(mAnchorDv, ImageFactory.newHttpImage(avatarUrl).setIsCircle(true).build());
    }
}
