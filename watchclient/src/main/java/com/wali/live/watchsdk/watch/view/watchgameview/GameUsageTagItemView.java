package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watch.SupportHelper;

/**
 * Created by zhujianning on 18-8-15.
 */

public class GameUsageTagItemView extends BaseGameTagItemView {
    private static final String TAG = "GameUsageTagItemView";
    private ImageView mTagIv;

    public GameUsageTagItemView(Context context) {
        this(context, null);
    }

    public GameUsageTagItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameUsageTagItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.game_usage_tag_item_view, this);
        mTagTv = (TextView) findViewById(R.id.tag_tv);
        mTagIv = (ImageView) findViewById(R.id.iv);
    }

    public void bind(GameInfoModel.GameTag tag) {
        if(tag == null) {
            return;
        }

        MyLog.d(TAG, "tag :" + tag.toString());
        if(tag.getTagType() == 1) {
            SupportHelper.SupportRes supportRes = SupportHelper.getSupportRes(tag.getTagName());
            if(supportRes == null) {
                supportRes = SupportHelper.getSupportResByUrl(tag.getActUrl());
            }

            if(supportRes != null) {
                mTagIv.setImageResource(supportRes.getImgRes());
                mTagTv.setText(supportRes.getNameRes());
            }
        }
    }
}
