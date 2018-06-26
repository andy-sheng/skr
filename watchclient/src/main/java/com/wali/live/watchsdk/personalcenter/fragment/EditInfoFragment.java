package com.wali.live.watchsdk.personalcenter.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.activity.BaseSdkActivity;
import com.base.dialog.MyAlertDialog;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.utils.CommonUtils;
import com.base.utils.toast.ToastUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.FragmentEvent;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.fragment.presenter.EditAvatarPresenter;
import com.wali.live.watchsdk.editinfo.fragment.presenter.IEditAvatarView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;

import static android.app.Activity.RESULT_OK;

/**
 * Created by zhujianning on 18-6-25.
 * 修改资料页面
 */

public class EditInfoFragment extends RxFragment implements FragmentDataListener, IEditAvatarView {
    private static final String TAG = "EditInfoFragment";
    public static final int REQUEST_CODE = GlobalData.getRequestCode();
    private static final int REQUEST_CODE_TAKE_PIC = 100;
    private static final int REQUEST_CODE_SELECT_PIC = 102;
    private static final int RESULT_CODE_CROP_PIC = 103;

    //presenter
    private EditAvatarPresenter mPresenter;

    //ui
    private TextView mBackTv;
    private BaseImageView mAvatorIv;
    private RelativeLayout mAvatorContainer;
    private RelativeLayout mNameContainer;
    private RelativeLayout mGenderContainer;
    private RelativeLayout mSignContainer;
    private TextView mNameTv;
    private TextView mGenderTv;
    private TextView mSignTv;
    private ProgressDialog mUploadAvatarPd;
    private RelativeLayout mAvatorPreviewContainer;
    private SimpleDraweeView mAvatorDv;
    private TextView mPreAvatorCancelTv;
    private TextView mPreAvitorConfirmTv;
    private View mTopView;

