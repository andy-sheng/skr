package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.util.HolderUtils;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

//频道页视频集入口
public class VideoCollectionCardView extends RelativeLayout {

    private BaseImageView coverIv;
    private TextView titleTv;
    private RelativeLayout containerRl;

    public VideoCollectionCardView(Context context) {
        this(context,null);
    }

    public VideoCollectionCardView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VideoCollectionCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.michannel_video_collection_layout,this);
        init();
    }

    private void init() {
        coverIv= (BaseImageView) findViewById(R.id.cover_iv);
        titleTv= (TextView) findViewById(R.id.title_tv);
        containerRl= (RelativeLayout) findViewById(R.id.container_rl);
    }

    public void bindData(ChannelLiveViewModel.ImageItem item){
        if(!TextUtils.isEmpty(item.getName())){
            titleTv.setText(item.getName());
        }else{
            titleTv.setVisibility(GONE);
        }
        HolderUtils.bindImage(coverIv,item.getImageUrl(),false,coverIv.getMeasuredWidth(),coverIv.getMeasuredHeight(), ScalingUtils.ScaleType.CENTER_CROP);
    }

}
