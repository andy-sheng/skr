package com.wali.live;

public class MsgUtils {

    private static class MsgUtilsHolder {
        private static final MsgUtils INSTANCE = new MsgUtils();
    }

    private MsgUtils() {

    }

    public static final MsgUtils getInstance() {
        return MsgUtilsHolder.INSTANCE;
    }

    public final static String TAG = "MsgUtils";


    public void test(String packageName, int i, String ss) {
        sendDataToPlugin(packageName, i, ss);
    }

    private Object sendDataToPlugin(String pluginName, int type, Object obj) {
//        if (TextUtils.isEmpty(pluginName)) {
//            return null;
//        }
//        try {
//            Class<?> pluginMsgClass = com.qihoo360.replugin.RePlugin.fetchClassLoader(pluginName).loadClass(pluginName + ".MsgUtils");
//            for (Method m : pluginMsgClass.getDeclaredMethods()) {
//                MyLog.d(TAG, m.getName());
//            }
//            for (Method m : pluginMsgClass.getMethods()) {
//                MyLog.d(TAG, "x " + m.getName());
//            }
//            Method instanceMethod = com.qihoo360.replugin.utils.ReflectUtils.getMethod(pluginMsgClass,"getInstance");
//            Object pluginMsgInstance = instanceMethod.invoke(null);
//            Method recvDataFromHostMethod = com.qihoo360.replugin.utils.ReflectUtils.getMethod(pluginMsgClass,"recvDataFromHost", int.class, Object.class);
//            Object returnData = recvDataFromHostMethod.invoke(pluginMsgInstance, type, obj);
//            return returnData;
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
        return null;
    }


    private Object recvDataFromPlugin(String pluginName, int type, Object obj) {

        if (type == 1) {
            return "host : recvDataFromPlugin" + " pluginName=" + pluginName + " type=" + type + " obj=" + obj;
        }
        return true;
    }

}
