package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.IFrescoCallBack;
import com.base.utils.display.DisplayUtils;
import com.facebook.imagepipeline.image.ImageInfo;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

/**
 * 直播水印（观看）
 */
public class WatchWaterMarkView extends RelativeLayout{
    private static final String TAG = "WatchWaterMarkView";

    private static final int TOP_MAGIN_PORT = DisplayUtils.dip2px(43.33f);
    private static final int TOP_MAGIN_LAND = DisplayUtils.dip2px(10f);

    private static final int RIGHT_MARGIN = DisplayUtils.dip2px(6.67f);

    View rootView;

    ImageView mMiboImg;


    TextView mMiLogoView;


    TextView mTimestampView;


    TextView mLiveTypeView;

    RelativeLayout mMiLogoArea;

    BaseImageView mIvHuYaLogo;

    private boolean mIsLandscape = false;
    private boolean mIsHuya = false;

    public WatchWaterMarkView(Context context) {
        this(context, null);
    }

    public WatchWaterMarkView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatchWaterMarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.water_mark_view, this);
        rootView = findViewById(R.id.root_water_mark_view);
        mIvHuYaLogo = (BaseImageView) this.findViewById(R.id.iv_huya_logo);
        mMiLogoArea = (RelativeLayout) this.findViewById(R.id.rl_mi_logo_area);
        mMiboImg = (ImageView) this.findViewById(R.id.mi_logo_img);
        mMiLogoView = (TextView) this.findViewById(R.id.mi_logo);
        mTimestampView = (TextView) this.findViewById(R.id.timestamp);
        mLiveTypeView = (TextView) this.findViewById(R.id.live_type);
    }

    public void setRoomData(RoomBaseDataModel myRoomData) {

        if (myRoomData != null && myRoomData.getLiveType() == LiveManager.TYPE_LIVE_HUYA) {
            //虎牙直播
            mIsHuya = true;
            adjustPosition();
            mIvHuYaLogo.setVisibility(VISIBLE);

            if (myRoomData.getHuyaInfo() != null && myRoomData.getHuyaInfo().hasLogoUrl()) {
                AvatarUtils.loadCoverByUrl(mIvHuYaLogo, myRoomData.getHuyaInfo().getLogoUrl(), false,0, 240, 200, new IFrescoCallBack() {
                    @Override
                    public void processWithInfo(ImageInfo info) {
                        ViewGroup.LayoutParams layoutParams = mIvHuYaLogo.getLayoutParams();

                        layoutParams.width = DisplayUtils.dip2px((float) info.getWidth() / 3);
                        layoutParams.height = DisplayUtils.dip2px((float) info.getHeight() / 3);
                        mIvHuYaLogo.setLayoutParams(layoutParams);
                    }

                    @Override
                    public void processWithFailure() {

                    }

                    @Override
                    public void process(Object object) {

                    }
                });
            }
            mMiLogoArea.setVisibility(GONE);
            rootView.setBackground(null);
            rootView.setPadding(0, 0, 0, 0);
        } else {
            //正常直播
            mIsHuya = false;
            mMiLogoArea.setVisibility(VISIBLE);
            mIvHuYaLogo.setVisibility(GONE);
            mMiboImg.setVisibility(VISIBLE);
            mMiLogoView.setText(String.valueOf(myRoomData.getUid()));
            rootView.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.live_mibologo_bg));
            rootView.setPadding(DisplayUtils.dip2px(7.33f), 0, DisplayUtils.dip2px(7.33f), 0);
        }
    }

    public static final float THIRD_LOGO_TOP_MARGIN = 140f + 22f;

    private void adjustPosition() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();

        if (mIsLandscape) {
            layoutParams.topMargin = TOP_MAGIN_LAND;
            layoutParams.rightMargin = RIGHT_MARGIN;
            RelativeLayout.LayoutParams ivHuYaLogoLayoutParams = (RelativeLayout.LayoutParams) mIvHuYaLogo.getLayoutParams();
            ivHuYaLogoLayoutParams.setMargins(0, 0, 0, 0);
        } else {
            layoutParams.topMargin = TOP_MAGIN_PORT;
            layoutParams.rightMargin = RIGHT_MARGIN;

            RelativeLayout.LayoutParams ivHuYaLogoLayoutParams = (RelativeLayout.LayoutParams) mIvHuYaLogo.getLayoutParams();
            int targetMargin = DisplayUtils.dip2px(THIRD_LOGO_TOP_MARGIN);
            ivHuYaLogoLayoutParams.setMargins(0, targetMargin, 0, 0);
        }

        setLayoutParams(layoutParams);
    }

    public void onOrientation(boolean isLandscape){
        this.mIsLandscape = isLandscape;
        if(mIsHuya){
            adjustPosition();
        }
    }
}
