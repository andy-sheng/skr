//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.common.base.FragmentDataListener;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.imagepicker.ImagePicker;
import com.imagepicker.fragment.ImagePickerFragment;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.CropImageView;

import java.io.File;
import java.util.ArrayList;

import io.rong.imkit.R;
import io.rong.imkit.RongExtension;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imlib.model.Conversation.ConversationType;

public class ImagePlugin implements IPluginModule, IPluginRequestPermissionResultCallback {
    ConversationType conversationType;
    String targetId;

    public ImagePlugin() {
    }

    public Drawable obtainDrawable(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.rc_ext_plugin_image_selector);
    }

    public String obtainTitle(Context context) {
        return context.getString(R.string.rc_plugin_image);
    }

    public void onClick(Fragment currentFragment, RongExtension extension) {
        this.conversationType = extension.getConversationType();
        this.targetId = extension.getTargetId();
        String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA"};
        if (PermissionCheckUtil.checkPermissions(currentFragment.getContext(), permissions)) {
            // todo 打开图片选择器选择图片
            openSelectPictureFragment(currentFragment, extension);
        } else {
            extension.requestPermissionForPluginResult(permissions, 255, this);
        }

    }

    private void openSelectPictureFragment(Fragment fragment, RongExtension extension) {
        Bundle bundle = new Bundle();
        ImagePicker.getInstance().setParams(ImagePicker.newParamsBuilder()
                .setSelectLimit(8)
                .setCropStyle(CropImageView.Style.CIRCLE)
                .build()
        );
        U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(fragment.getActivity(), ImagePickerFragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .setBundle(bundle)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
                        if (extension != null && extension.getExtensionClickListener() != null) {
                            ArrayList<ImageItem> imageItems = ImagePicker.getInstance().getSelectedImages();
                            ArrayList<Uri> list = new ArrayList<>();
                            for (ImageItem imageItem : imageItems) {
                                // todo 原来PictureSelectorActivity中uri 是这样生成的
                                list.add(Uri.parse("file://" + imageItem.getPath()));
                            }
                            extension.getExtensionClickListener().onImageResult(list, false);
                        }
                    }
                })
                .build());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public boolean onRequestPermissionResult(Fragment fragment, RongExtension extension, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionCheckUtil.checkPermissions(fragment.getActivity(), permissions)) {
            // todo 打开图片选择器选择图片
            openSelectPictureFragment(fragment, extension);
        } else {
            extension.showRequestPermissionFailedAlter(PermissionCheckUtil.getNotGrantedPermissionMsg(fragment.getActivity(), permissions, grantResults));
        }

        return true;
    }
}
