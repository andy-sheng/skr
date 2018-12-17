//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v4.os.EnvironmentCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.model.FileInfo;

public class FileTypeUtils {
    public static final FileFilter ALL_FOLDER_AND_FILES_FILTER = new FileFilter() {
        public boolean accept(File pathname) {
            return !pathname.isHidden();
        }
    };
    public static final int KILOBYTE = 1024;
    public static final int MEGABYTE = 1048576;
    public static final int GIGABYTE = 1073741824;

    public FileTypeUtils() {
    }

    public static int fileTypeImageId(String fileName) {
        int id;
        if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_image_file_suffix))) {
            id = R.drawable.rc_file_icon_picture;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_file_file_suffix))) {
            id = R.drawable.rc_file_icon_file;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_video_file_suffix))) {
            id = R.drawable.rc_file_icon_video;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_audio_file_suffix))) {
            id = R.drawable.rc_file_icon_audio;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_word_file_suffix))) {
            id = R.drawable.rc_file_icon_word;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_excel_file_suffix))) {
            id = R.drawable.rc_file_icon_excel;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_ppt_file_suffix))) {
            id = R.drawable.rc_file_icon_ppt;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_pdf_file_suffix))) {
            id = R.drawable.rc_file_icon_pdf;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_key_file_suffix))) {
            id = R.drawable.rc_file_icon_key;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_numbers_file_suffix))) {
            id = R.drawable.rc_file_icon_numbers;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_pages_file_suffix))) {
            id = R.drawable.rc_file_icon_pages;
        } else {
            id = R.drawable.rc_file_icon_else;
        }

        return id;
    }

    private static boolean checkSuffix(String fileName, String[] fileSuffix) {
        String[] var2 = fileSuffix;
        int var3 = fileSuffix.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String suffix = var2[var4];
            if (fileName != null && fileName.toLowerCase().endsWith(suffix)) {
                return true;
            }
        }

        return false;
    }

    public static Intent getOpenFileIntent(String fileName, String fileSavePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        String type = null;
        if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_image_file_suffix))) {
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            type = "image/*";
        }

        if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_file_file_suffix))) {
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            type = "text/plain";
        }

        if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_video_file_suffix))) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("oneshot", 0);
            intent.putExtra("configchange", 0);
            type = "video/*";
        }

        if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_audio_file_suffix))) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("oneshot", 0);
            intent.putExtra("configchange", 0);
            type = "audio/*";
        }

        if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_word_file_suffix))) {
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            type = "application/msword";
        }

        if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_excel_file_suffix))) {
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            type = "application/vnd.ms-excel";
        }

        if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_pdf_file_suffix))) {
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            type = "application/pdf";
        }

        if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_ppt_file_suffix))) {
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            type = "application/vnd.ms-powerpoint";
        }

        if (type != null && fileSavePath != null && isIntentHandlerAvailable(RongContext.getInstance(), intent)) {
            Uri uri = FileProvider.getUriForFile(RongContext.getInstance(), RongContext.getInstance().getApplicationContext().getPackageName() + RongContext.getInstance().getResources().getString(R.string.rc_authorities_fileprovider), new File(fileSavePath));
            intent.setDataAndType(uri, type);
            return intent;
        } else {
            return null;
        }
    }

    private static boolean isIntentHandlerAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> infoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return infoList.size() > 0;
    }

    public static List<FileInfo> getTextFilesInfo(File fileDir) {
        List<FileInfo> textFilesInfo = new ArrayList();
        FileFilter fileFilter = new io.rong.imkit.utils.FileTypeUtils.FileTypeFilter(RongContext.getInstance().getResources().getStringArray(R.array.rc_file_file_suffix));
        getFileInfos(fileDir, fileFilter, textFilesInfo);
        return textFilesInfo;
    }

    private static void getFileInfos(File fileDir, FileFilter fileFilter, List<FileInfo> fileInfos) {
        File[] listFiles = fileDir.listFiles(fileFilter);
        if (listFiles != null) {
            File[] var4 = listFiles;
            int var5 = listFiles.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                File file = var4[var6];
                if (file.isDirectory()) {
                    getFileInfos(file, fileFilter, fileInfos);
                } else if (file.length() != 0L) {
                    FileInfo fileInfo = getFileInfoFromFile(file);
                    fileInfos.add(fileInfo);
                }
            }
        }

    }

    public static List<FileInfo> getVideoFilesInfo(File fileDir) {
        List<FileInfo> videoFilesInfo = new ArrayList();
        FileFilter fileFilter = new io.rong.imkit.utils.FileTypeUtils.FileTypeFilter(RongContext.getInstance().getResources().getStringArray(R.array.rc_video_file_suffix));
        getFileInfos(fileDir, fileFilter, videoFilesInfo);
        return videoFilesInfo;
    }

    public static List<FileInfo> getAudioFilesInfo(File fileDir) {
        List<FileInfo> audioFilesInfo = new ArrayList();
        FileFilter fileFilter = new io.rong.imkit.utils.FileTypeUtils.FileTypeFilter(RongContext.getInstance().getResources().getStringArray(R.array.rc_audio_file_suffix));
        getFileInfos(fileDir, fileFilter, audioFilesInfo);
        return audioFilesInfo;
    }

    public static List<FileInfo> getOtherFilesInfo(File fileDir) {
        List<FileInfo> otherFilesInfo = new ArrayList();
        FileFilter fileFilter = new io.rong.imkit.utils.FileTypeUtils.FileTypeFilter(RongContext.getInstance().getResources().getStringArray(R.array.rc_other_file_suffix));
        getFileInfos(fileDir, fileFilter, otherFilesInfo);
        return otherFilesInfo;
    }

    public static List<FileInfo> getFileInfosFromFileArray(File[] files) {
        List<FileInfo> fileInfos = new ArrayList();
        File[] var2 = files;
        int var3 = files.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            File file = var2[var4];
            FileInfo fileInfo = getFileInfoFromFile(file);
            fileInfos.add(fileInfo);
        }

        return fileInfos;
    }

    private static FileInfo getFileInfoFromFile(File file) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(file.getName());
        fileInfo.setFilePath(file.getPath());
        fileInfo.setDirectory(file.isDirectory());
        if (file.isDirectory()) {
            fileInfo.setFileSize((long) getNumFilesInFolder(fileInfo));
        } else {
            fileInfo.setFileSize(file.length());
        }

        int lastDotIndex = file.getName().lastIndexOf(".");
        if (lastDotIndex > 0) {
            String fileSuffix = file.getName().substring(lastDotIndex + 1);
            fileInfo.setSuffix(fileSuffix);
        }

        return fileInfo;
    }

    public static int getNumFilesInFolder(FileInfo fileInfo) {
        if (!fileInfo.isDirectory()) {
            return 0;
        } else {
            File[] files = (new File(fileInfo.getFilePath())).listFiles(ALL_FOLDER_AND_FILES_FILTER);
            return files == null ? 0 : files.length;
        }
    }

    public static int getFileIconResource(FileInfo file) {
        return file.isDirectory() ? R.drawable.rc_ad_list_folder_icon : getFileTypeImageId(file.getFileName());
    }

    private static int getFileTypeImageId(String fileName) {
        int id;
        if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_file_file_suffix))) {
            id = R.drawable.rc_ad_list_file_icon;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_video_file_suffix))) {
            id = R.drawable.rc_ad_list_video_icon;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_audio_file_suffix))) {
            id = R.drawable.rc_ad_list_audio_icon;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_ppt_file_suffix))) {
            id = R.drawable.rc_ad_list_ppt_icon;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_pdf_file_suffix))) {
            id = R.drawable.rc_ad_list_pdf_icon;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_image_file_suffix))) {
            id = R.drawable.rc_file_icon_picture;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_word_file_suffix))) {
            id = R.drawable.rc_file_icon_word;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_excel_file_suffix))) {
            id = R.drawable.rc_file_icon_excel;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_key_file_suffix))) {
            id = R.drawable.rc_ad_list_key_icon;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_numbers_file_suffix))) {
            id = R.drawable.rc_ad_list_numbers_icon;
        } else if (checkSuffix(fileName, RongContext.getInstance().getResources().getStringArray(R.array.rc_pages_file_suffix))) {
            id = R.drawable.rc_ad_list_pages_icon;
        } else {
            id = R.drawable.rc_ad_list_other_icon;
        }

        return id;
    }

    public static String formatFileSize(long size) {
        if (size < 1024L) {
            return String.format("%d B", (int) size);
        } else if (size < 1048576L) {
            return String.format("%.2f KB", (float) size / 1024.0F);
        } else {
            return size < 1073741824L ? String.format("%.2f MB", (float) size / 1048576.0F) : String.format("%.2f G", (float) size / 1.07374182E9F);
        }
    }

    public String getSDCardPath() {
        String SDCardPath = null;
        String SDCardDefaultPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (SDCardDefaultPath.endsWith("/")) {
            SDCardDefaultPath = SDCardDefaultPath.substring(0, SDCardDefaultPath.length() - 1);
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.toLowerCase().contains("sdcard") && line.contains(".android_secure")) {
                    String[] array = line.split(" ");
                    if (array != null && array.length > 1) {
                        String temp = array[1].replace("/.android_secure", "");
                        if (!SDCardDefaultPath.equals(temp)) {
                            SDCardPath = temp;
                        }
                    }
                }
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }

        return SDCardPath;
    }

    public static String[] getExternalStorageDirectories(Context context) {
        List<String> results = new ArrayList();
        if (VERSION.SDK_INT >= 19) {
            File[] externalDirs = context.getExternalFilesDirs((String) null);
            File[] var3 = externalDirs;
            int var4 = externalDirs.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                File file = var3[var5];
                if (file != null) {
                    String path = file.getPath().split("/Android")[0];
                    boolean addPath;
                    if (VERSION.SDK_INT >= 21) {
                        addPath = Environment.isExternalStorageRemovable(file);
                    } else {
                        addPath = "mounted".equals(EnvironmentCompat.getStorageState(file));
                    }

                    if (addPath) {
                        results.add(path);
                    }
                }
            }
        }

        if (results.isEmpty()) {
            String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
            String s = "";

            try {
                Process process = (new ProcessBuilder(new String[0])).command("mount").redirectErrorStream(true).start();
                process.waitFor();
                InputStream is = process.getInputStream();

                for (byte[] buffer = new byte[1024]; is.read(buffer) != -1; s = s + new String(buffer)) {
                    ;
                }

                is.close();
            } catch (Exception var14) {
                var14.printStackTrace();
            }

            String[] lines = s.split("\n");
            String[] var23 = lines;
            int var25 = lines.length;

            for (int var26 = 0; var26 < var25; ++var26) {
                String line = var23[var26];
                if (!line.toLowerCase(Locale.US).contains("asec") && line.matches(reg)) {
                    String[] parts = line.split(" ");
                    String[] var10 = parts;
                    int var11 = parts.length;

                    for (int var12 = 0; var12 < var11; ++var12) {
                        String part = var10[var12];
                        if (part.startsWith("/") && !part.toLowerCase(Locale.US).contains("vold")) {
                            results.add(part);
                        }
                    }
                }
            }
        }

        int i;
        if (VERSION.SDK_INT >= 23) {
            for (i = 0; i < results.size(); ++i) {
                if (!((String) results.get(i)).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    results.remove(i--);
                }
            }
        } else {
            for (i = 0; i < results.size(); ++i) {
                if (!((String) results.get(i)).toLowerCase().contains("ext") && !((String) results.get(i)).toLowerCase().contains("sdcard")) {
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];

        for (i = 0; i < results.size(); ++i) {
            storageDirectories[i] = (String) results.get(i);
        }

        return storageDirectories;
    }

    public static class FileNameComparator implements Comparator<FileInfo> {
        protected static final int FIRST = -1;
        protected static final int SECOND = 1;

        public FileNameComparator() {
        }

        public int compare(FileInfo lhs, FileInfo rhs) {
            if (!lhs.isDirectory() && !rhs.isDirectory()) {
                return lhs.getFileName().compareToIgnoreCase(rhs.getFileName());
            } else if (lhs.isDirectory() == rhs.isDirectory()) {
                return lhs.getFileName().compareToIgnoreCase(rhs.getFileName());
            } else {
                return lhs.isDirectory() ? -1 : 1;
            }
        }
    }

    public static final class FileTypeFilter implements FileFilter {
        private String[] filesSuffix;

        public FileTypeFilter(String[] fileSuffix) {
            this.filesSuffix = fileSuffix;
        }

        public boolean accept(File pathname) {
            return !pathname.isHidden() && (pathname.isDirectory() || io.rong.imkit.utils.FileTypeUtils.checkSuffix(pathname.getName(), this.filesSuffix));
        }
    }
}
