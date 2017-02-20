package com.wali.live.common.gift.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.utils.CommonUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.jakewharton.rxbinding.view.RxView;
import com.live.module.common.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.base.BaseEvent;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.wali.live.common.gift.utils.DataformatUtils;
import com.wali.live.utils.AvatarUtils;
import com.base.utils.display.DisplayUtils;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import org.greenrobot.eventbus.EventBus;
import rx.Observer;

/**
 * @
 * Created by chengsimin on 16/3/10.
 */
public class GiftMoveAnimationView extends RelativeLayout {
    public static final String TAG = "GiftMoveAnimationView";

    private BaseImageView mAvatarIv;

    private TextView mGiftAnimationNameTv;

    private TextView mGiftAnimationInfoTv;

    private ImageView mUserBadgeIv;

    private SimpleDraweeView mGiftForegroundAnimationPlayerView;

    public GiftMoveAnimationView(Context context) {
        super(context);
        init(context);
    }

    public GiftMoveAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftMoveAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        inflate(context, R.layout.gift_foreground_animation_layout, this);
        bindView();
        RxView.clicks(findViewById(R.id.container))
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Void aVoid) {
                        MyLog.d(TAG, "click userid:" + mUserId);
                        if (CommonUtils.isFastDoubleClick(1000)) {
                            return;
                        }
                        if (mUserId != 0) {
                            EventBus.getDefault().post(new BaseEvent.UserActionEvent(BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO, mUserId, null));
                        }
                    }
                });
    }

    private void bindView() {
        mAvatarIv = (BaseImageView) findViewById(R.id.sender_iv);
        mGiftAnimationNameTv = (TextView) findViewById(R.id.name_tv);
        mGiftAnimationInfoTv = (TextView) findViewById(R.id.info_tv);
        mUserBadgeIv = (ImageView) findViewById(R.id.user_badge_iv);
        mGiftForegroundAnimationPlayerView = (SimpleDraweeView) findViewById(R.id.gift_foreground_animation_player_view);
    }

    public void setWebpPath(String webpPath, int width, int height) {
        MyLog.d(TAG, "path:" + webpPath);
        mGiftForegroundAnimationPlayerView.getLayoutParams().width = width;
        mGiftForegroundAnimationPlayerView.getLayoutParams().height = height;
        Uri uri = new Uri.Builder().scheme("file").appendPath(webpPath).build();
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(width,height))
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(mGiftForegroundAnimationPlayerView.getController())
                .setImageRequest(request)
                .setAutoPlayAnimations(true)
                .build();
        mGiftForegroundAnimationPlayerView.setController(controller);
    }

    private long mUserId;

    public void setNameAndGift(String name, String giftName, long userId, int certificationType, int level) {
        mGiftAnimationNameTv.setText(name);
        mGiftAnimationInfoTv.setText(getResources().getString(R.string.gift_send, giftName));
        this.mUserId = userId;
        if (userId == MyUserInfoManager.getInstance().getUser().getUid()) {
            AvatarUtils.loadAvatarByUidTs(mAvatarIv, userId, MyUserInfoManager.getInstance().getUser().getAvatar(), true);
        } else {
            AvatarUtils.loadAvatarByUid(mAvatarIv, userId, true);
        }
        if (certificationType > 0) {
            mUserBadgeIv.getLayoutParams().width = DisplayUtils.dip2px(15f);
            mUserBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(15f);
            mUserBadgeIv.setImageDrawable(DataformatUtils.getCertificationImgSource(certificationType));
        } else {
            mUserBadgeIv.getLayoutParams().width = DisplayUtils.dip2px(10f);
            mUserBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(10f);
            mUserBadgeIv.setImageDrawable(DataformatUtils.getLevelSmallImgSource(level));
        }
    }

}
