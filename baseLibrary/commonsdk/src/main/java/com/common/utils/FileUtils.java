package com.common.utils;

import android.text.TextUtils;

import com.common.log.MyLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by linjinbin on 15/2/10.
 */
public class FileUtils {

    public int KB = 1024;
    public int MB = 1024 * KB;

    private final HashMap<String, String> mFileTypes = new HashMap<>();

    {
        // images
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("47494638", "gif");
        mFileTypes.put("474946", "gif");    //added by mk
        mFileTypes.put("424D", "bmp");
    }

    FileUtils() {
    }

    /**
     * 返回该文件夹内所有文件的大小
     *
     * @param path
     * @return
     */
    public long getDirSize(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isFile()) {
                return file.length();
            } else if (file.isDirectory()) {
                long len = 0;
                for (File f : file.listFiles()) {
                    len += getDirSize(f.getAbsolutePath());
                }
                return len;
            }
        }
        return 0L;
    }

    /**
     * 传入一个文件路径，文件后缀入 /sdcard/a.jpg 返回 jpg
     *
     * @param path
     * @return
     */
    public String getSuffixFromFilePath(String path) {
        String extension = "";
        if (!TextUtils.isEmpty(path)) {
            int indexOfSlash = path.lastIndexOf('/');
            int indexOfDot = path.lastIndexOf('.');
            if (indexOfDot > indexOfSlash) {
                extension = path.substring(indexOfDot + 1);
            }
        }
        return extension;
    }

    /**
     * 传入一个文件路径，文件后缀入 http://www.baidu.com/a.jpg 返回 jpg
     *
     * @param path
     * @return
     */
    public String getSuffixFromUrl(String path, String defaultExt) {
        String extension = "";
        if (!TextUtils.isEmpty(path)) {
            int indexOfSlash = path.lastIndexOf('/');
            int indexOfDot = path.lastIndexOf('.');
            if (indexOfDot > indexOfSlash) {
                extension = path.substring(indexOfDot + 1);
            }
        }
        if (TextUtils.isEmpty(extension)) {
            return defaultExt;
        }
        return extension;
    }

    /**
     * 传入一个文件路径，返回文件名  /sdcard/a.jpg 返回 a.jpg
     *
     * @param path
     * @return
     */
    public String getFileNameFromFilePath(String path) {
        String extension = path;
        if (!TextUtils.isEmpty(path)) {
            int indexOfSlash = path.lastIndexOf('/');
            if (indexOfSlash != -1) {
                extension = path.substring(indexOfSlash + 1);
            }
        }
        return extension;
    }

    /**
     * 传入一个文件路径，返回文件名  /sdcard/aa.jpg 返回 aa
     *
     * @param path
     * @return
     */
    public String getFileNameFromFilePathWithoutExt(String path) {
        String extension = path;
        if (!TextUtils.isEmpty(path)) {
            int indexOfSlash = path.lastIndexOf('/');
            int indexOfDot = path.lastIndexOf('.');
            if (indexOfDot == -1) {
                return path.substring(indexOfSlash + 1);
            }
            if (indexOfDot > indexOfSlash) {
                return path.substring(indexOfSlash + 1, indexOfDot);
            }
        }
        return extension;
    }

    /**
     * 获取上传文件的类型(仅针对图片类型)
     *
     * @param filePath
     * @return
     */
    public String getImageFileType(String filePath) {
        return mFileTypes.get(getFileHeader(filePath));
    }

    /**
     * 获得文件头信息
     *
     * @param filePath
     * @return
     */
    private String getFileHeader(String filePath) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[3];
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    private String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }


    /**
     * 根据系统时间、前缀、后缀产生一个文件
     * 如 IMG_20180912_010233.jpg
     */
    public static File createFileByTs(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    /**
     * 得到一个文件的sha1签名摘要
     *
     * @param fileName
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static byte[] getFileSha1Digest(final String fileName)
            throws NoSuchAlgorithmException, IOException {
        if (TextUtils.isEmpty(fileName)) {
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
            MyLog.e(e);
        }

        return md.digest();
    }


    /**
     * 拷贝assets中的文件到sdcard
     *
     * @param srcPath  文件或者文件夹 如 effect
     * @param dstPath  目录
     * @param override 是否覆盖
     */
    public void copyAssetsToSdcard(String srcPath, String dstPath, boolean override) {
        try {
            String fileNames[] = U.app().getAssets().list(srcPath);
            if (fileNames.length > 0) {
                File file = new File(dstPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                for (String fileName : fileNames) {
                    if (!srcPath.equals("")) { // assets 文件夹下的目录
                        copyAssetsToSdcard(srcPath + File.separator + fileName, dstPath + File.separator + fileName, override);
                    } else { // assets 文件夹
                        copyAssetsToSdcard(fileName, dstPath + File.separator + fileName, override);
                    }
                }
            } else {
                File outFile = new File(dstPath);
                if (!override && outFile.exists()) {
                    MyLog.w("FileUtils", outFile.getAbsolutePath() + " exist,override is false,cancel");
                    return;
                }
                InputStream is = U.app().getAssets().open(srcPath);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
        }
    }

    public void deleteAllFiles(String srcPath) {
        if (TextUtils.isEmpty(srcPath)) {
            MyLog.w("FileUtils", "deleteAllFiles srcPath is null");
            return;
        }

        File file = new File(srcPath);
        deleteAllFiles(file);
    }

    public void deleteAllFiles(File root) {
        if (root == null || !root.exists()) {
            MyLog.w("FileUtils", "deleteAllFiles root error");
            return;
        }
        if(root.isFile()){
            root.delete();
            return;
        }
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
    }

    /**
     *
     * @param file
     * @param left
     */
    public void deleteEarlyFiles(File file, int left) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            // 文件修改时间排序
            Arrays.sort(childFile, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    long diff = f1.lastModified() - f2.lastModified();
                    if (diff > 0)
                        return 1;
                    else if (diff == 0)
                        return 0;
                    else
                        return -1;
                }
            });
            for (int i = childFile.length - 1; i >= 0; i--) {
                if (i <= childFile.length - left) {
                    File f = childFile[i];
                    U.getFileUtils().deleteAllFiles(f);
                }
            }
        }
    }


}

