package com.module.rankingmode.prepare.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfo;
import com.common.image.fresco.BaseImageView;
import com.module.rankingmode.R;

public class MatchingUserIconView extends Sprite {
    UserInfo userInfo;

    BaseImageView mIvUserIcon;

    //这个位置是addView的时候用的位置
    int[] viewLocation;

    int[] size;

    public MatchingUserIconView(Context context) {
        this(context, null);
    }

    public MatchingUserIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatchingUserIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(){
        inflate(getContext(), R.layout.matching_user_icon_view_layout, this);

        mIvUserIcon = findViewById(R.id.iv_user_icon);
    }

    public void setIconLocation(int rotation, int radio, int layerRadio){
        size = new int[]{(layerRadio - radio) * 2, (layerRadio - radio) * 2};
        viewLocation = getAreaInCircle(rotation, radio, layerRadio);
        viewLocation = new int[]{viewLocation[0] - size[0] / 2, viewLocation[1] - size[1] / 2};
    }

    private int[] getAreaInCircle(int rotation, int radio, int layerRadio){
        int[] location = new int[2];
        rotation = rotation % 360;

        if(0 <= rotation && rotation < 90){
            location[0] = layerRadio - (int) (Math.cos(Math.toRadians(rotation)) * radio);
            location[1] = layerRadio - (int) (Math.sin(Math.toRadians(rotation)) * radio);
        }else if(90 <= rotation && rotation < 180){
            rotation = rotation - 90;
            location[0] = layerRadio + (int) (Math.sin(Math.toRadians(rotation)) * radio);
            location[1] = layerRadio - (int) (Math.cos(Math.toRadians(rotation)) * radio);
        }else if(180 <= rotation && rotation < 270){
            rotation = rotation - 180;
            location[0] = layerRadio + (int) (Math.cos(Math.toRadians(rotation)) * radio);
            location[1] = layerRadio + (int) (Math.sin(Math.toRadians(rotation)) * radio);
        }else if(270 <= rotation && rotation < 360){
            rotation = rotation - 270;
            location[0] = layerRadio - (int) (Math.sin(Math.toRadians(rotation)) * radio);
            location[1] = layerRadio + (int) (Math.cos(Math.toRadians(rotation)) * radio);
        }

        return location;
    }

    public void loadUserIcon(String userIconUrl){
        AvatarUtils.loadAvatarByUrl(mIvUserIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getUid())
                        .setCircle(true)
                        .setTimestamp(MyUserInfoManager.getInstance().getAvatar())
                        .build());

    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        loadUserIcon("");
    }

    //缩放
    public void toScaleAnimation(float f){
        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1, f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0, f);

        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(scaleX).with(scaleY);//两个动画同时开始
        animatorSet.start();
    }


    /**
     * 这个是layer角度发生变化时候调用
     * @param rotaion
     */
    @Override
    public void onChangeRotation(float rotaion) {
        setRotation(-rotaion);
    }

    /**
     * 当layer大小改变的时候调用
     * @param witdh
     * @param height
     */
    @Override
    public void onChangeWindow(int witdh, int height) {

    }

    /**
     * 得到要addview时候的位置
     * @return
     */
    @Override
    public int[] getIconLocation() {
        return viewLocation;
    }

    /**
     * 得到当前view的大小
     * @return
     */
    @Override
    public int[] getSize() {
        return size;
    }
}
