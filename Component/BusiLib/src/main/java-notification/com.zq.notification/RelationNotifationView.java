package com.zq.notification;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * 关注弹窗通知
 */
public class RelationNotifationView extends RelativeLayout {

    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ExTextView mHintTv;
    ExTextView mFollowTv;

    public RelationNotifationView(Context context) {
        super(context);
        init();
    }

    public RelationNotifationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RelationNotifationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.relation_notification_view_layout, this);
        mAvatarIv = (SimpleDraweeView) findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) findViewById(R.id.name_tv);
        mHintTv = (ExTextView) findViewById(R.id.hint_tv);
        mFollowTv = (ExTextView) findViewById(R.id.follow_tv);

    }
}
