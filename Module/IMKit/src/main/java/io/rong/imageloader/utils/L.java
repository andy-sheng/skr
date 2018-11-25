//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.utils;

import android.util.Log;

import io.rong.imageloader.core.ImageLoader;

public final class L {
  private static final String LOG_FORMAT = "%1$s\n%2$s";
  private static volatile boolean writeDebugLogs = false;
  private static volatile boolean writeLogs = true;

  private L() {
  }

  /** @deprecated */
  @Deprecated
  public static void enableLogging() {
    writeLogs(true);
  }

  /** @deprecated */
  @Deprecated
  public static void disableLogging() {
    writeLogs(false);
  }

  public static void writeDebugLogs(boolean writeDebugLogs) {
    writeDebugLogs = writeDebugLogs;
  }

  public static void writeLogs(boolean writeLogs) {
    writeLogs = writeLogs;
  }

  public static void d(String message, Object... args) {
    if (writeDebugLogs) {
      log(3, (Throwable)null, message, args);
    }

  }

  public static void i(String message, Object... args) {
    log(4, (Throwable)null, message, args);
  }

  public static void w(String message, Object... args) {
    log(5, (Throwable)null, message, args);
  }

  public static void e(Throwable ex) {
    log(6, ex, (String)null);
  }

  public static void e(String message, Object... args) {
    log(6, (Throwable)null, message, args);
  }

  public static void e(Throwable ex, String message, Object... args) {
    log(6, ex, message, args);
  }

  private static void log(int priority, Throwable ex, String message, Object... args) {
    if (writeLogs) {
      if (args.length > 0) {
        message = String.format(message, args);
      }

      String log;
      if (ex == null) {
        log = message;
      } else {
        String logMessage = message == null ? ex.getMessage() : message;
        String logBody = Log.getStackTraceString(ex);
        log = String.format("%1$s\n%2$s", logMessage, logBody);
      }

      Log.println(priority, ImageLoader.TAG, log);
    }
  }
}