    //data
    private User mUser;
    private String mPath;//裁剪后的文件的path

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frag_edit_info_half, container, false);
    }

    @Override
    protected void bindView() {
        mBackTv = (TextView) mRootView.findViewById(R.id.back_tv);
        mAvatorIv = (BaseImageView) mRootView.findViewById(R.id.avatar_iv);
        mAvatorContainer = (RelativeLayout) mRootView.findViewById(R.id.avator_container);
        mNameContainer = (RelativeLayout) mRootView.findViewById(R.id.name_container);
        mGenderContainer = (RelativeLayout) mRootView.findViewById(R.id.gender_container);
        mSignContainer = (RelativeLayout) mRootView.findViewById(R.id.sign_container);
        mNameTv = (TextView) mRootView.findViewById(R.id.name_tv);
        mGenderTv = (TextView) mRootView.findViewById(R.id.gender_tv);
        mSignTv = (TextView) mRootView.findViewById(R.id.sign_tv);
        mTopView = mRootView.findViewById(R.id.place_holder_view);

        mAvatorPreviewContainer = (RelativeLayout) mRootView.findViewById(R.id.avator_preview_container);
        mAvatorDv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_dv);
        mPreAvatorCancelTv = (TextView) mRootView.findViewById(R.id.cancel_tv);
        mPreAvitorConfirmTv = (TextView) mRootView.findViewById(R.id.use_tv);

        mUser = MyUserInfoManager.getInstance().getUser();

        initListener();
        initPresenter();

        if(mUser != null) {
            bindAvator();
            bindNickName();
            bindGender();
            bindSign();
        } else {
            MyLog.w(TAG, "user Info is null");
        }
    }

    private void initPresenter() {
        mPresenter = new EditAvatarPresenter(this);
    }

    private void initListener() {
        RxView.clicks(mBackTv).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        FragmentNaviUtils.popFragmentFromStack(getActivity());
                    }
                });

        RxView.clicks(mNameContainer).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EditNameHalfFragment.openFragment((BaseSdkActivity) getActivity(), R.id.fl_edit_container, EditInfoFragment.this);
                    }
                });

        RxView.clicks(mGenderContainer).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EditGenderHalfFragment.openFragment((BaseSdkActivity) getActivity(), R.id.fl_edit_container, EditInfoFragment.this);
                    }
                });

        RxView.clicks(mSignContainer).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EditSignHalfFragment.openFragment((BaseSdkActivity) getActivity(), R.id.fl_edit_container, EditInfoFragment.this);
                    }
                });

        RxView.clicks(mAvatorContainer).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                     popAvatorSelDialog();
                    }
                });

        RxView.clicks(mPreAvatorCancelTv).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        hideAvatorDvMode();
                    }
                });
        RxView.clicks(mPreAvitorConfirmTv).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        clickUse();
                    }
                });
        RxView.clicks(mTopView).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        FragmentNaviUtils.popAllFragmentFromStack(getActivity());
                    }
                });
    }

    private void popAvatorSelDialog() {
        MyAlertDialog.Builder myAlertDialog = new MyAlertDialog.Builder(getActivity());
        String takePic = getResources().getString(R.string.take_pic);
        String selectPic = getResources().getString(R.string.select_photo_from_phone);
        String cancelText = getResources().getString(R.string.cancel);
        myAlertDialog.setItems(new String[]{takePic, selectPic, cancelText}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //手机拍照
                        clickTakePic();
                        break;
                    case 1:
                        //从相册选择
                        clickSelectPic();
                        break;
                    case 2:
                        //选择推荐
                        dialog.dismiss();
                        break;
                }
            }
        });
        myAlertDialog.create().show();
    }

    private void clickSelectPic() {
        MyLog.d(TAG, "select pic click");
        checkWritePermission(REQUEST_CODE_SELECT_PIC);
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
                        checkWritePermission(REQUEST_CODE_TAKE_PIC);
                    }
                });
    }

    private void clickUse() {
        MyLog.d(TAG, "use click");
        PermissionUtils.checkPermissionByType(
                (BaseActivity) getActivity(),
                PermissionUtils.PermissionType.READ_PHONE_STATE,
                new PermissionUtils.IPermissionCallback() {
                    @Override
                    public void okProcess() {
                        mPresenter.uploadAvatar(mPath);
                    }
                }
        );
    }

    private void checkWritePermission(final int from) {
        PermissionUtils.checkPermissionByType(
                (BaseActivity) getActivity(),
                PermissionUtils.PermissionType.WRITE_EXTERNAL_STORAGE,
                new PermissionUtils.IPermissionCallback() {
                    @Override
                    public void okProcess() {
                        MyLog.d(TAG, "write permission ok");
                        if (from == REQUEST_CODE_TAKE_PIC) {
                            startActivityForResult(mPresenter.setupTakePicIntent(), from);
                        } else if (from == REQUEST_CODE_SELECT_PIC) {
                            startActivityForResult(mPresenter.setupSelectPicIntent(), from);
                        }
                    }
                }
        );
    }

    private void bindAvator() {
        AvatarUtils.loadAvatarByUidTs(mAvatorIv, mUser.getUid(), mUser.getAvatar(), true);
    }

    private void bindNickName() {
        mNameTv.setText(TextUtils.isEmpty(mUser.getNickname()) ? String.valueOf(mUser.getUid()) : mUser.getNickname());
    }

    private void bindGender() {
        mGenderTv.setText(mUser.getGender() == 1 ? CommonUtils.getString(R.string.gender_man) : CommonUtils.getString(R.string.gender_woman));
    }

    private void bindSign() {
        mSignTv.setText(TextUtils.isEmpty(mUser.getSign()) ? CommonUtils.getString(R.string.default_sign_txt) : mUser.getSign());
    }

    private void showAvatorDvMode() {
        BaseImage baseImage = ImageFactory.newLocalImage(mPath)
                .setWidth(480).setHeight(480).build();
        FrescoWorker.loadImage(mAvatorDv, baseImage);
        mAvatorPreviewContainer.setVisibility(View.VISIBLE);
    }

    private void hideAvatorDvMode() {
        mPath = "";
        mAvatorPreviewContainer.setVisibility(View.GONE);
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
                    MyLog.d(TAG, "RESULT_CODE_CROP_PIC");
                    MyLog.d(TAG, "receiver");
                    Uri uri = mPresenter.getCropPicUri();
                    if (uri != null) {
                        mPath = uri.getPath().replaceAll("/external_files",
                                Environment.getExternalStorageDirectory().toString());
                        MyLog.d(TAG, "path = " + mPath);

                        showAvatorDvMode();
                    } else {
                        ToastUtils.showToast(getContext(), R.string.crop_failed);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
        MyLog.w(TAG, "requestCode=" + requestCode + ", resultCode=" + resultCode);
//        if (resultCode != RESULT_OK) {
//            return;
//        }
        if (requestCode == EditNameHalfFragment.REQUEST_CODE) {
            bindNickName();
        } else if(requestCode == EditGenderHalfFragment.REQUEST_CODE) {
            bindGender();
        } else if(requestCode == EditSignHalfFragment.REQUEST_CODE) {
            bindSign();
        }
    }

    @Override
    public <T> Observable.Transformer<T, T> bindUntilEvent() {
        return bindUntilEvent(FragmentEvent.DESTROY_VIEW);
    }

    @Override
    public void showProgressDialog(int msgRes) {
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
        if (getActivity() == null || getActivity().isFinishing()
                || mUploadAvatarPd == null || !mUploadAvatarPd.isShowing()) {
            return;
        }
        mUploadAvatarPd.dismiss();
    }

    @Override
    public void editSuccess(long avatar) {
        MyLog.d(TAG, "editSuccess avatar=" + avatar);
        ToastUtils.showToast(R.string.change_avatar_success);

        mUser.setAvatar(avatar);
        hideAvatorDvMode();
        bindAvator();
    }

    @Override
    public void editFailure() {
        MyLog.d(TAG, "editFailure");
        ToastUtils.showToast(R.string.change_avatar_failed);
    }

    public static void openFragment(BaseSdkActivity activity, int containerId) {
        FragmentNaviUtils.addFragment(activity, containerId, EditInfoFragment.class,
                new Bundle(), true, true, true);
    }
}
