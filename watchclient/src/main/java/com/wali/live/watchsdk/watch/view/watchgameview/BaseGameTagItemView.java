package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

public class BaseGameTagItemView extends RelativeLayout {
    private static final String TAG = "BaseGameTagItemView";

    protected TextView mTagTv;

    public BaseGameTagItemView(Context context) {
        this(context, null);
    }

    public BaseGameTagItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseGameTagItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.tag_item_view, this);
        mTagTv = (TextView) findViewById(R.id.tag_tv);
    }

    public void bind(GameInfoModel.GameTag tag) {
        if(tag == null) {
            return;
        }

        MyLog.d(TAG, "tag :" + tag.toString());

        if(tag.getTagType() == 0) {
            mTagTv.setText(tag.getTagName());
            mTagTv.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.bg_game_tag_gray));
        }
    }
}
