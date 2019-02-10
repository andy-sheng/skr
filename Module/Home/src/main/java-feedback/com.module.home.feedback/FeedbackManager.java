package com.module.home.feedback;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

// TODO: 2019/2/10 第三方反馈
public class FeedbackManager {

    static boolean sInit = false;

    static final boolean USE_PGY = false;
    static final boolean USE_ALI = true;

    public static void tryInit() {
        if (!sInit) {
            sInit = true;
            if (USE_ALI) {
                FeedbackAPI.init(U.app(),"25466017","82f5f51a6ee2cb94e70ff1a7d021757d");
                FeedbackAPI.setHistoryTextSize(20);//置标题栏中“历史反馈”的字号
//            FeedbackAPI.setBackIcon(R.drawable.ali_feedback_icon_more);//设置返回按钮的图片
                FeedbackAPI.setTranslucent(true);//默认使用沉浸式任务栏
                FeedbackAPI.setDefaultUserContactInfo("13800000000");// 联系方式
                FeedbackAPI.setUserNick(MyUserInfoManager.getInstance().getNickName());// 用户昵称

                JSONObject extInfo = new JSONObject();
                try {
                    extInfo.put("key1", "value1");
                } catch (JSONException e) {
                }
                FeedbackAPI.setAppExtInfo(extInfo);
            }
        }
    }

    /**
     * 阿里的用户反馈页面的ui样式和风格是在云端配置的
     */
    public static void openFeedbackActivity() {
        tryInit();
        if (USE_ALI) {
            FeedbackAPI.openFeedbackActivity(new Callable() {
                @Override
                public Object call() throws Exception {
                    // 成功了
                    return null;
                }
            }, new Callable() {
                @Override
                public Object call() throws Exception {
                    // 失败了
                    return null;
                }
            });
        } else if (USE_PGY) {
//            new PgyerFeedbackManager.PgyerFeedbackBuilder()
//                    .setShakeInvoke(false)           //设置是否摇一摇的方式激活反馈，默认为 true
//                    .setBarBackgroundColor("")      // 设置顶部按钮和底部背景色，默认颜色为 #2E2D2D
//                    .setBarButtonPressedColor("")        //设置顶部按钮和底部按钮按下时的反馈色 默认颜色为 #383737
//                    .setColorPickerBackgroundColor("")   //设置颜色选择器的背景色,默认颜色为 #272828
//                    .setBarImmersive(true)              //设置activity 是否以沉浸式的方式打开，默认为 false
//                    .setDisplayType(PgyerFeedbackManager.TYPE.DIALOG_TYPE)   //设置以Dialog 的方式打开
//                    .setMoreParam("KEY1", "VALUE1")
//                    .setMoreParam("KEY2", "VALUE2")
//                    .builder()
//                    .invoke();                  //激活直接显示的方式
        }

    }
}
