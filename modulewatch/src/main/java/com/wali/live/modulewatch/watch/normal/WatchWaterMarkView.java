package com.wali.live.modulewatch.watch.normal;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.wali.live.modulewatch.R;
import com.wali.live.modulewatch.event.RoomDataChangeEvent;
import com.wali.live.modulewatch.live.LiveManager;
import com.wali.live.modulewatch.watch.model.roominfo.RoomBaseDataModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 直播水印（观看）
 */
public class WatchWaterMarkView extends RelativeLayout {
    private static final String TAG = "WatchWaterMarkView";

    private static final int TOP_MAGIN_PORT = U.getDisplayUtils().dip2px(43.33f);
    private static final int TOP_MAGIN_LAND = U.getDisplayUtils().dip2px(10f);

    private static final int RIGHT_MARGIN = U.getDisplayUtils().dip2px(6.67f);

    ImageView mMiboImg;


    TextView mMiLogoView;


    TextView mTimestampView;


    TextView mLiveTypeView;

    protected RelativeLayout mMiLogoArea;

    protected BaseImageView mIvHuYaLogo;

    protected boolean mIsLandscape = false;
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

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @CallSuper
    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.water_mark_view, this);
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
         //   adjustPosition();
            mIvHuYaLogo.setVisibility(VISIBLE);

            if (myRoomData.getHuyaInfo() != null && myRoomData.getHuyaInfo().hasLogoUrl()) {
                // TODO 后期再加上
//                AvatarUtils.loadCoverByUrl(mIvHuYaLogo, myRoomData.getHuyaInfo().getLogoUrl(), false,0, 240, 200, new IFrescoCallBack() {
//                    @Override
//                    public void processWithInfo(ImageInfo info) {
//                        ViewGroup.LayoutParams layoutParams = mIvHuYaLogo.getLayoutParams();
//
//                        layoutParams.width = U.getDisplayUtils().dip2px((float) info.getWidth() / 3);
//                        layoutParams.height = U.getDisplayUtils().dip2px((float) info.getHeight() / 3);
//                        mIvHuYaLogo.setLayoutParams(layoutParams);
//                    }
//
//                    @Override
//                    public void processWithFailure() {
//
//                    }
//
//                    @Override
//                    public void process(Object object) {
//
//                    }
//                });
            }
            mMiLogoArea.setVisibility(GONE);
        } else {
            //正常直播
            mIsHuya = false;
            mMiLogoArea.setVisibility(VISIBLE);
            mIvHuYaLogo.setVisibility(GONE);
            mMiboImg.setVisibility(VISIBLE);
            mMiLogoView.setText(String.valueOf(myRoomData.getUid()));
        }
    }

    public static final float THIRD_LOGO_TOP_MARGIN = 140f + 22f;

    private void adjustPosition() {
        // 旧版本直播间会将第三方游戏直播的logo调整到视频右上角（而非秀场直播时整个区域的右上角）
        // 新版游戏直播直接放到视频区域的左下角　不需要这个位置调整了
        // type不准的游戏直播跟秀场直播的logo位置统一

//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
//
//        if (mIsLandscape) {
//            layoutParams.topMargin = TOP_MAGIN_LAND;
//            layoutParams.rightMargin = RIGHT_MARGIN;
//            RelativeLayout.LayoutParams ivHuYaLogoLayoutParams = (RelativeLayout.LayoutParams) mIvHuYaLogo.getLayoutParams();
//            ivHuYaLogoLayoutParams.setMargins(0, 0, 0, 0);
//        } else {
//            layoutParams.topMargin = TOP_MAGIN_PORT;
//            layoutParams.rightMargin = RIGHT_MARGIN;
//
//            RelativeLayout.LayoutParams ivHuYaLogoLayoutParams = (RelativeLayout.LayoutParams) mIvHuYaLogo.getLayoutParams();
//            int targetMargin = U.getDisplayUtils().dip2px(THIRD_LOGO_TOP_MARGIN);
//            ivHuYaLogoLayoutParams.setMargins(0, targetMargin, 0, 0);
//        }
//
//        setLayoutParams(layoutParams);
    }

    public void onOrientation(boolean isLandscape){
        this.mIsLandscape = isLandscape;
        if(mIsHuya){
           // adjustPosition();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        if (event != null && event.source != null) {
            switch (event.type) {
                case RoomDataChangeEvent.TYPE_CHANGE_THIRD_PARTY_INFO:
                    setRoomData(event.source);
                    break;
                default:
                    break;
            }
        }
    }
}
