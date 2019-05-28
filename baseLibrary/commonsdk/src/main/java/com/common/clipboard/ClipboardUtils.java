package com.common.clipboard;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.common.utils.U;

public class ClipboardUtils {
    /**
     * 实现文本复制功能
     * add by wangqianzhou
     *
     * @param content
     */
    public static void setCopy(String content) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) U.app().getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    /**
     * 实现粘贴功能
     * add by wangqianzhou
     *
     * @return
     */
    public static String getPaste() {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) U.app().getSystemService(Context.CLIPBOARD_SERVICE);
        CharSequence cs = cmb.getText();
        if (cs != null) {
            String str = cs.toString();
            if (str != null) {
                return str.trim();
            }
        }
        return "";
    }

    public static void clear() {
        ClipboardManager cmb = (ClipboardManager) U.app().getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(null);
    }
}
