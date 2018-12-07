package com.module.rankingmode.prepare.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.FrescoWorker;
import com.common.utils.U;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rankingmode.R;

/**
 * 匹配中view
 */
public class MatchingView extends RelativeLayout {
    RingCircleView mRingCircleView;

    public MatchingView(Context context) {
        super(context);
        init();
    }

    public MatchingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MatchingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.match_ing_view, this);
        mRingCircleView = (RingCircleView) findViewById(R.id.ring_circle_view);

        SimpleDraweeView simpleDraweeView = new SimpleDraweeView(getContext());
        simpleDraweeView.setBackgroundColor(U.app().getResources().getColor(R.color.blue));
        AvatarUtils.loadAvatarByUrl(simpleDraweeView,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getUid())
                        .setTimestamp(MyUserInfoManager.getInstance().getAvatar())
                        .setWidth(60)
                        .setHeight(60)
                        .setCircle(true)
                        .build());
        mRingCircleView.addIconView(simpleDraweeView);
        mRingCircleView.startCircleAnimator();
    }

}
