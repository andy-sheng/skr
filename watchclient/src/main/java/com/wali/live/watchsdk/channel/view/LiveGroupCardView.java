package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.util.HolderUtils;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

import static com.wali.live.utils.AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE;

//直播间组View
public class LiveGroupCardView extends RelativeLayout {

    public static final String TAG = LiveGroupCardView.class.getSimpleName();

    private BaseImageView coverIv;
    private BaseImageView frameIv;
    private LiveGroupListOuterThreeIcomView threeIcomView;

    public LiveGroupCardView(Context context) {
        this(context,null);
    }

    public LiveGroupCardView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LiveGroupCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.michannel_live_group_single_card_item,this);
        init();
    }

    private void init() {
        coverIv= (BaseImageView) findViewById(R.id.cover_iv);
        frameIv= (BaseImageView) findViewById(R.id.cover_iv_second);
        threeIcomView= (LiveGroupListOuterThreeIcomView) findViewById(R.id.gm_icons);
    }

    public void bindData(ChannelLiveViewModel.LiveGroupItem item){
        MyLog.d(TAG, " bindData "+ item.getNameText() + " cover: "+ item.getImageUrl());
        HolderUtils.bindImage(coverIv, item.getImageUrl(), false, coverIv.getWidth(), coverIv.getHeight(), ScalingUtils.ScaleType.CENTER_CROP);
        HolderUtils.bindImage(frameIv, AvatarUtils.getImgUrlByAvatarSize(item.getFrameUrl(), SIZE_TYPE_AVATAR_MIDDLE), false, frameIv.getWidth(), frameIv.getHeight(), ScalingUtils.ScaleType.FIT_XY);
        LiveGroupListOuterThreeIcomView.LiveGroupListOuterThreeIcons icons=new LiveGroupListOuterThreeIcomView.LiveGroupListOuterThreeIcons(item.getLiveCovers(),item.getGroupCnt());
        int smallIconWidth= DisplayUtils.dip2px(24.67f);
        threeIcomView.bindDate(icons,smallIconWidth,smallIconWidth);
    }
}
