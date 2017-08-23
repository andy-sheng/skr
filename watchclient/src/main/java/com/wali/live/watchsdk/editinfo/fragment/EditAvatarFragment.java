package com.wali.live.watchsdk.editinfo.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.EditInfoActivity;
import com.wali.live.watchsdk.editinfo.fragment.presenter.EditAvatarPresenter;
import com.wali.live.watchsdk.editinfo.fragment.presenter.IEditAvatarView;

import rx.Observable;

/**
 * Created by wangmengjie on 17-8-16.
 *
 * @mudule 编辑头像页面
 */

public class EditAvatarFragment extends RxFragment implements View.OnClickListener, IEditAvatarView {
    private static final String TAG = "EditAvatarFragment";

    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    private static final int REQUEST_CODE_TAKE_PIC = 100;
    private static final int REQUEST_CODE_SELECT_PIC = 102;
    private static final int RESULT_CODE_CROP_PIC = 103;

    private long mUidFromActivity = -1;
    private boolean mInfoChanged;
    private EditAvatarPresenter mPresenter;
    private User mMe;
    private String mPath;//裁剪后的文件的path

    private BackTitleBar mTitleBar;
    private SimpleDraweeView mAvatarDv;
    //编辑
    private TextView mTakePicTv;
    private TextView mSelectPicTv;
    private View mChangePicContainer;
    //预览
    private TextView mCancelTv;
    private TextView mUseTv;
    private View mPreviewContainer;

