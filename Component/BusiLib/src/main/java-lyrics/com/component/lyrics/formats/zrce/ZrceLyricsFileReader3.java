package com.component.lyrics.formats.zrce;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import okio.BufferedSource;
import okio.Okio;

import static okhttp3.internal.Util.closeQuietly;

public class ZrceLyricsFileReader3 extends ZrceLyricsFileReader {

    public String decodeLrc(InputStream in) {
        BufferedSource bufferedSource = null;
        byte[] bytes;
        try {
            bufferedSource = Okio.buffer(Okio.source(in));
            bytes = bufferedSource.readByteArray();
            byte keys[] = {'z', 'Q', 'Z', (byte) (0xAB), 'q', '@', '2', '0', '2', '0', 'D', '^', 'w', '7', '$', (byte) (0xA1)};
            for (int i = 0; i < bytes.length; i++) {
                byte ccc = bytes[i];
                byte f = (byte) (ccc ^ keys[i % 16]);
                bytes[i] = f;
            }

            return new String(bytes, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(bufferedSource);
        }

        return null;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("zrce2");
    }

    @Override
    public String getSupportFileExt() {
        return "zqlrc";
    }
}
