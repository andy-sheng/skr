//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utils;

public class StringUtils {
    private static final String SEPARATOR = "#@6RONG_CLOUD9@#";

    public StringUtils() {
    }

    public static String getKey(String arg1, String arg2) {
        return arg1 + "#@6RONG_CLOUD9@#" + arg2;
    }

    public static String getArg1(String key) {
        String arg = null;
        if (key.contains("#@6RONG_CLOUD9@#")) {
            int index = key.indexOf("#@6RONG_CLOUD9@#");
            arg = key.substring(0, index);
        }

        return arg;
    }

    public static String getArg2(String key) {
        String arg = null;
        if (key.contains("#@6RONG_CLOUD9@#")) {
            int index = key.indexOf("#@6RONG_CLOUD9@#") + "#@6RONG_CLOUD9@#".length();
            arg = key.substring(index, key.length());
        }

        return arg;
    }
}
