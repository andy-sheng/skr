package com.wali.live.common.action;

/**
 * Created by lan on 15/12/23.
 * 3000-3999
 */
public class PickerAction {
    /* use for PhotoPickerFragment & VideoPickerFragment & ImagePreviewFragment click*/
    public static final int ACTION_CANCEL = 3000;        // 取消选图,回退页面
    public static final int ACTION_OK = 3001;            // 点击确定,图片选择成功,回退到上个页面
    public static final int ACTION_BACK = 3002;          // folder进去之后的返回,二级titleBar的返回
    public static final int ACTION_SELECT = 3003;        // 点击选择
    public static final int ACTION_SELECT_ORIGIN = 30004; // 选择原图
    public static final int ACTION_SELECT_ORIGIN_TEXT = 30005; //选择原图文字

    /*use for TimePickerFragment click*/
    public static final int ACTION_TIME_BACK = 3100;          // 取消,回退页面
    public static final int ACTION_TIME_OK = 3101;            // 点击确定,时间选择成功
}
