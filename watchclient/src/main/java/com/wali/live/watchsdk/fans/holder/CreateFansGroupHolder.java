package com.wali.live.watchsdk.fans.holder;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.image.fresco.BaseImageView;
import com.base.keyboard.KeyboardUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.constant.FansConstant;
import com.wali.live.watchsdk.fans.dialog.CreateGroupDialog;
import com.wali.live.watchsdk.fans.dialog.listener.OnConfirmClickListener;
import com.wali.live.watchsdk.fans.listener.FansGroupListListener;
import com.wali.live.watchsdk.fans.model.item.CreateFansGroupModel;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.webview.WebViewActivity;

/**
 * Created by lan on 2017/11/8.
 */
public class CreateFansGroupHolder extends BaseHolder<CreateFansGroupModel> {
    private static final int MAX_GROUP_NAME_LENGTH = 4;

    private BaseImageView mAvatarIv;

    private TextView mNameTv;
    private TextView mRecommendTv;
    private TextView mCreateBtn;

    private CreateGroupDialog mDialog;

    private FansGroupListListener mListener;

    public CreateFansGroupHolder(View itemView, FansGroupListListener listener) {
        super(itemView);
        mListener = listener;
    }

    @Override
    protected void initView() {
        mAvatarIv = $(R.id.avatar_iv);
        mNameTv = $(R.id.name_tv);
        mRecommendTv = $(R.id.recommend_tv);
        mCreateBtn = $(R.id.create_btn);

        initListener();
    }

    private void initListener() {
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        mRecommendTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * @colorWrong
                 */
                WebViewActivity.open((Activity) itemView.getContext(), FansConstant.FANS_INDEX_URL);
            }
        });
    }

    @Override
    protected void bindView() {
        AvatarUtils.loadAvatarByUidTs(mAvatarIv,
                UserAccountManager.getInstance().getUuidAsLong(),
                MyUserInfoManager.getInstance().getAvatar(),
                true);

        mNameTv.setText(MyUserInfoManager.getInstance().getNickname());
    }

    private void showDialog() {
        if (mDialog == null) {
            mDialog = new CreateGroupDialog(itemView.getContext());
            mDialog.setOnConfirmClickListener(new OnConfirmClickListener() {
                @Override
                public void confirm() {
                    String name = mDialog.getEditMessage();
                    if (name.length() > MAX_GROUP_NAME_LENGTH) {
                        ToastUtils.showToast(itemView.getContext().getString(R.string.vfans_max_name_length, MAX_GROUP_NAME_LENGTH));
                        return;
                    }
                    if (!TextUtils.isEmpty(name)) {
                        mListener.createGroup(name);
                        mDialog.dismiss();
                        KeyboardUtils.hideKeyboard((BaseActivity) itemView.getContext());
                    }
                }
            });
            mDialog.setCanceledOnTouchOutside(true);
        }
        mDialog.show();
    }
}
