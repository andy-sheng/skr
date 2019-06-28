package com.module.playways.grab.room.view.chorus;

import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.ChorusRoundInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;

import java.util.List;


/**
 * 合唱的歌唱者看到的板子
 */
public class VideoChorusSelfSingCardView extends BaseChorusSelfCardView {

    public final static String TAG = "VideoChorusSelfSingCardView";

    public VideoChorusSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub, roomData);
    }

    @Override
    protected void init(View parentView) {
        super.init(parentView);
        int statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(U.app());
        {
            RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) parentView.getLayoutParams();
            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
        }
    }

    @Override
    protected boolean isForVideo() {
        return true;
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_video_chorus_self_sing_card_stub_layout;
    }

    public boolean playLyric() {
        return super.playLyric();
    }
}
