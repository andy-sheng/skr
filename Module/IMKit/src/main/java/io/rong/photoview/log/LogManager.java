//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.photoview.log;

import io.rong.photoview.log.Logger;
import io.rong.photoview.log.LoggerDefault;

public final class LogManager {
  private static Logger logger = new LoggerDefault();

  public LogManager() {
  }

  public static void setLogger(Logger newLogger) {
    logger = newLogger;
  }

  public static Logger getLogger() {
    return logger;
  }
}
