package com.module.playways.grab.createroom.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
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
import com.component.busilib.constans.GrabRoomType;
import com.component.busilib.verify.SkrVerifyUtils;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomServerApi;

/**
 * 一唱到底，创建房间页面
 */
public class GrabCreateRoomFragment extends BaseFragment {

    public static final String KEY_ROOM_TYPE = "key_room_type";
    public static final int ErrNoPublicRoomPermission = 8344139; //达成一唱到底60首，才能开启
    public static final int ErrRealAuth = 8344158; //实名认证未通过

    ExImageView mIvBack;
    ExImageView mFriendsRoom;
    ExImageView mSecretRoom;
    ExImageView mPublicRoom;

    TipsDialogView mTipsDialogView;

    SkrVerifyUtils mSkrVerifyUtils;

    @Override
    public int initView() {
        return R.layout.grab_create_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvBack = (ExImageView) getRootView().findViewById(R.id.iv_back);
        mFriendsRoom = (ExImageView) getRootView().findViewById(R.id.friends_room);
        mSecretRoom = (ExImageView) getRootView().findViewById(R.id.secret_room);
        mPublicRoom = (ExImageView) getRootView().findViewById(R.id.public_room);
        mSkrVerifyUtils = new SkrVerifyUtils();

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
                            goGrabCreateSpecialFragment(GrabRoomType.ROOM_TYPE_PUBLIC);
                        } else if (ErrNoPublicRoomPermission == result.getErrno()) {
                            if (TextUtils.isEmpty(result.getErrmsg())) {
                                showErrorMsgDialog("您还没有权限创建公开房间");
                            } else {
                                showErrorMsgDialog("" + result.getErrmsg());
                            }
                        } else if (ErrRealAuth == result.getErrno()) {
                            //实人认证
                            if (mTipsDialogView != null) {
                                mTipsDialogView.dismiss();
                            }
                            mTipsDialogView = new TipsDialogView.Builder(getContext())
                                    .setMessageTip(result.getErrmsg())
                                    .setConfirmTip("立即认证")
                                    .setCancelTip("放弃")
                                    .setConfirmBtnClickListener(new AnimateClickListener() {
                                        @Override
                                        public void click(View view) {
                                            mTipsDialogView.dismiss();
                                            ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                                    .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://app.inframe.mobi/oauth?from=room"))
                                                    .greenChannel().navigation();
                                        }
                                    })
                                    .setCancelBtnClickListener(new AnimateClickListener() {
                                        @Override
                                        public void click(View view) {
                                            mTipsDialogView.dismiss();
                                        }
                                    })
                                    .build();
                            mTipsDialogView.showByDialog();
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
        if (mTipsDialogView != null) {
            mTipsDialogView.dismiss();
        }
        mTipsDialogView = new TipsDialogView.Builder(getActivity())
                .setMessageTip(string)
                .setOkBtnTip("确认")
                .setOkBtnClickListener(new AnimateClickListener() {
                    @Override
                    public void click(View view) {
                        mTipsDialogView.dismiss();
                    }
                })
                .build();
        mTipsDialogView.showByDialog();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mTipsDialogView != null) {
            mTipsDialogView.dismiss();
        }
    }

    void goGrabCreateSpecialFragment(int roomType) {
        if (mTipsDialogView != null) {
            mTipsDialogView.dismiss(false);
        }

        mSkrVerifyUtils.checkAgeSettingState(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_ROOM_TYPE, roomType);
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabCreateSpecialFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setBundle(bundle)
                        .build());
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
