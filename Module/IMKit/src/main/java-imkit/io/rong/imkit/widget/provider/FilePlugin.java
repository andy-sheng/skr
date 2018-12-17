//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.HashSet;
import java.util.Iterator;

import io.rong.imkit.R;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.activity.FileManagerActivity;
import io.rong.imkit.model.FileInfo;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.IPluginRequestPermissionResultCallback;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imlib.IRongCallback.ISendMediaMessageCallback;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Message;
import io.rong.message.FileMessage;

public class FilePlugin implements IPluginModule, IPluginRequestPermissionResultCallback {
    private static final String TAG = "FileInputProvider";
    private static final int REQUEST_FILE = 100;
    private ConversationType conversationType;
    private String targetId;

    public FilePlugin() {
    }

    public Drawable obtainDrawable(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.rc_ic_files_selector);
    }

    public String obtainTitle(Context context) {
        return context.getString(R.string.rc_plugins_files);
    }

    public void onClick(Fragment currentFragment, RongExtension extension) {
        this.conversationType = extension.getConversationType();
        this.targetId = extension.getTargetId();
        String[] permissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
        if (PermissionCheckUtil.checkPermissions(currentFragment.getContext(), permissions)) {
            Intent intent = new Intent(currentFragment.getActivity(), FileManagerActivity.class);
            extension.startActivityForPluginResult(intent, 100, this);
        } else {
            extension.requestPermissionForPluginResult(permissions, 255, this);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && data != null) {
            HashSet<FileInfo> selectedFileInfos = (HashSet) data.getSerializableExtra("sendSelectedFiles");
            Iterator var5 = selectedFileInfos.iterator();

            while (var5.hasNext()) {
                FileInfo fileInfo = (FileInfo) var5.next();
                Uri filePath = Uri.parse("file://" + fileInfo.getFilePath());
                FileMessage fileMessage = FileMessage.obtain(filePath);
                if (fileMessage != null) {
                    fileMessage.setType(fileInfo.getSuffix());
                    Message message = Message.obtain(this.targetId, this.conversationType, fileMessage);
                    RongIM.getInstance().sendMediaMessage(message, (String) null, (String) null, (ISendMediaMessageCallback) null);
                }
            }
        }

    }

    public boolean onRequestPermissionResult(Fragment currentFragment, RongExtension extension, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionCheckUtil.checkPermissions(currentFragment.getActivity(), permissions)) {
            Intent intent = new Intent(currentFragment.getActivity(), FileManagerActivity.class);
            extension.startActivityForPluginResult(intent, 100, this);
        } else {
            extension.showRequestPermissionFailedAlter(PermissionCheckUtil.getNotGrantedPermissionMsg(currentFragment.getActivity(), permissions, grantResults));
        }

        return true;
    }
}
