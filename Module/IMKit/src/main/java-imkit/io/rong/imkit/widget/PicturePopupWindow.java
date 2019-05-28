//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.File;

import io.rong.common.FileUtils;
import io.rong.imkit.R;

public class PicturePopupWindow extends PopupWindow {
    private Button btn_save_pic;
    private Button btn_cancel;

    public PicturePopupWindow(final Context context, final File imageFile) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View menuView = inflater.inflate(R.layout.rc_pic_popup_window, (ViewGroup) null);
        menuView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                io.rong.imkit.widget.PicturePopupWindow.this.dismiss();
            }
        });
        this.btn_save_pic = (Button) menuView.findViewById(R.id.rc_content);
        this.btn_save_pic.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                File path = Environment.getExternalStorageDirectory();
                String defaultPath = context.getString(R.string.rc_image_default_saved_path);
                File dir = new File(path, defaultPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                if (imageFile != null && imageFile.exists()) {
                    String name = System.currentTimeMillis() + ".jpg";
                    FileUtils.copyFile(imageFile, dir.getPath() + File.separator, name);
                    MediaScannerConnection.scanFile(context, new String[]{dir.getPath() + File.separator + name}, (String[]) null, (OnScanCompletedListener) null);
                    Toast.makeText(context, String.format(context.getString(R.string.rc_save_picture_at), dir.getPath() + File.separator + name), 0).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.rc_src_file_not_found), 0).show();
                }

                io.rong.imkit.widget.PicturePopupWindow.this.dismiss();
            }
        });
        this.btn_cancel = (Button) menuView.findViewById(R.id.rc_btn_cancel);
        this.btn_cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                io.rong.imkit.widget.PicturePopupWindow.this.dismiss();
            }
        });
        this.setContentView(menuView);
        this.setWidth(-1);
        this.setHeight(-2);
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(-1342177280);
        this.setBackgroundDrawable(dw);
    }
}
