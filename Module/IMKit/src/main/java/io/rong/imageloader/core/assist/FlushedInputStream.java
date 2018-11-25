//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.assist;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FlushedInputStream extends FilterInputStream {
  public FlushedInputStream(InputStream inputStream) {
    super(inputStream);
  }

  public long skip(long n) throws IOException {
    long totalBytesSkipped;
    long bytesSkipped;
    for(totalBytesSkipped = 0L; totalBytesSkipped < n; totalBytesSkipped += bytesSkipped) {
      bytesSkipped = this.in.skip(n - totalBytesSkipped);
      if (bytesSkipped == 0L) {
        int by_te = this.read();
        if (by_te < 0) {
          break;
        }

        bytesSkipped = 1L;
      }
    }

    return totalBytesSkipped;
  }
}
