package com.common.log;

import android.util.Log;

import com.common.base.BuildConfig;
import com.common.utils.U;
import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.formatter.message.throwable.DefaultThrowableFormatter;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.ConsolePrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MyLog {
    private static AtomicInteger sCodeGenerator = new AtomicInteger(1);
    private final static HashMap<Integer, Long> sStartTimes = new HashMap<>();
    private final static HashMap<Integer, String> sActionNames = new HashMap<>();


    private static int sCurrentLogLevel = LogLevel.ALL;             //当前的日志级别
    static boolean sHasInit = false;
    static boolean sForceOpenFlag = false;

    static {
        sForceOpenFlag = U.getPreferenceUtils().getSettingBoolean("key_forceOpenFlag", false);
        Log.e("MyLog", "forceOpenFlag:" + sForceOpenFlag);
    }

    public static void init() {
        if (!sHasInit) {
            String logTag = U.getAppInfoUtils().getAppName();
            //存放的路径
            if (isDebugLogOpen()) {
                sCurrentLogLevel = LogLevel.ALL;
            } else {
//                sCurrentLogLevel = LogLevel.WARN;
                //这里开发中先全部放开日志
                sCurrentLogLevel = LogLevel.ALL;
            }
            LogConfiguration config = new LogConfiguration.Builder()
                    .logLevel(sCurrentLogLevel)            // 指定日志级别，低于该级别的日志将不会被打印，默认为 LogLevel.ALL
                    .tag("SKER")                                         // 指定 TAG，默认为 "X-LOG"
//                    .t()                                                   // 允许打印线程信息，默认禁止
//                    .st(2)                                                 // 允许打印深度为2的调用栈信息，默认禁止
//                .b()                                                   // 允许打印日志边框，默认禁止
//                .jsonFormatter(new MyJsonFormatter())                  // 指定 JSON 格式化器，默认为 DefaultJsonFormatter
//                .xmlFormatter(new MyXmlFormatter())                    // 指定 XML 格式化器，默认为 DefaultXmlFormatter
                    .throwableFormatter(new DefaultThrowableFormatter())        // 指定可抛出异常格式化器，默认为 DefaultThrowableFormatter
                    .threadFormatter(new MyThreadFormatter())              // 指定线程信息格式化器，默认为 DefaultThreadFormatter
//                .stackTraceFormatter(new MyStackTraceFormatter())      // 指定调用栈信息格式化器，默认为 DefaultStackTraceFormatter
//                .borderFormatter(new MyBoardFormatter())               // 指定边框格式化器，默认为 DefaultBorderFormatter
//                .addObjectFormatter(AnyClass.class,                    // 为指定类添加格式化器
//                        new AnyClassObjectFormatter())                     // 默认使用 Object.toString()
//                    .addInterceptor(new BlacklistTagsFilterInterceptor(    // 添加黑名单 TAG 过滤器
//                            "blacklist1", "blacklist2", "blacklist3"))
                    .addInterceptor(new MyInterceptor())                   // 添加一个日志拦截器
                    .build();

            Printer androidPrinter = new AndroidPrinter();             // 通过 android.util.Log 打印日志的打印器
            Printer consolePrinter = new ConsolePrinter();             // 通过 System.out 打印日志到控制台的打印器
            Printer filePrinter = new FilePrinter                      // 打印日志到文件的打印器
                    .Builder(U.getAppInfoUtils().getSubDirPath("logs"))                              // 指定保存日志文件的路径
                    .fileNameGenerator(new MyFileNameGenerator())        // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
                    .backupStrategy(new FileSizeBackupStrategy(50 * 1024 * 1024))          // 指定日志文件备份策略，默认为 FileSizeBackupStrategy(1024 * 1024)
                    .logFlattener(new MyFlattener())                       // 指定日志平铺器，默认为 DefaultFlattener
                    .build();

            XLog.init(                                                 // 初始化 XLog
                    config,                                                // 指定日志配置，如果不指定，会默认使用 new LogConfiguration.Builder().build()
                    androidPrinter,                                        // 添加任意多的打印器。如果没有添加任何打印器，会默认使用 AndroidPrinter(Android)/ConsolePrinter(java)
                    filePrinter
//                    ,ScreenLogPrinter.getInstance()
            );
        }
        sHasInit = true;
    }

    // ------------------------------------------------------------------------------
    // 日志打印方法
    // ------------------------------------------------------------------------------
    public static final void v(String msg) {
        if (!sHasInit) {
            return;
        }
        XLog.v(null, msg);
    }

    public static final void d(String msg) {
        if (!sHasInit) {
            return;
        }
        XLog.d(null, msg);
    }

    public static final void w(String msg) {
        if (!sHasInit) {
            return;
        }
        try {
            XLog.w(null, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void i(String msg) {
        if (!sHasInit) {
            return;
        }
        try {
            XLog.i(null, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void e(String msg) {
        if (!sHasInit) {
            return;
        }
        XLog.e(null, msg);
    }

    /*分割*/

    public static final void v(String tag, String msg) {
        if (!sHasInit) {
            return;
        }
        XLog.v(tag, msg);
    }

    public static final void d(String tag, String msg) {
        if (!sHasInit) {
            return;
        }
        XLog.d(tag, msg);
    }


    public static final void w(String tag, String msg) {
        if (!sHasInit) {
            return;
        }
        XLog.w(tag, msg);
    }


    public static final void i(String tag, String msg) {

        if (!sHasInit) {
            return;
        }
        XLog.i(tag, msg);
    }

    public static final void e(String tag, String msg) {
        if (!sHasInit) {
            return;
        }
        XLog.e(tag, msg);
    }

    /*分割*/


    public static final void d(String tag, String msg, Throwable tr) {
        if (!sHasInit) {
            return;
        }
        XLog.d(tag, msg, tr);
    }

    public static final void d(String tag, Throwable tr) {
        if (!sHasInit) {
            return;
        }
        XLog.d(tag, null, tr);
    }

    public static final void d(Throwable tr) {
        if (!sHasInit) {
            return;
        }
        XLog.d(null, null, tr);
    }

    public static final void e(String tag, String msg, Throwable tr) {
        if (!sHasInit) {
            return;
        }
        XLog.e(tag, msg, tr);
    }

    public static final void e(String tag, Throwable tr) {
        if (!sHasInit) {
            return;
        }
        XLog.e(tag, null, tr);
    }

    public static final void e(Throwable tr) {
        if (!sHasInit) {
            return;
        }
        XLog.e(null, null, tr);
    }


    // log

    /**
     * 设置日志级别
     *
     * @param level
     */
    public static void setLogcatTraceLevel(int level) {
        if (level > LogLevel.ALL || level < LogLevel.VERBOSE) {
            level = LogLevel.ALL;
        }

        if (level != sCurrentLogLevel) {
            sCurrentLogLevel = level;
            // 好像只能这么动态改了
            sHasInit = false;
            init();
        }
    }

    /**
     * 得到当前的日志级别
     *
     * @return
     */
    public static int getCurrentLogLevel() {
        return sCurrentLogLevel;
    }


    // ------------------------------------------------------------------------------
    // 性能统计
    // ------------------------------------------------------------------------------
    public static final Integer ps(String action) {
        Integer code = Integer.valueOf(sCodeGenerator.incrementAndGet());
        sStartTimes.put(code, System.currentTimeMillis());
        sActionNames.put(code, action);
        w(action + " starts");
        return code;
    }

    public static long pe(Integer code) {
        if (!sStartTimes.containsKey(code)) {
            return -1;
        }
        long startTime = sStartTimes.remove(code);
        String action = sActionNames.remove(code);
        long time = System.currentTimeMillis() - startTime;
        w(action + " ends in " + time + " ms");
        return time;
    }

    public static void setForceOpenFlag(boolean flag) {
        sForceOpenFlag = flag;
        U.getPreferenceUtils().setSettingBoolean("key_forceOpenFlag", flag);
//        if (sForceOpenFlag) {
//            ScreenLogPrinter.getInstance().onDebugOpenFlagChange(true);
//        } else {
//            ScreenLogPrinter.getInstance().onDebugOpenFlagChange(isDebugLogOpen());
//        }
    }

    public static boolean getForceOpenFlag() {
        return sForceOpenFlag;
    }

    public static boolean isDebugLogOpen() {
        return BuildConfig.DEBUG || sForceOpenFlag || U.getChannelUtils().isStaging();
    }
}
