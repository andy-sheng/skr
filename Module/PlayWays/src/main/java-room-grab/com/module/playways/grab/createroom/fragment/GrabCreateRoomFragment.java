package com.module.playways.grab.createroom.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GrabRoomType;
import com.dialog.view.TipsDialogView;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

/**
 * 一唱到底，创建房间页面
 */
public class GrabCreateRoomFragment extends BaseFragment {

    public static final String KEY_ROOM_TYPE = "key_room_type";

    ExImageView mIvBack;
    ExRelativeLayout mFriendsRoom;
    ExRelativeLayout mSecretRoom;
    ExRelativeLayout mPublicRoom;

    DialogPlus mDialogPlus;

    @Override
    public int initView() {
        return R.layout.grab_create_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mFriendsRoom = (ExRelativeLayout) mRootView.findViewById(R.id.friends_room);
        mSecretRoom = (ExRelativeLayout) mRootView.findViewById(R.id.secret_room);
        mPublicRoom = (ExRelativeLayout) mRootView.findViewById(R.id.public_room);

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });


        mFriendsRoom.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                goGrabCreateSpecialFragment(GrabRoomType.ROOM_TYPE_FRIEND);
            }
        });

        mSecretRoom.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                goGrabCreateSpecialFragment(GrabRoomType.ROOM_TYPE_SECRET);
            }
        });
        mPublicRoom.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                GrabRoomServerApi roomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
                ApiMethods.subscribe(roomServerApi.checkCreatePublicRoomPermission(), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        if (result.getErrno() == 0) {
                            if (result.getData().getBoolean("has")) {
                                goGrabCreateSpecialFragment(GrabRoomType.ROOM_TYPE_PUBLIC);
                            } else {
                                if (TextUtils.isEmpty(result.getData().getString("notice"))) {
                                    showErrorMsgDialog("您还没有权限创建公开房间");
                                } else {
                                    showErrorMsgDialog(result.getData().getString("notice"));
                                }
                            }
                        } else {
                            if (TextUtils.isEmpty(result.getErrmsg())) {
                                showErrorMsgDialog("您还没有权限创建公开房间");
                            } else {
                                showErrorMsgDialog("" + result.getErrmsg());
                            }
                        }
                    }
                }, GrabCreateRoomFragment.this);
            }
        });

    }

    public void showErrorMsgDialog(String string) {
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(getActivity())
                .setMessageTip(string)
                .setOkBtnTip("确认")
                .setOkBtnClickListener(new AnimateClickListener() {
                    @Override
                    public void click(View view) {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                    }
                })
                .build();

        mDialogPlus = DialogPlus.newDialog(U.getActivityUtils().getTopActivity())
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .create();
        mDialogPlus.show();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(false);
        }
    }

    void goGrabCreateSpecialFragment(int roomType) {
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(false);
        }
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_ROOM_TYPE, roomType);
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabCreateSpecialFragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .setBundle(bundle)
                .build());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