    private ProgressDialog mUploadAvatarPd;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.edit_avatar_layout, container, false);
    }

    @Override
    protected void bindView() {
        mTitleBar = $(R.id.title_bar);
        mAvatarDv = $(R.id.avatar_dv);
        mTakePicTv = $(R.id.take_pic_tv);
        mSelectPicTv = $(R.id.select_pic_tv);
        mChangePicContainer = $(R.id.change_pic_container);
        mCancelTv = $(R.id.cancel_tv);
        mUseTv = $(R.id.use_tv);
        mPreviewContainer = $(R.id.preview_container);

        mTitleBar.setTitle(getString(R.string.look_up_bin_avatar));
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mAvatarDv.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new RelativeLayout.LayoutParams(GlobalData.screenWidth, GlobalData.screenWidth);
        } else {
            layoutParams.width = GlobalData.screenWidth;
            layoutParams.height = GlobalData.screenWidth;
        }
        mAvatarDv.setLayoutParams(layoutParams);

        $click(mTakePicTv, this);
        $click(mSelectPicTv, this);
        $click(mTitleBar.getBackBtn(), this);
        $click(mCancelTv, this);
        $click(mUseTv, this);

        initView();
        initPresenter();
    }

    private void initView() {
        mMe = MyUserInfoManager.getInstance().getUser();
        syncAvatar();
    }

    private void initPresenter() {
        mPresenter = new EditAvatarPresenter(this);
    }

    private void syncAvatar() {
        mChangePicContainer.setVisibility(View.VISIBLE);

        AvatarUtils.loadAvatarByUidTs(mAvatarDv, mMe.getUid(), mMe.getAvatar(),
                AvatarUtils.SIZE_TYPE_AVATAR_LARGE, false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_iv) {
            closeFragment();
        } else if (id == R.id.take_pic_tv) {
            clickTakePic();
        } else if (id == R.id.select_pic_tv) {
            clickSelectPic();
        } else if (id == R.id.cancel_tv) {
            clickCancel();
        } else if (id == R.id.use_tv) {
            clickUse();
        }
    }

    private void clickTakePic() {
        MyLog.d(TAG, "take pic click");
        PermissionUtils.checkPermissionByType(
                (BaseActivity) getActivity(),
                PermissionUtils.PermissionType.CAMERA,
                new PermissionUtils.IPermissionCallback() {
                    @Override
                    public void okProcess() {
                        MyLog.d(TAG, "camera permission ok");
                        startActivityForResult(mPresenter.setupTakePicIntent(), REQUEST_CODE_TAKE_PIC);
                    }
                });
    }

    private void clickSelectPic() {
        MyLog.d(TAG, "select pic click");
        startActivityForResult(mPresenter.setupSelectPicIntent(), REQUEST_CODE_SELECT_PIC);
    }

    private void clickCancel() {
        MyLog.d(TAG, "cancel click");
        changeStateToPreview(false);
        syncAvatar();
    }

    private void clickUse() {
        MyLog.d(TAG, "use click");
        mPresenter.uploadAvatar(mPath);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_TAKE_PIC:
                    startActivityForResult(mPresenter.setupCropPicIntent(true),
                            RESULT_CODE_CROP_PIC);
                    break;
                case REQUEST_CODE_SELECT_PIC:
                    if (data != null && data.getData() != null) {
                        MyLog.d(TAG, "receive select url = " + data.getData());
                        mPresenter.setSelectPicUri(data.getData());
                        startActivityForResult(mPresenter.setupCropPicIntent(false),
                                RESULT_CODE_CROP_PIC);
                    }
                    break;
                case RESULT_CODE_CROP_PIC:
                    MyLog.d(TAG, "receiver");
                    Uri uri = mPresenter.getCropPicUri();
                    if (uri != null) {
                        mPath = uri.getPath().replaceAll("/external_files",
                                Environment.getExternalStorageDirectory().toString());
                        MyLog.d(TAG, "path = " + mPath);

                        BaseImage baseImage = ImageFactory.newLocalImage(mPath)
                                .setWidth(480).setHeight(480).build();
                        FrescoWorker.loadImage(mAvatarDv, baseImage);

                        changeStateToPreview(true);
                    } else {
                        ToastUtils.showToast(getContext(), R.string.crop_failed);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void changeStateToPreview(boolean isShowPre) {
        mChangePicContainer.setVisibility(isShowPre ? View.GONE : View.VISIBLE);
        mPreviewContainer.setVisibility(isShowPre ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showProgressDialog(@StringRes int msgRes) {
        if (mUploadAvatarPd == null) {
            mUploadAvatarPd = new ProgressDialog(getActivity());
        }
        String msg = getString(msgRes);
        if (!TextUtils.isEmpty(msg)) {
            mUploadAvatarPd.setMessage(msg);
        }
        mUploadAvatarPd.show();
    }

    @Override
    public void hideProgressDialog() {
        if (!getActivity().isFinishing() && mUploadAvatarPd != null && mUploadAvatarPd.isShowing()) {
            mUploadAvatarPd.dismiss();
        }
    }

    @Override
    public void editSuccess(long avatar) {
        MyLog.d(TAG, "editSuccess avatar=" + avatar);
        mInfoChanged = true;
        ToastUtils.showToast(R.string.change_avatar_success);

        mMe.setAvatar(avatar);
        changeStateToPreview(false);
        syncAvatar();

        closeFragment();
    }

    @Override
    public void editFailure(int code) {
        MyLog.w(TAG, "editFailure code=" + code);
        ToastUtils.showToast(R.string.change_avatar_failed);
    }

    @Override
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return bindUntilEvent();
    }

    private void closeFragment() {
        MyLog.w(TAG, "closeFragment infoChanged=" + mInfoChanged);
        if (mInfoChanged && mDataListener != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(EditInfoActivity.EXTRA_OUT_INFO_CHANGED, mInfoChanged);
            mDataListener.onFragmentResult(mRequestCode, Activity.RESULT_OK, bundle);
        }
        finish();
    }

    private void finish() {
        FragmentNaviUtils.popFragmentFromStack(getActivity());
    }

    @Override
    public boolean onBackPressed() {
        MyLog.d(TAG, "onBackPressed");
        closeFragment();
        return true;
    }

    public static void open(BaseActivity activity, FragmentDataListener listener, Bundle bundle) {
        BaseFragment fragment = FragmentNaviUtils.addFragment(activity, R.id.main_act_container,
                EditAvatarFragment.class, bundle, true, true, true);
        fragment.initDataResult(REQUEST_CODE, listener);
    }
}
