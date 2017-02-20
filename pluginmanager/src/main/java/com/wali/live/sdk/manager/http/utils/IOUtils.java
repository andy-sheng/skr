package com.wali.live.sdk.manager.http.utils;
/**
 * Created by chengsimin on 2016/12/12.
 */

import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;



public class IOUtils {

    public final static String TAG = IOUtils.class.getSimpleName();
    
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static void closeQuietly(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                Log.e("IOUtils", "IOException", e);
            }
        }
    }

    public static void closeQuietly(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public static void closeQuietly(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                Log.e("IOUtils", "IOException", e);
            }
        }
    }

    public static void closeQuietley(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e("IOUtils", "IOException", e);
            }
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static String readFileAsString(String path) throws IOException {
        return readFileAsBytes(path).toString();
    }

    private static ByteArrayOutputStream readFileAsBytes(String path) throws IOException {
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(path, "r");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream((int) f.length());
            byte[] buffer = new byte[8192];
            while (true) {
                int byteCount = f.read(buffer);
                if (byteCount == -1) {
                    return bytes;
                }
                bytes.write(buffer, 0, byteCount);
            }
        } finally {
            try {
                f.close();
            } catch (IOException ignored) {
            }
        }
    }


    private static final int BUFFER_SIZE = 1024;

    public static final String SUPPORTED_IMAGE_FORMATS[] = {"jpg", "png",
            "bmp", "gif", "webp"};

    /**
     * ���缂╀��涓����浠舵�����浠跺す涓�������������浠舵��涓�涓�gzip
     *
     * @param out
     * @param f
     * @param base
     * @throws Exception
     */
    public static void zip(final ZipOutputStream out, final File f,
                           String base, final FileFilter filter) throws IOException {

        if (base == null) {
            base = "";
        }
        FileInputStream in = null;
        try {
            if (f.isDirectory()) {
                File[] fl;
                if (filter != null) {
                    fl = f.listFiles(filter);
                } else {
                    fl = f.listFiles();
                }
                out.putNextEntry(new ZipEntry(base + File.separator));
                base = TextUtils.isEmpty(base) ? "" : base
                        + File.separator;

                for (int i = 0; i < fl.length; i++) {
                    zip(out, fl[i], base + fl[i].getName(), null);
                }

                File[] dirs = f.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });
                if (dirs != null) {
                    for (File subFile : dirs) {
                        zip(out, subFile,
                                base + File.separator + subFile.getName(),
                                filter);
                    }
                }
            } else {
                if (!TextUtils.isEmpty(base)) {
                    out.putNextEntry(new ZipEntry(base));
                } else {
                    final Date date = new Date();
                    out.putNextEntry(new ZipEntry(
                            String.valueOf(date.getTime()) + ".txt"));
                }
                in = new FileInputStream(f);

                int bytesRead = -1;
                final byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (final IOException e) {
            Log.e(TAG,"zipFiction failed with exception:" + e.toString());
        } finally {
            closeQuietly(in);
        }
    }

    public static void zip(final ZipOutputStream out, String fileName,
                           final InputStream inputStream) {
        try {
            if (!TextUtils.isEmpty(fileName)) {
                out.putNextEntry(new ZipEntry(fileName));
            } else {
                final Date date = new Date();
                out.putNextEntry(new ZipEntry(String.valueOf(date.getTime())
                        + ".txt"));
            }

            int bytesRead = -1;
            final byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.e(TAG,"zipFiction failed with exception:" + e.toString());
        }
    }

    public static void closeQuietly(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static void closeQuietly(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static void closeQuietly(OutputStream os) {
        if (os != null) {
            try {
                os.flush();
            } catch (IOException e) {
                // ignore
            }
            try {
                os.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static void copyFile(final File src, final File dest)
            throws IOException {
        if (src.getAbsolutePath().equals(dest.getAbsolutePath())) {
            return;
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dest);

            final byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) >= 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public static boolean unZip(final String zipFile, String targetDir) {
        if (TextUtils.isEmpty(targetDir) || TextUtils.isEmpty(zipFile)) {
            return false;
        }

        final int BUFFER = 4096;
        if (!targetDir.endsWith("/")) {
            targetDir += "/";
        }
        String strEntry;
        try {
            BufferedOutputStream dest = null;
            final FileInputStream fis = new FileInputStream(zipFile);
            final ZipInputStream zis = new ZipInputStream(
                    new BufferedInputStream(fis));
            ZipEntry entry;
            final byte data[] = new byte[BUFFER];
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                strEntry = entry.getName();
                final File entryFile = new File(targetDir + strEntry);

                if (strEntry.endsWith("/")) {
                    continue;
                } else {
                    final File parentFolder = new File(entryFile.getParent());
                    if ((parentFolder != null)
                            && (!parentFolder.exists() || !parentFolder
                            .isDirectory())) {
                        parentFolder.mkdirs();
                        hideFromMediaScanner(parentFolder);
                    }

                    final FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                }
            }
            zis.close();
        } catch (final IOException e) {
            return false;
        }
        return true;
    }

    /**
     */
    public static void hideFromMediaScanner(final File root) {
        final File file = new File(root, ".nomedia");
        if (!file.exists() || !file.isFile()) {
            try {
                file.createNewFile();
            } catch (final IOException e) {
            }
        }
    }

    public static byte[] getFileSha1Digest(final String fileName)
            throws NoSuchAlgorithmException, IOException {
        if(TextUtils.isEmpty(fileName)){
            return null;
        }
        final MessageDigest md = MessageDigest.getInstance("SHA1");
        final File file = new File(fileName);
        final FileInputStream inStream = new FileInputStream(file);
        final byte[] buffer = new byte[4096]; // Calculate digest per 1K

        int readCount = 0;
        while ((readCount = inStream.read(buffer)) != -1) {
            md.update(buffer, 0, readCount);
        }
        try {
            inStream.close();
        } catch (IOException e) {
        }

        return md.digest();
    }

    public static byte[] getFileMD5Digest(final String fileName)
            throws NoSuchAlgorithmException, IOException {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        final File file = new File(fileName);
        final FileInputStream inStream = new FileInputStream(file);
        final byte[] buffer = new byte[4096]; // Calculate digest per 1K

        int readCount = 0;
        while ((readCount = inStream.read(buffer)) != -1) {
            md.update(buffer, 0, readCount);
        }
        try {
            inStream.close();
        } catch (IOException e) {
        }
        return md.digest();
    }

    /**
     * @param file
     */
    public static void deleteDirs(final File file) {
        if (file.isDirectory()) {
            final File[] subFiles = file.listFiles();
            if ((subFiles != null) && (subFiles.length > 0)) {
                for (final File subF : subFiles) {
                    if (subF.isFile()) {
                        subF.delete();
                    } else {
                        deleteDirs(subF);
                    }
                }
            }
            file.delete();
        }
    }

    public static String getFileSuffix(String fileName) {
        final int dotPos = fileName.lastIndexOf('.');
        if (dotPos > 0) {
            return fileName.substring(dotPos + 1);
        }
        return "";
    }

    public static boolean isSupportImageSuffix(String suffix) {
        if (TextUtils.isEmpty(suffix)) {
            return false;
        }
        for (String supported : SUPPORTED_IMAGE_FORMATS) {
            if (supported.equalsIgnoreCase(suffix)) {
                return true;
            }
        }
        return false;
    }
}

