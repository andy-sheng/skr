package com.component.lyrics.formats.zrce;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import okio.BufferedSource;
import okio.Okio;

import static okhttp3.internal.Util.closeQuietly;

public class ZrceLyricsFileReader2 extends ZrceLyricsFileReader {

    @Override
    public String decodeLrc(InputStream in) {
        BufferedSource bufferedSource = null;
        byte[] bytes;
        try {
            bufferedSource = Okio.buffer(Okio.source(in));
            return bufferedSource.readString(Charset.forName("utf-8"));
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
        return "zrce2";
    }
}
