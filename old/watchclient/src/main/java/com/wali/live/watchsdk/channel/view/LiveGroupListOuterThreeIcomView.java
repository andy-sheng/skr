package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

import static com.wali.live.utils.AvatarUtils.SIZE_TYPE_AVATAR_SMALL;

/**
 * Created by xzy on 18-3-21.
 * 直播组icon外面的三个主播的小icon
 */

public class LiveGroupListOuterThreeIcomView extends LinearLayout {
    public static final String TAG = "LiveGroupListOuterThreeIcomView";
    LiveGroupListOuterThreeIcons mLiveGroupListOuterThreeIcons;

    BaseImageView icon1;

    BaseImageView icon2;

    BaseImageView icon3;

    List<BaseImageView> icons;

    TextView mGroupMemberCount;

    public LiveGroupListOuterThreeIcomView(Context context) {
        this(context, null);
    }

    public LiveGroupListOuterThreeIcomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveGroupListOuterThreeIcomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        icons = new ArrayList<>(3);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.layout_live_group_list, this);
        icon1 = (BaseImageView) findViewById(R.id.group_mem_item_icon1);
        icon2 = (BaseImageView) findViewById(R.id.group_mem_item_icon2);
        icon3 = (BaseImageView) findViewById(R.id.group_mem_item_icon3);
        icons.add(icon1);
        icons.add(icon2);
        icons.add(icon3);
        mGroupMemberCount = (TextView) findViewById(R.id.tv_live_group_num);
    }

    public void bindDate(final LiveGroupListOuterThreeIcons liveGroupListOuterThreeIcons, final int width, final int height) {
        mLiveGroupListOuterThreeIcons = liveGroupListOuterThreeIcons;
        if (mLiveGroupListOuterThreeIcons == null || mLiveGroupListOuterThreeIcons.getUrls().size() == 0) {
            Observable.from(icons).subscribe(new Subscriber<BaseImageView>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(BaseImageView baseImageView) {
                    baseImageView.setVisibility(GONE);
                }
            });

            mGroupMemberCount.setVisibility(GONE);
            return;
        }

        mGroupMemberCount.setVisibility(VISIBLE);
        if (mLiveGroupListOuterThreeIcons.getCount() > 3) {
            mGroupMemberCount.setText("+" + mLiveGroupListOuterThreeIcons.getCount());
        } else {
            mGroupMemberCount.setVisibility(GONE);
        }

        Observable.range(0, 3).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, e.getMessage());
            }

            @Override
            public void onNext(Integer integer) {
                if (integer >= mLiveGroupListOuterThreeIcons.getUrls().size()) {
                    icons.get(integer).setVisibility(GONE);
                } else {
                    icons.get(integer).setVisibility(VISIBLE);
                    AvatarUtils.loadCoverByUrl(icons.get(integer), AvatarUtils.getImgUrlByAvatarSize(liveGroupListOuterThreeIcons.getUrls().get(integer), SIZE_TYPE_AVATAR_SMALL),
                            true, com.live.module.common.R.drawable.avatar_default_a, width, height);
                }
            }
        });


    }


    public static class LiveGroupListOuterThreeIcons {
        List<String> urls;
        int count;

        public LiveGroupListOuterThreeIcons(List<String> urls, int count) {
            this.urls = urls;
            this.count = count;
        }

        public List<String> getUrls() {
            if (urls == null) {
                return new ArrayList<String>();
            }
            return urls;
        }

        public int getCount() {
            return count;
        }
    }
}
