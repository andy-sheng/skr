//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IoUtils {
  public static final int DEFAULT_BUFFER_SIZE = 32768;
  public static final int DEFAULT_IMAGE_TOTAL_SIZE = 512000;
  public static final int CONTINUE_LOADING_PERCENTAGE = 75;

  private IoUtils() {
  }

  public static boolean copyStream(InputStream is, OutputStream os, io.rong.imageloader.utils.IoUtils.CopyListener listener) throws IOException {
    return copyStream(is, os, listener, 32768);
  }

  public static boolean copyStream(InputStream is, OutputStream os, io.rong.imageloader.utils.IoUtils.CopyListener listener, int bufferSize) throws IOException {
    int current = 0;
    int total = is.available();
    if (total <= 0) {
      total = 512000;
    }

    byte[] bytes = new byte[bufferSize];
    if (shouldStopLoading(listener, current, total)) {
      return false;
    } else {
      do {
        int count;
        if ((count = is.read(bytes, 0, bufferSize)) == -1) {
          os.flush();
          return true;
        }

        os.write(bytes, 0, count);
        current += count;
      } while(!shouldStopLoading(listener, current, total));

      return false;
    }
  }

  private static boolean shouldStopLoading(io.rong.imageloader.utils.IoUtils.CopyListener listener, int current, int total) {
    if (listener != null) {
      boolean shouldContinue = listener.onBytesCopied(current, total);
      if (!shouldContinue && 100 * current / total < 75) {
        return true;
      }
    }

    return false;
  }

  public static void readAndCloseStream(InputStream is) {
    byte[] bytes = new byte['耀'];

    try {
      while(true) {
        if (is.read(bytes, 0, 32768) != -1) {
          continue;
        }
      }
    } catch (IOException var6) {
      ;
    } finally {
      closeSilently(is);
    }

  }

  public static void closeSilently(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception var2) {
        ;
      }
    }

  }

  public interface CopyListener {
    boolean onBytesCopied(int var1, int var2);
  }
}
