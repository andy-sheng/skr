package com.wali.live.watchsdk.fans.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.wali.live.watchsdk.R;

/**
 * Created by zhangyuehuan on 2017/11/12.
 *
 * @module 特权页
 */

public class FansPrivilegeFragment extends BaseFragment {
    public final static String EXTRA_TYPE = "extra_type";

    public static final int TYPE_UPGRADE_ACCELERATION = 0;
    public static final int TYPE_COLOR_BARRAGE = 1;
    public static final int TYPE_FREE_FLY_BARRAGE = 2;
    public static final int TYPE_BAN_BARRAGE = 3;
    public static final int TYPE_CHARM_MEDAL = 4;
    public static final int TYPE_TOUR_DIVIDE = 5;
    public static final int TYPE_MORE_FANS = 6;
    private int mType;
    private ImageView mMainIv;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_privilege, container, false);
    }

    @Override
    protected void bindView() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mType = bundle.getInt(EXTRA_TYPE);
        }
        mMainIv = $(R.id.main_iv);
        mMainIv.setImageResource(getTypeResId(mType));
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.zoom_in);
        mMainIv.startAnimation(animation);

        $click($(R.id.close_iv), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private int getTypeResId(int type) {
        int resId = R.drawable.live_pet_group_fans_p;
        switch (type) {
            case TYPE_UPGRADE_ACCELERATION:
                resId = R.drawable.live_pet_group_accelerate_big_p;
                break;
            case TYPE_COLOR_BARRAGE:
                resId = R.drawable.live_pet_group_barrage_p;
                break;
            case TYPE_FREE_FLY_BARRAGE:
                resId = R.drawable.live_pet_group_broadcast_p;
                break;
            case TYPE_BAN_BARRAGE:
                resId = R.drawable.live_pet_group_banned_p;
                break;
            case TYPE_CHARM_MEDAL:
                resId = R.drawable.live_pet_group_charm_title_p;
                break;
            case TYPE_TOUR_DIVIDE:
                resId = R.drawable.live_pet_group_money_p;
                break;
            case TYPE_MORE_FANS:
                resId = R.drawable.live_pet_group_fans_p;
                break;
        }
        return resId;
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void openFragment(BaseSdkActivity activity, int type) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TYPE, type);
        FragmentNaviUtils.addFragment(activity, R.id.main_act_container,
                FansPrivilegeFragment.class, bundle, true, false, true);
    }
}
