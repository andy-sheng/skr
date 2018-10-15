package com.wali.live.livesdk.live.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.dialog.MyAlertDialog;
import com.base.fragment.RxFragment;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.HttpImage;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.utils.toast.ToastUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.assist.Attachment;
import com.wali.live.livesdk.R;
import com.wali.live.common.photopicker.ClipImageActivity;
import com.wali.live.livesdk.live.manager.PrepareLiveCoverManager;
import com.wali.live.utils.AvatarUtils;

/**
 * Created by zyh on 2017/3/13.
 */

public class SelectCoverView extends RelativeLayout implements View.OnClickListener {
    // 选图相关
    public static final int MENU_PHOTO_TAKE = 0;
    public static final int MENU_PHOTO_CHOOSE = 1;

    private View mCoverContainer;
    private SimpleDraweeView mCoverBigDv;
    private SimpleDraweeView mCoverView;
    private TextView mCoverTv;
    private RxFragment mFragment;
    private PrepareLiveCoverManager mPrepareLiveCoverManager;

    private String mCoverUrl;

    public void setFragment(RxFragment fragment) {
        mFragment = fragment;
        initManager();
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    @Override
    public void onClick(View v) {
        showCoverSelectDialog();
    }

    public SelectCoverView(Context context) {
        super(context);
        init(context);
    }

    public SelectCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SelectCoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.cover_view, this);
        mCoverContainer = findViewById(R.id.cover_container);
        mCoverContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showCoverSelectDialog();
            }
        });
        mCoverBigDv = (SimpleDraweeView) findViewById(R.id.cover_big);
        mCoverTv = (TextView) findViewById(R.id.cover_text);
        mCoverView = (SimpleDraweeView) findViewById(R.id.cover_view);
        setCoverByAvatar();
    }

    public void setCoverByAvatar() {
        AvatarUtils.loadAvatarByUidTs(mCoverBigDv, MyUserInfoManager.getInstance().getUuid(),
                MyUserInfoManager.getInstance().getAvatar(), false);
    }

    private void initManager() {
        if (mFragment != null && !mFragment.isDetached()) {
            mPrepareLiveCoverManager = new PrepareLiveCoverManager(mFragment);
            mPrepareLiveCoverManager.setLoadFinishListener(new PrepareLiveCoverManager.LoadFinishListener() {
                @Override
                public void onLoadFinishUI(Attachment att, Drawable drawable) {
                    if (att != null && drawable != null && !TextUtils.isEmpty(att.getUrl())) {
                        mCoverBigDv.setVisibility(View.VISIBLE);
                        mCoverUrl = att.getUrl();
                        FrescoWorker.loadImage(mCoverBigDv, new HttpImage(mCoverUrl));
                        mCoverView.setVisibility(View.INVISIBLE);
                        mCoverTv.setVisibility(View.INVISIBLE);
                    } else {
                        ToastUtils.showToast(mFragment.getActivity(), R.string.upload_failed);
                    }
                }

                @Override
                public void onSelectedFinished(String filePath) {

                }
            });
        }
    }

    private void showCoverSelectDialog() {
        PermissionUtils.requestPermissionDialog((Activity) getContext(), PermissionUtils.PermissionType.READ_PHONE_STATE, new PermissionUtils.IPermissionCallback() {
            @Override
            public void okProcess() {
                if (mFragment == null || mFragment.isDetached()) {
                    return;
                }
                KeyboardUtils.hideKeyboardImmediately(mFragment.getActivity());
                MyAlertDialog.Builder builder = new MyAlertDialog.Builder(mFragment.getContext());
                builder.setItems(getResources().getStringArray(R.array.cover_item), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case MENU_PHOTO_TAKE:
                                mPrepareLiveCoverManager.onClickTakePicButton(mFragment);
                                break;
                            case MENU_PHOTO_CHOOSE:
                                mPrepareLiveCoverManager.onClickSelectPicButton();
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MyLog.w("SelectCoverView", "onActivityResult requestCode : " + requestCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PrepareLiveCoverManager.REQUEST_CODE_TAKE_PHOTO:
            case ClipImageActivity.REQUEST_CODE_CROP:
                if (mPrepareLiveCoverManager != null)
                    mPrepareLiveCoverManager.onActivityResult(requestCode, resultCode, data);
                break;
            default:
                break;
        }
    }

    public void onDestroy() {
        if (mPrepareLiveCoverManager != null) {
            mPrepareLiveCoverManager.onDestroy();
            mFragment = null;
        }
    }
}
