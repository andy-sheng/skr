package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.image.fresco.BaseImageView;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.dialog.view.EditTextWithOkBtnView;
import com.imagepicker.ImagePicker;
import com.imagepicker.fragment.ImagePickerFragment;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.CropImageView;
import com.module.RouterConstants;
import com.module.home.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class PersonFragment extends BaseFragment {

    BaseImageView mAvatarIv;
    ExTextView mNameTv;
    ExTextView mUpdateNicknameBtn;
    RelativeLayout mClearCacheBtn;
    RelativeLayout mLogoutBtn;

    @Override
    public int initView() {
        return R.layout.person_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mAvatarIv = (BaseImageView) mRootView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mUpdateNicknameBtn = (ExTextView) mRootView.findViewById(R.id.update_nickname_btn);
        mClearCacheBtn = (RelativeLayout) mRootView.findViewById(R.id.clear_cache_btn);
        mLogoutBtn = (RelativeLayout) mRootView.findViewById(R.id.logout_btn);

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance()
                .getAvatar())
                .build());
        mNameTv.setText(MyUserInfoManager.getInstance().getNickName());

        mUpdateNicknameBtn.setOnClickListener(new View.OnClickListener() {
            DialogPlus mDialogPlus;

            @Override
            public void onClick(View v) {
                EditTextWithOkBtnView editText = new EditTextWithOkBtnView(getContext());
                editText.getContentEt().setText(MyUserInfoManager.getInstance().getNickName());
                editText.getContentEt().setHint("输入昵称");
                editText.getOkBtn().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                        MyUserInfoManager.getInstance().updateInfo(editText.getContentEt().getText().toString(), -1, null, null);
                    }
                });
                mDialogPlus = DialogPlus.newDialog(getContext())
                        .setGravity(Gravity.CENTER)
                        .setContentHolder(new ViewHolder(editText))
                        .create();
                mDialogPlus.show();
            }
        });

        mAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePicker.getInstance().setParams(ImagePicker.newParamsBuilder()
                        .setSelectLimit(1)
                        .setCropStyle(CropImageView.Style.CIRCLE)
                        .build()
                );

                U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(getActivity(), ImagePickerFragment.class)
                        .setContainerViewId(R.id.person_main_containner)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object object) {
                                List<ImageItem> list = ImagePicker.getInstance().getSelectedImages();
                                if (list.size() > 0) {
                                    ImageItem imageItem = list.get(0);
                                    UploadTask uploadTask = UploadParams.newBuilder(imageItem.getPath())
                                            .setNeedCompress(true)
                                            .startUploadAsync(new UploadCallback() {
                                                @Override
                                                public void onProgress(long currentSize, long totalSize) {

                                                }

                                                @Override
                                                public void onSuccess(String url) {
                                                    U.getToastUtil().showShort("上传成功 url:" + url);
                                                    MyUserInfoManager.getInstance().updateInfo(null, -1, null, url);
                                                }

                                                @Override
                                                public void onFailure(String msg) {

                                                }

                                            });
                                }
                            }
                        })
                        .build());
            }
        });

        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserAccountManager.getInstance().logoff(true);
                ARouter.getInstance().build(RouterConstants.ACTIVITY_LOGIN).navigation();
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance()
                .getAvatar())
                .build());
        mNameTv.setText(MyUserInfoManager.getInstance().getNickName());
    }

    @Override
    public boolean useEventBus() {
        return true;
    }


}
