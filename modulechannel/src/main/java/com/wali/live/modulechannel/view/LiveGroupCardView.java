package com.wali.live.modulechannel.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;
import com.wali.live.modulechannel.util.HolderUtils;

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
        inflate(context, R.layout.channel_live_group_single_card_item,this);
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
        String url = TextUtils.isEmpty(item.getFrameUrl()) ? item.getFrameUrl() : item.getFrameUrl() + U.getImageUtils().getSizeSuffix(ImageUtils.SIZE.SIZE_480);
        HolderUtils.bindImage(frameIv, url, false, frameIv.getWidth(), frameIv.getHeight(), ScalingUtils.ScaleType.FIT_XY);
        LiveGroupListOuterThreeIcomView.LiveGroupListOuterThreeIcons icons=new LiveGroupListOuterThreeIcomView.LiveGroupListOuterThreeIcons(item.getLiveCovers(),item.getGroupCnt());
        int smallIconWidth= U.getDisplayUtils().dip2px(24.67f);
        threeIcomView.bindDate(icons,smallIconWidth,smallIconWidth);
    }
}
