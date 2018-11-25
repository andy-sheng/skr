//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import io.rong.imkit.R;
import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.image.PictureSelectorActivity;
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
      Intent intent = new Intent(currentFragment.getActivity(), PictureSelectorActivity.class);
      extension.startActivityForPluginResult(intent, 23, this);
    } else {
      extension.requestPermissionForPluginResult(permissions, 255, this);
    }

  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
  }

  public boolean onRequestPermissionResult(Fragment fragment, RongExtension extension, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (PermissionCheckUtil.checkPermissions(fragment.getActivity(), permissions)) {
      Intent intent = new Intent(fragment.getActivity(), PictureSelectorActivity.class);
      extension.startActivityForPluginResult(intent, 23, this);
    } else {
      extension.showRequestPermissionFailedAlter(PermissionCheckUtil.getNotGrantedPermissionMsg(fragment.getActivity(), permissions, grantResults));
    }

    return true;
  }
}
