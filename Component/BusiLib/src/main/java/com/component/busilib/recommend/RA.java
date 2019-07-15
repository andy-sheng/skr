package com.component.busilib.recommend;

import android.text.TextUtils;

import com.common.utils.U;

/**
 * Recommendation algorithm
 * 推荐算法参数相关
 */
public class RA {

    static String var;

    static String testList;

    public static String getVars() {
        if (var == null) {
            var = U.getPreferenceUtils().getSettingString("key_vars", "");
        }
        return var;
    }

    public static String getTestList() {
        if (testList == null) {
            testList = U.getPreferenceUtils().getSettingString("key_testList", "");
        }
        return testList;
    }

    public static void setVar(String var) {
        RA.var = var;
        U.getPreferenceUtils().setSettingString("key_vars", var);
    }

    public static void setTestList(String testList) {
        RA.testList = testList;
        U.getPreferenceUtils().setSettingString("key_testList", testList);
    }

    public static boolean hasTestList() {
        if(TextUtils.isEmpty(getTestList())){
            return false;
        }
        return true;
    }
}
